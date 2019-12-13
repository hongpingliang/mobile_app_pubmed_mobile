package com.bim.pubmed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.Handler;

import com.bim.core.Device;
import com.bim.core.Log;
import com.bim.core.Util;

public class Submitter implements Runnable {

	public static final boolean IS_DEBUG = false; 
	
	public static String URL_BASE;
	static {
		if (IS_DEBUG) {
//			URL_BASE = "http://152.144.16.41/pubmed/m/pubmed/";
			URL_BASE = "http://192.168.2.3/pubmed/m/pubmed/";
		} else {
			URL_BASE = "http://www.bioinfomobile.com/m/pubmed/";
		}
	}

	public static final String PAGE_GET_USER_INFO = "p_get_user.php";
	public static final String PAGE_UPDATE_USER = "p_update_user.php";
	public static final String PAGE_GET_ITEM_OVERALL = "p_get_item_overall.php";
	public static final String PAGE_SAVE_COMMENT = "p_save_comment.php";
	public static final String PAGE_GET_COMMENTS = "p_get_comments.php";

	private Activity activity;
	private SubmitterHandler handler;
	private String page;
	private String parameter;
	private String content;

	private int callType;

	private final Handler mLoadHandler = new Handler();
	final Runnable mLoadCallback = new Runnable() {
		public void run() {
			handler.onSubmitReady(Submitter.this, content);
		}
	};

	public Submitter(Activity activity, SubmitterHandler handler, String page,
			String parameter) {
		this.activity = activity;
		this.handler = handler;
		this.page = page;
		this.parameter = parameter;
	}

	public void run() {
		content = loadText(activity, page, parameter, User.getUser(activity));
		parse(content);
		mLoadHandler.post(mLoadCallback);
	}

	protected void parse(String content) {

	}

	public static String getURL(Activity activity, String page,
			String parameter, User user) {
		String url = URL_BASE + page;
		String dAId = Device.getAndroidId(activity);
		if (Util.isNull(dAId)) {
			url += Util.add("t", "0", "?");
		} else {
			url += Util.add("dAId", dAId, "?");
		}

		if (Util.isNotNull(parameter)) {
			url += parameter;
		}
		if (user != null) {
			url += Util.add("userId", user.getId() + "");
		}
		return url;
	}

	public static String loadText(Activity activity, String page,
			String parameter, User user) {
		String url = getURL(activity, page, parameter, user);
		Log.d(url);
		StringBuffer c = new StringBuffer();
		try {
			HttpURLConnection uc = (HttpURLConnection) new URL(url)
					.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(true);

			InputStream is = uc.getInputStream();
			String line;
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			while (null != (line = reader.readLine())) {
				c.append(line);
			}
			uc.disconnect();

		} catch (Exception e) {
			Log.d(e);
		}
		Log.d(c.toString());
		return c.toString();
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public int getCallType() {
		return callType;
	}

	public void setCallType(int callType) {
		this.callType = callType;
	}
}
