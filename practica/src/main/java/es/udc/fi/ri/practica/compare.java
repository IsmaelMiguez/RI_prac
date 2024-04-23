package es.udc.fi.ri.practica;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.apache.lucene.queryparser.classic.ParseException;

public class compare {
	
	public static void main(String[] args) throws IOException, ParseException {
		//string the ayuda en caso de fallo
		    String usage =
		        "SearchEvalTreeCovid"
		            + "  [-tTest|wilcoxon ALPHA_VALUE] [-results FILE_NAME_1 FILE_NAME_2]\n";
			
			if (args.length < 5) {
				System.out.println("More parametres are needed");
				  System.err.println("Usage: " + usage);
			      System.exit(1);
			}
		
			//Declaración de variables
			
			Boolean tTest = false;
			Boolean wilcoxon = false; 
			Integer alpha = 0;
			String results1= null;
			String results2= null;
			
			
		    //Inicialización de variables con argumentos
		    for (int i = 0; i < args.length; i++) {
		      switch (args[i]) {
		        case "-tTest":
		        	tTest = true;
		        	alpha = Integer.valueOf(args[++i]);
		          break;
		        case "-wilcoxon":
		        	wilcoxon = true;
		        	alpha = Integer.valueOf(args[++i]);
		          break;
		        case "-results":
		          results1 = args[++i];
		          results2 = args[i+2];
		          break;
		        default:
		          throw new IllegalArgumentException("unknown parameter " + args[i]);
		      }
		    }
		    
		    if ((tTest && wilcoxon) || (!tTest && !wilcoxon)) {
	            System.err.println("Please specify either -tTest or -wilcoxon");
	            System.err.println("Usage: " + usage);
	            System.exit(1);
	        }

	        if (results1 == null || results2 == null) {
	            System.err.println("Results files not specified");
	            System.err.println("Usage: " + usage);
	            System.exit(1);
	        }

	        // Leer los resultados
	        double[] model1Results = readResults(results1);
	        double[] model2Results = readResults(results2);

	        // Se hacen los test pertinentes
	        if (tTest) {
	            performTTest(model1Results, model2Results, alpha);
	        } else {
	            performWilcoxonTest(model1Results, model2Results, alpha);
	        }
	    }

	    private static double[] readResults(String fileName) throws IOException {
	        BufferedReader reader = new BufferedReader(new FileReader(fileName));
	        String line;
	        int count = 0;
	        while ((line = reader.readLine()) != null) {
	            count++;
	        }
	        reader.close();

	        double[] results = new double[count - 1]; 
	        reader = new BufferedReader(new FileReader(fileName));
	        reader.readLine(); 
	        int i = 0;
	        while ((line = reader.readLine()) != null) {
	            String[] parts = line.split(",");
	            results[i++] = Double.parseDouble(parts[1]); 
	        }
	        reader.close();

	        return results;
	    }

	    private static void performTTest(double[] model1Results, double[] model2Results, double alpha) {
	        double pValue = TestUtils.tTest(model1Results, model2Results);
	        System.out.println("T-Test Result:");
	        System.out.println("p-value: " + pValue);
	        if (pValue < alpha) {
	            System.out.println("Reject null hypothesis: There is a significant difference between the models.");
	        } else {
	            System.out.println("Fail to reject null hypothesis: There is no significant difference between the models.");
	        }
	    }

	    private static void performWilcoxonTest(double[] model1Results, double[] model2Results, double alpha) {
	        WilcoxonSignedRankTest test = new WilcoxonSignedRankTest();
	        double pValue = test.wilcoxonSignedRankTest(model1Results, model2Results, false);
	        System.out.println("Wilcoxon Signed-Rank Test Result:");
	        System.out.println("p-value: " + pValue);
	        if (pValue < alpha) {
	            System.out.println("Reject null hypothesis: There is a significant difference between the models.");
	        } else {
	            System.out.println("Fail to reject null hypothesis: There is no significant difference between the models.");
	        }
	    }
	}