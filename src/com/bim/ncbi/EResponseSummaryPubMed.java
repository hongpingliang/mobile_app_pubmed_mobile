package com.bim.ncbi;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class EResponseSummaryPubMed extends EResponse {

	private ArrayList<EArticle> articleList;

	public EResponseSummaryPubMed() {
		articleList = new ArrayList<EArticle>();
	}

	private EResponseSummaryPubMed(Parcel in) {
	}

	public void writeToParcel(Parcel dest, int flags) {
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public EResponseSummaryPubMed createFromParcel(Parcel in) {
			return new EResponseSummaryPubMed(in);
		}

		public EResponseSummaryPubMed[] newArray(int size) {
			return new EResponseSummaryPubMed[size];
		}
	};

	public ArrayList<EArticle> getArticleList() {
		return articleList;
	}

}
