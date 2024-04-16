package es.udc.fi.ri.practica;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class SearchEvalTreeCovid {

	 public static void main(String[] args) throws IOException, ParseException {
		//string the ayuda en caso de fallo
		    String usage =
		        "SearchEvalTreeCovid"
		            + "  [-index INDEX_PATH] [-search jm <lambda> | bm25 <k1>][-cut n] [-top topN] [-queries  all | <int1> | <int1-int2> ] \n";
			
			if (args.length < 11) {
				System.out.println("More parametres are needed");
				  System.err.println("Usage: " + usage);
			      System.exit(1);
			}
		
			//Declaración de variables
			
			String indexPath = null;
	        int  topN = 10;
	        int  cut = 10;
	        String q= null;
	        boolean all = false;
	        int  start = 0;
	        int  end = 0;
	    	String iModel = null; //o esto o el bool de abajo que son dos tipos y requieren un float como parametro??
	    	float lambda = 0;
	    	float k1 = 0;

		    //Inicialización de variables con argumentos
		    for (int i = 0; i < args.length; i++) {
		      switch (args[i]) {
		        case "-index":
		          indexPath = args[++i];
		          break;
		        case "-top":
		          topN =  Integer.valueOf(args[++i]);
		          break;
		        case "-cut":
			          cut =  Integer.valueOf(args[++i]);
			          break;
		        case "-queries":
			          q =  args[++i];
			          if(q.equalsIgnoreCase("all")) {
			        	  all = true;
			          }else if (q.contains("-")) {
			        	 String[] queries = q.split("-");
			        	 start=Integer.valueOf( queries[0]);
			        	 end = Integer.valueOf( queries[1]);
			        	 
			        	  if (end>start) {
			        		  System.err.println("Query selecction is incorrect");
			  	              System.exit(1);
			        	  }
			        	  
			          }else {
			        	  start = end =  Integer.valueOf(q);
			          }
			          break;
		        case "-search":
		        	iModel=args[++i];
		          if (iModel.equals("jm")){
				  lambda = Float.valueOf(args[++i]);
				  }else {
				  k1 = Float.valueOf(args[++i]);
			      }
		          break;
		        default:
		          throw new IllegalArgumentException("unknown parameter " + args[i]);
		      }
		    }
		    
		    if (indexPath == null) {
		        System.err.println("Usage: " + usage);
		        System.exit(1);
		      }
		    
		    
	        
	        //tratar con las queries
	        
	        List<Query> toExam = selectQueries(all, start, end); 
		    Directory dir;
			DirectoryReader indexReader;
			IndexSearcher indexSearcher;
			Analyzer analyzer;
			try {
				dir = FSDirectory.open(Paths.get(indexPath));
				indexReader = DirectoryReader.open(dir);
		        indexSearcher = new IndexSearcher(indexReader);
		        analyzer  = (new StandardAnalyzer());
		        
		        if (iModel.equalsIgnoreCase("jm")) {//comprobamos si se crea o se modifica
		        	indexSearcher.setSimilarity(new LMJelinekMercerSimilarity(lambda));
		             } else if(iModel.equalsIgnoreCase("bm25")) {  // Add new documents to an existing index:
		            	 indexSearcher.setSimilarity(new BM25Similarity(k1,(float) 0.75));
		             } else {
		            	 System.err.println("Wrong similarity Mode");
				         System.exit(1);
		             }
		   
				for(Query qr : toExam) {
					String[] queryText= qr.metadata().query().split(" ");
	                TopDocs topDocs = null;
					try {
						topDocs = indexSearcher.search(MultiFieldQueryParser.parse(queryText,new String[]{"text"}, analyzer), topN);
					} catch (IOException | ParseException e) {
						System.err.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
				         System.exit(1);
					}
					List<Judgments> judgmentsMap = loadJudgments(Integer.valueOf(qr.id()));
					
					calculate(topDocs, judgmentsMap,qr );
					
				}
			  
			    
			    
			        indexReader.close();
			        dir.close();
			} catch (CorruptIndexException e1) {
				System.out.println(" caught a " + e1.getClass() + "\n with message: " + e1.getMessage());
				e1.printStackTrace();
				return;
			} catch (IOException e1) {
				System.out.println(" caught a " + e1.getClass() + "\n with message: " + e1.getMessage());
				e1.printStackTrace();
				return;
			}
	    }

	 /**
	  * Funcion que revisa las queries para evitar problemas de vocabulario
	  * @param all boolean que indica si se seben usar todas las queries de la lista
	  * @param start Indice de la primera query a revisar
	  * @param end Indice de la ultima query a revisar
	  * @param queries lista de todas las queries parseadas del jason
	  * @return
	 * @throws IOException 
	  */
	    private static List<Query> selectQueries(boolean all, int start, int end) throws IOException {
	    	var is = IndexTreeCovid.class.getResourceAsStream( "/trec-covid/queries.jsonl");
	        ObjectReader reader = JsonMapper.builder().findAndAddModules().build()
	                .readerFor(CovidDocument.class);
	        
	        List<Query> queries = reader.<Query>readValues(is).readAll();
	    	
	    	if(all) {
	    		 return queries;
	    		
	    	}else {
	    		List<Query> selection = new ArrayList<Query>();
	    		for (int i = start; i<= end; i++) {
	    			selection.add(queries.get(i));
	    		}
	    		return selection;
	    	}
	}

	    /**
	     *Funcion que devuelve los juicios de valor de la query que se esta analizando 
	     * @int query id
	     * @return Lista de Judgment
	     * @throws IOException
	     */
	     private static List<Judgments> loadJudgments(int start) throws IOException {
	    	        
	    	        
	    	        var is = IndexTreeCovid.class.getResourceAsStream( "/trec-covid/qrels/test.tsv");
	    	        ObjectReader reader = JsonMapper.builder().findAndAddModules().build()
	    	                .readerFor(CovidDocument.class);	        
	    	        List<Judgments> jmts = reader.<Judgments>readValues(is).readAll();
	    	       
	   	    		List<Judgments> selection = new ArrayList<Judgments>();
	   	    		for (Judgments j : jmts) {
	   	    			if (j.query()== start) {
	   	    				selection.add(j);
	   	    			}
	   	    		}
	   	    		return selection;
	   	    	}
	    	    
	    	    

	/**
	 * Funcion que escribe en archivo 
	 * @param filepath path del archivo que debe escribir
	 * @param line linea que añade al archivo
	 * @throws IOException
	 */
		
	private static void writeToFile(String filepath, String line) throws IOException {
	    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filepath), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
	        writer.write(line);
	        writer.newLine();
	    }catch(IOException e){
			System.out.println("Impossible to write on file");
	}
	}
		 
	/**
	 */
	public static void calculate(TopDocs topDocs, List<Judgments> judgmentsMap, Query q )  {
		      
		      
	
		        // Metrics variables
		        double totalPAtN = 0;
		        double totalRecallAtN = 0;
		        double totalMAPAtN = 0;
		        double totalMRR = 0;
		        int evaluatedQueries = 0;
	
		        // Calculate metrics
	            double pAtN = calculatePAtN();
	            double recallAtN = calculateRecallAtN();
	            double mapAtN = calculateMAPAtN();
	            double mrr = calculateMRR();
	
	            // Print query and metrics
	            System.out.println("Query: " + q.metadata().query());
	            System.out.println("P@N: " + pAtN);
	            System.out.println("Recall@N: " + recallAtN);
	            System.out.println("MAP@N: " + mapAtN);
	            System.out.println("MRR: " + mrr);
	            System.out.println();
	
	            // Update total metrics
	            totalPAtN += pAtN;
	            totalRecallAtN += recallAtN;
	            totalMAPAtN += mapAtN;
	            totalMRR += mrr;
	            evaluatedQueries++; 
	
		        // Calculate average metrics
		        double avgPAtN = totalPAtN / evaluatedQueries;
		        double avgRecallAtN = totalRecallAtN / evaluatedQueries;
		        double avgMAPAtN = totalMAPAtN / evaluatedQueries;
		        double avgMRR = totalMRR / evaluatedQueries;
	
		        // Print average metrics
		        System.out.println("Average P@N: " + avgPAtN);
		        System.out.println("Average Recall@N: " + avgRecallAtN);
		        System.out.println("Average MAP@N: " + avgMAPAtN);
		        System.out.println("Average MRR: " + avgMRR);
	
		      
		    }
	


	    private static double calculatePAtN() {
	        // Implement P@N calculation
	        return 0;
	    }

	    private static double calculateRecallAtN() {
	        // Implement Recall@N calculation
	        return 0;
	    }

	    private static double calculateMAPAtN() {
	        // Implement MAP@N calculation
	        return 0;
	    }

	    private static double calculateMRR() {
	        // Implement MRR calculation
	        return 0;
	    }
	}

