package org.cny.cny4a.net.http;

import android.os.AsyncTask;

/**
 * The asynchronous task for HTTP request.
 * 
 * @author cny
 * 
 */
public class HTTPAsyncTask extends HTTPMClient {
	private ATask atsk;

	/**
	 * The default constructor by URL and call back.
	 * 
	 * @param url
	 *            the URL.
	 * @param cback
	 *            the call back.
	 */
	public HTTPAsyncTask(String url, HTTPCallback cback) {
		super(url, cback);
		this.atsk = new ATask();
	}

	/**
	 * Asynchronous task implementation class.
	 * 
	 * @author cny
	 * 
	 */
	public class ATask extends AsyncTask<HTTPAsyncTask, Float, HTTPAsyncTask> {

		@Override
		protected HTTPAsyncTask doInBackground(HTTPAsyncTask... params) {
			params[0].exec();
			return params[0];
		}

		@Override
		protected void onPostExecute(HTTPAsyncTask result) {
			if (error == null) {
				cback.onSuccess(HTTPAsyncTask.this);
			} else {
				cback.onError(HTTPAsyncTask.this, error);
			}
		}

		@Override
		protected void onProgressUpdate(Float... values) {
			cback.onProcess(HTTPAsyncTask.this, values[0]);
		}

		public void onProcess(float rate) {
			this.publishProgress(rate);
		}
	};

	@Override
	protected void onProcess(float rate) {
		this.atsk.onProcess(rate);
	}

	/**
	 * Start the asynchronous task.
	 */
	public void asyncExec() {
		this.atsk.execute(this);
	}
}
