package org.cny.cny4a.net.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

public class HTTPResponse {
	private HttpResponse reponse;
	private long contentLength;
	private String contentType;
	private String encoding = "UTF-8";
	private int statusCode;
	private String filename;
	private Map<String, String> headers = new HashMap<String, String>();

	public HTTPResponse(HttpResponse reponse) {
		this.init(reponse, "UTF-8");
	}

	public HTTPResponse(HttpResponse reponse, String encoding) {
		this.init(reponse, encoding);
	}

	private void init(HttpResponse reponse, String encoding) {
		if (reponse == null) {
			throw new RuntimeException("response is null");
		}
		if (encoding == null) {
			throw new RuntimeException("encoding is null");
		}
		this.reponse = reponse;
		this.encoding = encoding;
		this.statusCode = this.reponse.getStatusLine().getStatusCode();
		Header h;
		h = this.reponse.getFirstHeader("Content-Length");
		if (h == null) {
			this.contentLength = 0;
		} else {
			this.contentLength = Long.parseLong(h.getValue());
		}
		h = this.reponse.getFirstHeader("Content-Type");
		if (h == null) {
			this.contentType = null;
		} else {
			HeaderElement he = h.getElements()[0];
			this.contentType = he.getName();
			NameValuePair cnv = he.getParameterByName("charset");
			if (cnv != null) {
				this.encoding = cnv.getValue();
			}

		}
		h = this.reponse.getFirstHeader("Content-Disposition");
		if (h == null) {
			this.filename = null;
		} else {
			HeaderElement he = h.getElements()[0];
			NameValuePair cnv = he.getParameterByName("filename");
			if (cnv != null) {
				this.filename = toUtf8(cnv.getValue());
			}
		}
		for (Header hd : this.reponse.getAllHeaders()) {
			String cval = toUtf8(hd.getValue());
			if (cval == null) {
				continue;
			}
			this.headers.put(hd.getName(), cval);
		}

	}

	private String toUtf8(String data) {
		try {
			return new String(data.getBytes("ISO-8859-1"), this.encoding);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public HttpResponse getReponse() {
		return reponse;
	}

	public long getContentLength() {
		return contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public String getEncoding() {
		return encoding;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getFilename() {
		return filename;
	}

	public String getValue(String key) {
		return this.headers.get(key);
	}
}
