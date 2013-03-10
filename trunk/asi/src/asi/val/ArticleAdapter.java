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

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ArticleAdapter extends BaseAdapter {

	List<Article> articles;
	LayoutInflater inflater;
	boolean desc;
	boolean date;
	boolean image;
	SharedDatas shared;

	public ArticleAdapter(Context context, SharedDatas shared, List<Article> articles) {
		inflater = LayoutInflater.from(context);
		this.articles = articles;
		desc=shared.isDescriptionEnabled();
		date = shared.isDateEnabled();
		image = shared.isLoadImageEnabled();
		this.shared = shared;
	}

	public int getCount() {
		return articles.size();
	}

	public Object getItem(int position) {
		return articles.get(position);
	}

	public long getItemId(int positon) {
		return positon;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		//Mise à jour de la vue ou nouvel vue
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.articles_listes, null);
			holder.baseView = convertView.findViewById(R.id.griser);
			holder.viewColor = convertView.findViewById(R.id.color);
			holder.viewTitre = (TextView) convertView.findViewById(R.id.titre);
			holder.viewDate = (TextView) convertView.findViewById(R.id.date);
			holder.viewDescription = (TextView) convertView.findViewById(R.id.description);
			holder.viewImage = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Article current = this.articles.get(position);
		Bitmap bit = shared.getImageCache()
				.getBitmapForFile(current.getImageUrl());
		if(bit!=null && image){
			holder.viewImage.setImageBitmap(bit);
			holder.viewImage.setVisibility(View.VISIBLE);
		}else{
			holder.viewImage.setImageBitmap(null);
			holder.viewImage.setVisibility(View.INVISIBLE);
			Log.d("ASI","Article adapter : Image null for : "+current.getTitle());
		}
		if (shared.contain_articles_lues(current.getUri())) {
			holder.baseView.setBackgroundColor(Color.parseColor("#e7e7e7"));
			holder.viewImage.getDrawable().setColorFilter(Color.parseColor("#e7e7e7"),  Mode.DARKEN);
		} else {
			holder.baseView.setBackgroundColor(Color.parseColor("#ffffff"));
		}
		holder.viewColor.setBackgroundColor(Color.parseColor(current.getColor()));
		holder.viewTitre.setText(current.getTitle());
		if(desc){
			holder.viewDescription.setText(current.getDescription());
		}else{
			holder.viewDescription.setText(null);
			holder.viewDescription.setTextSize(2);
		}
		if(date){
			holder.viewDate.setText(current.getDate());
		}else{
			holder.viewDate.setText(null);
			holder.viewDate.setTextSize(0);
		}
		return convertView;
	}

	private class ViewHolder {
		View baseView;
		View viewColor;
		TextView viewTitre;
		TextView viewDate;
		TextView viewDescription;
		ImageView viewImage;
	};
}
