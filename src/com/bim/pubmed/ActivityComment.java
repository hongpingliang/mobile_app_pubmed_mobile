package com.bim.pubmed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.bim.core.Util;
import com.bim.ncbi.ActivityPub;
import com.bim.ncbi.EArticle;

public class ActivityComment extends ActivityPub implements SubmitterHandler {
	private EArticle article;
	private Comment myComment;

	private EditText mComment;
	private RadioGroup mVisibleRadio;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.comment);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.app_icon);

		Intent intent = getIntent();
		if (intent != null) {
			article = intent.getParcelableExtra("article");
			myComment = intent.getParcelableExtra("myComment");
		}

		if (article == null) {
			finish();
			return;
		}

		mComment = (EditText) findViewById(R.id.comment_comment_text);
		mVisibleRadio = (RadioGroup) findViewById(R.id.comment_radio_group);
		if (myComment != null) {
			setTitle(R.string.abstract_comment_edit);
			mComment.setText(myComment.getComment());
			if (myComment.getVisible() == 1) {
				mVisibleRadio.check(R.id.comment_visible_private_radio);
			} else {
				mVisibleRadio.check(R.id.comment_visible_public_radio);
			}
		} else {
			setTitle(R.string.abstract_comment_add);
			mVisibleRadio.check(R.id.comment_visible_public_radio);
		}
		initCancelButton(R.id.comment_cancel_button);
		View mSave = findViewById(R.id.comment_save_button);
		mSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				doSave();
			}
		});
	}

	private void doSave() {
		User user = User.getUser(this);
		if (user == null || Util.isNull(user.getNickName())
				|| Util.isNull(user.getEmail())) {
			startUserActivity();
		} else {
			save();
		}
	}

	private void save() {
		String comment = mComment.getText().toString();
		if (Util.isNull(comment)) {
			showMessage("Please enter comments");
			return;
		}

		String visible;
		if (mVisibleRadio.getCheckedRadioButtonId() == R.id.comment_visible_private_radio) {
			visible = "1";
		} else {
			visible = "0";
		}
		String p = Util.add("itemId", article.getId() + "");
		p += Util.add("comment", Util.nullToNone(comment));
		p += Util.add("visible", visible);
		Submitter submitter = new Submitter(this, this,
				Submitter.PAGE_SAVE_COMMENT, p);
		Thread thread = new Thread(submitter);

		thread.start();
		showMessage("Saving comment...");
	}

	public void onSubmitReady(Submitter submitter, String content) {
		closeDialog();

		finishOk();
	}

	private void finishOk() {
		setResult(RESULT_OK);
		finish();
	}

	private void startUserActivity() {
		Intent intent = new Intent(this, ActivityUser.class);
		this.startActivityForResult(intent, 0);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (RESULT_OK == resultCode) {
			User user = User.getUser(this);
			if (user != null && Util.isNotNull(user.getNickName())
					&& Util.isNotNull(user.getEmail())) {
				save();
			}
		}
	}
}
