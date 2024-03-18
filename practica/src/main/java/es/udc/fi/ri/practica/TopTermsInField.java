package es.udc.fi.ri.practica;

import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.document.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TopTermsInField {

    public static void main(String[] args) throws IOException {
        String indexPath = null;
        String field = null;
        int topN = 0;
        String outfilePath = null;

        // Mensaje de uso
        String usage = "java TopTermsInField [-index indexPath] [-field field] [-topN topN] [-outfile outfilePath]\n\n"
                + "This retrieves the top N terms in the specified field from the index and stores them in a file.";

        // Comprobación de argumentos
        if (args.length < 8) {
            System.out.println("More parametres are needed");
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        // Procesamiento de argumentos
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-index":
                    indexPath = args[++i];
                    break;
                case "-field":
                    field = args[++i];
                    break;
                case "-topN":
                    topN = Integer.parseInt(args[++i]);
                    break;
                case "-outfile":
                    outfilePath = args[++i];
                    break;
                default:
                    throw new IllegalArgumentException("Parámetro desconocido " + args[i]);
            }
        }

        // Comprobación de argumentos requeridos
        if (indexPath == null || field == null || topN == 0 || outfilePath == null) {
            System.err.println("Faltan argumentos requeridos.");
            System.exit(1);
        }

        // Apertura del directorio del índice
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

		// Obtener términos y frecuencias del campo especificado
        List<TermFreq> termFreqList = new ArrayList<>();
        Fields fields = MultiFields.getFields(indexReader);
        Terms terms = fields.terms(field);
        TermsEnum termsEnum = terms.iterator();
        BytesRef term;
        while ((term = termsEnum.next()) != null) {
            String termText = term.utf8ToString();
            int docFreq = termsEnum.docFreq();
            termFreqList.add(new TermFreq(termText, docFreq));
        }

        // Ordenar los términos por frecuencia de documento
        Collections.sort(termFreqList, Comparator.comparingInt(TermFreq::getDocFreq).reversed());

        // Obtener los términos principales
        List<TermFreq> topTerms = termFreqList.subList(0, Math.min(topN, termFreqList.size()));

        // Imprimir los términos principales
        System.out.println("Los términos principales en el campo '" + field + "' ordenados por frecuencia de documento:");
        for (TermFreq termFreq : topTerms) {
            System.out.println(termFreq.getTerm() + ": " + termFreq.getDocFreq());
        }

        // Escribir los términos principales en el archivo de salida
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outfilePath), StandardCharsets.UTF_8)) {
            for (TermFreq termFreq : topTerms) {
                writer.write(termFreq.getTerm() + ": " + termFreq.getDocFreq());
                writer.newLine();
            }
        }

        // Cerrar el lector y el directorio
        indexReader.close();
        dir.close();
    }
}
