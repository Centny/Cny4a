package org.cny.cny4a.net.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;

/**
 * the main class for GET/POST.<br/>
 * it implement default the common method for request the server by GET/POST
 * using AsyncTask.
 * 
 * @author cny
 * 
 */
public abstract class HTTPClient extends
		AsyncTask<HTTPClient, Float, HTTPClient> {

	@Override
	protected HTTPClient doInBackground(HTTPClient... params) {
		params[0].exec();
		return params[0];
	}

	@Override
	protected void onPostExecute(HTTPClient result) {
		if (this.error == null) {
			this.cback.onSuccess(this);
		} else {
			this.cback.onError(this, this.error);
		}
	}

	@Override
	protected void onProgressUpdate(Float... values) {
		this.cback.onProcess(this, values[0]);
	}

	//
	protected String url;
	protected List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();
	protected List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
	protected String rencoding = "UTF-8";
	protected HttpClient client = new DefaultHttpClient();
	protected Throwable error;
	protected HTTPResponse response;
	protected HttpUriRequest request;
	protected HTTPCallback cback;

	/**
	 * default constructor by URL and HTTPCallback.
	 * 
	 * @param url
	 *            the target URL.
	 * @param cback
	 *            the HTTPCallback instance.
	 */
	public HTTPClient(String url, HTTPCallback cback) {
		if (url == null || url.trim().length() < 1) {
			throw new InvalidParameterException("url is null or empty");
		}
		if (cback == null) {
			throw new InvalidParameterException("the callback is null");
		}
		this.setUrl(url);
		this.setCback(cback);
	}

	/**
	 * Set the URL.
	 * 
	 * @param url
	 *            the URL.
	 * @return the HTTPClient instance.
	 */
	public HTTPClient setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * Get the URL.
	 * 
	 * @return the URL.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Get the request arguments.
	 * 
	 * @return the arguments.
	 */
	public List<BasicNameValuePair> getArgs() {
		return args;
	}

	/**
	 * Get the request headers.
	 * 
	 * @return the headers.
	 */
	public List<BasicNameValuePair> getHeaders() {
		return headers;
	}

	/**
	 * Add one request arguments.
	 * 
	 * @param key
	 *            the key.
	 * @param val
	 *            the value.
	 * @return the HTTPClient instance.
	 */
	public HTTPClient addArgs(String key, String val) {
		this.args.add(new BasicNameValuePair(key, val));
		return this;
	}

	/**
	 * Add on request header.
	 * 
	 * @param key
	 *            the key
	 * @param val
	 *            the value.
	 * @return the HTTPClient instance.
	 */
	public HTTPClient addHeader(String key, String val) {
		this.headers.add(new BasicNameValuePair(key, val));
		return this;
	}

	/**
	 * Get HTTPCallback.
	 * 
	 * @return the HTTPCallback.
	 */
	public HTTPCallback getCback() {
		return cback;
	}

	/**
	 * Set HTTPCallback.
	 * 
	 * @param cback
	 *            the HTTPCallback.
	 */
	public void setCback(HTTPCallback cback) {
		this.cback = cback;
	}

	/**
	 * Get the HttpClient instance.
	 * 
	 * @return the HttpClient.
	 */
	public HttpClient getClient() {
		return client;
	}

	/**
	 * Set the HttpClient instance.
	 * 
	 * @param client
	 *            the HttpClient.
	 */
	public void setClient(HttpClient client) {
		this.client = client;
	}

	/**
	 * Get the error instance.
	 * 
	 * @return the error instance.
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * Get the HTTP response.
	 * 
	 * @return the HTTPResponse.
	 */
	public HTTPResponse getResponse() {
		return response;
	}

	/**
	 * Get the HTTP request.
	 * 
	 * @return the HTTP request.
	 */
	public HttpUriRequest getRequest() {
		return request;
	}

	/**
	 * Get the request encoding.
	 * 
	 * @return the encoding.
	 */
	public String getRencoding() {
		return rencoding;
	}

	/**
	 * Set the request encoding.
	 * 
	 * @param rencoding
	 *            the encoding.
	 */
	public void setRencoding(String rencoding) {
		this.rencoding = rencoding;
	}

	private void exec() {
		try {
			this.request = this.createRequest();
			if (this.request == null) {
				throw new Exception("the request is null");
			}
			this.cback.onRequest(this, this.request);
			this.response = new HTTPResponse(this.client.execute(request));
			OutputStream out = null;
			out = this.cback.onBebin(this, this.response);
			if (out == null) {
				throw new Exception("the OutputStream is null");
			}
			HttpEntity entity = response.getReponse().getEntity();
			InputStream is;
			long rsize = 0;
			long clen = this.response.getContentLength();
			is = entity.getContent();
			byte[] buf = new byte[4096];
			int length = -1;
			while ((length = is.read(buf)) != -1) {
				out.write(buf, 0, length);
				rsize += length;
				this.onProcess(rsize, clen);
			}
			this.error = null;
			this.cback.onEnd(this, out);
		} catch (Exception e) {
			this.error = e;
		}
	}

	/**
	 * call it when transfer process.
	 * 
	 * @param rsize
	 *            the data size already read.
	 * @param clen
	 *            the Content-Length.
	 */
	protected void onProcess(long rsize, long clen) {
		if (clen > 0) {
			this.publishProgress((float) (((double) rsize) / ((double) clen)));
		} else {
			this.publishProgress((float) 0);
		}
	}

	/**
	 * Create HttpUriRequest instance.
	 * 
	 * @return the HttpUriRequest instance.
	 * @throws Exception
	 *             throw exception.
	 */
	public abstract HttpUriRequest createRequest() throws Exception;

	/**
	 * the normal HTTP client extends HTTPClient for GET/POST.
	 * 
	 * @author cny
	 * 
	 */
	public static class HTTPMClient extends HTTPClient {
		private String method = "GET";

		/**
		 * default constructor by URL and call back.
		 * 
		 * @param url
		 *            the URL.
		 * @param cback
		 *            the HTTPCallback.
		 */
		public HTTPMClient(String url, HTTPCallback cback) {
			super(url, cback);
		}

		/**
		 * Set the request method,default GET.
		 * 
		 * @param method
		 *            the target method.
		 * @return the HTTPMClient instance.
		 */
		public HTTPMClient setMethod(String method) {
			this.method = method;
			return this;
		}

		@Override
		public HttpUriRequest createRequest() throws Exception {
			if ("GET".equals(method)) {
				String params = URLEncodedUtils.format(this.args,
						this.rencoding);
				HttpGet get;
				if (params.length() > 0) {
					if (this.url.indexOf("?") > 0) {
						get = new HttpGet(this.url + "&" + params);
					} else {
						get = new HttpGet(this.url + "?" + params);
					}
				} else {
					get = new HttpGet(this.url);
				}
				for (BasicNameValuePair nv : this.headers) {
					get.addHeader(nv.getName(), nv.getValue());
				}
				return get;
			} else if ("POST".equals(method)) {
				HttpPost post = new HttpPost(this.url);
				post.setEntity(new UrlEncodedFormEntity(this.args, this
						.getRencoding()));
				for (BasicNameValuePair nv : this.headers) {
					post.addHeader(nv.getName(), nv.getValue());
				}
				return post;
			} else {
				return null;
			}
		}
	}
}
