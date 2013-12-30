package org.cny.cny4a.net.http;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 * the normal HTTP client extends HTTPClient for GET/POST.
 * 
 * @author cny
 * 
 */
public abstract class HTTPMClient extends HTTPClient {
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
			String params = URLEncodedUtils.format(this.args, this.rencoding);
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
				get.addHeader(nv.getName(), new String(
						nv.getValue().getBytes(), "ISO-8859-1"));
			}
			return get;
		} else if ("POST".equals(method)) {
			HttpPost post = new HttpPost(this.url);
			post.setEntity(new UrlEncodedFormEntity(this.args, this
					.getRencoding()));
			for (BasicNameValuePair nv : this.headers) {
				post.addHeader(nv.getName(), new String(nv.getValue()
						.getBytes(), "ISO-8859-1"));
			}
			return post;
		} else {
			return null;
		}
	}
}
