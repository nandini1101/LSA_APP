package com.org.lsa.custom;

import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class WsUtility {

	static String targetURL_Global = "";

	public static String executePostHttps(String targetURL, String data,
			String methodStr) throws IOException, SQLException {
		URL url;
		String jsonData = data;
		targetURL_Global = targetURL;
		HttpURLConnection connection = null;

		if (Utility.showLogs == 0) {

			Log.w("targetURL", targetURL);

		}

		try {
			disableSSLCertificateChecking();

			url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(methodStr);
			if (methodStr.equalsIgnoreCase("post")
					|| methodStr.equalsIgnoreCase("put")) {
				connection.setRequestProperty("Content-Type",
						"application/json; charset=UTF-8");
				connection.setRequestProperty("Content-Length",
						"" + Integer.toString(data.getBytes("UTF-8").length));
				connection.setRequestProperty("Content-Language", "en-US");
				connection.setUseCaches(false);
				connection.setDoInput(true);
				connection.setDoOutput(true);
				OutputStream os = connection.getOutputStream();
				os.write(data.getBytes("UTF-8"));
				os.flush();
				os.close();
			} else if (methodStr.equalsIgnoreCase("get")) {
				connection.setRequestProperty("Accept", "application/json");
			}
			int responseCode = connection.getResponseCode();
			
			if (Utility.showLogs == 0) {

				Log.w("json result ", ""+responseCode);
			}
			
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
			}

			String[] bits = targetURL.split("/");
			String targetURL_str = bits[bits.length - 1];

			if (Utility.showLogs == 0) {

				System.out.println("Res: " + response.toString().trim()
						+ " targetURL_str: " + targetURL_str);
			}

			if (!response.toString().trim().startsWith("success")
					&& (methodStr.equalsIgnoreCase("POST") || methodStr
							.equalsIgnoreCase("PUT"))) {
//				JSONObject jsonToMail = new JSONObject();
//				jsonToMail.put(
//						"data",
//						"Version Name:"
//								+ Utility.getVersionNameCode(Utility.contextAct)
//								+ "\nranastalam1234" + targetURL_str
//								+ "nicrrsurvey" + jsonData);
//				executePostHttps("http://carzone.esy.es/sendMail.php",
//						jsonToMail.toString().trim(), "POST");
				// System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@sendMail mail");
				// Log.i("@@@@@@@@@@@@@@@@@@@@@@@@sendMail mail", "mail");
				// Utility.sendEmail("Version Name:"+Utility.getVersionNameCode(Utility.contextAct
				// )+"\nranastalam1234"+targetURL_str+"nicrrsurvey"+data,Utility.contextAct);
			}

			rd.close();

			return response.toString();
		} catch (Exception e) {
			try {
				if (methodStr.equalsIgnoreCase("POST")
						|| methodStr.equalsIgnoreCase("PUT")) {
//					JSONObject jsonToMail = new JSONObject();
//					jsonToMail
//							.put("data",
//									"Version Name:"
//											+ Utility.getVersionNameCode(Utility.contextAct)
//											+ "\nranastalam1234"
//											+ targetURL_Global + "nicrrsurvey"
//											+ jsonData);
//					executePostHttps("http://carzone.esy.es/sendMail.php",
//							jsonToMail.toString().trim(), "POST");

				}
			} catch (Exception ee) {
				ee.printStackTrace();
			}
			System.out.println(e);
			return e.toString();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
	 * aid testing on a local box, not for use on production.
	 */
	private static void disableSSLCertificateChecking() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// Not implemented
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// Not implemented
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("TLS");

			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

}
