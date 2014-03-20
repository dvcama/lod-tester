package org.geodi.lodtester;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

public final class DefaultParamsProvider {

	public static final String standardQuery = "SELECT * {?s ?p ?o} LIMIT 1";
	public static final String pickAnUriSameDomain = "SELECT * {?s ?any ?class   FILTER(REGEX(STR(?s),'${domain}'))} LIMIT 1 OFFSET 1000";
	public static final String pickAnUri = "SELECT * {?s ?any ?class  FILTER( !REGEX(STR(?s),'openlink')) FILTER(!REGEX(STR(?class),'www.w3.org')) FILTER(REGEX(STR(?s),'^http://'))} LIMIT 1 OFFSET 1000";
	public static final String pickASameAs = "SELECT * {?s owl:sameAs ?class} LIMIT 1";

	public static final String connectionsQuery = "SELECT (COUNT(distinct ?o) AS ?no) ${graph} where {[] <${prop}> ?o. FILTER(regex(STR(?o),'^${domain}'))}";

	public static final String objPropQuery = "select distinct ?p ${graph}  where {[] ?p ?o. FILTER(isURI(?o)) FILTER(!regex(STR(?o),'^${domain}'))}";

	public static ArrayList<String> skipProps() {
		ArrayList<String> a = new ArrayList<String>();
		
		/* skipping some properties that probably are not connected to other endpoints*/
		
		a.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		a.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");
		a.add("http://www.w3.org/2000/01/rdf-schema#domain");
		a.add("http://www.w3.org/2000/01/rdf-schema#range");
		a.add("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");
		a.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		a.add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		a.add("http://www.w3.org/2002/07/owl#onProperty");
		a.add("http://www.w3.org/2002/07/owl#someValuesFrom");
		a.add("http://xmlns.com/foaf/0.1/depiction");
		a.add("http://xmlns.com/foaf/0.1/accountServiceHomepage");
		a.add("http://xmlns.com/foaf/0.1/page");
		a.add("http://xmlns.com/foaf/0.1/homepage");
		a.add("http://dati.camera.it"); // prefix
		a.add("http://lod.xdams.org"); // prefix
		a.add("http://www.openlinksw.com"); // prefix
		a.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1");
		a.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#_2");
		a.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#_3");
		a.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#_4");
		a.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#_5"); 
		a.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");
		a.add("http://www.w3.org/2002/07/owl#priorVersion");
		a.add("http://www.w3.org/2002/07/owl#imports");
		
		
		return a;
	}

	public static JsonNode getStatsQueries() {
		StringBuilder statistic = new StringBuilder();
		statistic.append("{\"list\":[");

		statistic.append("{\"key\":\"total number of triples\",");
		statistic.append("\"value\":\"SELECT (COUNT(*) AS ?no) { ?s ?p []  }\"},");

		statistic.append("{\"key\":\"total number of entities\",");
		statistic.append("\"value\":\"SELECT (COUNT(?s) AS ?no) { ?s a []  }\"},");

		statistic.append("{\"key\":\"total number of blankNodes\",");
		statistic.append("\"value\":\"SELECT (COUNT(distinct ?s) AS ?no) { ?s ?p [] FILTER(isBlank(?s)) }\"},");

		// statistic.append("{\"key\":\"total number of distinct resource URIs in the same endpoint\",");
		// statistic.append("\"value\":\"SELECT (COUNT(DISTINCT ?s ) AS ?no) { { ?s ?p ?o  } UNION { ?o ?p ?s } FILTER(!isBlank(?s) && !isLiteral(?s)) }\"},");

		statistic.append("{\"key\":\"total number of distinct classes\",");
		statistic.append("\"value\":\"SELECT (COUNT(distinct ?o) AS ?no) { ?s a ?o }\"},");

		statistic.append("{\"key\":\"total number of distinct predicates\",");
		statistic.append("\"value\":\"SELECT (count(distinct ?p) AS ?no) { ?s ?p ?o }\"},");

		statistic.append("{\"key\":\"total number of entities described by dc:title\",");
		statistic.append("\"value\":\"SELECT (count(distinct ?s) AS ?no) { ?s <http://purl.org/dc/elements/1.1/title> ?o }\"},");

		statistic.append("{\"key\":\"total number of entities described by rdfs:label\",");
		statistic.append("\"value\":\"SELECT (count(distinct ?s) AS ?no) { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o }\"},");

		statistic.append("{\"key\":\"total number of entities described by dc:date\",");
		statistic.append("\"value\":\"SELECT (count(distinct ?s) AS ?no) { ?s <http://purl.org/dc/elements/1.1/date>  ?o }\"}");

		// statistic.append("{\"key\":\"total number of distinct subject nodes\",");
		// statistic.append("\"value\":\"SELECT (COUNT(DISTINCT ?s ) AS ?no) {  ?s ?p []  }\"}");

		// statistic.append("{\"key\":\"total number of distinct object nodes\",");
		// statistic.append("\"value\":\"SELECT (COUNT(DISTINCT ?o ) AS ?no) {  ?s ?p ?o  filter(!isLiteral(?o)) }\"},");

		// statistic.append("{\"key\":\"exhaustive list of classes used in the dataset\",");
		// statistic.append("\"value\":\"SELECT DISTINCT ?type { ?s a ?type }\"},");

		// statistic.append("{\"key\":\"exhaustive list of properties used in the dataset\",");
		// statistic.append("\"value\":\"SELECT DISTINCT ?p { ?s ?p ?o }\"},");

		// statistic.append("{\"key\":\"table: class vs. total number of instances of the class\",");
		// statistic.append("\"value\":\"SELECT  ?class (COUNT(?s) AS ?count ) { ?s a ?class } GROUP BY ?class ORDER BY ?count\"},");

		// statistic.append("{\"key\":\"table: property vs. total number of triples using the property\",");
		// statistic.append("\"value\":\"SELECT  ?p (COUNT(?s) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count\"},");

		// statistic.append("{\"key\":\"table: property vs. total number of distinct subjects in triples using the property\",");
		// statistic.append("\"value\":\"SELECT  ?p (COUNT(DISTINCT ?s ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count\"},");

		// statistic.append("{\"key\":\"table: property vs. total number of distinct objects in triples using the property\",");
		// statistic.append("\"value\":\"SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count\"}");

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
