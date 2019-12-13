package com.bim.pubmed;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.AdTargetingOptions;
import com.bim.core.Device;
import com.bim.core.Log;
import com.bim.core.PubSetting;
import com.bim.core.Util;
import com.bim.ncbi.ActivityPub;
import com.bim.ncbi.DevicePubMed;
import com.bim.ncbi.ECallListener;
import com.bim.ncbi.ECallSearch;
import com.bim.ncbi.EDatabase;
import com.bim.ncbi.EField;
import com.bim.ncbi.EFieldType;
import com.bim.ncbi.EResponseSearch;
import com.bim.ncbi.SearchPubMed;

/*
 * Device post
 * Abstract. one article debug
 */
public class ActivityPubMed extends ActivityPub implements ECallListener,
		AdListener {
	private AdLayout adView;
	private static final String APP_ID = "4a345a32484248513339433753344535";

	private static final String PKG_NAME = "com.bim.pubmedp";

	public static final int ACTIVITY_MY_SEARCH = 1;
	private ListSearchFieldAdapter mFieldListAdapter;

	private RadioGroup mAndOrRadioGroup;
	private EditText mSearchTerm;
	private CheckBox mHuman;
	private CheckBox mAnimal;
	private CheckBox mMale;
	private CheckBox mFemale;

	private Spinner mSortBySpinner;
	private int sortById;
	public static SearchPubMed search;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.pubmed);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.app_icon_title);

		mAndOrRadioGroup = (RadioGroup) findViewById(R.id.pubmed_radio_group);

		mSearchTerm = (EditText) findViewById(R.id.pubmed_search_term_text);
		mSearchTerm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		        	doSearch();
		            return true;
		        }
		        return false;
		    }
		});

		mHuman = (CheckBox) findViewById(R.id.pubmed_search_human_checkbox);
		mAnimal = (CheckBox) findViewById(R.id.pubmed_search_animal_checkbox);
		mMale = (CheckBox) findViewById(R.id.pubmed_search_male_checkbox);
		mFemale = (CheckBox) findViewById(R.id.pubmed_search_female_checkbox);

		Button searchButton = (Button) findViewById(R.id.pubmed_button_search);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				doSearch();
			}
		});

		ImageButton addFieldButton = (ImageButton) findViewById(R.id.pubmed_search_add_field_button);
		addFieldButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mFieldListAdapter.add("AU", null);
				mFieldListAdapter.notifyDataSetChanged();
			}
		});

		Button cancelButton = (Button) findViewById(R.id.pubmed_button_clear);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mSearchTerm.setText(null);
				mFieldListAdapter.resetValue();
				mHuman.setChecked(false);
				mAnimal.setChecked(false);
				mMale.setChecked(false);
				mFemale.setChecked(false);
			}
		});

		mSortBySpinner = (Spinner) findViewById(R.id.pubmed_sort_by_spinner);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
				R.array.pubmed_sort_by_list,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSortBySpinner.setAdapter(adapter);
		mSortBySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				sortById = position;
			}

			public void onNothingSelected(AdapterView arg0) {

			}
		});

		initFieldListView();

		PubSetting setting = getSetting(this);

		if (isFirstCreated) {
			DevicePubMed.save(this, Device.ACTION_START, setting.isFirstTime()
					+ "");
		}
		isFirstCreated = false;

		if (setting.isFirstTime()) {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					Intent intent = new Intent(ActivityPubMed.this,
							ActivityWelcome.class);
					ActivityPubMed.this.startActivityForResult(intent, 0);
				}
			}, 3000);
			setting.setFirstTime(false);
		}

		closeDialog();

		AdRegistration.enableLogging(this, false);
		// For debugging purposes flag all ad requests as tests, but set to
		// false for production builds
		AdRegistration.enableTesting(this, false);

		adView = (AdLayout) findViewById(R.id.ad_view);
		adView.setListener(this);
		try {
			AdRegistration.setAppKey(getApplicationContext(), APP_ID);
		} catch (Exception e) {
			Log.d(e);
			return;
		}
		LoadAd();
	}

	private void initFieldListView() {
		mFieldListAdapter = new ListSearchFieldAdapter(this);
		mFieldListAdapter.add("AU", null);
		mFieldListAdapter.add("TA", null);
		mFieldListAdapter.add("TI", null);

		ListView mListView = (ListView) findViewById(R.id.pubmed_search_field_list);
		mListView.setDivider(null);
		mListView.setDividerHeight(0);
		mListView.setAdapter(mFieldListAdapter);
	}

	public void doNotify() {

		Intent intent = new Intent(this, ActivityPubMed.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		String pubmed = Util.getResourceString(this, R.string.app_name);
		String message = "New articles";
		showNotification(R.drawable.app_icon, R.drawable.app_icon, "from "
				+ pubmed, pubmed, message, pendingIntent);
	}

	private String checkSearchById(String termIn) {
		String term = Util.trim(termIn);
		if (Util.isDigitOnly(term)) {
			return term + "[uid]";
		} else {
			return null;
		}
	}

	private void doSearch() {
		// Log.d("start...");
		// startService(new Intent(this, SearchService.class));
		// Log.d("started");

		String op;
		if (mAndOrRadioGroup.getCheckedRadioButtonId() == R.id.pubmed_radio_and) {
			op = "AND";
		} else {
			op = "OR";
		}

		String field = "";
		if (mFieldListAdapter.getFieldList() != null) {
			int cnt = 0;
			for (EField f : mFieldListAdapter.getFieldList()) {
				String value = f.getValue();
				if (f.getType() == null || Util.isNull(value)) {
					continue;
				}

				if (cnt > 0) {
					field += " " + op + " ";
				}
				field += value + "[" + f.getType().getId() + "]";

				// ("2008"[PDAT] : "2009"[PDAT]))
				cnt++;
			}
		}

		String term = mSearchTerm.getText().toString();
		if (Util.isNull(term) && Util.isNull(field)) {
			displayError("Please enter a term");
			return;
		}

		String search;
		if (Util.isNull(term)) {
			search = field;
		} else {
			String uid = checkSearchById(term);
			if (Util.isNotNull(uid)) {
				search = uid;
			} else {
				if (Util.isNull(field)) {
					search = parseSearchTerm(term);
					// search = term + "[All Fields]";
				} else {
					search = parseSearchTerm(term) + " " + op + " " + field;
					// search = term + "[All Fields] " + op + " " + field;
				}
			}
		}

		if (mHuman.isChecked() && mAnimal.isChecked()) {
			search += " " + op + " "
					+ "(humans[MeSH Terms] OR animals[MeSH Terms:noexp])";
		} else if (mHuman.isChecked()) {
			search += " " + op + " " + "humans[MeSH Terms]";
		} else if (mAnimal.isChecked()) {
			search += " " + op + " " + "animals[MeSH Terms:noexp]";
		}

		if (mMale.isChecked() && mMale.isChecked()) {
			search += " " + op + " "
					+ "(male[MeSH Terms] OR female[MeSH Terms])";
		} else if (mMale.isChecked()) {
			search += " " + op + " " + "male[MeSH Terms]";
		} else if (mFemale.isChecked()) {
			search += " " + op + " " + "female[MeSH Terms]";
		}
		search = search.replaceAll(" ", "+");

		showLoadingDialog();
		ECallSearch eSearch = new ECallSearch(this);
		eSearch.getRequest().setDb(EDatabase.PUBMED);
		eSearch.getRequest().setTerm(search);
		eSearch.getRequest().setSort(getSortBy());
		eSearch.getRequest().setRetmax(10);

		httpThread = new Thread(eSearch);
		httpThread.start();
	}

	protected String parseSearchTerm(String termIn) {
		String term = Util.trim(termIn);
		if (Util.isNull(term)) {
			return term;
		}
		String t = "(" + term + "[All Fields] OR " + term + "[MeSH Terms])";
		if (term.indexOf(" ") < -1) {
			return t;
		}

		String[] words = term.split(" ");
		if (words == null || words.length < 1) {
			return t;
		}

		boolean isAndOrExist = false;
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (Util.isNull(word)) {
				continue;
			}
			if ("and".equalsIgnoreCase(word) || "or".equalsIgnoreCase(word)) {
				list.add(word.toUpperCase());
				isAndOrExist = true;
			} else {
				list.add("(" + word + "[All Fields] OR " + word
						+ "[MeSH Terms]" + ")");
			}
		}

		if (!isAndOrExist) {
			List<String> newList = new ArrayList<String>();
			int cnt = 0;
			for (String s : list) {
				if (cnt > 0) {
					newList.add("AND");
				}
				newList.add(s);
				cnt++;
			}
			list = newList;
		}

		if (list.size() < 2) {
			return t;
		}

		t = "";
		int cnt = 0;
		for (String s : list) {
			if (cnt > 0) {
				t += " ";
			}
			t += s;
			cnt++;
		}

		return t;
	}

	private String getSortBy() {

		switch (sortById) {
		case 0:
			return null;
		case 1:
			return "pub+date";
		case 2:
			return "first+author";
		case 3:
			return "last+author";
		case 4:
			return "journal";
		case 5:
			return "title";
		}
		return null;
	}

	public void onESearchOkay(EResponseSearch response, ECallSearch eSearch) {
		if (response == null) {
			return;
		}
		Log.d("found: " + response.getIdList().size());
		if (response.getIdList().size() < 1) {
			displayError("No result found");
			return;
		}

		if (response.getQueryKey() <= 0 || Util.isNull(response.getWebEnv())) {
			displayError("Failed to get query key and webenv");
			return;
		}

		search = new SearchPubMed();
		search.setTerm(mSearchTerm.getText().toString());
		if (mAndOrRadioGroup.getCheckedRadioButtonId() == R.id.pubmed_radio_and) {
			search.setAnd(true);
		} else {
			search.setAnd(false);
		}
		search.setHuman(mHuman.isChecked());
		search.setAnimal(mAnimal.isChecked());
		search.setMale(mMale.isChecked());
		search.setFemale(mFemale.isChecked());
		search.setFieldList(mFieldListAdapter.getFieldList());
		if (sortById > 0) {
			search.setSort(eSearch.getRequest().getSort());
		}
		search.setResult(response.getCount());

		ActivityListArticle.isFirstCreated = true;
		Intent intent = new Intent(this, ActivityListArticle.class);
		// intent.putExtra("search", search);
		intent.putExtra("response", response);
		this.startActivityForResult(intent, 1);
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.pubmed, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.menu_pubmed_my_search:
			startMySearchActivity();
			return true;
		case R.id.menu_pubmed_my_article:
			startMyArticleActivity();
			return true;
		case R.id.menu_pubmed_upgrade:
			doUpgrade();
			return true;
		}

		return true;
	}

	private void startMySearchActivity() {
		ActivityMySearch.isFirstCreated = true;
		Intent intent = new Intent(this, ActivityMySearch.class);
		this.startActivityForResult(intent, ACTIVITY_MY_SEARCH);
	}

	private void startMyArticleActivity() {
		ActivityMyArticle.isFirstCreated = true;
		Intent intent = new Intent(this, ActivityMyArticle.class);
		this.startActivityForResult(intent, 2);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (intent == null) {
			return;
		}
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		if (requestCode == ACTIVITY_MY_SEARCH) {
			onMySearchReturn(intent);
		}
	}

	private void onMySearchReturn(Intent intent) {
		SearchPubMed search = intent.getParcelableExtra("search");
		if (search == null) {
			return;
		}

		ArrayList<EField> fields = (ArrayList<EField>) intent
				.getSerializableExtra("fieldList");
		if (fields == null) {
			fields = new ArrayList<EField>();
		}

		int cnt = fields.size();
		for (int i = cnt; i < 3; i++) {
			EFieldType type = EFieldType.findType("AU");
			EField field = new EField(type, null);
			fields.add(field);
		}
		search.setFieldList(fields);

		mSearchTerm.setText(search.getTerm());
		if (search.isAnd()) {
			mAndOrRadioGroup.check(R.id.pubmed_radio_and);
		} else {
			mAndOrRadioGroup.check(R.id.pubmed_radio_or);
		}
		mHuman.setChecked(search.isHuman());
		mAnimal.setChecked(search.isAnimal());
		mMale.setChecked(search.isMale());
		mFemale.setChecked(search.isFemale());

		mFieldListAdapter.setFieldList(search.getFieldList());
		mFieldListAdapter.notifyDataSetChanged();
		doSearch();
	}

	private static PubSetting _appSetting;

	public static PubSetting getSetting(Activity activity) {
		if (_appSetting != null) {
			return _appSetting;
		}
		_appSetting = new PubSetting();
		_appSetting.load(activity);
		if (_appSetting.isFirstTime()) {
			_appSetting.save(activity);
		}

		return _appSetting;
	}

	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		if (savedInstanceState == null) {
			return;
		}

		savedInstanceState.putString("searchTerm", mSearchTerm.getText()
				.toString());
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState == null) {
			return;
		}
		String searchTerm = savedInstanceState.getString("searchTerm");
		mSearchTerm.setText(searchTerm);
	}

	private void doUpgrade() {
		try {
			ApplicationInfo info = getPackageManager().getApplicationInfo(
					PKG_NAME, 0);
			if (info == null) {
				showUpgradeDialog();
				return;
			}
		} catch (PackageManager.NameNotFoundException e) {
			showUpgradeDialog();
			return;
		}
		upgradeData();
	}

	private void upgradeData() {
		try {
			Intent intent = new Intent();

			String searchData = getSavedData(ActivityMySearch.MY_SEARCH_FILE_NAME);
			if (!Util.isNull(searchData)) {
				intent.putExtra("searchData", searchData);
			}
			String articleData = getSavedData(ActivityMyArticle.MY_ARTICLE_FILE_NAME);
			if (!Util.isNull(articleData)) {
				intent.putExtra("articleData", articleData);
			}
			intent.setComponent(new ComponentName(PKG_NAME, PKG_NAME
					+ ".ActivityPubMed"));
			startActivity(intent);
			finish();
		} catch (RuntimeException e) {
			Log.d(e);
		}
	}

	private void showUpgradeDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		String msg = "Thank for your interesting. "
				+ "\n\nTo upgrade, tap \"View in Market\" button.  There are more features in pro version."
				+ "\n\nWith your support, we will keep adding more."
				+ "\n\nThanks!\n";
		alert.setTitle("PubMed Mobile Pro");
		alert.setMessage(msg);
		alert.setPositiveButton("View in Market",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Util.startViewMarktMapActivity(PKG_NAME,
								ActivityPubMed.this);
						dialog.dismiss();
					}
				});

		alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
				upgradeData();
			}
		});

		alert.show();
	}

	public String getSavedData(String fileName) {
		StringBuffer content = new StringBuffer();
		try {

			FileInputStream fIn = openFileInput(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fIn));
			String line;
			while ((line = br.readLine()) != null) {
				if (Util.isNull(line)) {
					continue;
				}
				content.append(line + "\n");
			}
			fIn.close();

		} catch (Exception e) {
		}
		return content.toString();
	}

	public void LoadAd() {
		// Load the ad with the appropriate ad targeting options.
		AdTargetingOptions adOptions = new AdTargetingOptions();
		adView.loadAd(adOptions);
	}

	/**
	 * This event is called after a rich media ads has collapsed from an
	 * expanded state.
	 */
	@Override
	public void onAdCollapsed(AdLayout view) {

	}

	/**
	 * This event is called if an ad fails to load.
	 */
	@Override
	public void onAdFailedToLoad(AdLayout view, AdError error) {
		Log.d("Ad failed to load. Code: " + error.getResponseCode()
				+ ", Message: " + error.getResponseMessage());
	}

	/**
	 * This event is called once an ad loads successfully.
	 */
	@Override
	public void onAdLoaded(AdLayout view, AdProperties adProperties) {
		Log.d(adProperties.getAdType().toString() + " Ad loaded successfully.");
	}

	/**
	 * This event is called after a rich media ad expands.
	 */
	@Override
	public void onAdExpanded(AdLayout view) {
		Log.d("Ad expanded.");
	}
}