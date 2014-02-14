package org.geodi.lodtester.test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.geodi.lodtester.DefaultParamsProvider;

public class GetValues {

	public static String pickAnUri(String endpointUrl) throws UnsupportedEncodingException {
		String uri = "";

		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(endpointUrl);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		method.addRequestHeader("Accept", "application/sparql-results+json");
		try {
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				// System.out.println("Method failed: " +
				// method.getStatusLine());
				// System.out.println(endpointUrl);
				return uri;
			}
			// System.out.println(method.getResponseBodyAsString());
			ObjectMapper m = new ObjectMapper();
			StringWriter writer = new StringWriter();
			IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
			String responseString = writer.toString();
			JsonNode rootNode = m.readTree(responseString);
			uri = rootNode.findPath("s").findPath("value").getTextValue();
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return uri;

	}

	public static String getTot(String endpointUrl) {
		String result = "";

		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(endpointUrl);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		method.addRequestHeader("Accept", "application/sparql-results+json");
		try {
			int statusCode = client.executeMethod(method);
			// System.out.println(method.getResponseBodyAsString());
			ObjectMapper m = new ObjectMapper();
			StringWriter writer = new StringWriter();
			IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
			String responseString = writer.toString();
			JsonNode rootNode = m.readTree(responseString);
			result = rootNode.findPath("no").findPath("value").getTextValue();
		} catch (Exception e) {
	 
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return "0";

	}

	public static List<String> findConnections(Map<String, String> m, String thisEndpoint) throws UnsupportedEncodingException {
		List<String> result = new ArrayList<String>();
		
		
		
		for (String endpoint : m.keySet()) {
			if(!endpoint.equals(thisEndpoint)){
				String domain = m.get(endpoint).replaceAll("(http://[^/:]+).+", "$1");
				System.out.println("\t\tdomain "+domain);
				String query = DefaultParamsProvider.connectionsQuery.replaceAll("\\$\\{domain\\}",domain);
				System.out.println("\t\tquery "+query);
				String tot =  getTot(thisEndpoint + "?query=" + URLEncoder.encode(query, "UTF-8"));
				System.out.println(tot);
			}
			
			
			
		}
		
		
		
		return result;
	}
}
