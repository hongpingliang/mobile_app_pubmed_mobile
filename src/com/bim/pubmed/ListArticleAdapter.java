package com.bim.pubmed;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bim.ncbi.EArticle;

public class ListArticleAdapter extends ListAdapter {
	private ActivityListArticle activity;
	protected LayoutInflater inflater;
	private List<EArticle> articleList;

	public ListArticleAdapter(ActivityListArticle activity) {
		this.activity = activity;
		this.articleList = new ArrayList<EArticle>();
		this.inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void addArticleList(List<EArticle> list) {
		if (list != null) {
			articleList.addAll(list);
		}
	}

	public int getCount() {
		if ( articleList.size() >=  activity.getAvailabeArticleCount() ) {
			return articleList.size();
		} else {
			return articleList.size()+1;
		}
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

	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			view = inflater.inflate(R.layout.list_article_row, parent,
					false);
		}
		View loadingView = view.findViewById(R.id.list_row_loading_view);
		View dataView = view.findViewById(R.id.list_row_data_view);
		final EArticle article = getArticle(position);
		if (article == null) {
			loadingView.setVisibility(View.VISIBLE);
			dataView.setVisibility(View.GONE);
			activity.chekAndLoadMore();
			return view;
		}

		loadingView.setVisibility(View.GONE);
		dataView.setVisibility(View.VISIBLE);
		
		return getArticleView(activity, view, article, position);
	}

	public List<EArticle> getArticleList() {
		return articleList;
	}

	public void setArticleList(List<EArticle> articleList) {
		this.articleList = articleList;
	}
}
