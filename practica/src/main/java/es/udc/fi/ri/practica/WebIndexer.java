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
import org.apache.lucene.store.Directory;//https://lucene.apache.org/core/8_0_0/core/org/apache/lucene/store/Directory.html
import org.apache.lucene.store.FSDirectory;//https://https://lucene.apache.org/core/8_0_0/core/org/apache/lucene/store/FSDirectory.html
import org.apache.lucene.store.LockObtainFailedException;
import org.jsoup.Jsoup; //añadido a las dependencias
import org.jsoup.nodes.Document;
//https://lucene.apache.org/core/4_0_0/core/org/apache/lucene/document/Document.html


public class WebIndexer implements AutoCloseable {
	


	//para la creacion de la clase analyzer (indica que el metodo nueva INTANCIA DE CLASS ESTA OBSOLETO
	@SuppressWarnings({ "deprecation" })
public static void main(String[] args) throws Exception {
		
		 //Variables necesarias podemos inicializarlas en el main y pasarlas como parametros si es necesario
			String INDEX_PATH = null;
			String DOCS_PATH = null;
			boolean create = false;
			int numThreads = Runtime.getRuntime().availableProcessors();
			boolean infoThread= false;
			boolean infoIndex=false;
			boolean termVec = false;
			boolean title=false;
			boolean body=false;
			boolean useAnalyzer=false;
			Analyzer analyzer=null; //porque me indica que esta duplicado??
			
			 // Cargar propiedades desde el archivo config.properties
			Properties properties = loadProperties();
			 // Obtener valores de propiedades según sea necesario
	        List<String> onlyDoms = new ArrayList<>();
	        onlyDoms.add(properties.getProperty("onlyDoms"));
	        onlyDoms.add(properties.getProperty("onlyDoms1"));
	        onlyDoms.add(properties.getProperty("onlyDoms2"));
        
             		
		//string the ayuda en caso de fallo
	    String usage =
	        "WebIndexer"
	            + " [-index INDEX_PATH] [-docs DOCS_PATH] [-create] [-numThreads num] [-h] [-p] [-titleTermVectors] [-bodyTermVectors] [-analyzer]  \n\n"
	            + "This indexes the documents in DOCS_PATH, creating a Lucene index"
	            + "in INDEX_PATH that can be searched with SearchFiles\n";
	   
	
	    if (args.length <4) {
			System.out.println("A folder is needed for the index");
			  System.err.println("Usage: " + usage);
		      System.exit(1);
		}
	    
	    
	    for (int i = 0; i < args.length; i++) {
	      switch (args[i]) {
	        case "-index":
	          INDEX_PATH = args[++i];
	          break;
	        case "-docs":
	          DOCS_PATH = args[++i];
	          break;
	        case "-create":
	           create = true;
	          break;
	        case "-numThreads":
	          int aux = Integer.valueOf(args[++i]);
	          if (aux <  numThreads) numThreads = aux;
	          break;
	        case "-h":
	          infoThread = true;
	          break;
	        case "-p":
		      infoIndex = true;
		      break;
	        case "-titleTermVectors":
	        	termVec = true;
	        	title = true;
		      break;
	        case "-bodyTermVectors":
	        	termVec = true;
	        	body = true;
		      break;
	        case "-analyzer":
	        	//ver opciones que damos por defecto el standard para ver que string se le da a la funcion
		          useAnalyzer = true;
		          break;
	        default:
	          throw new IllegalArgumentException("unknown parameter " + args[i]);
	      }
	    }

	    if (DOCS_PATH == null) {
	      System.err.println("Usage: " + usage);
	      System.exit(1);
	    }
	    
	    if (INDEX_PATH == null) {
		      System.err.println("Usage: " + usage);
		      System.exit(1);
		    }
    
	    //generacion de pool de hilos
	    ExecutorService executorService = Executors.newFixedThreadPool(numThreads);	    	    
	    try {
	        //comprobamos y cargamos el analyzer que se va a utilizar
	        if (useAnalyzer) {
	            String claseAnalyzer2 = properties.getProperty("analyzer2");
	            analyzer =  (Analyzer) Class.forName(claseAnalyzer2).newInstance(); //cambiar por la la
	        } else {
	            String claseAnalyzer1 = properties.getProperty("analyzer1"); 
	            analyzer = (Analyzer) Class.forName(claseAnalyzer1).newInstance(); 
	        }
	        
	        //Para procesar en cada hilo
	        final String finalDOCS_PATH = DOCS_PATH;
	        final String finalINDEX_PATH = INDEX_PATH;
	        final List<String> finalOnlyDoms = onlyDoms;
	        final Boolean finalCreate = create;
	        final Boolean finalInfoThread = infoThread;
	        final Boolean finalTitle = title;
	        final Boolean finalBody = body;
	        final Boolean finalTer = termVec;
	        final Analyzer finalAnalyzer1 = analyzer;
	        Set<String> finalvisitedURLs =  new HashSet<>();
			
	        
	        Path urlsPath = Path.of(DOCS_PATH);//archivo donde estan las url
	        Files.list(urlsPath)
	            .filter(p -> p.toString().endsWith(".url"))
	            .forEach(urlFile -> {
	                executorService.submit(() -> {
	                    try {
	                        processUrlFile(urlFile, finalDOCS_PATH, finalINDEX_PATH, finalCreate,
	                            finalInfoThread, finalTitle, finalBody, finalTer, finalAnalyzer1, finalvisitedURLs,  finalOnlyDoms); 
	                    } catch (IOException e) {
	                        e.printStackTrace(); // Handle appropriately
	                    }
	                });
	            });
	            
	        //  Imprime si se ha creado el indice
	        if (infoIndex) {
	            long startTime = System.currentTimeMillis();
	            executorService.shutdown();
	            while (!executorService.isTerminated()) {
	                //Comprueba si los hilos han acabado 
	            }
	            long elapsedTime = System.currentTimeMillis() - startTime;
	            System.out.println("Created index in " + elapsedTime + " milliseconds");
	        }

	    } finally {
	        // se cierra la pool de hilos
	        executorService.shutdown();
	    }

	}
//Procesado de la URL llama a funciones auxiliares que estan más abajo
private static void processUrlFile(Path urlFile, String docsPath, String indexPath,
                                    boolean createIndex, boolean infoThread,
                                    boolean titleTermVectors, boolean bodyTermVectors,
                                    boolean termVec, Analyzer analyzer, Set<String> visitedURLs ,
                                    List<String> onlyDoms) throws IOException {
  
     if (infoThread) {
    	 Date start = new Date();
         System.out.println(Thread.currentThread().getName() +
                 " started processing " + urlFile.getFileName() + "at" + start.toString());
     }
     try {
    	// Leer el archivo de URL y procesar cada URL
         List<String> urls = Files.readAllLines(urlFile);
         for (String url : urls) {
             // Descargar y indexar la página asociada a la URL
             downloadAndIndexPage(docsPath, indexPath, createIndex,
                     titleTermVectors, bodyTermVectors, termVec, analyzer,visitedURLs,url, onlyDoms);
         }
	} catch (IOException e) {		
		e.printStackTrace();
	} catch (InterruptedException e) {		
		e.printStackTrace();
	}
 
     if (infoThread) {
    	 Date end = new Date();
         System.out.println(Thread.currentThread().getName() +
                 " finished processing " + urlFile.getFileName()+ "at" + end.toString());
     }
 }
 //descarga de la web e indexado, requiere funciones auxiliares ver más abajo 
private static void downloadAndIndexPage( String docsPath, String indexPath,
                                          boolean createIndex, boolean titleTermVectors,
                                          boolean bodyTermVectors,boolean termVec, Analyzer analyzer,Set<String> visitedURLs ,
                                         String url,
                                          List<String> onlyDoms)
         throws  InterruptedException {
     // peticion de la url
         // Extract information from the response
    	 try {
    		 HttpClient httpClient = HttpClient.newHttpClient(); //CREAMOS CLIENTE
                 if(validateURL(url)){//COGEMOS LA PRIMERA DE LA LISTA
        
				if (isLegal(url, onlyDoms)) {
					if (!(isVisited(url, visitedURLs))){
					
		             HttpRequest request = HttpRequest.newBuilder()// CREAMOS E INICIAMOS REQUEST
		                     .uri(new URI(url))
		                     .build();
		             HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()); //HAcemos la peticion y la recojemos 
		             if (response.statusCode() == 200) {// Check si la respuesta es  200 OK , de momento no tratamos las 300	
										
                    insertToIndex(docsPath, url, analyzer,indexPath, createIndex, titleTermVectors,
                             bodyTermVectors, termVec,visitedURLs, response);
		             }
					}
				}
				}
		
         }catch (Exception e) {
                 e.printStackTrace();
         }
 
}
//extraemos el titulo
 private static String extractTitle(String pageContent) {
     //extraer el titulo con JSOUP
	 Document html = Jsoup.parse(pageContent);
     String titleSt = html.title().toString();
    
     return titleSt;
 }
