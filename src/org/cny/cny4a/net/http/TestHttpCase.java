package org.cny.cny4a.net.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.cny.cny4a.net.http.HTTP.HTTPDownCallback;
import org.cny.cny4a.net.http.HTTP.HTTPNameDlCallback;
import org.cny.cny4a.test.MainActivity;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class TestHttpCase extends
		ActivityInstrumentationTestCase2<MainActivity> {
	public TestHttpCase() {
		super(MainActivity.class);
	}

	File dl;
	private Throwable rerr = null;
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

	public class Abc extends HTTP.HTTPMCallback {
		private CountDownLatch cdl;
		private String name;

		public Abc(CountDownLatch cdl, String name) {
			super();
			this.cdl = cdl;
			this.name = name;
		}

		@Override
		public void onError(HTTPClient c, Throwable err) {
			cdl.countDown();
			rerr = err;
		}

		@Override
		public void onSuccess(HTTPClient c, String data) {
			cdl.countDown();
			if (c.getClient() == null) {
				rerr = new Exception("client is null");
				return;
			}
			if (c.getRequest() == null) {
				rerr = new Exception("rquest is null");
				return;
			}
			if (c.getError() != null) {
				rerr = new Exception("response:" + c.getError().getMessage());
				return;
			}
			if (c.getResponse().getStatusCode() != 200) {
				rerr = new Exception("response code:"
						+ c.getResponse().getStatusCode());
				return;
			}
			if (c.getCback() != this) {
				rerr = new Exception("call back error");
				return;
			}
			System.out.println(c.getRencoding());
			if (!"OK".equals(data)) {
				String msg = "response:" + data + "," + this.name;
				if (rerr != null) {
					msg = rerr.getMessage() + ",," + msg;
				}
				rerr = new Exception(msg);
				return;
			}
		}
	};

	public void testDoGet() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(9);
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				// 1
				HTTP.doGet("http://" + ts_ip + ":8000/g_args?a=1&b=abc&c=这是中文",
						new Abc(cdl, "1"));
				List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
				args.add(new BasicNameValuePair("a", "1"));
				// 2
				HTTP.doGet("http://" + ts_ip + ":8000/g_args?b=abc&c=这是中文",
						args, new Abc(cdl, "2"));
				args.add(new BasicNameValuePair("b", "abc"));
				// 3
				HTTP.doGet("http://" + ts_ip + ":8000/g_args?c=这是中文", args,
						new Abc(cdl, "3"));
				args.add(new BasicNameValuePair("c", "这是中文"));
				// 4
				HTTP.doGet("http://" + ts_ip + ":8000/g_args", args, new Abc(
						cdl, "4"));
				// 5
				HTTP.doPost("http://" + ts_ip + ":8000/g_args?c=这是中文", args,
						new Abc(cdl, "5"));
				//
				HTTPAsyncTask dc;
				// 6
				dc = new HTTPAsyncTask("http://" + ts_ip + ":8000/g_args",
						new Abc(cdl, "6"));
				dc.addArgs("a", "1");
				dc.addArgs("b", "abc");
				dc.addArgs("c", "这是中文");
				dc.setMethod("GET");
				dc.asyncExec();
				// 7
				dc = new HTTPAsyncTask("http://" + ts_ip + ":8000/h_args",
						new Abc(cdl, "7"));
				dc.getHeaders().addAll(args);
				dc.setMethod("GET");
				dc.asyncExec();
				// 8
				dc = new HTTPAsyncTask("http://" + ts_ip + ":8000/h_args",
						new Abc(cdl, "8"));
				dc.getHeaders().addAll(args);
				dc.setMethod("POST");
				dc.setClient(new DefaultHttpClient());
				dc.setRencoding("UTF-8");
				dc.asyncExec();
				// 9
				dc = new HTTPAsyncTask("http://" + ts_ip + ":8000/h_args",
						new Abc(cdl, "9"));
				dc.addHeader("a", "1");
				dc.addHeader("b", "abc");
				dc.addHeader("c", "这是中文");
				dc.setMethod("POST");
				dc.setClient(new DefaultHttpClient());
				dc.setRencoding("UTF-8");
				dc.asyncExec();
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
	}

	public void testNotSupport() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final HTTPCallback eback = new HTTP.HTTPMCallback() {

			@Override
			public void onError(HTTPClient c, Throwable err) {
				cdl.countDown();

			}

			@Override
			public void onSuccess(HTTPClient c, String data) {
				cdl.countDown();
				rerr = new Exception("response not error");
			}
		};
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTPAsyncTask dc;
				//
				dc = new HTTPAsyncTask("http://" + ts_ip + ":8000/g_args",
						eback);
				dc.setMethod("NO SUPPPORTED");
				dc.asyncExec();
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
	}

	public void testNewError() {
		try {
			new HTTPAsyncTask(null, null);
		} catch (RuntimeException e) {

		}
		try {
			new HTTPAsyncTask("http://" + ts_ip + ":8000/g_args", null);
		} catch (RuntimeException e) {

		}
	}

	public void testOutputError() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final HTTPCallback eback = new HTTP.HTTPMCallback() {

			@Override
			public OutputStream onBebin(HTTPClient c, HTTPResponse r) {
				return null;
			}

			@Override
			public void onError(HTTPClient c, Throwable err) {
				cdl.countDown();

			}

			@Override
			public void onSuccess(HTTPClient c, String data) {
				cdl.countDown();
				rerr = new Exception("response not error");
			}
		};
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTPAsyncTask dc;
				//
				dc = new HTTPAsyncTask("http://" + ts_ip + ":8000/g_args",
						eback);
				dc.setMethod("GET");
				dc.asyncExec();
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
	}

	public void testDoPost() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final HTTPCallback cback = new HTTP.HTTPMCallback() {

			@Override
			public void onError(HTTPClient c, Throwable err) {
				cdl.countDown();
				rerr = err;
			}

			@Override
			public void onSuccess(HTTPClient c, String data) {
				cdl.countDown();
				if (!"OK".equals(data)) {
					rerr = new Exception("response:" + data);
				}
			}
		};
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
				args.add(new BasicNameValuePair("a", "1"));
				args.add(new BasicNameValuePair("b", "abc"));
				HTTP.doPost("http://" + ts_ip + ":8000/g_args?c=这是中文", args,
						cback);
				args.add(new BasicNameValuePair("c", "这是中文"));
				//
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
	}

	public void testDoGetDown() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final File p = new File(this.dl, "www.txt");
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTP.doGetDown("http://" + ts_ip
						+ ":8000/g_args?a=1&b=abc&c=这是中文",
						new HTTPDownCallback(p.getAbsolutePath()) {

							@Override
							public void onSuccess(HTTPClient c) {
								super.onSuccess(c);
								cdl.countDown();
							}

							@Override
							public void onError(HTTPClient c, Throwable err) {
								super.onError(c, err);
								rerr = err;
								cdl.countDown();
							}

						});
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
		File rp = new File(this.dl, "www.txt");
		assertTrue(rp.getAbsolutePath() + " not found", rp.exists());
		FileReader r = new FileReader(rp);
		BufferedReader reader = new BufferedReader(r);
		String line = null;
		while ((line = reader.readLine()) != null) {
			Log.e("Line", line);
		}
		reader.close();
		assertTrue(new File(this.dl, "www.txt").delete());
	}

	public void testDoGetDown3() throws Throwable {
		for (int i = 1; i < 5; i++) {
			testDl(i);
		}
	}

	private void testDl(final int sw) throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTP.doGetDown("http://" + ts_ip + ":8000/dl?sw=" + sw,
						new HTTPNameDlCallback(dl.getAbsolutePath()) {

							@Override
							public void onSuccess(HTTPClient c) {
								super.onSuccess(c);
								cdl.countDown();
							}

							@Override
							public void onError(HTTPClient c, Throwable err) {
								super.onError(c, err);
								rerr = err;
								cdl.countDown();
							}

						});
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
		File p = new File(this.dl, "测试.pdf");
		assertTrue(p.exists());
		assertTrue(new File(this.dl, "测试.pdf").delete());
	}

	//
	public void testDoGetDown4() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTP.doGetDown("http://" + ts_ip + ":8000/dl?sw=5",
						new HTTPNameDlCallback(dl.getAbsolutePath()) {

							@Override
							public void onSuccess(HTTPClient c) {
								super.onSuccess(c);
								cdl.countDown();
							}

							@Override
							public void onError(HTTPClient c, Throwable err) {
								super.onError(c, err);
								rerr = err;
								cdl.countDown();
							}

						});
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
		File p = new File(this.dl, "dl");
		assertTrue(p.exists());
		assertTrue(new File(this.dl, "dl").delete());
	}

	public void testDoGetDown5() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final HTTP.HTTPNameDlCallback cback = new HTTPNameDlCallback(
				dl.getAbsolutePath()) {

			@Override
			public void onSuccess(HTTPClient c) {
				super.onSuccess(c);
				cdl.countDown();
			}

			@Override
			public void onError(HTTPClient c, Throwable err) {
				super.onError(c, err);
				rerr = err;
				cdl.countDown();
			}

		};
		cback.setDefaultName("abc");
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTP.doGetDown("http://" + ts_ip + ":8000/dl?sw=5", cback);
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
		File p = new File(this.dl, "abc");
		assertTrue(p.exists());
		assertTrue(new File(this.dl, "abc").delete());
	}

	public void testDoGetDown6() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
		args.add(new BasicNameValuePair("sw", "1"));
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTP.doGetDown("http://" + ts_ip + ":8000/dl", args,
						new HTTPNameDlCallback(dl.getAbsolutePath()) {

							@Override
							public void onSuccess(HTTPClient c) {
								super.onSuccess(c);
								cdl.countDown();
							}

							@Override
							public void onError(HTTPClient c, Throwable err) {
								super.onError(c, err);
								rerr = err;
								cdl.countDown();
							}

						});
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
		File p = new File(this.dl, "测试.pdf");
		assertTrue(p.exists());
		assertTrue(new File(this.dl, "测试.pdf").delete());
	}

	public void testDoGetDown7() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
		args.add(new BasicNameValuePair("sw", "1"));
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTP.doGetDown("http://" + ts_ip + ":8000/dl", args,
						new HTTPNameDlCallback(dl.getAbsolutePath()) {

							@Override
							public void onEnd(HTTPClient c, OutputStream out)
									throws Exception {
								throw new Exception("make exception");
							}

							@Override
							public void onSuccess(HTTPClient c) {
								super.onSuccess(c);
								cdl.countDown();
								rerr = new Exception("not error");
							}

							@Override
							public void onError(HTTPClient c, Throwable err) {
								super.onError(c, err);
								cdl.countDown();
							}

						});
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
	}

	public void testDoPostDown() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
		args.add(new BasicNameValuePair("sw", "1"));
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTP.doPostDown("http://" + ts_ip + ":8000/dl_p", args,
						new HTTPNameDlCallback(dl.getAbsolutePath()) {

							@Override
							public void onSuccess(HTTPClient c) {
								super.onSuccess(c);
								cdl.countDown();
							}

							@Override
							public void onError(HTTPClient c, Throwable err) {
								super.onError(c, err);
								rerr = err;
								cdl.countDown();
							}

						});
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
		File p = new File(this.dl, "测试.pdf");
		assertTrue(p.exists());
		assertTrue(new File(this.dl, "测试.pdf").delete());
	}

	public void testBufferSize() {
		HTTPClient hc = new HTTPRunnable("http://www.baidu.com",
				new HTTP.HTTPDownCallback());
		hc.setBsize(1024000);
		System.out.println(hc.getBsize());
		try {
			hc.setBsize(100);
		} catch (Exception e) {

		}
	}
}
