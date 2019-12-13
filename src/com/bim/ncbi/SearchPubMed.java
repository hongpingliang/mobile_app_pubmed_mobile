package com.bim.ncbi;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.os.Parcel;
import android.os.Parcelable;

import com.bim.core.EParceble;
import com.bim.core.Util;
import com.bim.ncbi.EField;

public class SearchPubMed extends EParceble {
	private String time;
	private String term;
	private boolean and = true;
	private boolean human;
	private boolean animal;
	private boolean male;
	private boolean female;
	private ArrayList<EField> fieldList;
	private String sort;
	private int result;

	private boolean checked;

	public SearchPubMed() {
		time = Util.formatDate(new Date());
	}

	private SearchPubMed(Parcel in) {
		time = in.readString();
		term = in.readString();
		and = readBoolean(in);
		human = readBoolean(in);
		animal = readBoolean(in);
		male = readBoolean(in);
		female = readBoolean(in);
		result = in.readInt();
		sort = in.readString();
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(time);
		out.writeString(term);
		writeBoolean(out, isAnd());
		writeBoolean(out, isHuman());
		writeBoolean(out, isAnimal());
		writeBoolean(out, isMale());
		writeBoolean(out, isFemale());
		out.writeInt(result);
		out.writeString(sort);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public SearchPubMed createFromParcel(Parcel in) {
			return new SearchPubMed(in);
		}

		public SearchPubMed[] newArray(int size) {
			return new SearchPubMed[size];
		}
	};

	public String toJsonString() {
		JSONStringer json = new JSONStringer();
		try {
			json.object();
			json.key("time").value(time);
			if (!Util.isNull(term)) {
				json.key("term").value(getTerm());
			}
			if (!isAnd()) {
				json.key("and").value(isAnd());
			}
			if (isHuman()) {
				json.key("human").value(isHuman());
			}
			if (isAnimal()) {
				json.key("animal").value(isAnimal());
			}
			if (isMale()) {
				json.key("male").value(isMale());
			}
			if (isFemale()) {
				json.key("female").value(isFemale());
			}
			if (getResult() > 0) {
				json.key("result").value(getResult());
			}
			if (!Util.isNull(getSort())) {
				json.key("sort").value(getSort());
			}

			if (fieldList != null && fieldList.size() > 0) {
				int cnt = 0;
				for (EField f : fieldList) {
					if (f.getType() == null || Util.isNull(f.getValue())) {
						continue;
					}
					cnt++;
				}
				if (cnt > 0) {
					json.key("fieldList");
					json.array();
					for (EField f : fieldList) {
						if (f.getType() == null || Util.isNull(f.getValue())) {
							continue;
						}
						JSONObject item = new JSONObject();
						item.put("typeId", f.getType().getId());
						item.put("value", f.getValue());
						json.value(item);
					}
					json.endArray();
				}
			}
			json.endObject();
			return json.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean parse(String jsonStr) {
		if (Util.isNull(jsonStr)) {
			return false;
		}

		try {
			JSONObject json = new JSONObject(jsonStr.trim());
			time = json.getString("time");
			term = getString(json, "term");
			if (json.has("and")) {
				and = getBoolean(json, "and");
			}
			human = getBoolean(json, "human");
			animal = getBoolean(json, "animal");
			male = getBoolean(json, "male");
			female = getBoolean(json, "female");
			result = getInt(json, "result");
			sort = getString(json, "sort");

			if (json.has("fieldList")) {
				JSONArray array = json.getJSONArray("fieldList");
				if (array != null && array.length() > 0) {
					fieldList = new ArrayList<EField>();
					for (int i = 0; i < array.length(); i++) {
						JSONObject arrayJson = array.getJSONObject(i);
						EField f = new EField();
						if (f.parse(arrayJson)) {
							fieldList.add(f);
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public boolean isHuman() {
		return human;
	}

	public void setHuman(boolean human) {
		this.human = human;
	}

	public boolean isAnimal() {
		return animal;
	}

	public void setAnimal(boolean animal) {
		this.animal = animal;
	}

	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public boolean isFemale() {
		return female;
	}

	public void setFemale(boolean female) {
		this.female = female;
	}

	public ArrayList<EField> getFieldList() {
		return fieldList;
	}

	public void setFieldList(ArrayList<EField> fieldList) {
		this.fieldList = fieldList;
	}

	public boolean isAnd() {
		return and;
	}

	public void setAnd(boolean and) {
		this.and = and;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

}
