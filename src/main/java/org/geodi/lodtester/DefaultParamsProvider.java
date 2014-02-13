package org.geodi.lodtester;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

public final class DefaultParamsProvider {

	public static final String standardQuery = "SELECT * {?s ?p ?o} LIMIT 1";
	public static final String pickAnUri = "SELECT * {?s a ?class FILTER(!isBlank(?s) ) FILTER( !REGEX(STR(?s),'openlink')) FILTER(!REGEX(STR(?class),'www.w3.org'))} LIMIT 1 OFFSET 1000";

	public static JsonNode getStatsQueries() {
		StringBuilder statistic = new StringBuilder();
		statistic.append("{\"list\":[");
		
		statistic.append("{\"key\":\"total number of triples\",");
		statistic.append("\"value\":\"SELECT (COUNT(*) AS ?no) { ?s ?p []  }\"},");
		
		statistic.append("{\"key\":\"total number of entities\",");
		statistic.append("\"value\":\"SELECT COUNT(distinct ?s) AS ?no { ?s a []  }\"},");
		
		statistic.append("{\"key\":\"total number of distinct resource URIs in the same endpoint\",");
		statistic.append("\"value\":\"SELECT (COUNT(DISTINCT ?s ) AS ?no) { { ?s ?p ?o  } UNION { ?o ?p ?s } FILTER(!isBlank(?s) && !isLiteral(?s)) }\"},");
		
		statistic.append("{\"key\":\"total number of distinct classes\",");
		statistic.append("\"value\":\"SELECT COUNT(distinct ?o) AS ?no { ?s rdf:type ?o }\"},");
		
		statistic.append("{\"key\":\"total number of distinct predicates\",");
		statistic.append("\"value\":\"SELECT count(distinct ?p) AS ?no { ?s ?p ?o }\"},");
		
		statistic.append("{\"key\":\"total number of distinct subject nodes\",");
		statistic.append("\"value\":\"SELECT (COUNT(DISTINCT ?s ) AS ?no) {  ?s ?p ?o   }\"}");
		
//		statistic.append("{\"key\":\"total number of distinct object nodes\",");
//		statistic.append("\"value\":\"SELECT (COUNT(DISTINCT ?o ) AS ?no) {  ?s ?p ?o  filter(!isLiteral(?o)) }\"},");
		 
//		statistic.append("{\"key\":\"exhaustive list of classes used in the dataset\",");
//		statistic.append("\"value\":\"SELECT DISTINCT ?type { ?s a ?type }\"},");
		
//		statistic.append("{\"key\":\"exhaustive list of properties used in the dataset\",");
//		statistic.append("\"value\":\"SELECT DISTINCT ?p { ?s ?p ?o }\"},");
		
//		statistic.append("{\"key\":\"table: class vs. total number of instances of the class\",");
//		statistic.append("\"value\":\"SELECT  ?class (COUNT(?s) AS ?count ) { ?s a ?class } GROUP BY ?class ORDER BY ?count\"},");

//		statistic.append("{\"key\":\"table: property vs. total number of triples using the property\",");
//		statistic.append("\"value\":\"SELECT  ?p (COUNT(?s) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count\"},");
		
//		statistic.append("{\"key\":\"table: property vs. total number of distinct subjects in triples using the property\",");
//		statistic.append("\"value\":\"SELECT  ?p (COUNT(DISTINCT ?s ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count\"},");
		
//		statistic.append("{\"key\":\"table: property vs. total number of distinct objects in triples using the property\",");
//		statistic.append("\"value\":\"SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count\"}");
		
		statistic.append("]}");
		ObjectMapper m = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = m.readTree(statistic.toString());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rootNode;
	}

}
