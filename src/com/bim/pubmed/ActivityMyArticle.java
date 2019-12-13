package com.bim.pubmed;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

import com.bim.core.Device;
import com.bim.core.Util;
import com.bim.ncbi.ActivityPub;
import com.bim.ncbi.DevicePubMed;
import com.bim.ncbi.EArticle;

public class ActivityMyArticle extends ActivityPub {
	private ListMyArticleAdapter mListAdapter;
	public static final String MY_ARTICLE_FILE_NAME = "pubmed_my_article.txt";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.my_article);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.app_icon_title);

		mListAdapter = new ListMyArticleAdapter(this);
		ListView mListView = (ListView) findViewById(R.id.my_article_list);
		mListView.setAdapter(mListAdapter);
		mListView.setDivider(null);

		refreshDisplay();

		if (isFirstCreated) {
			DevicePubMed.save(this, Device.ACTION_MY_ARTICLE, mListAdapter
					.getArticleList().size()
					+ "");
		}
		isFirstCreated = false;
	}

	private void refreshDisplay() {
		List<EArticle> articleList = getArticleList();
		if (articleList == null || articleList.size() < 1) {
			displayError("No saved article");
			return;
		}
		mListAdapter.setArticleList(articleList);
		mListAdapter.notifyDataSetChanged();
	}

	public static void saveArticles(Activity activity, List<EArticle> list,
			int mode) {
		try {
			FileOutputStream fOut = activity.openFileOutput(
					MY_ARTICLE_FILE_NAME, mode);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			for (EArticle a : list) {
				osw.write(a.toJsonString() + "\n");
			}
			osw.flush();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<EArticle> getArticleList() {
		ArrayList<EArticle> articleList = new ArrayList<EArticle>();
		try {
			FileInputStream fIn = openFileInput(MY_ARTICLE_FILE_NAME);
			BufferedReader br = new BufferedReader(new InputStreamReader(fIn));
			String line;
			while ((line = br.readLine()) != null) {
				if (Util.isNull(line)) {
					continue;
				}
				// Log.d(line);
				EArticle a = new EArticle();
				if (a.parse(line)) {
					articleList.add(a);
				}
			}
			fIn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return articleList;
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.my_article, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.menu_my_article_select_all:
			selectAll();
			return true;
		case R.id.menu_my_article_delete:
			delete();
			return true;
		case R.id.menu_my_article_email_article:
			email();
			return true;
		case R.id.menu_my_article_back:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}

		return true;
	}

	private void email() {
		List<EArticle> list = getSelected();
		if (list.size() < 1) {
			displayError("Please check a aritcle");
			return;
		}
		ActivityListArticle.email(this, list);
	}

	private void selectAll() {
		for (EArticle a : mListAdapter.getArticleList()) {
			a.setChecked(true);
		}
		mListAdapter.notifyDataSetChanged();
	}

	public List<EArticle> getSelected() {
		ArrayList<EArticle> list = new ArrayList<EArticle>();
		for (EArticle a : mListAdapter.getArticleList()) {
			if (a.isChecked()) {
				list.add(a);
			}
		}
		return list;
	}

	private void delete() {
		ArrayList<EArticle> list = new ArrayList<EArticle>();
		ArrayList<EArticle> saveList = new ArrayList<EArticle>();
		for (EArticle a : mListAdapter.getArticleList()) {
			if (a.isChecked()) {
				list.add(a);
			} else {
				saveList.add(a);
			}
		}
		if (list.size() < 1) {
			displayError("Please check a article");
			return;
		}
		saveArticles(this, saveList, MODE_PRIVATE);
		refreshDisplay();
	}

	public void startShowAbstractActivity(EArticle article) {
		ActivityAbstract.isFirstCreated = true;
		Intent intent = new Intent(this, ActivityAbstract.class);
		intent.putExtra("article", article);
		this.startActivityForResult(intent, 1);
	}
}
