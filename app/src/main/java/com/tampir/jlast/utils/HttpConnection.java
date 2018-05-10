/*
 * HttConnection.java
 * 
 * author : rahmatul.hidayat@gmail.com
 */
package com.tampir.jlast.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.tampir.jlast.App;
import com.tampir.jlast.BuildConfig;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpConnection {
	public interface OnTaskFinishListener {
		void onStart();
		void onFinished(String jsonString, Error err);
	}
	public static String METHOD_POST = "POST";
	public static String METHOD_GET = "GET";
	public static class Error{
		public String Code;
		public String Message;
		public Error(String Code, String Message){
			this.Code = Code;
			this.Message = Message;
		}
	}

	public static class Task extends AsyncTask<Void, String, String> {
		OnTaskFinishListener listenCallback;
		String action;
		String urlParameter;
		String method;

		public Task(String method, String action, String urlParameters, OnTaskFinishListener listenCallback) {
			this.listenCallback = listenCallback;
			this.urlParameter = urlParameters;
			this.action = action;
			this.method = method;
		}

		@Override
		protected void onPreExecute() {
			if(this.listenCallback != null) this.listenCallback.onStart();
		}

		@Override
		protected String doInBackground(Void... params) {
			if (!App.isConnected()) return "{error:\"240\",message:\"Tidak terhubung internet\"}";
			try {
				//addGeneralParameter
				this.urlParameter += "&time=" + URLEncoder.encode(time_for_api(), "UTF-8");
				if (method.matches("GET")){
					this.action+="?"+this.urlParameter;
					this.urlParameter = "";
				}
				if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG,Const.HOST + this.action+" - "+this.urlParameter);
				return excutePost(this.action,this.urlParameter, this.method);
			} catch (Exception ex) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String jsonString) {
			Error err = null;
			ContentJson json = new ContentJson(jsonString);
			if (json.has("error")){
				err = new Error(json.getString("error"),json.getString("message"));
			}
			if(this.listenCallback != null) this.listenCallback.onFinished(jsonString, err);
		}

		@Override
		protected void onCancelled() {
			if(this.listenCallback != null){
				this.listenCallback.onFinished("", new Error("207","Process Canceled"));
			}
		}
	}

	public static class UploadFileToServer extends AsyncTask<String, String, String> {
		File sourceFile;
		int totalSize = 0;
		OnTaskFinishListener listenCallback;
		String action;
		String urlParameter;

		public UploadFileToServer(File mfile, String action, String urlParameters, OnTaskFinishListener listenCallback) {
			this.listenCallback = listenCallback;
			this.urlParameter = urlParameters;
			this.action = action;
			this.sourceFile = mfile;
		}

		@Override
		protected void onPreExecute() {
			totalSize = (int)sourceFile.length();
			if(this.listenCallback != null) this.listenCallback.onStart();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			Log.d("PROG", progress[0]);
			//donut_progress.setProgress(Integer.parseInt(progress[0])); //Updating progress
		}

		@Override
		protected String doInBackground(String... args) {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection connection = null;
			String fileName = sourceFile.getName();

			String res;
			URL url;
			String targetURL = Const.HOST + action;

			try {
				url = new URL(targetURL);
				if (url.getProtocol().toLowerCase().equals("https")) {
					HttpsURLConnection https = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
					connection = https;
				} else {
					connection = (HttpURLConnection) url.openConnection();
				}

				connection.setRequestMethod("POST");
				String boundary = "---------------------------boundary";
				String tail = "\r\n--" + boundary + "--\r\n";
				connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				connection.setDoOutput(true);

				String metadataPart = "--" + boundary + "\r\n"
						+ "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
						+ "" + "\r\n";

				String fileHeader1 = "--" + boundary + "\r\n"
						+ "Content-Disposition: form-data; name=\"fileToUpload\"; filename=\""
						+ fileName + "\"\r\n"
						+ "Content-Type: application/octet-stream\r\n"
						+ "Content-Transfer-Encoding: binary\r\n";

				long fileLength = sourceFile.length() + tail.length();
				String fileHeader2 = "Content-length: " + fileLength + "\r\n";
				String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
				String stringData = metadataPart + fileHeader;

				long requestLength = stringData.length() + fileLength;
				connection.setRequestProperty("Content-length", "" + requestLength);
				connection.setFixedLengthStreamingMode((int) requestLength);
				connection.connect();

				DataOutputStream out = new DataOutputStream(connection.getOutputStream());
				out.writeBytes(stringData);
				out.flush();

				int progress = 0;
				int bytesRead = 0;

				BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(sourceFile));
				byte buf[] = new byte[bufInput.available()];
				while ((bytesRead = bufInput.read(buf)) != -1) {
					// write output
					out.write(buf, 0, bytesRead);
					out.flush();
					progress += bytesRead; // Here progress is total uploaded bytes

					publishProgress(""+(int)((progress*100)/totalSize)); // sending progress percent to publishProgress
				}

				// Write closing boundary and close stream
				out.writeBytes(tail);
				out.flush();
				out.close();

				// Get server response
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = "";
				StringBuilder response = new StringBuilder();
				while((line = reader.readLine()) != null) {
					response.append(line);
				}

				res = response.toString();
				if (res.matches("")){
					res = "{error:\"240\",message:\"empty data\"}";
				}
				return res;

			} catch (Exception e) {
				// Exception
				res = "{error:\"260\",message:\"Lost Connection!\"}";
				try {
					res = "{error:\"" + connection.getResponseCode() + "\",message:\"" + connection.getResponseMessage() + "\"}";
					int httpStatus =  connection.getResponseCode();
					if (httpStatus==500){
						//recreate session
						recreate_session();
						res = "{error:\"" + connection.getResponseCode() + "\",message:\"Silahkan coba sesaat lagi\"}";
					}
				} catch (MalformedURLException e1) {
					if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG, "MalformedURLException: " + e1.getMessage());
				} catch (ProtocolException e1) {
					if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG, "ProtocolException: " + e1.getMessage());
				} catch (IOException e1) {
					if(BuildConfig.BUILD_TYPE == "debug") try {
						Log.e(Const.TAG, "IOException: " + e1.getMessage()+" Response code "+connection.getResponseCode());
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				} catch (Exception e1) {
					if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG, "Exception: " + e1.getMessage());
				}
			} finally {
				if (connection != null) connection.disconnect();
			}
			return res;
		}

		@Override
		protected void onPostExecute(String jsonString) {
			Log.e("Response", "Response from server: " + jsonString);

			Error err = null;
			ContentJson json = new ContentJson(jsonString);
			if (json.has("error")){
				err = new Error(json.getString("error"),json.getString("message"));
			}
			if(this.listenCallback != null) this.listenCallback.onFinished(jsonString, err);
		}

		@Override
		protected void onCancelled() {
			if(this.listenCallback != null){
				this.listenCallback.onFinished("", new Error("207","Process Canceled"));
			}
		}
	}

	public static String excutePost(String action, String urlParameters) throws IOException {
		return excutePost(action,urlParameters,METHOD_POST);
	}
	public static String excutePost(String action, String urlParameters, String method) throws IOException {
		String res;
		URL url;
		HttpURLConnection connection = null;
		String targetURL = Const.HOST + action;
		try {
			url = new URL(targetURL);
			if (url.getProtocol().toLowerCase().equals("https")) {
				HttpsURLConnection https = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
				connection = https;
			} else {
				connection = (HttpURLConnection) url.openConnection();
			}

			//addGeneralParameter
			String time = time_for_api();
			urlParameters += "&bhs=" + URLEncoder.encode("ID", "UTF-8");
			urlParameters += "&time=" + URLEncoder.encode(time, "UTF-8");
			urlParameters += "&network=" + URLEncoder.encode(Connectivity.ConnectionName(App.context).toString(), "UTF-8");
			urlParameters += "&versi_app=" + URLEncoder.encode(App.getAppVersion() +"", "UTF-8");
			urlParameters += "&build_app=" + URLEncoder.encode(App.getAppBuild() +"", "UTF-8");
			if (App.gps.gpslocation!=null) {
				urlParameters += "&akurasi=" + URLEncoder.encode(App.gps.gpslocation.getAccuracy() + "", "UTF-8");
				if(BuildConfig.BUILD_TYPE == "debug"){
					urlParameters += "&latitude=" + URLEncoder.encode("-6.6019038", "UTF-8");
					urlParameters += "&longitude=" + URLEncoder.encode("106.8059027" + "", "UTF-8");
				}else {
					urlParameters += "&latitude=" + URLEncoder.encode(App.gps.gpslocation.getLatitude() + "", "UTF-8");
					urlParameters += "&longitude=" + URLEncoder.encode(App.gps.gpslocation.getLongitude() + "", "UTF-8");
				}
				urlParameters += "&gps=" + URLEncoder.encode(App.gps.gpslocation.getProvider()+"", "UTF-8");
			}
			ContentJson user = App.storage.getCurrentUser();
			String user_id = "";
			if (user!=null){
				user_id = user.getString("id");
				urlParameters += "&user_id=" + URLEncoder.encode(user_id+"", "UTF-8");
			}

			if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG,Const.HOST + action + " - full > " + urlParameters);
			connection.setRequestMethod(method);
			connection.setConnectTimeout(30000);

			connection.setRequestProperty("Content-Language", "en-US");
			connection.setRequestProperty("signature", General.md5(time.substring(0,8) + "j3Ist" + user_id + urlParameters));

			if (getWebSession()!=null) connection.setRequestProperty("Cookie", getWebSession());
			if (connection.getRequestMethod().matches("POST")) {
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
				connection.setUseCaches (false);
				connection.setDoInput(true);
				connection.setDoOutput(true);

				//Send request
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
			}

			//Get Response
			Map<String, List<String>> map = connection.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				//if(BuildConfig.BUILD_TYPE == "debug") Log.i(Const.TAG,"Key : " + entry.getKey() + " ,Value : " + entry.getValue());
				if (entry.getKey()!=null) {
					if (entry.getKey().matches("Set-Cookie")) {
						saveWebSession(entry.getValue());
						if(BuildConfig.BUILD_TYPE == "debug") Log.i(Const.TAG,"GetSession : " + entry.getValue());
					}
				}
			}

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
			}
			rd.close();

			res = response.toString();
			if (res.matches("")){
				res = "{error:\"240\",message:\"empty data\"}";
			}
			if(BuildConfig.BUILD_TYPE == "debug") Log.i(Const.TAG,targetURL+"-->"+res);
		} catch (Exception e) {
			res = "{error:\"260\",message:\"Lost Connection!\"}";
			try {
				res = "{error:\"" + connection.getResponseCode() + "\",message:\"" + connection.getResponseMessage() + "\"}";
				int httpStatus =  connection.getResponseCode();
				if (httpStatus==500){
					//recreate session
					recreate_session();
					res = "{error:\"" + connection.getResponseCode() + "\",message:\"Silahkan coba sesaat lagi\"}";
				}
			} catch (MalformedURLException e1) {
				if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG, "MalformedURLException: " + e1.getMessage());
			} catch (ProtocolException e1) {
				if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG, "ProtocolException: " + e1.getMessage());
			} catch (IOException e1) {
				if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG, "IOException: " + e1.getMessage()+" Response code "+connection.getResponseCode());
			} catch (Exception e1) {
				if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG, "Exception: " + e1.getMessage());
			}
			e.printStackTrace();
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
		if (res==null){
			res = "{error:\"241\",message:\"Trouble Connection\"}";
		}
		return  res;
	}


	public static String time_for_api(){
		return android.text.format.DateFormat.format("yyyyMMddhhmmss", Calendar.getInstance()).toString();
	}

	private static void saveWebSession(List<String> session){
		websession = session;
		App.storage.setDataReplace("{\"session\":\"" + session.get(0) + "\"}", "session");
	}
	private static String getWebSession(){
		if (websession!=null) {
			return websession.get(0);
		}else{
			ContentJson sess = App.storage.getData("session");
			if (sess!=null) return sess.getString("session");
			else return null;
		}
	}
	public static void recreate_session(){}
	private static List<String> websession;
}
