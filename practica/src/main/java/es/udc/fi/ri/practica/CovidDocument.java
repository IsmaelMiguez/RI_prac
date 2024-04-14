package es.udc.fi.ri.practica;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CovidDocument( String id, String title, String text,String url,String pubmed_id) 
{

}
