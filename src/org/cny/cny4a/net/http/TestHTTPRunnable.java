package org.cny.cny4a.net.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import org.cny.cny4a.net.http.HTTP.HTTPDownCallback;
import org.cny.cny4a.net.http.HTTPRunnable.HTTPThreadTask;
import org.cny.cny4a.test.MainActivity;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class TestHTTPRunnable extends
		ActivityInstrumentationTestCase2<MainActivity> {

	public TestHTTPRunnable() {
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

	public void testThread() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final File p = new File(this.dl, "www.txt");
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTPThreadTask hr = new HTTPThreadTask("http://" + ts_ip
						+ ":8000/g_args?a=1&b=abc&c=这是中文",
						new HTTPDownCallback(p.getAbsolutePath()) {

							@Override
							public void onSuccess(HTTPClient c) {
								cdl.countDown();
								super.onSuccess(c);
							}

							@Override
							public void onError(HTTPClient c, Throwable err) {
								cdl.countDown();
								super.onError(c, err);
								rerr = err;
							}

						});
				hr.start();
				// try {
				// hr.join();
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
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

	public void testThreadErr() throws Throwable {
		this.rerr = null;
		final CountDownLatch cdl = new CountDownLatch(1);
		final File p = new File(this.dl, "www.txt");
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTPThreadTask hr = new HTTPThreadTask("http://" + ts_ip
						+ ":8000/g_args?a=1&b=abc&c=这是中文",
						new HTTPDownCallback(p.getAbsolutePath()) {

							@Override
							public OutputStream onBebin(HTTPClient c,
									HTTPResponse r) throws Exception {
								return null;
							}

							@Override
							public void onSuccess(HTTPClient c) {
								cdl.countDown();
								super.onSuccess(c);
								rerr = new Exception("error message");
							}

							@Override
							public void onError(HTTPClient c, Throwable err) {
								cdl.countDown();
								super.onError(c, err);

							}

						});
				hr.start();
				try {
					hr.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		cdl.await();
		if (this.rerr != null) {
			assertNull(this.rerr.getMessage(), this.rerr);
		}
	}

	public void testJoinErr() throws Throwable {
		this.runTestOnUiThread(new Runnable() {

			@Override
			public void run() {
				HTTPThreadTask hr = new HTTPThreadTask("http://" + ts_ip
						+ ":8000/g_args?a=1&b=abc&c=这是中文",
						new HTTP.HTTPDownCallback());
				System.err.println(hr.getThr() == null);
				try {
					hr.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