//extraemos la parte marcada como cuerpo del html
 private static String extractBody(String pageContent) {
     // /extraer el cuerpo con JSOUP
	 Document html = Jsoup.parse(pageContent);
	 String bodySt = html.body().toString();
     return bodySt;
 }
 
//extraemos la parte marcada como cuerpo del html
private static String extractBodyNotag(String pageContent) {
    // /extraer el cuerpo con JSOUP
	 Document html = Jsoup.parse(pageContent);
	 String bodySt = html.body().text();
    return bodySt;
}
//Se crean los dos archivos indicados en la practica
 private static List<Path> saveToFile(String docsPath, String url, String pageContent,
                                String title, String body, String bodyNoTag) throws IOException {
     
	 List<Path> pth = new ArrayList<>();
	 // guardar .loc file
     Path locFilePath = Path.of(docsPath, getFileName(url, ".loc"));
     Files.writeString(locFilePath, pageContent, StandardOpenOption.CREATE);
     pth.add(locFilePath);
     // guardar .loc.notags file
     Path locNoTagsFilePath = Path.of(docsPath, getFileName(url, ".loc.notags"));
     String contentNoTags = title + "\n" + bodyNoTag;
     Files.writeString(locNoTagsFilePath, contentNoTags, StandardOpenOption.CREATE);
     pth.add(locNoTagsFilePath);
     return pth;
 }
