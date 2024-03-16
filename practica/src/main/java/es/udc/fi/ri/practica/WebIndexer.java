package es.udc.fi.ri.practica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.demo.knn.KnnVectorDict;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.KeywordField;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;
import org.jsoup.Jsoup; //añadido a las dependencias
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

//import simpledemo.IndexFiles;


public class WebIndexer implements AutoCloseable {
	
	 private Set<String> visitedURLs =  new HashSet<>();
	 private List<String> URLList = new LinkedList<>();
	 private String onlyDoms;
	
	@SuppressWarnings({ "deprecation", "deprecation" })
	public static void main(String[] args) throws Exception {
		
		  // Cargar propiedades desde el archivo config.properties
        Properties properties = loadProperties();
       
        // Obtener valores de propiedades según sea necesario
        String onlyDoms = properties.getProperty("onlyDoms");
      
 		//Variables necesarias
	    String INDEX_PATH = "index";
	    String DOCS_PATH = null;
	    String analyzer= null;
	    boolean create = false;
	    int numThreads = Runtime.getRuntime().availableProcessors();
	    boolean infoThread= false;
	    boolean infoIndex=false;
	    boolean title=false;
	    boolean body=false;
	    boolean useAnalyzer=false;
	    Analyzer analyzer1=null; //porque me indica que esta duplicado??
		
		//string the ayuda en caso de fallo
	    String usage =
	        "java org.apache.lucene.demo.IndexFiles"
	            + " [-index INDEX_PATH] [-docs DOCS_PATH] [-create] [-numThreads] [-h] [-p] [-titleTermVectors] [-bodyTermVectors] [-analyzer]  \n\n"
	            + "This indexes the documents in DOCS_PATH, creating a Lucene index"
	            + "in INDEX_PATH that can be searched with SearchFiles\n";
	   
	
	    if (args.length != 1) {
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
		      title = true;
		      break;
	        case "-bodyTermVectors":
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
	            analyzer1 =  (Analyzer) Class.forName(claseAnalyzer2).newInstance(); //cambiar por la la
	        } else {
	            String claseAnalyzer1 = properties.getProperty("analyzer1"); 
	            analyzer1 = (Analyzer) Class.forName(claseAnalyzer1).newInstance(); 
	        }
	        
	        //Para procesar cada archivo en un hilo distinto
	        final String finalDOCS_PATH = DOCS_PATH;
	        final String finalINDEX_PATH = INDEX_PATH;
	        final Boolean finalCreate = create;
	        final Boolean finalInfoThread = infoThread;
	        final Boolean finalInfoIndex = infoIndex;
	        final Boolean finalBody = body;
	        final Analyzer finalAnalyzer1 = analyzer1;
	        Path urlsPath = Path.of(DOCS_PATH);//archivo donde estan las url
	        Files.list(urlsPath)
	            .filter(p -> p.toString().endsWith(".url"))
	            .forEach(urlFile -> {
	                executorService.submit(() -> {
	                    try {
	                        processUrlFile(urlFile, finalDOCS_PATH, finalINDEX_PATH, finalCreate,
	                            finalInfoThread, finalInfoIndex, finalBody, finalAnalyzer1); //pendiente como escoger y tratar el analyzes
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
	                //Comprueba si loa hilos han acabado 
	            }
	            long elapsedTime = System.currentTimeMillis() - startTime;
	            System.out.println("Created index in " + elapsedTime + " milliseconds");
	        }

	    } finally {
	        // se cierra la pool de hilos
	        executorService.shutdown();
	    }

}

 private static void processUrlFile(Path urlFile, String docsPath, String indexPath,
                                    boolean createIndex, boolean infoThread,
                                    boolean titleTermVectors, boolean bodyTermVectors,
                                    Analyzer analyzer) throws IOException {
   

     if (infoThread) {
         System.out.println(Thread.currentThread().getName() +
                 " started processing " + urlFile.getFileName());
     }
     try {
    	// Leer el archivo de URL y procesar cada URL
         List<String> urls = Files.readAllLines(urlFile);
         for (String url : urls) {
             // Descargar y indexar la página asociada a la URL
             downloadAndIndexPage(url, docsPath, indexPath, createIndex,
                     titleTermVectors, bodyTermVectors, analyzer);
         }
	} catch (IOException e) {		
		e.printStackTrace();
	} catch (InterruptedException e) {		
		e.printStackTrace();
	} catch (URISyntaxException e) { 
		e.printStackTrace();
	}

  
     if (infoThread) {
         System.out.println(Thread.currentThread().getName() +
                 " finished processing " + urlFile.getFileName());
     }
 }
//pendiente comprobar 
 private static void downloadAndIndexPage(String url, String docsPath, String indexPath,
                                          boolean createIndex, boolean titleTermVectors,
                                          boolean bodyTermVectors, Analyzer analyzer)
         throws IOException, InterruptedException, URISyntaxException {
     // peticion de la url
     HttpClient httpClient = HttpClient.newHttpClient();
     HttpRequest request = HttpRequest.newBuilder()
             .uri(new URI(url))
             .build();

     HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()); //HAcemos la peticion y la recojemos 

     // Check si la respuesta es  200 OK , de momento no tratamos las 300
     if (response.statusCode() == 200) {
         // Extract information from the response
         String pageContent = response.body();
         String title = extractTitle(pageContent);
         String body = extractBody(pageContent);

         // Guardar el contenido a los archivos
         saveToFile(docsPath, url, pageContent, title, body); //funciones que crean los dos archivos que expecifica el enunciado

         // Indexar el contenido con Lucene
         try {
             System.out.println("Indexing to directory '" + indexPath + "'...");

             Directory dir = FSDirectory.open(Paths.get(indexPath));
             IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

             if (createIndex) {
               // Create a new index in the directory, removing any
               // previously indexed documents:
               iwc.setOpenMode(OpenMode.CREATE);
             } else {
               // Add new documents to an existing index:
               iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
             }

             
             
             
             // Optional: for better indexing performance, if you
             // are indexing many documents, increase the RAM
             // buffer.  But if you do this, increase the max heap
             // size to the JVM (eg add -Xmx512m or -Xmx1g):
             //
             // iwc.setRAMBufferSizeMB(256.0);

             KnnVectorDict vectorDictInstance = null;
             long vectorDictSize = 0;
             if (vectorDictSource != null) {
               KnnVectorDict.build(Paths.get(vectorDictSource), dir, KNN_DICT);
               vectorDictInstance = new KnnVectorDict(dir, KNN_DICT);
               vectorDictSize = vectorDictInstance.ramBytesUsed();
             }

             try (IndexWriter writer = new IndexWriter(dir, iwc);
                 IndexFiles indexFiles = new IndexFiles(vectorDictInstance)) {
               indexFiles.indexDocs(writer, docDir);

               // NOTE: if you want to maximize search performance,
               // you can optionally call forceMerge here.  This can be
               // a terribly costly operation, so generally it's only
               // worth it when your index is relatively static (ie
               // you're done adding documents to it):
               //
               // writer.forceMerge(1);
             } finally {
               IOUtils.close(vectorDictInstance);
             }
             Date end = new Date();
             try (IndexReader reader = DirectoryReader.open(dir)) {
               System.out.println(
                   "Indexed "
                       + reader.numDocs()
                       + " documents in "
                       + (end.getTime() - start.getTime())
                       + " ms");
               if (reader.numDocs() > 100
                   && vectorDictSize < 1_000_000
                   && System.getProperty("smoketester") == null) {
                 throw new RuntimeException(
                     "Are you (ab)using the toy vector dictionary? See the package javadocs to understand why you got this exception.");
               }
             }
           } catch (Exception e) {
             System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
           }
         }
        	 
}

private void indexDocs(final IndexWriter writer, Path path) throws IOException {
	    if (Files.isDirectory(path)) {
	      Files.walkFileTree(
	          path,
	          new SimpleFileVisitor<>() {
	            @Override
	            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
	              try {
	                indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
	              } catch (
	                  @SuppressWarnings("unused")
	                  IOException ignore) {
	                ignore.printStackTrace(System.err);
	                // don't index files that can't be read.
	              }
	              return FileVisitResult.CONTINUE;
	            }
	          });
	    } else {
	      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
	    }
 }

