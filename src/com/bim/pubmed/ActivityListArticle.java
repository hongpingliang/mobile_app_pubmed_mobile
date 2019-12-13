package com.bim.pubmed;

import java.util.ArrayList;
import java.util.Collections;
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
import com.bim.core.Log;
import com.bim.core.Util;
import com.bim.ncbi.ActivityPub;
import com.bim.ncbi.DevicePubMed;
import com.bim.ncbi.EArticle;
import com.bim.ncbi.ECallSummary;
import com.bim.ncbi.ECallSummaryPubMed;
import com.bim.ncbi.EDatabase;
import com.bim.ncbi.ERequest;
import com.bim.ncbi.EResponse;
import com.bim.ncbi.EResponseSearch;
import com.bim.ncbi.EResponseSummaryPubMed;
import com.bim.ncbi.SearchPubMed;

public class ActivityListArticle extends ActivityPub {
	public final static int ACTIVITY_SORT = 2;
	private ListArticleAdapter mListAdapter;
	private ERequest request;
	private SearchPubMed search;
	private EResponseSearch searchResponse;
	private int sortResId;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.list_article);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.app_icon_title);

		Intent intent = getIntent();
		if (intent == null) {
			Log.d("Error in ActivityListArticle:onCreate");
			return;
		}

		searchResponse = intent.getParcelableExtra("response");
		// search = intent.getParcelableExtra("search");
		search = ActivityPubMed.search;

		// searchResponse = new EResponseSearch();
		// searchResponse.setCount(20);

		if (searchResponse == null || searchResponse.getCount() < 1) {
			displayError("no search searchRequest/response");
			return;
		}

		ECallSummaryPubMed eSummary = new ECallSummaryPubMed(this);
		request = eSummary.getRequest();
//		request.setRetstart(1);
		request.setDb(EDatabase.PUBMED);
		request.setQuery_key(searchResponse.getQueryKey());
		request.setWebEnv(searchResponse.getWebEnv());

		mListAdapter = new ListArticleAdapter(this);
		ListView mListView = (ListView) findViewById(R.id.list_article_list);
		mListView.setAdapter(mListAdapter);
		mListView.setDivider(null);

		setTitle(Util.getResourceString(this, R.string.list_article_result)
				+ ": " + getAvailabeArticleCount());

		closeDialog();

		if (isFirstCreated && search != null) {
			DevicePubMed.save(this, Device.ACTION_SEARCH, search
					.toJsonString());
		}
		isFirstCreated = false;
	}

	public void onESummaryOkay(EResponse response, ECallSummary eSummary) {
		EResponseSummaryPubMed responseMed = (EResponseSummaryPubMed) response;

		List<EArticle> list = responseMed.getArticleList();
		if (list != null && list.size() > 0) {
			Log.d("load: " + list.size());
			mListAdapter.addArticleList(list);
			mListAdapter.notifyDataSetChanged();
			int newStart = request.getRetstart() + 10;
			request.setRetstart(newStart);
		}
	}

	public int getAvailabeArticleCount() {
		return searchResponse.getCount();
	}

	public void startShowAbstractActivity(EArticle article) {
		ActivityAbstract.isFirstCreated = true;
		Intent intent = new Intent(this, ActivityAbstract.class);
		intent.putExtra("article", article);
		this.startActivityForResult(intent, 1);
	}

	public void startSortActivity() {
		Intent intent = new Intent(this, ActivitySort.class);
		intent.putExtra("sortBy", sortResId);
		this.startActivityForResult(intent, ACTIVITY_SORT);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (intent == null) {
			return;
		}
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		if (requestCode == ACTIVITY_SORT) {
			sort(intent);
		}
	}

	private void sort(Intent intent) {
		sortResId = intent.getIntExtra("sortBy", 0);
		if (sortResId <= 0) {
			return;
		}

		if (mListAdapter.getArticleList().size() < 2) {
			return;
		}

		ArticleComparator comparator = new ArticleComparator(sortResId, false);
		Collections.sort(mListAdapter.getArticleList(), comparator);
		mListAdapter.notifyDataSetChanged();
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.list_article, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.menu_list_article_save_article:
			save();
			return true;
		case R.id.menu_list_article_email_article:
			email();
			return true;
		case R.id.menu_list_article_back:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		case R.id.menu_list_article_save_search:
			saveSearch();
			return true;
		case R.id.menu_list_article_sort:
			startSortActivity();
			return true;
		}

		return true;
	}

	private void saveSearch() {
		if (search == null) {
			return;
		}
		ArrayList<SearchPubMed> list = new ArrayList<SearchPubMed>();
		list.add(search);
		ActivityMySearch.saveSearch(this, list, MODE_APPEND);
	}

	private List<EArticle> getCheckedArticleList() {
		List<EArticle> list = new ArrayList<EArticle>();
		for (EArticle a : mListAdapter.getArticleList()) {
			if (a.isChecked()) {
				list.add(a);
			}
		}
		return list;
	}

	private void email() {
		List<EArticle> list = getCheckedArticleList();
		if (list.size() < 1) {
			displayError("Please check a article");
			return;
		}
		email(this, list);
	}

	public static void email(Activity activity, List<EArticle> list) {

		String subject = "PubMed Articles";
		String idStrig = "";
		String copyString = "";
		String content = "";
		int cnt = 1;
		
		for (EArticle a : list) {
			content += cnt + ". ";
			content += a.getTitle();
			content += "\n";
			content += "  https://www.ncbi.nlm.nih.gov/pubmed/" + a.getId();
			content += "\n\n";
			if ( cnt > 1) {
				idStrig += ",";
				copyString += " ";
			}
			idStrig += a.getId();
			copyString += a.getId();
			cnt++;
		}
		
		String t = "";
			if ( list.size() > 1 ) {
			t += "https://www.ncbi.nlm.nih.gov/pubmed/" + idStrig + "\n\n";
			t += content;
			t += "\n\n";
			t += "ids: \n";
			t += copyString;
		} else {
			t += content;
		}

		Util.doEmail(activity, subject, t);
	}

	public void chekAndLoadMore() {
		if (mListAdapter.getArticleList().size() >= getAvailabeArticleCount()) {
			return;
		}

		if (httpThread != null && httpThread.isAlive()) {
			return;
		}

		showLoadingDialog();
		ECallSummaryPubMed eSummary = new ECallSummaryPubMed(this);
		eSummary.setRequest(request);
		httpThread = new Thread(eSummary);
		httpThread.start();
	}

	private void save() {
		List<EArticle> list = getCheckedArticleList();
		if (list.size() < 1) {
			displayError("Please check a article");
			return;
		}

		ActivityMyArticle.saveArticles(this, list, MODE_APPEND);

		showMessage("Article saved");
	}

}
