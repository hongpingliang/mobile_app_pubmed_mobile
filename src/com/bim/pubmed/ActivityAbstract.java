package com.bim.pubmed;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.bim.core.Device;
import com.bim.core.EParceble;
import com.bim.core.Log;
import com.bim.core.Util;
import com.bim.ncbi.ActivityPub;
import com.bim.ncbi.DevicePubMed;
import com.bim.ncbi.EArticle;
import com.bim.ncbi.ECallFetch;
import com.bim.ncbi.ECallFetchPubMed;
import com.bim.ncbi.EDatabase;
import com.bim.ncbi.EResponseFetch;
import com.bim.ncbi.EResponseFetchPubChem;

public class ActivityAbstract extends ActivityPub implements SubmitterHandler {
	public static final int ACTION_COMMENT = 1;
	public static final int ACTION_RATING = 2;

	private Thread httpThread;
	private EArticle article;
	private TextView mAbstractLabel;

	private Button mEditComment;
	private Button mViewAll;

	public double myRating;
	private Comment myComment;
	private int numOfComments;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.show_abstract);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.app_icon);

		Intent intent = getIntent();
		if (intent == null) {
			Log.d("Error in ActivityAbstract:onCreate");
			return;
		}
		article = intent.getParcelableExtra("article");

//		article = new EArticle();
//		article.setTitle("DDD cddd");
//		article.setId(17864325);

		if (article == null) {
			displayError("No article");
			return;
		}

		mAbstractLabel = (TextView) findViewById(R.id.show_abstract_abstract_label);
		if (Util.isNull(article.getAbsContent())) {
			showLoadingDialog();
			ArrayList<EArticle> articles = new ArrayList<EArticle>();
			articles.add(article);
			ECallFetchPubMed eFetch = new ECallFetchPubMed(this, articles);
			eFetch.getRequest().setDb(EDatabase.PUBMED);
			httpThread = new Thread(eFetch);
			httpThread.start();
		} else {
			mAbstractLabel.setText(article.getAbsContent());
		}
		refreshArticleView();

		if (isFirstCreated) {
			DevicePubMed.save(this, Device.ACTION_SHOW_ABSTRACT, article
					.getId()
					+ "");
		}
		isFirstCreated = false;

		closeDialog();

		View mLink = findViewById(R.id.show_abstract_link_text_view);
		mLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				showPubMedPage();
			}
		});

		View mFullArticleLink = findViewById(R.id.show_abstract_full_article_textview);

		mFullArticleLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				showFullTextPage(ActivityAbstract.this, article);
			}
		});
		if (Util.isNull(article.getPmc())) {
			mFullArticleLink.setVisibility(View.GONE);
		} else {
			mFullArticleLink.setVisibility(View.VISIBLE);
		}
		
		mEditComment = (Button) findViewById(R.id.abstract_comment_edit);
		mEditComment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startCommentActivity();
			}
		});
		mViewAll = (Button) findViewById(R.id.abstract_comment_view_all);
		mViewAll.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startListCommentActivity();
			}
		});
	}
	
	private void showPubMedPage() {
		String url = "https://www.ncbi.nlm.nih.gov/pubmed/";
		url += article.getId();

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivityForResult(intent, 0);
	}

	private void refreshArticleView() {
		if (article == null) {
			displayError("No article");
			return;
		}
	}

	public void onEFetchOkay(EResponseFetch responseIn, ECallFetch eFetch) {
		EResponseFetchPubChem response = (EResponseFetchPubChem) responseIn;
		mAbstractLabel
				.setText(response.getArticleList().get(0).getAbsContent());
		loadItemDetail();
	}

	public static void showFullTextPage(Activity activity, EArticle a) {
		if (Util.isNull(a.getPmc())) {
			return;
		}

		String url = "https://www.ncbi.nlm.nih.gov/pmc/articles/" + a.getPmc()
				+ "/";

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		activity.startActivityForResult(intent, 0);
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.show_abstract, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.menu_show_abstract_save_article:
			save();
			return true;
		case R.id.menu_show_abstract_email_article:
			email();
			return true;
		case R.id.menu_show_abstract_evernote:
			evernote();
			return true;
		case R.id.menu_show_abstract_back:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}

		return true;
	}

	private void email() {
		String subject = "PubMed: " + article.getTitle();
		String content = article.getAbsContent();
		Util.doEmail(this, subject, content);
	}
	
	private void evernote() {
		String subject = "PubMed: " + article.getTitle();
		String content = article.getAbsContent();
		String url = "https://www.ncbi.nlm.nih.gov/pubmed/";
		url += article.getId();
		
		Intent intent = new Intent("com.evernote.action.CREATE_NEW_NOTE");
		intent.putExtra(Intent.EXTRA_TITLE, subject);
		intent.putExtra(Intent.EXTRA_TEXT, content);
		intent.putExtra("sourceURL", url);
		intent.putExtra("sourceApplication ", "PubMed Mobile - By Crinus");
		
		try {
			startActivity(intent);
		} catch (Exception e) {
			showMessage("No evernote app found");
		}
	}		

	public void save() {
		ArrayList<EArticle> list = new ArrayList<EArticle>();
		list.add(article);
		ActivityMyArticle.saveArticles(this, list, MODE_APPEND);
		showMessage("Article saved");
	}

	private void loadItemDetail() {
		String p = Util.add("itemId", article.getId() + "");
		Submitter submitter = new Submitter(this, this,
				Submitter.PAGE_GET_ITEM_OVERALL, p);
		Thread thread = new Thread(submitter);
		thread.start();
		showLoadingDialog();
	}

	public void onSubmitReady(Submitter submitter, String content) {
		closeDialog();
		myRating = 0;
		myComment = null;
		numOfComments = 0;

		mEditComment.setVisibility(View.VISIBLE);

		parseMyReview(content);
		
		TextView mPrefix = (TextView) findViewById(R.id.abstract_my_comment_prefix_label);
		TextView mComment = (TextView) findViewById(R.id.abstract_my_comment_label);
		if (myComment == null) {
			mPrefix.setVisibility(View.GONE);
			mComment.setVisibility(View.GONE);
			mEditComment.setText(R.string.abstract_comment_add);
		} else {
			String prefix;
			if ( myComment.getVisible() == 1) {
				prefix = "My note\n";
			} else {
				prefix = "My comment\n";
			}
			mPrefix.setText(prefix);
			mPrefix.setVisibility(View.VISIBLE);
			mComment.setText(myComment.getComment());
			mComment.setVisibility(View.VISIBLE);
			mEditComment.setText(R.string.abstract_comment_edit);
		}

		TextView mNumOfComm = (TextView) findViewById(R.id.abstract_comment_cnt_label);
		if (numOfComments < 1) {
			mNumOfComm.setVisibility(View.GONE);
			mViewAll.setVisibility(View.GONE);
		} else {
			mNumOfComm.setText("Public comments (" + numOfComments + ")");
			mNumOfComm.setVisibility(View.VISIBLE);
			mViewAll.setVisibility(View.VISIBLE);
		}
	}

	private void parseMyReview(String content) {
		if (Util.isNull(content)) {
			return;
		}
		try {
			JSONObject jsonRoot = new JSONObject(content);
			myRating = EParceble.getDouble(jsonRoot, "myRating");
			JSONObject commentObj = EParceble.getObject(jsonRoot, "myComment");
			if (commentObj != null) {
				myComment = new Comment();
				myComment.parse(commentObj);
			}
			myRating = EParceble.getDouble(jsonRoot, "myRating");
			numOfComments = EParceble.getInt(jsonRoot, "numOfComments");
		} catch (Exception e) {
			Log.d(e);
		}
	}

	private void startListCommentActivity() {
		Intent intent = new Intent(this, ActivityListComment.class);
		intent.putExtra("article", article);
		this.startActivityForResult(intent, 0);
	}

	private void startCommentActivity() {
		Intent intent = new Intent(this, ActivityComment.class);
		intent.putExtra("article", article);
		intent.putExtra("myComment", myComment);
		this.startActivityForResult(intent, ACTION_COMMENT);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		switch (requestCode) {
		case ACTION_COMMENT:
			onActivityResultComment(requestCode, resultCode, intent);
			break;

		default:
			break;
		}
	}

	protected void onActivityResultComment(int requestCode, int resultCode,
			Intent intent) {
		if (RESULT_OK == resultCode) {
			loadItemDetail();
		}

	}
}
