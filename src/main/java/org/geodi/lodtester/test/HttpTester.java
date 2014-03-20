package org.geodi.lodtester.test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;

public class HttpTester {

	public static boolean testSpecificContent(String endpointUrl, String contentType) throws HttpException, IOException {
		boolean isOk = false;

		HttpClient client = new HttpClient();
		// Create a method instance.
		GetMethod method = new GetMethod(endpointUrl);
		// Provide custom retry handler is necessary
		method.getParams().setSoTimeout(60000);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(2, false));
		method.addRequestHeader("Accept", contentType);
		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				// System.out.print("\t" + method.getStatusLine());
			}
			// System.out.println(method.getResponseBodyAsString());
			Header[] headers = method.getResponseHeaders();
			for (Header header : headers) {
				// System.out.println("\t\t"+header);
				if (contentType.toLowerCase().contains(header.getValue().toLowerCase()) || header.getValue().toLowerCase().contains(contentType.toLowerCase())) {
					isOk = true;
					break;
				}
			}
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return isOk;

	}

	public static boolean testJsonP(String anURI) throws IOException {
		boolean isOk = false;
		if (anURI != null && !anURI.equals("")) {
			HttpClient client = new HttpClient();
			GetMethod method = new GetMethod(anURI + "&callback=dvcama");
			method.getParams().setSoTimeout(60000);
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(2, false));
			method.addRequestHeader("Accept", "application/sparql-results+json");
			try {
				int statusCode = client.executeMethod(method);
				StringWriter writer = new StringWriter();
				IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
				String responseString = writer.toString();
				// System.out.println(responseString);
				if (responseString.contains("dvcama") || responseString.contains("dvcama")) {
					isOk = true;
				}
			} finally {
				// Release the connection.
				method.releaseConnection();
			}
		}
		return isOk;
	}

	public static boolean testAvailability(String anURI, boolean hasForm, boolean just404) throws HttpException, IOException {
		boolean isOk = false;
		if (anURI != null && !anURI.equals("")) {
			HttpClient client = new HttpClient();
			GetMethod method = new GetMethod(anURI);
			method.getParams().setSoTimeout(60000);
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(2, false));
			method.addRequestHeader("Accept", "text/html");
			try {
				int statusCode = client.executeMethod(method);
				StringWriter writer = new StringWriter();
				IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
				String responseString = writer.toString();
				if (hasForm) {
					if (responseString.contains("textarea") || responseString.contains("input")) {
						isOk = true;
					}
				} else {
					if (statusCode < 400) {
						isOk = true;
					} else {
						if (just404 && statusCode == 404) {
							isOk = false;
						} else {
							isOk = true;
						}
					}

				}
			} finally {
				// Release the connection.
				method.releaseConnection();
			}
		}
		return isOk;
	}

}
