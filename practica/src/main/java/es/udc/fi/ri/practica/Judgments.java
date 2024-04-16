package es.udc.fi.ri.practica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import es.udc.fi.ri.practica.Query.Metadata;

public record Judgments( @JsonProperty("query-id")int query, @JsonProperty("corpus-id") String corpus, int score) 
{
	
}
