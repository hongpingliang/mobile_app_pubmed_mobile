package com.bim.pubmed;

import android.text.Html;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.bim.core.Util;
import com.bim.ncbi.ActivityPub;
import com.bim.ncbi.EArticle;

public abstract class ListAdapter extends BaseAdapter {

	protected void startShowAbstractActivity(final ActivityPub activityBase,
			EArticle article) {
		if (activityBase instanceof ActivityListArticle) {
			ActivityListArticle activity = (ActivityListArticle) activityBase;
			activity.startShowAbstractActivity(article);
			return;
		}
		if (activityBase instanceof ActivityMyArticle) {
			ActivityMyArticle activity = (ActivityMyArticle) activityBase;
			activity.startShowAbstractActivity(article);
			return;
		}
	}

	protected View getArticleView(final ActivityPub activity, View view,
			final EArticle article, int position) {
		
		CheckBox mCheckbox = (CheckBox) view
				.findViewById(R.id.list_article_checkbox);
		mCheckbox.setOnCheckedChangeListener(null);
		mCheckbox.setChecked(article.isChecked());
		mCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				article.setChecked(isChecked);
			}
		});

		TextView mTitle = (TextView) view.findViewById(R.id.list_article_title);
		mTitle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startShowAbstractActivity(activity, article);
			}
		});
		String num = "<small><font color=\"#909090\">" + (position + 1) + ".  " + "</font></small>";		
		String title = num + article.getTitle();
		mTitle.setText(Html.fromHtml(title));
/*
		Spannable str = (Spannable) mTitle.getText();
		// new StyleSpan(android.graphics.Typeface.ITALIC);
		str.setSpan(new ForegroundColorSpan(Color.BLACK), 0, num.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		str.setSpan(new RelativeSizeSpan(0.75f), 0, num.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		str.setSpan(new UnderlineSpan(), num.length(), title.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
*/
		TextView mNoAbstract = (TextView) view
				.findViewById(R.id.list_article_row_no_abstract);
		if (article.isHasAbstract()) {
			mNoAbstract.setVisibility(View.GONE);
		} else {
			mNoAbstract.setVisibility(View.VISIBLE);
		}

		TextView mAuther = (TextView) view
				.findViewById(R.id.list_article_author);
		mAuther.setText(article.getAuthorStr());

		TextView mJournal = (TextView) view
				.findViewById(R.id.list_article_journal);
		mJournal.setText(article.getJournalInfo());

		TextView mPMid = (TextView) view.findViewById(R.id.list_article_pmid);
		mPMid.setText("PMID: " + article.getId());

		View mFreeArticle = view
				.findViewById(R.id.list_article_pmc_free_article_button);
		mFreeArticle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ActivityAbstract.showFullTextPage(activity, article);
			}
		});
		if ( Util.isNull(article.getPmc())) {
			mFreeArticle.setVisibility(View.GONE);
		} else {
			mFreeArticle.setVisibility(View.VISIBLE);			
		}

		return view;
	}
}
