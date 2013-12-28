package org.cny.cny4a.net.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cny.cny4a.test.MainActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;

public class TestHTTPCallback extends
		ActivityInstrumentationTestCase2<MainActivity> {
	public TestHTTPCallback() {
		super(MainActivity.class);
	}

	File dl;
	private String ts_ip;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		InputStream in = this.getActivity().getAssets().open("ts_ip.dat");
		assertNotNull("the TServer ip config file is not found", in);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		this.ts_ip = reader.readLine();
		in.close();
		assertNotNull("the TServer ip is not found", this.ts_ip);
		assertFalse("the TServer ip is not found", this.ts_ip.isEmpty());
		File ext = Environment.getExternalStorageDirectory();
		this.dl = new File(ext, "dl");
		if (!this.dl.exists()) {
			this.dl.mkdirs();
		}
	}

	public void testHTTPMCallback() throws Exception {
		HttpClient c = new DefaultHttpClient();
		HttpGet get = new HttpGet("http://" + ts_ip
				+ ":8000/t_args?a=1&b=abc&c=这是中文");
		HttpResponse rp = c.execute(get);
		HTTPResponse resp = new HTTPResponse(rp, "kkk");
		HTTP.HTTPMCallback cback = new HTTP.HTTPMCallback() {

			@Override
			public void onError(HTTPClient c, Throwable err) {

			}

			@Override
			public void onSuccess(HTTPClient c, String data) {

			}

		};
		cback.onBebin(null, resp);
		cback.onSuccess(null);
		System.out.println(cback.getOut());
	}

	public void testHTTPDownCallback() throws Exception {
		HTTP.HTTPDownCallback cback = new HTTP.HTTPDownCallback();
		cback.onSuccess(null);
		cback.onError(null, new Exception("testing..."));
		System.err.println(cback.getFilepath() + "");
	}

	public void testHTTPNameDlCallback() throws Exception {
		HTTP.HTTPNameDlCallback cback = new HTTP.HTTPNameDlCallback("/opt/");
		cback.setDefaultName("name");
		cback.setFilepath("/opt/kkk");
		cback.setSdir("/oprss/");
		System.err.println(cback.getDefaultName() + "");
		System.err.println(cback.getFilepath() + "");
		System.err.println(cback.getFname() + "");
		System.err.println(cback.getSdir() + "");
	}

	public void testHTTPJsonCallback() {
		HTTP.HTTPJsonCallback cback = new HTTP.HTTPJsonCallback() {

			@Override
			public void onSuccess(HTTPClient c, JSONArray arg0) {

			}

			@Override
			public void onSuccess(HTTPClient c, JSONObject arg0) {

			}

			@Override
			public void onFailure(HTTPClient c, Throwable arg0) {

			}

		};
		cback.onSuccess(null, "{}");
		cback.onSuccess(null, "{\"ab\"}");
		cback.onSuccess(null, "{\"ab\":\"123\"}");
		cback.onSuccess(null, "[{\"ab\":\"123\"}");
		cback.onSuccess(null, "[{\"ab\":\"123\"}]");
		cback.onError(null, new Exception("test error"));
	}

	public void testHTTP() {
		new HTTP();
	}
}
