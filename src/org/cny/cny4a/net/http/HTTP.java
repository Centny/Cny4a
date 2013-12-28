package org.cny.cny4a.net.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.cny.cny4a.net.http.HTTPClient.HTTPMClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Static class method to HTTP GET/POST.
 * 
 * @author cny
 * 
 */
public class HTTP {
	/**
	 * Do a POST request.
	 * 
	 * @param url
	 *            the target URL.
	 * @param args
	 *            the POST arguments.
	 * @param cb
	 *            HTTP call back instance.
	 */
	public static void doPost(String url, List<BasicNameValuePair> args,
			HTTPCallback cb) {
		HTTPMClient hc = new HTTPMClient(url, cb);
		if (args != null) {
			hc.getArgs().addAll(args);
		}
		hc.setMethod("POST");
		hc.execute(hc);
	}

	/**
	 * Do a GET request.
	 * 
	 * @param url
	 *            the target URL
	 * @param args
	 *            the GET arguments.
	 * @param cb
	 *            HTTP call back instance.
	 */
	public static void doGet(String url, List<BasicNameValuePair> args,
			HTTPCallback cb) {
		HTTPMClient hc = new HTTPMClient(url, cb);
		if (args != null) {
			hc.getArgs().addAll(args);
		}
		hc.setMethod("GET");
		hc.execute(hc);
	}

	/**
	 * Do a GET request.
	 * 
	 * @param url
	 *            the target URL
	 * @param cb
	 *            HTTP call back instance.
	 */
	public static void doGet(String url, HTTPCallback cb) {
		doGet(url, null, cb);
	}

	/**
	 * Do a GET download.
	 * 
	 * @param url
	 *            the target URL.
	 * @param args
	 *            the GET arguments.
	 * @param cb
	 *            HTTP call back instance.
	 */
	public static void doGetDown(String url, List<BasicNameValuePair> args,
			HTTPDownCallback cb) {
		HTTPMClient dc = new HTTPMClient(url, cb);
		if (args != null) {
			dc.getArgs().addAll(args);
		}
		dc.setMethod("GET");
		dc.execute(dc);
	}

	/**
	 * Do a GET download.
	 * 
	 * @param url
	 *            the target URL.
	 * @param cb
	 *            HTTP call back instance.
	 */
	public static void doGetDown(String url, HTTPDownCallback cb) {
		doGetDown(url, null, cb);
	}

	/**
	 * Do a POST download.
	 * 
	 * @param url
	 *            the target URL.
	 * @param args
	 *            the POST arguments.
	 * @param cb
	 *            HTTP call back instance.
	 */
	public static void doPostDown(String url, List<BasicNameValuePair> args,
			HTTPDownCallback cb) {
		HTTPMClient dc = new HTTPMClient(url, cb);
		if (args != null) {
			dc.getArgs().addAll(args);
		}
		dc.setMethod("POST");
		dc.execute(dc);
	}

	/**
	 * the normal method call back class implemented HTTPCallback for GET/POST.
	 * 
	 * @author cny
	 * 
	 */
	public abstract static class HTTPMCallback implements HTTPCallback {
		private ByteArrayOutputStream out = new ByteArrayOutputStream();
		private HTTPClient c;
		protected String bencoding;

		@Override
		public OutputStream onBebin(HTTPClient c, HTTPResponse r) {
			this.c = c;
			this.bencoding = r.getEncoding();
			return out;
		}

		@Override
		public void onEnd(HTTPClient c, OutputStream out) {
		}

		@Override
		public void onProcess(HTTPClient c, float rate) {
		}

		@Override
		public void onRequest(HTTPClient c, HttpUriRequest r) {

		}

		@Override
		public void onSuccess(HTTPClient c) {
			try {
				this.onSuccess(c, this.data());
			} catch (Exception e) {
				this.onError(c, e);
			}
		}

		/**
		 * Get response data.
		 * 
		 * @return the response data string.
		 * @throws Exception
		 *             exception when error,like encoding error.
		 */
		public String data() throws Exception {
			String bcode = this.c.response.getEncoding();
			if (bcode != null) {
				this.bencoding = bcode;
			}
			return new String(this.out.toByteArray(), this.bencoding);
		}

		/**
		 * Get the OutputStream for store HTTP response data.
		 * 
		 * @return the OutputStream.
		 */
		public OutputStream getOut() {
			return this.out;
		}

		/**
		 * call it when response data success.
		 * 
		 * @param c
		 *            the HTTPClient instance.
		 * @param data
		 *            the response data.
		 */
		public abstract void onSuccess(HTTPClient c, String data);
	}

	/**
	 * the normal JSON call back class implemented HTTPMCallback for GET/POST.
	 * 
	 * @author cny
	 * 
	 */
	public abstract static class HTTPJsonCallback extends HTTPMCallback {

