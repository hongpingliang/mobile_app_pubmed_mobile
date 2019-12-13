package com.bim.pubmed;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bim.ncbi.EArticle;

public class ListMyArticleAdapter extends ListAdapter {
	private ActivityMyArticle activity;
	protected LayoutInflater inflater;
	private List<EArticle> articleList;

	public ListMyArticleAdapter(ActivityMyArticle activity) {
		this.activity = activity;
		this.articleList = new ArrayList<EArticle>();
		this.inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return articleList.size();
	}

	public EArticle getArticle(int position) {
		if (position < articleList.size()) {
			return articleList.get(position);
		} else {
			return null;
		}
	}

	public Object getItem(int position) {
		return getArticle(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_article_row, parent,
					false);
		}
		View loadingView = convertView.findViewById(R.id.list_row_loading_view);
		View dataView = convertView.findViewById(R.id.list_row_data_view);
		final EArticle article = getArticle(position);

		loadingView.setVisibility(View.GONE);
		dataView.setVisibility(View.VISIBLE);

		return getArticleView(activity, convertView, article, position);
	}

	public List<EArticle> getArticleList() {
		return articleList;
	}

	public void setArticleList(List<EArticle> articleList) {
		if ( articleList == null ) {
			this.articleList = new ArrayList<EArticle>();
		} else {
			this.articleList = articleList;
		}
	}
}
