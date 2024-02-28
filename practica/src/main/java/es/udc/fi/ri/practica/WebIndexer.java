package es.udc.fi.ri.practica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.demo.knn.KnnVectorDict;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

import es.udc.fi.ri.practica.WebIndexer;

//serverpoolthread clase java para el multihilo
//cliente http.HttpClient
// libreria jsoup para el procesado de los html 
//


public class WebIndexer implements AutoCloseable {
	
	public static void main(String[] args) throws Exception {
	    String usage =
	        "java org.apache.lucene.demo.IndexFiles"
	            + " [-index INDEX_PATH] [-docs DOCS_PATH] [-create] [-numThreads] [-h] [-p] [-titleTermVectors] [-bodyTermVectors] [-analyzer]  \n\n"
	            + "This indexes the documents in DOCS_PATH, creating a Lucene index"
	            + "in INDEX_PATH that can be searched with SearchFiles\n";
	   
	    String INDEX_PATH = "index";
	    String DOCS_PATH = null;
	    boolean create = false;
	    int numThreads = Runtime.getRuntime().availableProcessors();
	    boolean infoThread= false;
	    boolean infoIndex=false;
	    boolean title=false;
	    boolean body=false;
	    boolean useAnalyzer=false;
	    
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
	        case "-numThread":
	          int aux = Integer.valueOf(args[++i]);
	          if (aux <  numThreads) numThreads = aux;
	          break;
	        case "-h":
	          infoThread = true;
	          break;
	        case "-p":
		      infoIndex = true;
		      break;
	        case "-titleTermVector":
		      title = true;
		      break;
	        case "-bodyTermVectors":
		      body = true;
		      break;
	        case "-analyzer":
	        	//ver opciones que damos por defecto el standard
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

	   
	    //
	    final Path docDir = Paths.get(DOCS_PATH);
	    if (!Files.isReadable(docDir)) {
	      System.out.println(
	          "Document directory '"
	              + docDir.toAbsolutePath()
	              + "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }

	    Date start = new Date();
	    try {
	      System.out.println("Indexing to directory '" + INDEX_PATH + "'...");

	      Directory dir = FSDirectory.open(Paths.get( INDEX_PATH));
	      Analyzer analyzer = new StandardAnalyzer();
	      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

	      if (create) {
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

	     /* KnnVectorDict vectorDictInstance = null;
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
	      }*/

	      Date end = new Date();
	      try (IndexReader reader = DirectoryReader.open(dir)) {
	        System.out.println(
	            "Indexed "
	                + reader.numDocs()
	                + " documents in "
	                + (end.getTime() - start.getTime())
	                + " ms");
	        if (reader.numDocs() > 100
	           // && vectorDictSize < 1_000_000
	            && System.getProperty("smoketester") == null) {
	          throw new RuntimeException(
	              "Are you (ab)using the toy vector dictionary? See the package javadocs to understand why you got this exception.");
	        }
	      }
	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
	    }
	  }
	

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
	

}