//funcion que realiza la adaptación del nombre del archivo, remplazando http y los caracteres no permitidos en el nombre y añadiendo la extensión dada por parametro
 private static String getFileName(String url, String extension) {
     return url.replaceAll("^https?://", "").replaceAll("/", "_").replace("?", "").replace(":", "").replace("\\", "").replace("*", "").replace(">", "").replace("<", "") + extension;
 }
 //Funcion (de DSI) para cargar del archivo config.properties las configuraciones dadas, demomento doms y los tipos de Analyzers
 private static Properties loadProperties() throws IOException {
     Properties properties = new Properties();
     try (InputStream input = WebIndexer.class.getClassLoader().getResourceAsStream("config.properties")) {
         if (input != null) {
             properties.load(input);
         } else {
             throw new IOException("No se pudo cargar el archivo de configuración 'config.properties'");
         }
     }
     return properties;     
 }
 // funcion para el  crawler sacada de los apuntes de clase no se ha añadido la de robot
 private static boolean isVisited(String URL, Set<String> visitedURLs ) {
     return visitedURLs.contains(URL);
 }
//marca como visitada
 private static void setVisited(String URL, Set<String> visitedURLs) {
     visitedURLs.add(URL);
 }

 private static boolean isLegal(String URL,List<String>  onlyDoms) {	 
	 
	 for(int i = 0; i< onlyDoms.size(); i++) {
		 if  (URL.contains(onlyDoms.get(i))) return true; 
	 } 
  	 return false;  
 }
 
