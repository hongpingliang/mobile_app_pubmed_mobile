package com.bim.ncbi;

import java.util.ArrayList;

import org.xml.sax.SAXException;

import com.bim.core.Util;

public class ECallFetchPubMed extends ECallFetch {
	private static final String TAG_PMID = "PMID";
	private static final String TAG_AbstractText = "AbstractText";

	private boolean isCatchValue = false;
	private String catchedTagValue;

	private EResponseFetchPubChem response;

	private EArticle currArticle;

	public ECallFetchPubMed(ActivityPub activityBase, ArrayList<EArticle> articles) {
		super(activityBase);
		response = new EResponseFetchPubChem();
		response.setArticleList(articles);
		getRequest().setRetmode("text");
		getRequest().setRettype("abstract");
		
		isLoadPubMedAbstract = true;

		String idStr = "";
		int cnt = 0;
		for (EArticle a : articles) {
			if (cnt > 0) {
				idStr += ",";
			}
			idStr += a.getId();
			cnt++;
		}
		getRequest().setId(idStr);
	}
	
	protected void onLoadTextReady(String content) {
		response.getArticleList().get(0).setAbsContent(content);
	}
	

	public void startElement(String uri, String localName, String name,
			org.xml.sax.Attributes attributes) throws SAXException {

		catchedTagValue = null;
		if (TAG_PMID.equals(localName) || TAG_AbstractText.equals(localName)) {
			isCatchValue = true;
		} else {
			isCatchValue = false;
		}
	}

	public void characters(char ch[], int start, int length) {
		if (!isCatchValue) {
			return;
		}
		catchedTagValue = getString(ch, start, length);
	}

	int id = 0;
	public void endElement(String namespaceURI, String localName, String qName) {

		if (TAG_PMID.equals(localName)) {
			id = Util.toInt(catchedTagValue);
			currArticle = response.findArticle(id);
			return;
		}

		if (currArticle != null && TAG_AbstractText.equals(localName)) {
			currArticle.setAbsContent(id + "\n" + catchedTagValue);
			return;
		}
	}

	public EResponseFetchPubChem getResponse() {
		return response;
	}
}
