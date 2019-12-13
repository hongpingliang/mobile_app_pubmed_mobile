package com.bim.pubmed;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

import com.bim.core.EParceble;
import com.bim.core.Log;
import com.bim.core.Util;

public class User implements Parcelable {
	private int id;
	private String nickName;
	private String email;
	private int numOfSubmit;

	private static User user;

	public static final String ANONYMOUS = "anonymous";

	public static User getUser(Activity activity) {
		if (user != null) {
			return user;
		}

		int userId = Setting.getInt(activity, Setting.KEY_USER_ID);
		if (userId > 0) {
			user = new User();
			user.setId(userId);
			user.setNickName(Setting.getString(activity,
					Setting.KEY_USER_NICK_NAME));
			user.setEmail(Setting.getString(activity,
					Setting.KEY_USER_EMAIL));
			return user;
		}

		String content = Submitter.loadText(activity,
				Submitter.PAGE_GET_USER_INFO, null, null);
		return parseUser(activity, content);
	}

	public static User parseUser(Activity activity, String content) {
		if (Util.isNull(content)) {
			return null;
		}
		user = parseOnly(content);
		if (user != null) {
			Setting.set(activity, Setting.KEY_USER_ID, user.getId());
			if (Util.isNotNull(user.getNickName())) {
				Setting.set(activity, Setting.KEY_USER_NICK_NAME, user
						.getNickName());
			}
			if (Util.isNotNull(user.getEmail())) {
				Setting.set(activity, Setting.KEY_USER_EMAIL, user
						.getEmail());
			}
		}
		return user;
	}

	private static User parseOnly(String content) {
		if (Util.isNull(content)) {
			return null;
		}
		try {
			JSONObject json = new JSONObject(content);
			int id = EParceble.getInt(json, "id");

			if (id > 0) {
				User user = new User();
				user.parse(json);
				return user;
			}
		} catch (Exception e) {
			Log.d(e);
		}
		return null;
	}

	public User() {

	}

	public String getUserName() {
		if (Util.isNull(nickName)) {
			return ANONYMOUS;
		} else {
			return nickName;
		}
	}

	private User(Parcel in) {
		id = in.readInt();
		nickName = in.readString();
		email = in.readString();
		numOfSubmit = in.readInt();
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(nickName);
		out.writeString(email);
		out.writeInt(numOfSubmit);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void parse(JSONObject json) throws Exception {
		id = EParceble.getInt(json, "id");
		nickName = EParceble.getString(json, "nickName");
		email = EParceble.getString(json, "email");
		numOfSubmit = EParceble.getInt(json, "numOfSubmit");
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getNumOfSubmit() {
		return numOfSubmit;
	}

	public void setNumOfSubmit(int numOfSubmit) {
		this.numOfSubmit = numOfSubmit;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
