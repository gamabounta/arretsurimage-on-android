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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;
import android.util.Log;

public class DownloadVideoThread extends Thread {

	private Video vid;

	private FileOutputStream out;

	private InputStream in;

	private int totalsize;

	private int size;

	private String error;

	private boolean cancel;
	
	public DownloadVideoThread(Video v){
		vid = v;
		error = null;
		cancel = false;
		in = null;
		out = null;
		totalsize = -1;
		size = 0;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		try {
			// on vérifie que l'on peut enregistrer
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state))
				throw new StopException(
						"La carte SD n'est pas montée, impossible d'enregistrer");

			HttpURLConnection.getFollowRedirects();
			URL url = new URL(vid.get_download_url());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			in = conn.getInputStream();
			this.totalsize = conn.getContentLength();
			Log.d("ASI", "Download " + this.totalsize / 1000);
			if (in == null)
				throw new RuntimeException("stream is null");

			File temp = this.get_download_path();
			temp.createNewFile();
			Log.d("ASI", "vid save=" + temp.getAbsolutePath());

			out = new FileOutputStream(temp);
			byte buf[] = new byte[1024]; //Taille du buffer 128
			do {
				int numread = in.read(buf);
				if (numread <= 0)
					break;
				out.write(buf, 0, numread);
				// comptage du nombre de bytes
				size += numread;
				// Lancer une erreur quand le thread est stoppé
				if (cancel)
					throw new StopException("Stop");
			} while (true);
			conn.disconnect();
			Log.d("ASI", "Final-download=" + size / 1000);
			
			if(!this.is_video_complete()){
				throw new StopException("Video incomplète");
			}
			
		} catch (Exception e) {
			error= e.toString();
			Log.e("ASI", error);
			
			//On efface le fichier si erreur
			File down = this.get_download_path();
			if (down.exists())
				down.delete();
		} finally {
			// Dans tous les cas on ferme le bufferedReader s'il n'est pas null
			this.Stop_buffer();
		}
	}

	private void Stop_buffer() {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	public void Stop_download() {
		Log.d("ASI", "Cancelled_video : "+this.vid.getShortTitle_and_number());
		cancel = true;
		this.set_error("Stop");
	}

	public File get_download_path() {
		File path = new File(Environment.getExternalStorageDirectory() + "/ASI");
		if (!path.exists())
			path.mkdir();
		String correctpath = vid.getTitle();
		correctpath = correctpath.replaceAll("\\W", "_");
		correctpath = correctpath.replaceAll("_+", "_");
		correctpath = correctpath.replaceAll("_$", "");
		File temp = new File(path.getAbsolutePath() + "/" + "ASI-"
				+ correctpath + "-" + vid.getNumber() + ".mp4");
		// + "ASI-"+vid.getTitle()+"-"+vid.getNumber()+".mp4");
		return (temp);
	}

	public String get_pourcentage_download() {
		if (totalsize == -1)
			return ("En préparation");
		int psize = this.size / 1000;
		int ptot = this.totalsize / 1000;
		String pour = psize + " / " + ptot + " ko";
		return (pour);
	}
	
	public int getProgress() {
		if (totalsize <= 0)
			return (0);
		float pour = (float) this.size / (float) this.totalsize *100;
		return ((int)pour);
	}

	public boolean is_video_complete() {
		if (this.size < this.totalsize)
			return false;
		return true;
	}

	public String get_error() {
		return (this.error);
	}

	private void set_error(String er) {
		this.error = er;
	}

	public Video get_download_video() {
		// TODO Auto-generated method stub
		return this.vid;
	}

}
