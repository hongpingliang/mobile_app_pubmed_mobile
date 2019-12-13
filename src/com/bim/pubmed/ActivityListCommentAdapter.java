package com.bim.pubmed;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bim.core.Util;

public class ActivityListCommentAdapter extends BaseAdapter {
	private List<Comment> commentList;
	protected LayoutInflater inflater;

	public ActivityListCommentAdapter(ActivityListComment activity) {
		this.inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		if (commentList == null) {
			return 0;
		}
		return commentList.size();
	}

	public Object getItem(int position) {
		return commentList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View rowView, ViewGroup parent) {
		final Comment comment = (Comment) commentList.get(position);

		if (rowView == null) {
			rowView = inflater
					.inflate(R.layout.list_comment_row, parent, false);
		}

		TextView mName = (TextView) rowView
				.findViewById(R.id.comment_row_submitter_label);
		String name = comment.getSubmitterNickName();
		if (Util.isNull(name)) {
			name = User.ANONYMOUS;
		}
		mName.setText(name);

		TextView mDate = (TextView) rowView
				.findViewById(R.id.comment_row_date_submitted_label);
		mDate.setText(comment.getSubmitDate());

		TextView mComment = (TextView) rowView
				.findViewById(R.id.comment_row_comment_label);
		mComment.setText(comment.getComment());

		return rowView;
	}

	public List<Comment> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<Comment> commentList) {
		this.commentList = commentList;
	}

}
