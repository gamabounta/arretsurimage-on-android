/***************************************************************************
    begin                : aug 01 2010
    copyright            : (C) 2010 by Benoit Valot
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

import android.os.Parcel;
import android.os.Parcelable;

public class Article implements Parcelable {

	private String title;

	private String description;

	private String uri;

	private String date;

	private String color;
	
	private String image;

	public Article(String t, String d, String u) {
		this.title = t;
		this.description = d;
		this.uri = u;
		this.date = "";
		this.color = null;
		this.image = "";
	}

	public Article() {
		this.title = "";
		this.description = "";
		this.uri = "";
		this.date = "";
		this.color = null;
		this.image = "";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String des) {
		this.description = des;
	}

	public void setDescriptionOnRSS(String des) {
		des = des.replaceAll("\n", "");
		// des = des.replaceAll("<br />", "");
		String[] parse = des.split("<br />");
		if (parse.length > 1) {
			this.determined_color(parse[0]);
			des = parse[1];
		}
		int fin = des.length();
		if (fin > 100)
			fin = 100;
		des = des.substring(0, fin);
		des = des.replaceFirst(" \\w+$", "");
		des = des + " ...";
		this.description = des;
	}

	public boolean isEmission(){
		if(this.getColor().equals("#3A36FF"))
				return true;
			else
				return(false);
	}
	
	public boolean isChronique(){
		if(this.getColor().equals("#FF398E"))
				return true;
			else
				return(false);
	}
	
	public boolean isArticle(){
		if(this.getColor().equals("#3399FF"))
				return true;
			else
				return(false);
	}
	
	public boolean isViteDit(){
		if(this.getColor().equals("#FEC763"))
				return true;
			else
				return(false);
	}
	

	private void determined_color(String title) {
		// TODO Auto-generated method stub
		// Log.d("ASI","cat= "+title);
		if (title.contains("Vite dit"))
			this.color = "#FEC763";
		else if (title.contains("chronique"))
			this.color = "#FF398E";
		else if (title.contains("mission"))
			this.color = "#3A36FF";
		else if (title.contains("Article"))
			this.color = "#3399FF";
	}

	public void setDescriptionOnRecherche(String html) {
		html = html.replaceAll("\n", "");
		html = html.replaceAll("\\s+", " ");

		int fin = html.length();
		if (fin > 100)
			fin = 100;
		html = html.substring(0, fin);
		html = html.replaceFirst(" \\w+$", "");
		html = html + " ...";
		this.description = html;
	}

	public void setDescriptionOnForum(String html) {
		html = html.replaceAll("%%", "\n");
		this.description = html;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setDate(String dat) {
		// <pubDate>Tue, 31 Aug 2010 19:37:08 +0200</pubDate>
		dat = dat.replaceAll("\\+0\\d+", "");
		this.date = dat.replaceFirst("^.*, ", "");
	}

	public String getDate() {
		return date;
	}

	public void setColor(String color2) {
		if (this.color == null)
			this.color = color2;
	}

	public String getColor() {
		if (this.color == null)
			return ("#ACB7C6");
		return color;
	}

	public void set_color_from_recherche(String rec) {
		if (rec.contains("vite"))
			this.color = "#FEC763";
		else if (rec.contains("chro"))
			this.color = "#FF398E";
		else if (rec.contains("emi"))
			this.color = "#3A36FF";
		else if (rec.contains("doss"))
			this.color = "#3399FF";
	}
	
	public String getImageUrl() {
		return image;
	}

	public void setImageUrl(String imageUrl) {
		imageUrl =  imageUrl.replace("320x240", "140x105");
		imageUrl = imageUrl.replace("player_e", "player_s");
		this.image = imageUrl;
	}
	
	public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
		public Article createFromParcel(Parcel in) {
			return new Article(in);
		}

		public Article[] newArray(int size) {
			return new Article[size];
		}
	};

	private Article(Parcel in) {
		this.title = in.readString();
		this.description = in.readString();
		this.uri = in.readString();
		this.date = in.readString();
		this.color = in.readString();
		this.image = in.readString();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(this.title);
		out.writeString(this.description);
		out.writeString(this.uri);
		out.writeString(this.date);
		out.writeString(this.getColor());
		out.writeString(this.image);
	}


}
