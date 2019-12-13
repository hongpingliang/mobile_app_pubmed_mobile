package com.bim.pubmed;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.bim.core.EParceble;
import com.bim.core.Util;

public class Comment implements Parcelable {

	private int id;
	private String itemId;
	private String comment;
	private int submitterId;
	private String submitterNickName;
	private String submitDate;
	private int visible;

	public Comment() {
	}

	public boolean isDataOk() {
		if (Util.isNull(comment)) {
			return false;
		}
		return true;
	}

	public void parse(JSONObject json) throws Exception {
		id = EParceble.getInt(json, "id");
		itemId = EParceble.getString(json, "itemId");
		comment = EParceble.getString(json, "comment");
		submitterId = EParceble.getInt(json, "submitterId");
		submitterNickName = EParceble.getString(json, "nickName");
		submitDate = EParceble.getString(json, "submitDate");
		visible = EParceble.getInt(json, "visible");
	}

	public String toString() {
		return getId() + " " + getComment();
	}

	private Comment(Parcel in) {
		id = in.readInt();
		itemId = in.readString();
		comment = in.readString();
		submitterId = in.readInt();
		submitterNickName = in.readString();
		submitDate = in.readString();
		visible = in.readInt();
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(itemId);
		out.writeString(comment);
		out.writeInt(submitterId);
		out.writeString(submitterNickName);
		out.writeString(submitDate);
		out.writeInt(visible);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Comment createFromParcel(Parcel in) {
			return new Comment(in);
		}

		public Comment[] newArray(int size) {
			return new Comment[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(int submitterId) {
		this.submitterId = submitterId;
	}

	public String getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(String submitDate) {
		this.submitDate = submitDate;
	}

	public String getSubmitterNickName() {
		return submitterNickName;
	}

	public void setSubmitterNickName(String submitterNickName) {
		this.submitterNickName = submitterNickName;
	}

	public int getVisible() {
		return visible;
	}

	public void setVisible(int visible) {
		this.visible = visible;
	}

}
