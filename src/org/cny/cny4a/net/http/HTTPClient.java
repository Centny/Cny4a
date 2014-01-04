package org.cny.cny4a.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * the main class for GET/POST.<br/>
 * it implement default the common method for request the server by GET/POST
 * using AsyncTask.
 * 
 * @author cny
 * 
 */
public abstract class HTTPClient {
	public static final int BUF_SIZE = 102400;
	//
	protected String url;
	protected List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();
	protected List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
	protected String rencoding = "UTF-8";
	protected HttpClient client;
	protected Throwable error;
	protected HTTPResponse response;
	protected HttpUriRequest request;
	protected HTTPCallback cback;
	protected int bsize = BUF_SIZE;
	private boolean running = false;
	private int phttp = 80;
	private int phttps = 443;
	private boolean https = false;

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
	 * Set the HTTP client.
	 * 
	 * @param client
	 *            the client to set
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

	/**
	 * Get buffer size.
	 * 
	 * @return the buffer size.
	 */
	public int getBsize() {
		return bsize;
	}

	/**
	 * Set the buffer size.
	 * 
	 * @param bsize
	 *            the buffer size.
	 */
	public void setBsize(int bsize) {
		if (bsize < 1024) {
			throw new RuntimeException("the buffer size less 1024");
		}
		this.bsize = bsize;
	}

	/**
	 * Get the HTTPS port.
	 * 
	 * @return the phttps
	 */
	public int getPhttps() {
		return phttps;
	}

	/**
	 * Set the HTTPS port.
	 * 
	 * @param phttps
	 *            the phttps to set
	 */
	public void setPhttps(int phttps) {
		this.phttps = phttps;
	}

	/**
	 * If HTTPS.
	 * 
	 * @return the https
	 */
	public boolean isHttps() {
		return https;
	}

	/**
	 * Set if HTTPS.
	 * 
	 * @param https
	 *            the https to set
	 */
	public void setHttps(boolean https) {
		this.https = https;
	}

	/**
	 * Get the HTTP port.
	 * 
	 * @return the phttp
	 */
	public int getPhttp() {
		return phttp;
	}

	/**
	 * Set the HTTP port.
	 * 
	 * @param phttp
	 *            the phttp to set
	 */
	public void setPhttp(int phttp) {
		this.phttp = phttp;
	}

	/**
	 * execute the HTTP request.
	 */
	public void exec() {
		try {
			this.client = this.createClient();
			this.running = true;
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
			byte[] buf = new byte[this.bsize];
			int length = -1;
			while ((length = is.read(buf)) != -1) {
				out.write(buf, 0, length);
				rsize += length;
				this.onProcess(rsize, clen);
				if (!this.running) {
					throw new InterruptedException("Transfter file stopped");
				}
			}
			this.error = null;
			this.cback.onEnd(this, out);
		} catch (Exception e) {
			this.error = e;
		}
		this.running = false;
	}

	/**
	 * Get the HTTPS port.
	 * 
	 * @param url
	 *            target URL.
	 * @return the port,default is 443.
	 */
	static int httpsPort(String url) {
		Pattern ptn = Pattern.compile("^http[s]?\\:\\/\\/[^\\:]*\\:[0-9]+");
		Matcher m = ptn.matcher(url);
		if (!m.find()) {
			return 443;
		}
		String pu = m.group();
		String sport = pu.substring(pu.lastIndexOf(":") + 1);
		return Integer.parseInt(sport);
	}

	/**
	 * Create the HttpClient.<br/>
	 * it will check the URL if HTTPS and get the target port.
	 * 
	 * @return the HttpClient.
	 * @throws Exception
	 *             the err.
	 */
	protected HttpClient createClient() throws Exception {
		if (this.url.matches("^https\\:\\/\\/.*$")) {
			this.https = true;
			this.phttps = httpsPort(this.url);
		}
		if (this.https) {
			return this.newHttpsClient(this.phttp, this.phttps);
		} else {
			return new DefaultHttpClient();
		}
	}

	/**
	 * The full X509TrustManager.
	 * 
	 * @author cny
	 * 
	 */
	public class FullX509TrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};

	/**
	 * The full access SSL socket factory.
	 * 
	 * @author cny
	 * 
	 */
	public class FullSSLSocketFactory extends SSLSocketFactory {
		SSLContext ctx = SSLContext.getInstance("TLS");

		public FullSSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);
			ctx.init(null, new TrustManager[] { new FullX509TrustManager() },
					null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return ctx.getSocketFactory().createSocket(socket, host, port,
					autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return ctx.getSocketFactory().createSocket();
		}
	}

	/**
	 * create HTTPS HttpClient.
	 * 
	 * @param https_p
	 *            the HTTPS port.
	 * @return the HttpClient.
	 * @throws Exception
	 *             the error.
	 */
	public HttpClient newHttpsClient(int http_p, int https_p) throws Exception {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);
		SSLSocketFactory sf = new FullSSLSocketFactory(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", new PlainSocketFactory(), http_p));
		registry.register(new Scheme("https", sf, https_p));
		ClientConnectionManager ccm = new ThreadSafeClientConnManager(params,
				registry);
		return new DefaultHttpClient(ccm, params);
	}

	/**
	 * stop transfer.
	 */
	public void stop() {
		this.running = false;
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
			this.onProcess((float) (((double) rsize) / ((double) clen)));
		} else {
			this.onProcess((float) 0);
		}
	}

	/**
	 * The on process method when transfer data.
	 * 
	 * @param rate
	 *            the transfered rate.
	 */
	protected abstract void onProcess(float rate);

	/**
	 * Create HttpUriRequest instance.
	 * 
	 * @return the HttpUriRequest instance.
	 * @throws Exception
	 *             throw exception.
	 */
	public abstract HttpUriRequest createRequest() throws Exception;

}