	  /** Indexes a single document */
	  void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
	    try (InputStream stream = Files.newInputStream(file)) {
	      // make a new, empty document
	    	Document doc = new Document();

	      // Add the path of the file as a field named "path".  Use a
	      // field that is indexed (i.e. searchable), but don't tokenize
	      // the field into separate words and don't index term frequency
	      // or positional information:
	      doc.add(new KeywordField("path", file.toString(), Field.Store.YES));

	      // Add the last modified date of the file a field named "modified".
	      // Use a LongField that is indexed with points and doc values, and is efficient
	      // for both filtering (LongField#newRangeQuery) and sorting
	      // (LongField#newSortField).  This indexes to milli-second resolution, which
	      // is often too fine.  You could instead create a number based on
	      // year/month/day/hour/minutes/seconds, down the resolution you require.
	      // For example the long value 2011021714 would mean
	      // February 17, 2011, 2-3 PM.
	      doc.add(new LongField("modified", lastModified, Field.Store.NO));

	      // Add the contents of the file to a field named "contents".  Specify a Reader,
	      // so that the text of the file is tokenized and indexed, but not stored.
	      // Note that FileReader expects the file to be in UTF-8 encoding.
	      // If that's not the case searching for special characters will fail.
	      doc.add(
	          new TextField(
	              "contents",
	              new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

	      if (demoEmbeddings != null) {
	        try (InputStream in = Files.newInputStream(file)) {
	          float[] vector =
	              demoEmbeddings.computeEmbedding(
	                  new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
	          doc.add(
	              new KnnFloatVectorField(
	                  "contents-vector", vector, VectorSimilarityFunction.DOT_PRODUCT));
	        }
	      }

	      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
	        // New index, so we just add the document (no old document can be there):
	        System.out.println("adding " + file);
	        writer.addDocument(doc);
	      } else {
	        // Existing index (an old copy of this document may have been indexed) so
	        // we use updateDocument instead to replace the old one matching the exact
	        // path, if present:
	        System.out.println("updating " + file);
	        writer.updateDocument(new Term("path", file.toString()), doc);
	      }
	    }
	  }

	  //extraemos la parte marcada como titulo del html
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
//Se crean los dos archivos indicados en la practica
 private static void saveToFile(String docsPath, String url, String pageContent,
                                String title, String body) throws IOException {
     // guardar .loc file
     Path locFilePath = Path.of(docsPath, getFileName(url, ".loc"));
     Files.writeString(locFilePath, pageContent, StandardOpenOption.CREATE);

     // guardar .loc.notags file
     Path locNoTagsFilePath = Path.of(docsPath, getFileName(url, ".loc.notags"));
     String contentNoTags = title + "\n" + body;
     Files.writeString(locNoTagsFilePath, contentNoTags, StandardOpenOption.CREATE);
 }
//funcion que realiza la adaptación del nombre del archivo, remplazando http y los caracteres no permitidos en el nombre y añadiendo la extensión dada por parametro
 private static String getFileName(String url, String extension) {
     return url.replaceAll("^https?://", "").replaceAll("/", "_") + extension;
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
     
     
    /*
     *     Properties properties = new Properties();
    try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
        properties.load(input);
    }
    return properties;
}
     */
 }
 
 // funcion del crawler sacada de los apuntes de clase revisar si esta haciendo lo mismo que la llamada de antes al cliente de http

 public void crawl(String url, String onlyDoms) { 
	 this.onlyDoms = onlyDoms;
	 URLList.add(url);
     while (!URLList.isEmpty()) {
         String URL = URLList.remove(0);
         if (isVisited(URL) || !isLegal(URL, onlyDoms))
             continue;
         try {
             URL website = new URL(URL);
             HttpURLConnection connection = (HttpURLConnection) website.openConnection();
             connection.setRequestMethod("GET");
             Scanner scanner = new Scanner(connection.getInputStream());
             StringBuilder HTMLBuilder = new StringBuilder();
             while (scanner.hasNextLine()) {
                 HTMLBuilder.append(scanner.nextLine());
             }
             String HTML = HTMLBuilder.toString();
             scanner.close();
              //llamamos a la funcion que vomprobara la existencia de nuevas url y las metera en la lista de url a visitar
             listOfAnchors(HTML);
                
             setVisited(URL);
             insertToIndex(HTML);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }

 private boolean isVisited(String URL) {
     return visitedURLs.contains(URL);
 }

 private void setVisited(String URL) {
     visitedURLs.add(URL);
 }

 private boolean isLegal(String URL,String  onlyDoms) {
    return (URL.endsWith(onlyDoms));
    
 }

 private void listOfAnchors(String HTML) {
     
	 // Expresión regular para encontrar enlaces en la página HTML
     Pattern pattern = Pattern.compile("<a\\s+href\\s*=\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE);
     Matcher matcher = pattern.matcher(HTML);

     // Iterar sobre los resultados de la expresión regular y extraer los enlaces
     while (matcher.find()) {
         String anchor = matcher.group(1);
         URLList.add(anchor);
     }
 }

 private void insertToIndex(String HTML) {
	 /*  
	 try {
	        // Crear un nuevo analizador estándar de Lucene
	        Analyzer analyzer = new StandardAnalyzer();

	        // Configurar el directorio de índice
	        Directory indexDir = FSDirectory.open(Paths.get("index"));

	        // Configurar el analizador de consultas
	        IndexWriterConfig config = new IndexWriterConfig(analyzer);

	        // Crear un escritor de índice
	        IndexWriter writer = new IndexWriter(indexDir, config);

	        // Crear un nuevo documento
	        Document doc = new Document();

	        // Agregar el contenido HTML al documento
	        doc.add(new TextField("content", HTML, Field.Store.YES));

	        // Escribir el documento en el índice
	        writer.addDocument(doc);

	        // Cerrar el escritor de índice
	        writer.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    */
	}

 
 
 
	//funcion de cierre de los hilos
 @Override
 public void close() throws Exception {
	 IOUtils.close(vectorDict); //sacado de IndexFile
		
	}

}
