package es.udc.fi.ri.practica;

import java.io.BufferedReader; //https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html
import java.io.IOException; //https://docs.oracle.com/javase/8/docs/api/java/io/IOException.html
import java.io.InputStream; //https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html
import java.io.InputStreamReader;//https://docs.oracle.com/javase/8/docs/api/java/io/InputStreamReader.html
import java.net.InetAddress; //https://docs.oracle.com/javase/8/docs/api/java/net/InetAddress.html
import java.net.URI; //https://docs.oracle.com/javase/8/docs/api/java/net/URI.html
import java.net.URISyntaxException;
import java.net.http.HttpClient; //https://docs.oracle.com/en%2Fjava%2Fjavase%2F11%2Fdocs%2Fapi%2F%2F/java.net.http/java/net/http/HttpClient.html
import java.net.http.HttpRequest; //https://docs.oracle.com/en%2Fjava%2Fjavase%2F11%2Fdocs%2Fapi%2F%2F/java.net.http/java/net/http/HttpRequest.html
import java.net.http.HttpResponse;//https://docs.oracle.com/en%2Fjava%2Fjavase%2F11%2Fdocs%2Fapi%2F%2F/java.net.http/java/net/http/HttpResponse.html
import java.nio.charset.StandardCharsets; //https://docs.oracle.com/javase/8/docs/api/java/nio/charset/StandardCharsets.html
import java.nio.file.Files;//https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html
import java.nio.file.Path;//https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html
import java.nio.file.Paths;//https://docs.oracle.com/javase/8/docs/api/java/nio/file/Paths.html
import java.nio.file.StandardOpenOption;//https://docs.oracle.com/javase/8/docs/api/java/nio/file/StandardOpenOption.html
import java.nio.file.attribute.BasicFileAttributes;//https://docs.oracle.com/javase%2F7%2Fdocs%2Fapi%2F%2F/java/nio/file/attribute/BasicFileAttributes.html
import java.nio.file.attribute.FileTime;//https://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/FileTime.html
import java.util.ArrayList;//https://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html
import java.util.Date;//https://docs.oracle.com/javase/8/docs/api/java/util/Date.html
import java.util.HashSet;//https://docs.oracle.com/javase/8/docs/api/java/util/HashSet.html
import java.util.LinkedList;//https://docs.oracle.com/javase/8/docs/api/java/util/LinkedList.html
import java.util.List;//https://docs.oracle.com/javase/8/docs/api/java/util/List.html
import java.util.Properties;//https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html
import java.util.Set;//https://docs.oracle.com/javase/8/docs/api/java/util/Set.html
import java.util.concurrent.ExecutorService;//https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
import java.util.concurrent.Executors;//https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html
import java.util.regex.Matcher;//https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html
import java.util.regex.Pattern;//https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html

import org.apache.lucene.analysis.Analyzer;//https://lucene.apache.org/core/7_0_1/core/org/apache/lucene/analysis/Analyzer.html
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;//https://lucene.apache.org/core/7_0_1/core/org/apache/lucene/document/DateTools.html
import org.apache.lucene.document.Field;//https://lucene.apache.org/core/7_0_1/core/org/apache/lucene/document/Field.html
import org.apache.lucene.document.FieldType;
//import org.apache.lucene.document.Field.TermVector//https://lucene.apache.org/core/5_4_1/core/org/apache/lucene/document/Field.TermVector.html
import org.apache.lucene.document.KeywordField;//https://lucene.apache.org/core/9_9_0/core/org/apache/lucene/document/KeywordField.html
import org.apache.lucene.document.LongField;//https://lucene.apache.org/core/9_9_0/core/org/apache/lucene/document/LongField.html
import org.apache.lucene.document.StringField;//https://lucene.apache.org/core/9_9_0/core/org/apache/lucene/document/StringField.html
import org.apache.lucene.document.TextField;//https://lucene.apache.org/core/9_9_0/core/org/apache/lucene/document/TextField.html
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;//https://lucene.apache.org/core/7_0_1/core/org/apache/lucene/index/IndexWriter.html
import org.apache.lucene.index.IndexWriterConfig;//https://lucene.apache.org/core/7_0_1/core/org/apache/lucene/index/IndexWriterConfig.html
import org.apache.lucene.index.IndexWriterConfig.OpenMode;//https://lucene.apache.org/core/7_0_1/core/org/apache/lucene/index/IndexWriterConfig.OpenMode.html
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.Directory;//https://lucene.apache.org/core/8_0_0/core/org/apache/lucene/store/Directory.html
import org.apache.lucene.store.FSDirectory;//https://https://lucene.apache.org/core/8_0_0/core/org/apache/lucene/store/FSDirectory.html
import org.apache.lucene.store.LockObtainFailedException;
import org.jsoup.Jsoup; //añadido a las dependencias
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
//https://lucene.apache.org/core/4_0_0/core/org/apache/lucene/document/Document.html
import com.fasterxml.jackson.databind.json.JsonMapper;


public class IndexTreeCovid  implements AutoCloseable {
	

