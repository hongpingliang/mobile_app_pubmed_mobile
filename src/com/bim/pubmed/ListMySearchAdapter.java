package com.bim.pubmed;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bim.core.Util;
import com.bim.ncbi.EField;
import com.bim.ncbi.SearchPubMed;

public class ListMySearchAdapter extends BaseAdapter {
	private ActivityMySearch activity;
	protected LayoutInflater inflater;
	private List<SearchPubMed> searchList;

	public ListMySearchAdapter(ActivityMySearch activity) {
		this.activity = activity;
		this.inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		if ( searchList == null ) {
			return 0;
		}
		return searchList.size();
	}

	public SearchPubMed getSearch(int position) {
		return searchList.get(position);
	}

	public Object getItem(int position) {
		return getSearch(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.my_search_row, parent,
					false);
		}

		final SearchPubMed search = getSearch(position);

		CheckBox mCheckbox = (CheckBox) convertView
				.findViewById(R.id.my_search_row_checkbox);
		mCheckbox.setOnCheckedChangeListener(null);
		mCheckbox.setChecked(search.isChecked());
		mCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				search.setChecked(isChecked);
			}
		});

		TextView mTerm = (TextView) convertView
				.findViewById(R.id.my_search_row_term);
		mTerm.setText(search.getTerm());

		String t = "";
		if (search.getFieldList() != null && searchList.size() > 0) {
			int cnt = 0;
			for (EField f : search.getFieldList()) {
				if (f.getType() == null || Util.isNull(f.getValue())) {
					continue;
				}
				if (cnt > 0) {
					t += "\n";
				}
				t += f.getType().getLabel() + ":   " + f.getValue();
				cnt++;
			}
		}
		TextView mFields = (TextView) convertView
				.findViewById(R.id.my_search_row_fields);
		mFields.setText(t);

		String humOrA = "";
		if (search.isHuman()) {
			humOrA += Util.getResourceString(activity, R.string.pubmed_human);
		}
		if (search.isAnimal()) {
			if (!Util.isNull(humOrA)) {
				humOrA += ", ";
			}
			humOrA += Util.getResourceString(activity, R.string.pubmed_animal);
		}
		if (search.isMale()) {
			if (!Util.isNull(humOrA)) {
				humOrA += ",   ";
			}
			humOrA += Util.getResourceString(activity, R.string.pubmed_male);
		}
		if (search.isFemale()) {
			if (!Util.isNull(humOrA)) {
				humOrA += ", ";
			}
			humOrA += Util.getResourceString(activity, R.string.pubmed_female);
		}
		TextView mHumanOrAnimal = (TextView) convertView
				.findViewById(R.id.my_search_row_humanOrAnimal);
		mHumanOrAnimal.setText(humOrA);
		if (Util.isNull(humOrA)) {
			mHumanOrAnimal.setVisibility(View.GONE);
		} else {
			mHumanOrAnimal.setVisibility(View.VISIBLE);
		}

		TextView mAndOr = (TextView) convertView
				.findViewById(R.id.my_search_row_and);
		if (search.isAnd()) {
			mAndOr.setText(R.string.pubmed_radio_and);
		} else {
			mAndOr.setText(R.string.pubmed_radio_or);
		}

		TextView mTime = (TextView) convertView
				.findViewById(R.id.my_search_row_time);
		mTime.setText(search.getTime());

		TextView mResult = (TextView) convertView
				.findViewById(R.id.my_search_row_result);
		mResult.setText(Util.getResourceString(activity,
				R.string.list_article_result)
				+ ": " + search.getResult());

		ImageButton runButton = (ImageButton) convertView
				.findViewById(R.id.my_search_row_button_run);
		runButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				activity.runSearch(search);
			}
		});
		return convertView;
	}

	public List<SearchPubMed> getSearchList() {
		return searchList;
	}

	public void setSearchList(List<SearchPubMed> searchList) {
		if (searchList == null) {
			searchList = new ArrayList<SearchPubMed>();
		} else {
			this.searchList = searchList;
		}
	}
}
