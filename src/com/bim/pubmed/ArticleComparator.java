package com.bim.pubmed;

import java.util.Comparator;

import com.bim.core.Util;
import com.bim.ncbi.EArticle;

public class ArticleComparator implements Comparator {

	private int byResId;
	private boolean isDescOrder;

	public ArticleComparator(int byResId, boolean isDescOrder) {
		this.byResId = byResId;
		this.isDescOrder = isDescOrder;
	}

	public int compare(Object object1, Object object2) {
		EArticle a = (EArticle) object1;
		EArticle b = (EArticle) object2;

		String aStr = null;
		String bStr = null;
		switch (byResId) {
		case R.id.sort_by_pub_date:
			aStr = a.getPubDate();
			bStr = b.getPubDate();
			break;
		case R.id.sort_by_first_author:
			aStr = a.getFirstAuthor();
			bStr = b.getFirstAuthor();
			break;
		case R.id.sort_by_last_author:
			aStr = a.getLastAuthor();
			bStr = b.getLastAuthor();
			break;
		case R.id.sort_by_journal:
			aStr = a.getJournal();
			bStr = b.getJournal();
			break;
		case R.id.sort_by_title:
			aStr = a.getTitle();
			bStr = b.getTitle();
			break;

		default:
			break;
		}
		
		if ( isDescOrder ) {
			String t = aStr;
			aStr = bStr;
			bStr = t;
		}
		
		return byName(aStr, bStr);
	}

	private int byName(String a, String b) {
		if (Util.isNull(a) && Util.isNull(b)) {
			return 0;
		} else if ( Util.isNull(a)) {
			return -1;
		} else if ( Util.isNull(b)) {
			return 1;
		} else {
			return a.compareToIgnoreCase(b);
		}
	}
}