package com.bim.pubmed;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

import com.bim.core.JSONUtil;
import com.bim.core.Log;
import com.bim.core.Util;
import com.bim.ncbi.ActivityPub;
import com.bim.ncbi.EArticle;

public class ActivityListComment extends ActivityPub implements
		SubmitterHandler {

	private ActivityListCommentAdapter mRowAdapter;
	private EArticle article;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.list_comment);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.app_icon);

		Intent intent = getIntent();
		if (intent != null) {
			article = intent.getParcelableExtra("article");
		}


		if (article == null) {
			finish();
			return;
		}

		setTitle("Comments: " + article.getTitle());
		mRowAdapter = new ActivityListCommentAdapter(this);
		ListView mListView = (ListView) findViewById(R.id.comment_listview);
		mListView.setAdapter(mRowAdapter);
		mListView.setDivider(null);
		mListView.setDividerHeight(0);

		loadComment();
	}

	private void loadComment() {
		String p = Util.add("itemId", article.getId() + "");

		Submitter submitter = new Submitter(this, this,
				Submitter.PAGE_GET_COMMENTS, p);
		httpThread = new Thread(submitter);
		httpThread.start();
		showLoadingDialog();
	}

	public void onSubmitReady(Submitter submitter, String content) {
		closeDialog();
		List<Comment> list = new ArrayList<Comment>();
		if (Util.isNotNull(content)) {
			try {
				JSONObject jsonRoot = new JSONObject(content);
				JSONArray resultArray = JSONUtil.getArray(jsonRoot,
						"Result");
				if (resultArray != null && resultArray.length() > 0) {
					for (int i = 0; i < resultArray.length(); i++) {
						JSONObject element = resultArray.getJSONObject(i);
						Comment comment = new Comment();
						comment.parse(element);
						if (Util.isNotNull(comment.getComment())) {
							list.add(comment);
						}
					}
				}
			} catch (Exception e) {
				Log.d(e);
			}
		}
		if (list.size() < 1) {
			showMessage("No comments");
			finish();
		} else {
			mRowAdapter.setCommentList(list);
			mRowAdapter.notifyDataSetChanged();
		}
	}
}
