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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.opencsv.CSVWriter;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
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
	        "TrainingTestTrecCovid"
	            + " [-index INDEX_PATH] [-evaljm | -evalbm25] [-cut CUT_MODE] [-metrica METRICA_MODE] \n\n";
	   
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
		          q = args[++i];
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
		          q = args[++i];
		          String[] queries11 = p.split("-");
		          int1 =Integer.valueOf( queries11[0])-1;
		          int2 = Integer.valueOf( queries11[1])-1;
		          String[] queries21 = q.split("-");
		          int3 =Integer.valueOf( queries21[0])-1;
		          int4 = Integer.valueOf( queries21[1])-1;
	          break;	          
	        case "-cut":
	        	cut = Integer.valueOf(args[++i]);
	          break;
	        case "-metrica":
	        	metrica = args[++i];
	        	break;
	        default:
	          throw new IllegalArgumentException("unknown parameter " + args[++i]);
	      }
	    }

	    if (index == null) {
	      System.err.println("Usage: " + usage);
	      System.exit(1);
	    }
	    
	    if( (evaljm == true && evalbm25 == true)||(evaljm == false && evalbm25 == false)) {
		      System.err.println(" evaljm and evalbm25 are not compatible. Only one option can be loaded");
		      System.exit(1);
		      }
	    List<String> arg = new ArrayList<String>();
	     arg.add("-index");
   	 	 arg.add(index);
	   	 arg.add("-queries");
	   	 arg.add(p);
	   	 arg.add("-cut");
	   	 arg.add(String.valueOf(cut));
	   	 arg.add("-top");
	   	 arg.add(String.valueOf(100));
	   	 arg.add("-search");
	    List<String> arg2 =  new ArrayList<String>();
	    arg2.add("-index");
	   	 arg2.add(index);
	   	 arg2.add("-queries");
	   	 arg2.add(q);
	   	 arg2.add("-cut");
	   	 arg2.add(String.valueOf(cut));
	   	 arg2.add("-top");
	   	 arg2.add(String.valueOf(100));
	   	 arg2.add("-search");
	    
	 // Ejecutar proceso de entrenamiento o prueba
        if (evaljm) {//TO-DO PENDIENTE DE VER COMO ELABORAMOS LOS ARGUMENTOS A PASAR
        	//crear el string de los argumentos
        	
        	 arg.add("jm");
        	 arg2.add("jm");
        	 
        	
        } else if (evalbm25) {
        	//crear el string de los argumentos
        	 arg.add("bm25");
        	 arg2.add("bm25");
        	
        	  
        } else {
            System.err.println("Please specify either -evaljm or -evalbm25");
            System.exit(1);
        }
        
        HashMap<Double, Double> resultadosEntrenamiento = probarHiperparametros(arg, evaljm, metrica) ;
        HashMap<Double, Double> resultadosPrueba = probarHiperparametros(arg2,evaljm, metrica);
        //to do, extraer del Hashmap los valores mas altos.
        escribirResultados("jm.training." + (int1 + 1) + "-" + (int2 + 1), resultadosEntrenamiento, metrica);
        escribirResultados("jm.test." + (int3 + 1) + "-" + (int4 + 1), resultadosPrueba, metrica);
    }
    
	
	private static HashMap<Double, Double> probarHiperparametros(List<String> arg1, boolean evaljm ,String metrica) throws IOException, ParseException{
		HashMap<Double, Double> resultados = new HashMap<Double, Double>();
		 if (evaljm) {
			 Double[] parametros  = {0.001,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
				for(Double p : parametros ) {
					arg1.add(String.valueOf(p));
					 SearchEvalTrecCovid.main(arg1.toArray(new String[11]));
					 String path= "/TREC-COVID."+arg1.get(9)+"."+arg1.get(5)+".hits.lambda."+p+".q."+arg1.get(3)+".csv";	
					Estadisticos st = readStat(path);	
				    resultados.put(p,writeStat(metrica,st));
				    arg1.remove(String.valueOf(p));
				    	
				}
		 }else {
			 Double[] parametros = {0.4,0.6,0.8,1.0,1.2,1.4,1.6,1.8,2.0};
				for(Double p : parametros ) {
					arg1.add(String.valueOf(p));
					 SearchEvalTrecCovid.main(arg1.toArray(new String[11]));
					 String path= "/TREC-COVID."+arg1.get(9)+"."+arg1.get(5)+".hits.k1."+p+".q."+arg1.get(3);	
					Estadisticos st = readStat(path);	
				    resultados.put(p,writeStat(metrica,st));
				    arg1.remove(String.valueOf(p));
				    	
				}
		 }
		return resultados;
		
	}
	
	private static Double  writeStat(String metrica,Estadisticos st) {
		switch(metrica) {
		case "PN": 
			return st.PN();			
		case "RecallN":
			return st.RecallN();
		case "MAPN":
			return st.RecallN();
		case "MRR": 
			return st.RecallN();
		default:
			 throw new IllegalArgumentException("unknown parameter " + metrica);	
		}
	}


	private static Estadisticos readStat(String path) {
		 
        var is = TrainingTestTrecCovid.class.getResourceAsStream(path);	        
        Estadisticos stats;
        
        try (Scanner scanner = new Scanner(is)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts[0].compareToIgnoreCase("promedios")==0) {
	                if (parts.length == 5) {
	                    double PN = Double.parseDouble(parts[1]);
	                    double RecallN =  Double.parseDouble(parts[2]);;
	                    double MAPN =  Double.parseDouble(parts[3]);;
	                    double MRR =   Double.parseDouble(parts[4]);;
	                   stats= new Estadisticos(PN, RecallN, MAPN, MRR);
	                   return stats;
	                }
                }
            }
            return null;
       
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
	        Document documento = indexReader.storedFields().document(topDocs.scoreDocs[i].doc);
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

	

    
    private static List<Judgments> loadJudgments(int id) throws IOException {
        
        
        var is = IndexTrecCovid.class.getResourceAsStream( "/trec-covid/qrels/test.tsv");	        
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

        char delimitador = ','; // Coma como delimitador
	    char quotechar = '"';    // Carácter de comillas
	    char escapechar = '\\';  // Carácter de escape
	    String lineEnd = "\n";   // Terminador de línea
	    try (CSVWriter writer = new CSVWriter(new FileWriter(nombreArchivo, true), delimitador, quotechar, escapechar, lineEnd)) {
	        //to do pendiente de revisar
	//    	writer.writeNext(resultados, false);
	    } catch (IOException e) {
	        System.out.println("Impossible to write on file");
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
	    

	    

