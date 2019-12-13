package com.bim.ncbi;

import org.xml.sax.SAXException;

import com.bim.core.Util;

public class ECallSummaryPubMed extends ECallSummary {

	private static final String TAG_Title = "Title";
	// private static final String TAG_AuthorList = "AuthorList";
	private static final String TAG_Author = "Author";
	private static final String TAG_FullJournalName = "FullJournalName";
	private static final String TAG_PubDate = "PubDate";
	private static final String TAG_Volume = "Volume";
	private static final String TAG_Issue = "Issue";
	private static final String TAG_Pages = "Pages";
	private static final String TAG_HasAbstract = "HasAbstract";
	private static final String TAG_pmc = "pmc";

	private EResponseSummaryPubMed response;

	private EArticle currentArticle;

	private boolean isCurrent = false;
	private String catchedTagValue;
	private String currentNameAttValue;

	public ECallSummaryPubMed(ActivityPub activityBase) {
		super(activityBase);
		response = new EResponseSummaryPubMed();
	}

	public void startElement(String uri, String localName, String name,
			org.xml.sax.Attributes attributes) throws SAXException {

		isCurrent = true;
		currentNameAttValue = null;
		catchedTagValue = null;
		if (TAG_DocSum.equals(localName)) {
			currentArticle = new EArticle();
			return;
		}

		if (TAG_Item.equals(localName) && attributes != null) {
			currentNameAttValue = attributes.getValue(TAG_ATT_NAME);
		}
	}

	public void characters(char ch[], int start, int length) {
		if ( isCurrent ) {
			if ( Util.isNull(catchedTagValue)) {
				catchedTagValue = "";
			}
			catchedTagValue += Util.nullToNone(getString(ch, start, length));
		}
//		Log.d(catchedTagValue);
	}

	public void endElement(String namespaceURI, String localName, String qName) {
		isCurrent = false;
		if (currentArticle == null) {
			return;
		}

		if (TAG_DocSum.equals(localName)) {
			if (currentArticle.isDataOkay()) {
				response.getArticleList().add(currentArticle);
			}
			currentArticle = null;
			return;
		}

		if (TAG_Id.equals(localName)) {
			currentArticle.setId(Util.toInt(catchedTagValue));
			return;
		}

		if (Util.isNull(currentNameAttValue)) {
			return;
		}
		if (TAG_Title.equals(currentNameAttValue)) {
			currentArticle.setTitle(catchedTagValue);
		} else if (TAG_FullJournalName.equals(currentNameAttValue)) {
			currentArticle.setJournal(catchedTagValue);
		} else if (TAG_Volume.equals(currentNameAttValue)) {
			currentArticle.setVolume(catchedTagValue);
		} else if (TAG_Issue.equals(currentNameAttValue)) {
			currentArticle.setIssue(catchedTagValue);
		} else if (TAG_Pages.equals(currentNameAttValue)) {
			currentArticle.setPages(catchedTagValue);
		} else if (TAG_PubDate.equals(currentNameAttValue)) {
			currentArticle.setPubDate(catchedTagValue);
		} else if (TAG_HasAbstract.equals(currentNameAttValue)) {
			int i = Util.toInt(catchedTagValue);
			if (i > 0) {
				currentArticle.setHasAbstract(true);
			} else {
				currentArticle.setHasAbstract(false);
			}
		} else if (TAG_pmc.equals(currentNameAttValue)) {
			currentArticle.setPmc(catchedTagValue);
		} else if (TAG_Author.equals(currentNameAttValue)) {
			currentArticle.addAuthor(catchedTagValue);
		}
		currentNameAttValue = null;
		catchedTagValue = null;
	}

	public EResponseSummaryPubMed getResponse() {
		return response;
	}
}
