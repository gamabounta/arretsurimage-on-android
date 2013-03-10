/***************************************************************************
    begin                : jan 01 2013
    copyright            : (C) 2013 by Benoit Valot
    email                : benvalot@gmail.com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 23 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package asi.val;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class FragmentPostComment extends FragmentAsiBase implements OnClickListener {
	private OnPostCommentListener listener;

	private EditText txt_comment;

	public interface OnPostCommentListener {
		public void OnPostComment(String comment);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.listener = (OnPostCommentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnPostCommentListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ASI", "onCreateView FragmentPostComment");
		View view = inflater.inflate(R.layout.post_comment_view, container, false);
		txt_comment = (EditText) view.findViewById(R.id.comment_texte);
		txt_comment.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
				| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		Button comment = (Button) view.findViewById(R.id.post_button);
		comment.setOnClickListener(this);
		this.definedButtonComment(view);
		if(savedInstanceState!=null){
			txt_comment.setText(savedInstanceState.getString("comment"));
		}
		return (view);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("comment", txt_comment.getText().toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View arg0) {
		String comment = txt_comment.getText().toString();
		if(!comment.equals(""))
			listener.OnPostComment(comment);
	}
	
	private void definedButtonComment(View view){
		Button b;
		b = (Button) view.findViewById(R.id.comment_B);
		b.setOnClickListener(
				new Button.OnClickListener() {
					public void onClick(View arg0) {
						txt_comment.append("[b][/b]");
					}
				});
		b = (Button) view.findViewById(R.id.comment_I);
		b.setOnClickListener(
				new Button.OnClickListener() {
					public void onClick(View arg0) {
						txt_comment.append("[i][/i]");
					}
				});
		b = (Button) view.findViewById(R.id.comment_small);
		b.setOnClickListener(
				new Button.OnClickListener() {
					public void onClick(View arg0) {
						txt_comment.append("[small][/small]");
					}
				});
		b = (Button) view.findViewById(R.id.comment_large);
		b.setOnClickListener(
				new Button.OnClickListener() {
					public void onClick(View arg0) {
						txt_comment.append("[large][/large]");
					}
				});
		b = (Button) view.findViewById(R.id.comment_url);
		b.setOnClickListener(
				new Button.OnClickListener() {
					public void onClick(View arg0) {
						txt_comment.append("[url=http://][/url]");
					}
				});
		b = (Button) view.findViewById(R.id.comment_quote);
		b.setOnClickListener(
				new Button.OnClickListener() {
					public void onClick(View arg0) {
						txt_comment.append("[quote=DS][/quote]");
					}
				});
	}

}
