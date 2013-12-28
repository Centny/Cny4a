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
		result.onPostExecute();
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

	public HTTPClient setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public List<BasicNameValuePair> getArgs() {
		return args;
	}

	public List<BasicNameValuePair> getHeaders() {
		return headers;
	}

	public HTTPClient addArgs(String key, String val) {
		this.args.add(new BasicNameValuePair(key, val));
		return this;
	}

	public HTTPClient addHeader(String key, String val) {
		this.headers.add(new BasicNameValuePair(key, val));
		return this;
	}

	public HTTPCallback getCback() {
		return cback;
	}

	public void setCback(HTTPCallback cback) {
		this.cback = cback;
	}

	public HttpClient getClient() {
		return client;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public Throwable getError() {
		return error;
	}

	public HTTPResponse getResponse() {
		return response;
	}

	public HttpUriRequest getRequest() {
		return request;
	}

	public String getRencoding() {
		return rencoding;
	}

	public void setRencoding(String rencoding) {
		this.rencoding = rencoding;
	}

	private void exec() {
		try {
			this.request = this.createRequest(this);
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

	protected void onProcess(long rsize, long clen) {
		if (clen > 0) {
			this.publishProgress((float) (((double) rsize) / ((double) clen)));
		} else {
			this.publishProgress((float) 0);
		}
	}

	public void onPostExecute() {
		if (this.error == null) {
			this.cback.onSuccess(this);
		} else {
			this.cback.onError(this, this.error);
		}
	}

	public abstract HttpUriRequest createRequest(HTTPClient c) throws Exception;

	public static class HTTPMClient extends HTTPClient {
		private String method = "GET";

		public HTTPMClient(String url, HTTPCallback cback) {
			super(url, cback);
		}

		public HTTPMClient setMethod(String method) {
			this.method = method;
			return this;
		}

		public HttpUriRequest createRequest(HTTPClient c) throws Exception {
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
				post.setEntity(new UrlEncodedFormEntity(this.args, c
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
