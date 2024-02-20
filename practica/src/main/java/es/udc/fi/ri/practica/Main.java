package es.udc.fi.ri.practica;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
/**
 * Hello world!
 *
 */
public class Main 
{
    public static void main( String[] args )
    {
    	Analyzer analyzer = new StandardAnalyzer();
        System.out.println( "Hello World!" );
    }
}