		@Override
		public void onSuccess(HTTPClient c, String data) {
			Throwable err;
			try {
				this.onSuccess(c, new JSONObject(data));
				return;
			} catch (JSONException e) {
				err = e;
			}
			try {
				this.onSuccess(c, new JSONArray(data));
				return;
			} catch (JSONException e) {
				err = e;
			}
			this.onFailure(c, err);
		}

		@Override
		public void onError(HTTPClient c, Throwable error) {
			this.onFailure(c, error);
		}

		/**
		 * call it when response JSON array.
		 * 
		 * @param c
		 *            the HTTPClient instance.
		 * @param ary
		 *            the JSONArray object.
		 */
		public abstract void onSuccess(HTTPClient c, JSONArray ary);

		/**
		 * call it when response JSON object.
		 * 
		 * @param c
		 *            the HTTPClient instance.
		 * @param obj
		 *            the JSONObject object.
		 */
		public abstract void onSuccess(HTTPClient c, JSONObject obj);

		/**
		 * call it when error occur.
		 * 
		 * @param c
		 *            the HTTPClient instance.
		 * @param err
		 *            the error instance.
		 */
		public abstract void onFailure(HTTPClient c, Throwable err);
	}

	/**
	 * the normal download call back class implemented HTTPCallback for
	 * GET/POST.<br/>
	 * it will auto download the file data to specified file path.
	 * 
	 * @author cny
	 * 
	 */
	public static class HTTPDownCallback implements HTTPCallback {
		private String filepath;
		private FileOutputStream fos;

		/**
		 * the default constructor.
		 */
		public HTTPDownCallback() {

		}

		/**
		 * the constructor by file path.
		 * 
		 * @param filepath
		 *            the file path to save.
		 */
		public HTTPDownCallback(String filepath) {
			this.filepath = filepath;
		}

		@Override
		public OutputStream onBebin(HTTPClient c, HTTPResponse r)
				throws Exception {
			this.fos = new FileOutputStream(this.filepath, false);
			return new BufferedOutputStream(this.fos);
		}

		@Override
		public void onEnd(HTTPClient c, OutputStream out) throws Exception {
			out.close();
		}

		@Override
		public void onProcess(HTTPClient c, float rate) {

		}

		@Override
		public void onSuccess(HTTPClient c) {
			try {
				this.fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onError(HTTPClient c, Throwable err) {
			try {
				this.fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onRequest(HTTPClient c, HttpUriRequest r) {

		}

		/**
		 * Get the file path.
		 * 
		 * @return the file path.
		 */
		public String getFilepath() {
			return filepath;
		}

		/**
		 * Set the file path.
		 * 
		 * @param filepath
		 *            the file path.
		 */
		public void setFilepath(String filepath) {
			this.filepath = filepath;
		}

	}

	/**
	 * the auto check file name download call back class extends
	 * HTTPDownCallback.<br/>
	 * it will use the filename to save file in Content-Disposition response
	 * header.using default name or end node of URL when Content-Disposition is
	 * not found.
	 * 
	 * @author cny
	 * 
	 */
	public static class HTTPNameDlCallback extends HTTPDownCallback {
		private String defaultName;
		private String sdir;
		private String fname;

		/**
		 * the save folder path.
		 * 
		 * @param sdir
		 *            the folder path to save.
		 */
		public HTTPNameDlCallback(String sdir) {
			this.sdir = sdir;
		}

		@Override
		public OutputStream onBebin(HTTPClient c, HTTPResponse r)
				throws Exception {
			this.fname = r.getFilename();
			if (this.fname == null && this.defaultName != null) {
				this.fname = this.defaultName;
			}
			if (this.fname == null) {
				this.fname = this.getUrlName(c);
			}
			File f = new File(this.sdir, this.fname);
			this.setFilepath(f.getAbsolutePath());
			return super.onBebin(c, r);
		}

		/**
		 * Get the end node of URL as file name.
		 * 
		 * @param c
		 *            the HTTPClient instance.
		 * @return the URL file name.
		 */
		private String getUrlName(HTTPClient c) {
			String url = c.getUrl();
			url = url.split("\\?")[0];
			String[] urls = url.split("/");
			return urls[urls.length - 1];
		}

		/**
		 * Get the default name.
		 * 
		 * @return the default name.
		 */
		public String getDefaultName() {
			return defaultName;
		}

		/**
		 * Set the default name.
		 * 
		 * @param defaultName
		 *            the default name.
		 */
		public void setDefaultName(String defaultName) {
			this.defaultName = defaultName;
		}

		/**
		 * Get the folder path for saving file.
		 * 
		 * @return the folder path.
		 */
		public String getSdir() {
			return sdir;
		}

		/**
		 * Set the folder path for saving file.
		 * 
		 * @param sdir
		 *            the folder path.
		 */
		public void setSdir(String sdir) {
			this.sdir = sdir;
		}

		/**
		 * Get file name.
		 * 
		 * @return the file name is saved.
		 */
		public String getFname() {
			return fname;
		}

	}
}