		//para la creacion de la clase analyzer (indica que el metodo nueva INTANCIA DE CLASS ESTA OBSOLETO
		@SuppressWarnings({ "deprecation" })
	public static void main(String[] args) throws Exception {

	List<CovidDocument> docu = null;		
				
	 //Variables necesarias podemos inicializarlas en el main y pasarlas como parametros si es necesario
	String index = null;
	String docs = null;
	String openMode= null;
	String iModel = null; //o esto o el bool de abajo que son dos tipos y requieren un float como parametro??
	float lambda = 0;
	float k1 = 0;
	int numThreads = Runtime.getRuntime().availableProcessors();
	 
	//string the ayuda en caso de fallo
    String usage =
        "IndexTrecCovid"
            + " [-index INDEX_PATH] [-docs DOCS_PATH] [-openmode mode] [-indexingmodel model value] \n\n"
            + "This indexes the documents in DOCS_PATH, creating a Lucene index";
   
//comprobamos que tenemos los argumentos necesarios
    if (args.length <9) {
		System.out.println("A folder is needed for the index");
		  System.err.println("Usage: " + usage);
	      System.exit(1);
	}
    
			    
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-index":
          index = args[++i];
          break;
        case "-docs":
          docs = args[++i];
          break;
        case "-openmode":
           openMode = args[++i];
          break;
        case "-indexinmodel":
          if (args[++i].equals("jm")){
		lambda = Float.valueOf(args[++i]);
		}else {
		k1 = Float.valueOf(args[++i]);
	}
          break;
        default:
          throw new IllegalArgumentException("unknown parameter " + args[i]);
      }
    }

    if (index == null) {
      System.err.println("Usage: " + usage);
      System.exit(1);
    }
    
    if (docs == null) {
	      System.err.println("Usage: " + usage);
	      System.exit(1);
	    
    //generacion de pool de hilos
    ExecutorService executorService = Executors.newFixedThreadPool(numThreads);	    	    
    try {
        //comprobamos y cargamos el analyzer que se va a utilizar
		   
		        
		        //Para procesar en cada hilo
		        final String finalINDEX_PATH = index;
		        final String finalopenMode = openMode;
		        final String finaliMode = iModel;
		        final float finallambda = lambda;
		        final float finalK1 = k1;
		        
		        var is = IndexTreeCovid.class.getResourceAsStream(docs);
		        ObjectReader reader = JsonMapper.builder().findAndAddModules().build()
		                .readerFor(CovidDocument.class);
		        try {
		            docu = reader.<CovidDocument>readValues(is).readAll();
		        } catch (IOException e) {
		        	System.err.println("Usage: " + usage);
		            System.exit(1);
		        }   
		        
		        for (CovidDocument d : docu) {
		        	
                executorService.submit(()-> {
		         insertToIndex(finalINDEX_PATH, d, finalopenMode, finaliMode,finallambda,finalK1);
                   } );
		            }
		            
		 	     
		            while (!executorService.isTerminated()) {
		                //Comprueba si los hilos han acabado 
		            }
		            long elapsedTime = System.currentTimeMillis() ;
		            System.out.println("Created index in " + elapsedTime + " milliseconds");
		        

		    } finally {
		        // se cierra la pool de hilos
		        executorService.shutdown();
		    }

		}
    }
	
	
	 private static void insertToIndex(String INDEX_Path,CovidDocument Cdoc ,  String finalopenMode, 
			 							String finaliMode, float finallambda, float finalK1) {  
		 try {
	         // Configurar el directorio de índice
	          Directory dir = FSDirectory.open(Paths.get(INDEX_Path));
	          // Configurar el analizador de consultas
	          IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
	             if (finalopenMode.equalsIgnoreCase("create")) {//comprobamos si se crea o se modifica
	               iwc.setOpenMode(OpenMode.CREATE);
	             } else if(finalopenMode.equalsIgnoreCase("create_or_append")) {  // Add new documents to an existing index:
	               iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	             } else if (finalopenMode.equalsIgnoreCase("append")) {
	            	 iwc.setOpenMode(OpenMode.APPEND);
	             }else {
	            	 System.err.println("Wrong open Mode");
			            System.exit(1);
	             }

	             if (finaliMode.equalsIgnoreCase("jm")) {//comprobamos si se crea o se modifica
		               iwc.setSimilarity(new LMJelinekMercerSimilarity(finallambda));
		             } else if(finaliMode.equalsIgnoreCase("bm25")) {  // Add new documents to an existing index:
		               iwc.setSimilarity(new BM25Similarity(finalK1,(float) 0.75));
		             } else {
		            	 System.err.println("Wrong similarity Mode");
				            System.exit(1);
		             }
	             
	  	        // Crear un escritor de índice
	  	        IndexWriter writer = new IndexWriter(dir, iwc);
	  	        //lñector del archivo
	  	      org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
              doc.add(new KeywordField("id", Cdoc.id(), Field.Store.YES));
              doc.add(new TextField("title", Cdoc.title(), Field.Store.YES));
              doc.add(new TextField("text", Cdoc.text(), Field.Store.YES));
              doc.add(new TextField("url", Cdoc.url(), Field.Store.YES));
              doc.add(new TextField("pubmed_id", Cdoc.pubmed_id(), Field.Store.YES));
              writer.addDocument(doc);
          
     
	  	        
		 } catch(LockObtainFailedException e) {
			 System.out.println("Retry ");
			 
			 try {
				 Thread.sleep(1500);

				 insertToIndex(  INDEX_Path, Cdoc ,   finalopenMode, 
							 finaliMode, finallambda,  finalK1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			 }
		} catch (Exception e) {
	             System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());   	   
	           }		  
	}
	 

	 
		//funcion de cierre de los hilos
	 @Override
	 public void close() throws Exception {
			
		}

	}


