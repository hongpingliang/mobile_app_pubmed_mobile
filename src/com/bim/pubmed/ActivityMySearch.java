package com.bim.pubmed;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import com.bim.core.Util;
import com.bim.ncbi.ActivityPub;
import com.bim.ncbi.DevicePubMed;
import com.bim.ncbi.SearchPubMed;

public class ActivityMySearch extends ActivityPub {
	public static final String MY_SEARCH_FILE_NAME = "pubmed_my_search.txt";
	private ListMySearchAdapter mRowAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.my_search);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.app_icon);

		Button backButton = (Button) findViewById(R.id.my_search_button_back);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		mRowAdapter = new ListMySearchAdapter(this);
		mRowAdapter.setSearchList(loadSearch());
		ListView mListView = (ListView) findViewById(R.id.my_search_list);
		mListView.setAdapter(mRowAdapter);

		if (mRowAdapter.getCount() < 1) {
			displayError("No saved search");
		}

		if (isFirstCreated) {
			DevicePubMed.save(this, Device.ACTION_MY_SEARCH, mRowAdapter
					.getCount()
					+ "");
		}
		isFirstCreated = false;
	}

	public static void saveSearch(Activity activity,
			ArrayList<SearchPubMed> searches, int mode) {
		try {
			FileOutputStream fOut = activity.openFileOutput(
					MY_SEARCH_FILE_NAME, mode);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			for (SearchPubMed search : searches) {

				String t = search.toJsonString() + "\n";
				// Log.d(t);
				osw.write(t);

			}
			osw.flush();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<SearchPubMed> loadSearch() {
		List<SearchPubMed> searchList = new ArrayList<SearchPubMed>();
		try {
			FileInputStream fIn = openFileInput(MY_SEARCH_FILE_NAME);
			BufferedReader br = new BufferedReader(new InputStreamReader(fIn));
			String line;
			while ((line = br.readLine()) != null) {
				if (Util.isNull(line)) {
					continue;
				}
				SearchPubMed s = new SearchPubMed();
				if (s.parse(line)) {
					searchList.add(s);
				}
			}
			fIn.close();

		} catch (Exception e) {
			//e.printStackTrace();
		}

		Collections.reverse(searchList);
		return searchList;
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.my_search, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.menu_my_search_select_all:
			selectAll();
			return true;
		case R.id.menu_my_search_delete:
			delete();
			return true;
		case R.id.menu_my_search_back:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}

		return true;
	}

	private void selectAll() {
		for (SearchPubMed s : mRowAdapter.getSearchList()) {
			s.setChecked(true);
		}
		mRowAdapter.notifyDataSetChanged();
	}

	private void delete() {
		ArrayList<SearchPubMed> list = new ArrayList<SearchPubMed>();
		ArrayList<SearchPubMed> saveList = new ArrayList<SearchPubMed>();
		for (SearchPubMed s : mRowAdapter.getSearchList()) {
			if (s.isChecked()) {
				list.add(s);
			} else {
				saveList.add(s);
			}
		}
		if (list.size() < 1) {
			displayError("Please check a search");
			return;
		}
		saveSearch(this, saveList, MODE_PRIVATE);
		mRowAdapter.setSearchList(loadSearch());
		mRowAdapter.notifyDataSetChanged();
	}

	public void runSearch(SearchPubMed search) {
		Intent intent = new Intent();
		intent.putExtra("search", search);
		setResult(RESULT_OK, intent);
		if (search.getFieldList() != null) {
			intent.putExtra("fieldList", search.getFieldList());
		}
		finish();
	}
}
