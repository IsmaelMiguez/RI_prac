package es.udc.fi.ri.practica;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;


public class TrainingTestTrecCovid {	
	
	//para la creacion de la clase analyzer (indica que el metodo nueva INTANCIA DE CLASS ESTA OBSOLETO
	public static void main(String[] args) throws Exception {
					
		 //Variables necesarias podemos inicializarlas en el main y pasarlas como parametros si es necesario
		Boolean evaljm = false;
		Boolean evalbm25 = false; 
		String p = null;
		String q = null;
		Integer int1 = 0;
		Integer int2 = 0;
		Integer int3 = 0;
		Integer int4 = 0;
		String index = null;
		Integer cut = 0;
		String metrica = null;
		
		
	    String usage =
	        "IndexTrecCovid"
	            + " [-index INDEX_PATH] [-evaljm/-evalbm25] [-cut CUT_MODE] [-metrica METRICA_MODE] \n\n"
	            + "This indexes the documents in DOCS_PATH, creating a Lucene index";
	   
	//comprobamos que tenemos los argumentos necesarios
	    if (args.length <7) {
			System.out.println("A folder is needed for the index");
			  System.err.println("Usage: " + usage);
		      System.exit(1);
		}
	    
				    
	    for (int i = 0; i < args.length; i++) {
	      switch (args[i]) {
	        case "-index":
	          index = args[++i];
	          break;
	        case "-evaljm":
		          evaljm = true;
		          p = args[++i];
		          q = args[i+2];
		          String[] queries1 = p.split("-");
		          int1 =Integer.valueOf( queries1[0])-1;
		          int2 = Integer.valueOf( queries1[1])-1;
		          String[] queries2 = q.split("-");
		          int3 =Integer.valueOf( queries2[0])-1;
		          int4 = Integer.valueOf( queries2[1])-1;
	          break;
	        case "-evalbm25":
		          evalbm25 = true;
		          p = args[++i];
		          q = args[i+2];
		          String[] queries11 = p.split("-");
		          int1 =Integer.valueOf( queries11[0])-1;
		          int2 = Integer.valueOf( queries11[1])-1;
		          String[] queries21 = q.split("-");
		          int3 =Integer.valueOf( queries21[0])-1;
		          int4 = Integer.valueOf( queries21[1])-1;
	          break;	          
	        case "-cut":
	        	cut = Integer.valueOf(args[i++]);
	          break;
	        case "-metrica":
	        	metrica = args[i++];
	        default:
	          throw new IllegalArgumentException("unknown parameter " + args[i]);
	      }
	    }

	    if (index == null) {
	      System.err.println("Usage: " + usage);
	      System.exit(1);
	    }
	    
	    if (evaljm == true && evalbm25 == true) {
		      System.err.println(" evaljm and evalbm25 are not compatible. Only one option can be loaded");
		      System.exit(1);
		      }
	    
	 // Ejecutar proceso de entrenamiento o prueba
        if (evaljm) {
            Map<Double, Double> resultadosEntrenamiento = processTraining("jm", index, int1, int2, cut, metrica);
            Map<Double, Double> resultadosPrueba = processTest("jm", index, int3, int4, cut, metrica);
            escribirResultados("jm.training." + (int1 + 1) + "-" + (int2 + 1), resultadosEntrenamiento, metrica);
            escribirResultados("jm.test." + (int3 + 1) + "-" + (int4 + 1), resultadosPrueba, metrica);
        } else if (evalbm25) {
            Map<Double, Double> resultadosEntrenamiento = processTraining("bm25", index, int1, int2, cut, metrica);
            Map<Double, Double> resultadosPrueba = processTest("bm25", index, int3, int4, cut, metrica);
            escribirResultados("bm25.training." + (int1 + 1) + "-" + (int2 + 1), resultadosEntrenamiento, metrica);
            escribirResultados("bm25.test." + (int3 + 1) + "-" + (int4 + 1), resultadosPrueba, metrica);
        } else {
            System.err.println("Please specify either -evaljm or -evalbm25");
            System.exit(1);
        }
    }
    
	
	private static Map<Double, Double> processTraining(String model, String indexPath, int start, int end, int cut, String metrica) throws IOException, ParseException {
	    Map<Double, Double> resultados = new HashMap<>();
	    Directory dir = FSDirectory.open(Paths.get(indexPath));
	    IndexReader indexReader = DirectoryReader.open(dir);
	    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
	    Analyzer analyzer = new StandardAnalyzer();

	    for (float param : getParametros(model)) {
	        double metricaPromedio = 0;
	        indexSearcher.setSimilarity(getSimilarity(model, param));

	        for (int i = start; i <= end; i++) {
	            List<Judgments> judgmentsMap = loadJudgments(i); // Carga de judgmentsMap para cada consulta
	            String queryText = obtenerTextoQueryDesdeArchivo(i);
	            TopDocs topDocs = indexSearcher.search(new QueryParser("text", analyzer).parse(queryText), cut);
	            double metricaValor = calcularMetrica(indexReader, topDocs, judgmentsMap, cut, metrica);
	            metricaPromedio += metricaValor;
	        }

	        metricaPromedio /= (end - start + 1);
	        resultados.put((double)param, metricaPromedio);
	    }

	    indexReader.close();
	    dir.close();

	    return resultados;
	}


