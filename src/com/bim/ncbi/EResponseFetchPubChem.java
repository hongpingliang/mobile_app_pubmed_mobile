package com.bim.ncbi;

import java.util.ArrayList;

public class EResponseFetchPubChem extends EResponseFetch {
	private ArrayList<EArticle> articleList;

	public EResponseFetchPubChem() {
		articleList = new ArrayList<EArticle>();
	}

	public EArticle findArticle(int id) {
		for (EArticle a : articleList) {
			if ( id == a.getId()) {
				return a;
			}
		}
		return null;
	}
	public ArrayList<EArticle> getArticleList() {
		return articleList;
	}

	public void setArticleList(ArrayList<EArticle> articleList) {
		this.articleList = articleList;
	}

}
