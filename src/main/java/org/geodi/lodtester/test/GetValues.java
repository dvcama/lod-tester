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

	public static String pickAnUri(String endpointUrl) throws IOException {

		// same domain
		String uri = "";

		HttpClient client = new HttpClient();
		String aEndpointUri = endpointUrl;
		String domain = aEndpointUri.replaceAll("(^http://[^/]+).*", "$1").replaceAll(":[0-9]+$", "");
		String query = DefaultParamsProvider.pickAnUriSameDomain;
		aEndpointUri += java.net.URLEncoder.encode(query.replaceAll("\\$\\{domain\\}", domain), "UTF-8");
		GetMethod method = new GetMethod(aEndpointUri);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(2, false));
		method.getParams().setSoTimeout(180000);
		method.addRequestHeader("Accept", "application/sparql-results+json");
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_OK) {
				ObjectMapper m = new ObjectMapper();
				StringWriter writer = new StringWriter();
				IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
				String responseString = writer.toString();
				JsonNode rootNode = m.readTree(responseString);
				uri = rootNode.findPath("s").findPath("value").getTextValue();
			}
		} catch (Exception a) {
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		// other domain
		if (uri.equals("")) {
			aEndpointUri = endpointUrl;
			client = new HttpClient();
			query = DefaultParamsProvider.pickAnUri;
			aEndpointUri += java.net.URLEncoder.encode(query, "UTF-8");
			method = new GetMethod(aEndpointUri);
			uri = "";
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(2, false));
			method.getParams().setSoTimeout(180000);
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
			} finally {
				// Release the connection.
				method.releaseConnection();
			}

		}
		return uri;

	}

	public static String getTot(String endpointUrl) {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(endpointUrl);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(2, false));
		method.getParams().setSoTimeout(180000);
		method.addRequestHeader("Accept", "application/sparql-results+json");
		int statusCode = 0;
		String responseString = "";
		try {
			statusCode = client.executeMethod(method);
			// System.out.println(method.getResponseBodyAsString());
			ObjectMapper m = new ObjectMapper();
			StringWriter writer = new StringWriter();
			IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
			responseString = writer.toString();
			JsonNode rootNode = m.readTree(responseString);

			return rootNode.findPath("no").findPath("value").getTextValue();
		} catch (Exception e) {
			System.out.println("error: statusCode " + statusCode + " - " + e.getMessage());
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return "-1";

	}

	public static List<String> getObjPropList(Map.Entry<String, String> thisEndpoint) {
		List<String> result = new ArrayList<String>();
		int statusCode = 0;
		String responseString = "";

		String query = DefaultParamsProvider.objPropQuery;
		String domain = thisEndpoint.getKey().replaceAll("(http://[^/:]+).+", "$1");
		System.out.println("\t\tdomain " + domain);
		query = query.replaceAll("\\$\\{domain\\}", domain);
		if (thisEndpoint.getValue().equals("")) {
			query = query.replaceAll("\\$\\{graph\\}", "");
		} else {
			query = query.replaceAll("\\$\\{graph\\}", " FROM <" + thisEndpoint.getValue() + ">");
		}
		HttpClient client = new HttpClient();
		GetMethod method = null;
		try {
			method = new GetMethod(thisEndpoint.getKey() + "?query=" + URLEncoder.encode(query, "UTF-8"));
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(2, false));
			method.getParams().setSoTimeout(180000);
			method.addRequestHeader("Accept", "application/sparql-results+json");

			statusCode = client.executeMethod(method);
			// System.out.println(method.getResponseBodyAsString());
			ObjectMapper m = new ObjectMapper();
			StringWriter writer = new StringWriter();
			IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
			responseString = writer.toString();
			JsonNode rootNode = m.readTree(responseString);
			// System.out.println(responseString);
			for (JsonNode jsonNode : rootNode.findPath("bindings").findValues("value")) {
				// System.out.println(jsonNode);
				result.add(jsonNode.getTextValue());
			}
		} catch (Exception e) {
			System.out.println("statusCode: " + statusCode + " | " + responseString);
		} finally {
			// Release the connection.
			if (method != null)
				method.releaseConnection();
		}

		return result;
	}

	public static List<String> findConnections(Map<String, String> endpoints, ArrayList<String> testDomains, Map.Entry<String, String> thisEndpoint) {
		List<String> result = new ArrayList<String>();
		List<String> prop = GetValues.getObjPropList(thisEndpoint);
		List<String> skipProp = DefaultParamsProvider.skipProps();

		for (String aProp : prop) {

			if (skipProp.contains(aProp) || skipProp.contains(aProp.replaceAll("(http://[^/:]+).+", "$1"))) {
				continue;
			}
			for (String endpoint : endpoints.keySet()) {
				if (!endpoint.equals(thisEndpoint.getKey())) {
					try {
						String domain = endpoint.replaceAll("(http://[^/:]+).+", "$1");
						String query = DefaultParamsProvider.connectionsQuery.replaceAll("\\$\\{domain\\}", domain);
						query = query.replaceAll("\\$\\{prop\\}", aProp);
						if (thisEndpoint.getValue().equals("")) {
							query = query.replaceAll("\\$\\{graph\\}", "");
						} else {
							query = query.replaceAll("\\$\\{graph\\}", " FROM <" + thisEndpoint.getValue() + ">");
						}
						String tot = getTot(thisEndpoint.getKey() + "?query=" + URLEncoder.encode(query, "UTF-8"));
						if (!tot.equals("0")) {
							System.out.println("\t\tquery " + query);
							System.out.println(tot);
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			}
			for (String domain : testDomains) {
				try {
					String query = DefaultParamsProvider.connectionsQuery.replaceAll("\\$\\{domain\\}", domain);
					query = query.replaceAll("\\$\\{prop\\}", aProp);
					query = query.replaceAll("\\$\\{graph\\}", "");
					String tot = getTot(thisEndpoint.getKey() + "?query=" + URLEncoder.encode(query, "UTF-8"));
					if (!tot.equals("0")) {
						System.out.println("\t\tquery " + query);
						System.out.println(tot);
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

		}
		return result;
	}
}