	private static Map<Double, Double> processTest(String model, String indexPath, int start, int end, int cut, String metrica) throws IOException, ParseException {
	    Map<Double, Double> resultados = new HashMap<>();
	    Directory dir = FSDirectory.open(Paths.get(indexPath));
	    IndexReader indexReader = DirectoryReader.open(dir);
	    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
	    Analyzer analyzer = new StandardAnalyzer();

	    for (int i = start; i <= end; i++) {
	        List<Judgments> judgmentsMap = loadJudgments(i); // Carga de judgmentsMap para cada consulta
	        String queryText = obtenerTextoQueryDesdeArchivo(i);
	        TopDocs topDocs = indexSearcher.search(new QueryParser("text", analyzer).parse(queryText), cut);
	        
	        double metricaValor = calcularMetrica(indexReader, topDocs, judgmentsMap, cut, metrica);
	        resultados.put((double) i, metricaValor);
	    }

	    indexReader.close();
	    dir.close();

	    return resultados;
	}
    
    
	private static double calcularMetrica(IndexReader indexReader, TopDocs topDocs, List<Judgments> judgmentsList, int cut, String metrica) throws IOException {
	    int relevantes = 0;
	    double acumulador = 0;
	    double reverse = 0;
	    Set<String> relevantIds = new HashSet<>();
	    
	    for (Judgments judgment : judgmentsList) {
	    	relevantIds.add(judgment.corpus());
	    }

	    for (int i = 0; i < cut; i++) {
	        Document documento = indexReader.document(topDocs.scoreDocs[i].doc);
	        String documentId = documento.getValues("id")[0];
	        if (relevantIds.contains(documentId)) {
	            relevantes++;
	            acumulador += (double) relevantes / (i + 1);
	            reverse += (double) (1.0 / (i + 1));
	        }
	    }

	    // Calcular métrica
	    double metricaValor = 0;
	    switch (metrica) {
	        case "P":
	            metricaValor = (double) relevantes / cut;
	            break;
	        case "R":
	            metricaValor = (double) relevantes / relevantIds.size();
	            break;
	        case "MRR":
	            metricaValor = (double) reverse / cut;
	            break;
	        case "MAP":
	            metricaValor = (double) acumulador / relevantIds.size();
	            break;
	        default:
	            System.err.println("Métrica no válida: " + metrica);
	            break;
	    }

	    return metricaValor;
	}

	
    
    
    private static List<Query> selectQueries(int start, int end) throws IOException {
    	var is = IndexTreeCovid.class.getResourceAsStream( "/trec-covid/queries.jsonl");
        ObjectReader reader = JsonMapper.builder().findAndAddModules().build()
                .readerFor(Query.class);
        
        List<Query> queries = reader.<Query>readValues(is).readAll();
    	
    		List<Query> selection = new ArrayList<Query>();
    		for (int i = start; i<= end; i++) {
    			selection.add(queries.get(i));
    		}
    		return selection;
    }
    	


    
    private static List<Judgments> loadJudgments(int id) throws IOException {
        
        
        var is = IndexTreeCovid.class.getResourceAsStream( "/trec-covid/qrels/test.tsv");	        
        List<Judgments>jmts = new ArrayList<Judgments>();
        
        try (Scanner scanner = new Scanner(is)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("\t");
                if (!(parts[0].compareToIgnoreCase("query-id")==0)) {
	                if (parts.length == 3) {
	                    int queryId = Integer.parseInt(parts[0]);
	                    String corpusId = parts[1];
	                    int score = Integer.parseInt(parts[2]);
	                   jmts.add(new Judgments(queryId, corpusId, score));
	                }
                }
            }
            List<Judgments> selection = new ArrayList<Judgments>();
    		for (Judgments j : jmts) {
    			if (j.query()== id) {
    				selection.add(j);
    			}
    		}
    		return selection;
       
   	}
        }
    

    private static void escribirResultados(String tipo, Map<Double, Double> resultados, String metrica) throws IOException {
        String nombreArchivo = "TREC-COVID." + tipo + "." + metrica + ".csv";

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(nombreArchivo), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            // Escribir encabezado si el archivo está vacío
            if (Files.size(Paths.get(nombreArchivo)) == 0) {
                writer.write("Lambda/K1," + metrica);
                writer.newLine();
            }

            // Escribir los resultados en el archivo
            for (Map.Entry<Double, Double> entry : resultados.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    private static void escribirResultadoTest(String tipo, double lambdaOptimo, double metricaValor, String metrica) throws IOException {
        String nombreArchivo = "TREC-COVID." + tipo + ".test." + metrica + ".csv";

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(nombreArchivo), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            // Escribir encabezado si el archivo está vacío
            if (Files.size(Paths.get(nombreArchivo)) == 0) {
                writer.write("Lambda/K1," + metrica);
                writer.newLine();
            }

            // Escribir el resultado de prueba en el archivo
            writer.write(lambdaOptimo + "," + metricaValor);
            writer.newLine();
        }
    }

    private static LMJelinekMercerSimilarity getSimilarity(String model, float param) {
       if (model.equals("jm")) {
            return new LMJelinekMercerSimilarity(param);
        } else {
            return null; 
        }
    }

    private static float[] getParametros(String model) {
        if (model.equals("jm")) {
            return new float[]{0.001f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
        } else {
            return new float[]{0.4f, 0.6f, 0.8f, 1.0f}; // Parámetros para BM25
        }
    }


    private static double obtenerLambdaOptimo(String model, String indexPath, int start, int end, int cut, String metrica) throws IOException, ParseException {
        double lambdaOptimo = 0;
        double maxMetrica = Double.MIN_VALUE;
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader indexReader = DirectoryReader.open(dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer();

        for (float param : getParametros(model)) {
            double metricaPromedio = 0;
            if (model.equalsIgnoreCase("jm")) {//comprobamos si se crea o se modifica
	        	indexSearcher.setSimilarity(new LMJelinekMercerSimilarity(param));
	             } else if(model.equalsIgnoreCase("bm25")) {  // Add new documents to an existing index:
	            	 indexSearcher.setSimilarity(new BM25Similarity(param,(float) 0.75));
	             } else {
	            	 System.err.println("Wrong similarity Mode");
			         System.exit(1);
	             }

            for (int i = start; i <= end; i++) {
                String queryText = obtenerTextoQueryDesdeArchivo(i);
                TopDocs topDocs = indexSearcher.search(new QueryParser("text", analyzer).parse(queryText), cut);
                List<Judgments> judgmentsMap = loadJudgments(i); // Carga de judgmentsMap para cada consulta
                double metricaValor = calcularMetrica(indexReader, topDocs, judgmentsMap, cut, metrica);
                metricaPromedio += metricaValor;
            }

            metricaPromedio /= (end - start + 1);

            if (metricaPromedio > maxMetrica) {
                maxMetrica = metricaPromedio;
                lambdaOptimo = param;
            }
        }

        indexReader.close();
        dir.close();

        return lambdaOptimo;
    }

    private static String obtenerTextoQueryDesdeArchivo(int queryId) {
        
        return ""; 
    }
}
	    

