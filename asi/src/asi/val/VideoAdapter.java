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

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoAdapter extends BaseAdapter {

	List<File> videos;
	LayoutInflater inflater;
	SharedDatas shared;

	public VideoAdapter(Context context, SharedDatas shared, List<File> videos) {
		inflater = LayoutInflater.from(context);
		this.videos = videos;
		this.shared = shared;
	}

	public int getCount() {
		return videos.size();
	}

	public File getItem(int position) {
		return videos.get(position);
	}

	public long getItemId(int positon) {
		return positon;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		//Mise à jour de la vue ou nouvel vue
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.video_listes, null);
			holder.viewTitre = (TextView) convertView.findViewById(R.id.titre);
			holder.viewLength = (TextView) convertView.findViewById(R.id.length);
			holder.viewImage = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		File current = this.videos.get(position);
		Bitmap bit = this.getBitmapForFile(current);
		if(bit!=null){
			holder.viewImage.setImageBitmap(bit);
		}else{
			holder.viewImage.setImageResource(R.drawable.video_menu);
		}
		holder.viewTitre.setText(current.getName().replaceAll("_", " "));
		int leng = (int) (current.length() / 1000);
		holder.viewLength.setText(leng + " ko");
		return convertView;
	}
	
	private Bitmap getBitmapForFile(File video){
		File img = new File(video.getAbsolutePath().replace(
				".mp4", ".jpg"));
		if(img.exists()){
			Bitmap bit = BitmapFactory.decodeFile(img.getAbsolutePath());
			return(bit);
		}else
			return(null);
	}

	private class ViewHolder {
		TextView viewTitre;
		TextView viewLength;
		ImageView viewImage;
	};
}
