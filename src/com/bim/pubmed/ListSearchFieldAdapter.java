package com.bim.pubmed;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.bim.core.Util;
import com.bim.ncbi.EField;
import com.bim.ncbi.EFieldType;

public class ListSearchFieldAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private ArrayList<EField> fieldList;

	public ListSearchFieldAdapter(Activity activity) {
		this.activity = activity;
		this.fieldList = new ArrayList<EField>();
		this.inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public EField add(String id, String value) {
		EFieldType type = EFieldType.findType(id);
		EField field = new EField(type, value);
		fieldList.add(field);
		return field;
	}

	public int getCount() {
		return fieldList.size();
	}

	public Object getField(int position) {
		return fieldList.get(position);
	}

	public Object getItem(int position) {
		return fieldList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final EField field = (EField) getField(position);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_field_row, parent,
					false);
		}
		
		
		Spinner spinner = (Spinner) convertView
				.findViewById(R.id.pubmed_list_field_spinner);
		ArrayAdapter adapter = new ArrayAdapter(activity,
				android.R.layout.simple_spinner_item, EFieldType.getLabels());
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View v,
                      int position, long id) {
            	String label = EFieldType.findLabel(position);
            	if ( Util.isNull(label)) {
            		return;
            	}
            	EFieldType type = EFieldType.findTypeByLabel(label);
            	field.setType(type);                 
            }            
            public void onNothingSelected(AdapterView arg0) {
            
            }
       });
		
		if (field.getType() != null) {
			EFieldType type = field.getType();
			int index = EFieldType.findIndexByLabel(type.getLabel());
			if (index >= 0) {
				spinner.setSelection(index, true);
			}
		}

		EditText mValue = (EditText) convertView
				.findViewById(R.id.pubmed_list_field_value);
		mValue.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if ( s == null ) {
					field.setValue(null);
				} else {
					field.setValue(s.toString());
				}
			}
		});
		mValue.setText(field.getValue());
		if (field.getType() != null) {
			if ( "DP".equals(field.getType().getId()) ) {
				mValue.setHint(R.string.list_article_row_date_hint);
			} else {
				mValue.setHint(null);
			}
		}
		
		return convertView;
	}

	public void resetValue() {
		for (EField f: fieldList) {
			f.setValue(null);
		}
		notifyDataSetChanged();
	}
	
	public ArrayList<EField> getFieldList() {
		return fieldList;
	}

	public void setFieldList(ArrayList<EField> fieldList) {
		this.fieldList = fieldList;
	}
}

