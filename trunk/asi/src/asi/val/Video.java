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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Video implements Parcelable {

	private String dailymotion;
	 
	private String title;

	private int number;
	
	private String image;
	
	private boolean acte;

	public Video(String url) {
		this.dailymotion = "";
		this.title = "ASI";
		this.image="";
		this.number = 0;
		this.set_dailymotion_url(url);
		this.acte = false;
	}

	public Video() {
		this.dailymotion = "";
		this.title = "ASI";
		this.image="";
		this.number = 0;
		this.acte=false;
	}
	
	public void defined_actes(String lignecode){
		Pattern p = Pattern.compile(".*width=\"(\\d+)\".*");
		Matcher m = p.matcher(lignecode);
		if (m.matches()){
			try{
				if (Integer.parseInt(m.group(1)) <= 360){
					this.acte = true;
				}
			}catch(Exception e){
			}
		}
	}

	public String parse_to_url(String asi) {
		Log.d("ASI","Recherche de vidéos");
		Pattern p = Pattern
		//		.compile(".*\\<a href\\=\"(http\\:\\/\\/iphone\\.dailymotion\\.com.*)\" title=\"voir.*");
				.compile(".*src\\=\"(http\\:\\/\\/www\\.dailymotion\\.com\\/embed\\/video.*?)\".*");
		Matcher m = p.matcher(asi);
		if (m.matches()) {
			Log.d("ASI","Recherche de vidéos : vidéos trouvées");
			String s = m.group(1).replace("embed/", "");
			this.set_dailymotion_url(s);
			return (s);
		} else
			return (null);
	}

	public String get_href_link_url() throws Exception {
		//String link = this.get_relink_adress();
		//String link = this.get_download_url();
		String href = "<p style=\"text-align: center;\"><a href=\"" + dailymotion+ "&vidnum="+number
				+ "\" target=\"_blank\">" + "<span><br/>&gt; Cliquez pour voir la vidéo &lt;</span></a></p>";
		// <a href="http://www.bernard-mabille.com/" target="_blank">Bernard
		// Mabille</a>
		return (href);
	}

	public String get_relink_adress() throws Exception {
	BufferedReader in = null;
	String relink = "";
		try {
			HttpURLConnection.getFollowRedirects();
			URL url = new URL(this.get_download_url());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			//conn.setRequestProperty("User-agent", "iPhone");
			in = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
			relink = conn.getURL().toString();
			conn.disconnect();
		} catch (java.net.ProtocolException e) {
			throw new StopException("Problème de connexion");
		} catch (Exception e) {
			throw e;
		}finally {
			// Dans tous les cas on ferme le bufferedReader s'il n'est pas null
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return(relink);
	}

	public String get_download_url() throws Exception {
		//StringBuffer sb = new StringBuffer("");
		String link = "";
		BufferedReader in = null;
		try {
			//URL url = new URL(dailymotion.replace("iphone", "www"));
			URL url = new URL(this.dailymotion);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			//conn.addRequestProperty("User-Agent","Mozilla/5.0 (Android; Mobile; rv:23.0) Gecko/23.0 Firefox/23.0");
			conn.addRequestProperty("User-Agent","Mozilla/5.0 (X11; U; Linux i686; rv:29.0) Gecko/20100101 Firefox/29.0");

			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), "UTF-8"), 8192);
			String ligneCodeHTML;
			//Pattern p = Pattern.compile(".*type=\"video/x-m4v\" href=\"(.*)\" src=.*");
			Pattern p = Pattern.compile(".*video_url.*?(http.*?)%22.*");
			// video_url%22%3A%22http%253A%252F%252Fwww.dailymotion.com%252Fcdn%252FH264-512x384%252Fvideo%252Fxx0vzt.mp4%253Fauth%253D1359695428-b214027f38c08f78662c9fd2dcb555ba%22
			while ((ligneCodeHTML = in.readLine()) != null) {
				//Log.d("dailymotion",ligneCodeHTML);
				Matcher m = p.matcher(ligneCodeHTML);
				if (m.matches()){
					link = m.group(1);
					link = link.replaceAll("%253A", ":");
					link = link.replaceAll("%252F", "/");
					link = link.replaceAll("%253F", "?");
					link = link.replaceAll("%253D", "=");
				}
			}
			conn.disconnect();
		} catch (java.net.ProtocolException e) {
			throw new StopException("Problème de connexion");
		} catch (Exception e) {
			throw e;
		} finally {
			// Dans tous les cas on ferme le bufferedReader s'il n'est pas null
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		// On retourne le string si non vide
		if(link.equals(""))
			throw new StopException("Impossible de récupérer le lien de la vidéo");
		Log.d("ASI","Video = "+link);
		return link;
	}

	public void set_dailymotion_url(String url) {
		String[] parse = url.split("&vidnum=");
		if(parse.length>1){
			this.setNumber(Integer.parseInt(parse[1]));
			this.dailymotion = parse[0];
		} else {
			this.setNumber(0);
			this.dailymotion = url;
		}
		Log.d("ASI","vidurl="+this.dailymotion);
		Log.d("ASI","vidnum="+this.number);
	}
	
	public void setURL(String url) {
		this.dailymotion = url;
	}

	public String getURL() {
		return dailymotion;
	}
	
	public String getLinkURL() {
		return dailymotion+"&vidnum="+number;
	}
	
	public void setTitle(String page_title) {
		if(!page_title.equals(""))
			this.title = page_title;
		else
			this.title ="ASI";
	}

	public String getTitle() {
		return title;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
	public String getTitle_and_number(){
		return(title+" - "+number);
	}
	public String getShortTitle_and_number(){
		String s =title+" - "+number;
		if(title.length()>25){
			s = title.substring(0, 10)+" ... "+title.substring(title.length()-10)+" - "+number;
		}
		return(s);
	}
	
	public boolean isActe() {
		return acte;
	}
	
	public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
		public Video createFromParcel(Parcel in) {
			return new Video(in);
		}

		public Video[] newArray(int size) {
			return new Video[size];
		}
	};

	private Video(Parcel in) {
		this.dailymotion = in.readString();
		this.title = in.readString();
		this.number = in.readInt();
		this.image = in.readString();
		this.acte = in.readByte() != 0;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(this.dailymotion);
		out.writeString(this.title);
		out.writeInt(this.number);
		out.writeString(this.image);
		out.writeByte((byte) (this.acte ? 1 : 0));
	}
}