private static boolean validateURL(String url) {
     try {
         URI uri = new URI(url);
         // Comprobar si la URI tiene un esquema (protocolo) y un host
         return uri.getScheme() != null && uri.getHost() != null;
     } catch (URISyntaxException e) {
         return false;
     }
 }

 private static void listOfAnchors(String HTML, List<String> URLList) {
     
	 // Expresión regular para encontrar enlaces en la página HTML
     Pattern pattern = Pattern.compile("<a\\s+href\\s*=\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE);
     Matcher matcher = pattern.matcher(HTML);

     // Iterar sobre los resultados de la expresión regular y extraer los enlaces
     while (matcher.find()) {
         String anchor = matcher.group(1);
         if (validateURL(anchor)) URLList.add(anchor);
     }
 }

 private static void insertToIndex(String docsPath,String url,Analyzer analyzer, 
		 String INDEX_Path, boolean create, boolean title, boolean body, boolean term, 
		 Set<String> visited, HttpResponse<String> response) {  
	 try {
	  // Indexar el contenido con Lucene
		 String pageContent = response.body();
         String titleTag = extractTitle(pageContent);
         String bodyTag = extractBody(pageContent);
         String bodyNoTag = extractBodyNotag(pageContent);

         // Guardar el contenido a los archivos
        List<Path> file = saveToFile(docsPath, url, pageContent, titleTag, bodyTag, bodyNoTag); //funciones que crean los dos archivos que expecifica el enunciado
        //llamamos a la funcion que comprobara la existencia de nuevas url y las metera en la lista de url a visitar
        //listOfAnchors(bodyTag, URLList);
        //Comenzamos el proceso de indexado
		
         System.out.println("Indexing to directory '" + INDEX_Path + "'...");
         // Crear un nuevo analizador estándar de Lucene
         //el analizer ha sido inicializado, o lo pasamos como parametro o se accede, variable "global
	     // Analyzer analyzer = new StandardAnalyzer();
         
         //extraigo los path the la lista para poder usarlos
         Path locPath = file.get(0);
	     Path notag = file.get(1);
         
         // Configurar el directorio de índice
          Directory dir = FSDirectory.open(Paths.get(INDEX_Path));
          // Configurar el analizador de consultas
          IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
             if (create) {//comprobamos si se crea o se modifica
               iwc.setOpenMode(OpenMode.CREATE);
             } else {  // Add new documents to an existing index:
               iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
             }

  	        // Crear un escritor de índice
  	        IndexWriter writer = new IndexWriter(dir, iwc);
  	        //lñector del archivo
  	        try (InputStream stream = Files.newInputStream(notag)) {
  	          // Crear un nuevo documento
  	        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
           /************************************pendiente de añadir los campos, ver enunciado**/
  	        // Agregar el contenido HTML al documento
  	       
  	        
  	        doc.add(new KeywordField("path", locPath.toString(), Field.Store.YES));
  	        doc.add(new StringField("hostname", InetAddress.getLocalHost().getHostName(), Field.Store.YES));
  	        doc.add(new StringField("thread", Thread.currentThread().getName(),Field.Store.YES ));
  	        
  	        long locSize = Files.size(locPath) / 1024; // size in KB
            long notagsSize = Files.size(notag) / 1024; // size in KB
  	        
            doc.add(new LongField("locKb",locSize, Field.Store.YES));
  	        doc.add(new LongField("notagsKb", notagsSize, Field.Store.YES));
  	        
  	        BasicFileAttributes attr = Files.readAttributes(locPath, BasicFileAttributes.class);
            FileTime creationTime = attr.creationTime();
            FileTime lastAccessTime = attr.lastAccessTime();
            FileTime lastModifiedTime = attr.lastModifiedTime();
          
            doc.add(new TextField("creationTime", creationTime.toString(), Field.Store.YES));
            doc.add(new TextField("lastAccessTime", lastAccessTime.toString(), Field.Store.YES));
            doc.add(new TextField("lastModifiedTime", lastModifiedTime.toString(), Field.Store.YES));

            Date creationDate = new Date(creationTime.toMillis());
            Date lastAccessDate = new Date(lastAccessTime.toMillis());
            Date lastModifiedDate = new Date(lastModifiedTime.toMillis());
           
            String creationTimeLucene = DateTools.dateToString(creationDate, DateTools.Resolution.SECOND);
            String lastAccessTimeLucene = DateTools.dateToString(lastAccessDate, DateTools.Resolution.SECOND);
            String lastModifiedTimeLucene = DateTools.dateToString(lastModifiedDate, DateTools.Resolution.SECOND);
            
            doc.add(new TextField("creationTimeLucene", creationTimeLucene, Field.Store.YES));
            doc.add(new TextField("lastAccessTimeLucene", lastAccessTimeLucene, Field.Store.YES));
            doc.add(new TextField("lastModifiedTimeLucene", lastModifiedTimeLucene, Field.Store.YES));

            if(term) {
            	FieldType t = new FieldType();
            	t.setTokenized(true);
            	t.setStored(true);
            	t.setOmitNorms(true);
            	t.setStoreTermVectors(true);
            	t.setStoreTermVectorOffsets(true);
            	t.setStoreTermVectorPositions(true);
            	t.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            	
            	
            	if(title) {
            		doc.add(new Field ("title", titleTag, t));
            	}else {
            		 doc.add(new TextField("title", titleTag, Field.Store.YES));
            	}
            	if(body) {
            		doc.add(new Field ("body", bodyNoTag, t));
            	}else {
            		doc.add(new TextField("body", bodyNoTag, Field.Store.YES));
            	}
            }else {
            
  	        doc.add(new TextField("title", titleTag, Field.Store.YES));
  	        doc.add(new TextField("body", bodyNoTag, Field.Store.YES));
            }
  	        doc.add(
  	            new TextField(
  	                "contents",
  	                new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
  	        writeOn(writer, url, visited, doc);
  	       
  	        }
  	       
	 } catch(LockObtainFailedException e) {
		 System.out.println("Retry ");
		 
		 try {
			 Thread.sleep(1500);

			 insertToIndex( docsPath, url, analyzer, 
					  INDEX_Path,  create,  title,  body,  term, 
					  visited, response);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		 
	
		
	}}	  catch (Exception e) {
             System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
          
        	   
           }
	       
	  
}
 
private static void writeOn( IndexWriter writer , String url, Set<String> visited, org.apache.lucene.document.Document doc) {
	try {
		 // Escribir el documento en el índice
	      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
	        // si hay que crear un nuevo indice
	        System.out.println("adding " + url);
	        writer.addDocument(doc);
	      } else {
	        // si se puede crear o modificar
	        System.out.println("updating " + url);
	        writer.updateDocument(new org.apache.lucene.index.Term("path", url.toString()), doc);
	      }
		 writer.close(); 
 	      //esta URl se marca como visitada
           setVisited(url, visited);
          }
	 catch(LockObtainFailedException e) {
		 try {
			 Thread.sleep(3000);
			 writeOn(writer, url, visited, doc);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	 
	}catch (Exception ee) {
		
	}
		
}


 
	//funcion de cierre de los hilos
 @Override
 public void close() throws Exception {
		
	}

}
