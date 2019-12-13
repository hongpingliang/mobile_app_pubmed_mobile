package com.bim.pubmed;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.bim.core.Util;
import com.bim.ncbi.ActivityPub;

public class ActivityUser extends ActivityPub implements SubmitterHandler {

	private EditText mName;
	private EditText mEmail;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.user);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.app_icon);

		initCancelButton(R.id.user_cancel_button);
		View mSave = findViewById(R.id.user_save_button);
		mSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				doSave();
			}
		});

		mName = (EditText) findViewById(R.id.user_name_text);
		mEmail = (EditText) findViewById(R.id.user_email_text);
		User user = User.getUser(this);
		if (user != null) {
			mName.setText(user.getNickName());
			mEmail.setText(user.getEmail());
		}
	}

	private void doSave() {
		String name = mName.getText().toString();
		if (Util.isNull(name)) {
			showMessage("Please enter name");
			return;
		}

		String email = mEmail.getText().toString();
		if (Util.isNull(email)) {
			showMessage("Please enter email");
			return;
		}

		String p = Util.add("name", name + "");
		p += Util.add("name", Util.nullToNone(name));
		p += Util.add("email", Util.nullToNone(email));

		Submitter submitter = new Submitter(this, this,
				Submitter.PAGE_UPDATE_USER, p);
		Thread thread = new Thread(submitter);

		thread.start();
		showMessage("Saving ...");
	}

	public void onSubmitReady(Submitter submitter, String content) {
		closeDialog();
		
		User.parseUser(this, content);

		setResult(RESULT_OK);
		finish();
	}
}
