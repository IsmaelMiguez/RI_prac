package es.udc.fi.ri.practica;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TopTermsInDoc  {
	
	public static void main(String[] args) throws Exception {
		
		//string the ayuda en caso de fallo
	    String usage =
	        "TopTermsInDoc"
	            + " [-index indexPath] [-field field] [-docID docID / -url url] [-topN topN] [-outfile outfilePath] \n\n"
	            + "This search the documents in indexPath, showing doc information";
		
		if (args.length < 10) {
			System.out.println("More parametres are needed");
			  System.err.println("Usage: " + usage);
		      System.exit(1);
		}
	
		//Declaración de variables
		String indexPath = null;
        String field = null;
        int docID = 0;
        Boolean topN = false;
	int N;
        String outfilePath = null;
        String url = null;

	    //Inicialización de variables con argumentos
	    for (int i = 0; i < args.length; i++) {
	      switch (args[i]) {
	        case "-index":
	          indexPath = args[++i];
	          break;
	        case "-field":
	          field = args[++i];
	          break;
	        case "-docID":
	           docID = Integer.valueOf(args[++i]);
	          break;
	        case "-url":
	          url = args[++i];
	          break;
	        case "-outfile":
	        	outfilePath = args[++i];
	          break;
	        case "-topN":
	         topN = true;
	         N = Integer.valueOf(args[++i]);
		 break;
	        default:
	          throw new IllegalArgumentException("unknown parameter " + args[i]);
	      }
	    }
	    if (indexPath == null || field == null || (docID == 0 && url == null) || topN == 0 || outfilePath == null) {
            System.err.println("Missing required arguments.");
            System.exit(1);
        }
	    
	    Directory dir;
		DirectoryReader indexReader;

		try {
			dir = FSDirectory.open(Paths.get(indexPath));
			indexReader = DirectoryReader.open(dir);
		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
			return;
		}
	    
		// Lista para almacenar la información del documento
        List<String> documentInfo = new ArrayList<>();
        
     // Obtener información del documento según el docID o la URL
        if (docID != 0) {
            getDocumentInfoByDocID(indexReader, docID, field, documentInfo);
        } else {
		//comprobar que la url es valida
            getDocumentInfoByURL(indexReader, url, field, documentInfo);
        }
        
        
     // Ordenar la información de los documentos por TF x IDF
        Collections.sort(documentInfo, (doc1, doc2) -> {
            double tfidf1 = Double.parseDouble(doc1.split("\t")[3]);
            double tfidf2 = Double.parseDouble(doc2.split("\t")[3]);
            return Double.compare(tfidf2, tfidf1); // Orden descendente
        });

        // Obtener los primeros N documentos
        List<String> topNDocumentInfo = documentInfo.subList(0, Math.min(N, documentInfo.size()));

        // Imprimir los primeros N documentos
        System.out.println("Top " + N + " documentos:");
        for (String docInfo : topNDocumentInfo) {
            System.out.println(docInfo);
        }

        // Escribir los primeros N documentos en el archivo de salida
        writeToFile(outfilePath, topNDocumentInfo);

        // Cerrar el indexReader y el directorio
        indexReader.close();
        dir.close();
    }

    // Método para obtener información del documento por docID
    private static void getDocumentInfoByDocID(DirectoryReader indexReader, int docID, String field, List<String> documentInfo) throws IOException {
        for (int i = 0; i < indexReader.maxDoc(); i++) {
            if (indexReader.document(i).getField("docID").numericValue().intValue() == docID) {
                String title = indexReader.document(i).get("title");
                String body = indexReader.document(i).get("body");
                double tfidf = calculateTFIDF(indexReader, i, field);
                documentInfo.add(String.format("DocID: %d\tTítulo: %s\tContenido: %s\tTF x IDF: %.6f", docID, title, body, tfidf));
                break;
            }
        }
    }

    // Método para obtener información del documento por URL
    private static void getDocumentInfoByURL(DirectoryReader indexReader, String url, String field, List<String> documentInfo) throws IOException {
        for (int i = 0; i < indexReader.maxDoc(); i++) {
            if (indexReader.document(i).get("url").equals(url)) {
                int docID = indexReader.document(i).getField("docID").numericValue().intValue();
                String title = indexReader.document(i).get("title");
                String body = indexReader.document(i).get("body");
                double tfidf = calculateTFIDF(indexReader, i, field);
                documentInfo.add(String.format("DocID: %d\tTítulo: %s\tContenido: %s\tTF x IDF: %.6f", docID, title, body, tfidf));
                break;
            }
        }
    }
    
    
    // Método para calcular TF x IDF
    private static double calculateTFIDF(DirectoryReader indexReader, int docID, String field) throws IOException {
        Terms terms = indexReader.getTermVector(docID, field);
        if (terms == null) return 0.0;

        TermsEnum termsEnum = terms.iterator();
        int docFreq = 0;
        int totalFreq = 0;
        while (termsEnum.next() != null) {
            docFreq++;
            totalFreq += termsEnum.totalTermFreq();
        }

        double idf = Math.log((double) indexReader.maxDoc() / (docFreq + 1));
        double tf = 1 + Math.log(totalFreq);
        return tf * idf;
    }

    // Método para escribir en un archivo
    private static void writeToFile(String filepath, List<String> content) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filepath), StandardCharsets.UTF_8)) {
            for (String line : content) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
	    
	
