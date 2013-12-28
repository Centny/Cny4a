package org.cny.cny4a.net.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

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

	public void testDoGet() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTP.doGet("http://www.baidu.com", new HTTP.HTTPMCallback() {

					@Override
					public void onError(HTTPClient c, Throwable err) {
						cdl.countDown();
						rerr = err;
					}

					@Override
					public void onSuccess(HTTPClient c, String data) {
						cdl.countDown();
						assertTrue(data.length() > 0);
					}
				});
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
				HTTP.doGetDown("http://www.baidu.com",
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

	// public void testDoGetDown2() throws Throwable {
	// final CountDownLatch cdl = new CountDownLatch(1);
	// this.runTestOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// HTTP.doGetDown("http://192.168.1.10/测试.dat",
	// new HTTPNameDlCallback(dl.getAbsolutePath()) {
	//
	// @Override
	// public void onSuccess(HTTPClient c) {
	// super.onSuccess(c);
	// cdl.countDown();
	// }
	//
	// @Override
	// public void onError(HTTPClient c, Throwable err) {
	// super.onError(c, err);
	// cdl.countDown();
	// }
	//
	// });
	// }
	// });
	// cdl.await();
	// File p = new File(this.dl, "测试.dat");
	// assertTrue(p.exists());
	// assertTrue(new File(this.dl, "测试.dat").delete());
	// }

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
}
