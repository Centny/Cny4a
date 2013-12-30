package org.cny.cny4a.net.http;

import android.os.Handler;
import android.os.Message;
import android.util.Pair;

/**
 * The runnable call extends HTTPMClient for GET/POST.
 * 
 * @author cny
 * 
 */
public class HTTPRunnable extends HTTPMClient implements Runnable {

	/**
	 * Default constructor by URL and call back.
	 * 
	 * @param url
	 *            the URL.
	 * @param cback
	 *            the call back.
	 */
	public HTTPRunnable(String url, HTTPCallback cback) {
		super(url, cback);
	}

	@Override
	public void run() {
		this.exec();
		if (this.error == null) {
			Message msg = new Message();
			msg.what = 2;
			msg.obj = new Pair<HTTPClient, Object>(this, null);
			S_PROC.sendMessage(msg);
		} else {
			Message msg = new Message();
			msg.what = 3;
			msg.obj = new Pair<HTTPClient, Throwable>(this, this.error);
			S_PROC.sendMessage(msg);
		}
	}

	@Override
	protected void onProcess(float rate) {
		Message msg = new Message();
		msg.what = 1;
		msg.obj = new Pair<HTTPClient, Float>(this, rate);
		S_PROC.sendMessage(msg);
	}

	/**
	 * The thread task client for HTTP GET/POST.
	 * 
	 * @author cny
	 * 
	 */
	public static class HTTPThreadTask extends HTTPRunnable {
		private Thread thr = null;

		/**
		 * Default constructor by URL and call back.
		 * 
		 * @param url
		 *            the URL.
		 * @param cback
		 *            the call back.
		 */
		public HTTPThreadTask(String url, HTTPCallback cback) {
			super(url, cback);
		}

		/**
		 * Start method.
		 */
		public void start() {
			this.thr = new Thread(this);
			this.thr.start();
		}

		/**
		 * Join the thread.
		 * 
		 * @throws InterruptedException
		 *             error.
		 */
		public void join() throws InterruptedException {
			if (this.thr != null) {
				this.thr.join();
			}
		}

		/**
		 * Get the target thread.
		 * 
		 * @return the thread.
		 */
		public Thread getThr() {
			return thr;
		}

	}

	private static final Handler S_PROC = new Handler() {

		@Override
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Pair<HTTPClient, Float> hcf = (Pair<HTTPClient, Float>) msg.obj;
				hcf.first.getCback().onProcess(hcf.first, hcf.second);
				break;
			case 2:
				Pair<HTTPClient, Object> hcn = (Pair<HTTPClient, Object>) msg.obj;
				hcn.first.getCback().onSuccess(hcn.first);
				break;
			case 3:
				Pair<HTTPClient, Throwable> hco = (Pair<HTTPClient, Throwable>) msg.obj;
				hco.first.getCback().onError(hco.first, hco.second);
				break;
			}
		}

	};
}
