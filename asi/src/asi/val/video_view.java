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

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.MediaController;
import android.widget.VideoView;

public class video_view extends Activity {
	private String url_ok;

	private int current_position;

	private VideoView video;

	private MediaController control;
	
	private loading_video loading;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ASI", "video_Oncreate");
		setContentView(R.layout.video);

		// Récupération de la listview créée dans le fichier main.xml
		// mywebview = (WebView) this.findViewById(R.id.WebViewperso);
		video = (VideoView) findViewById(R.id.myvideo);
		control = new MediaController(this);
		new get_video_url().execute(this.getIntent().getExtras().getString(
				"url"));
		current_position = 0;
		loading = new loading_video();

	}

	public void load_video() {
		try {
			video.setMediaController(control);
			video.setVideoURI(Uri.parse(url_ok));
			video.requestFocus();
			video.setSaveEnabled(true);
			video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
	            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
	            	new erreur_dialog(video_view.this,"Impossible de lire la vidéo","").show();
	    			if(loading.getStatus().toString().equals("RUNNING"))
	    				loading.cancel(true);
	                Log.e("ASI", "MediaPlayer.onError: what=" + what + " extra=" + extra);
	                return(true);
	            }
	        });
			if (video.isPlaying()) {
				video.pause();
				video.stopPlayback();
			}
			video.start();
			loading.execute(video);

		} catch (Exception e) {
			new erreur_dialog(this,"Chargement de la vidéo",e).show();
		}

	}

	//	
	public void onResume() {
		Log.d("ASI", "onResume");
		super.onResume();
		video.seekTo(current_position);
		video.start();
		Log.d("ASI", "Position : " + current_position);
		// new loading_video().execute(video);
	}

	//	
	public void onSaveInstanceState(final Bundle b) {
		Log.d("ASI", "onSaveInstanceState");
		current_position = video.getCurrentPosition();
		Log.d("ASI", "Position : " + current_position);
		super.onSaveInstanceState(b);
	}

	public void onPause() {
		if (video.isPlaying()) {
			video.pause();
			video.stopPlayback();
		}
		// current_position=video.getCurrentPosition();
		// Log.d("ASI","Position : "+current_position);
		super.onPause();
		Log.d("ASI", "video_Onpause");
	}

	// public void onBackPressed() {
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something on back.
			if (video.isPlaying()) {
				video.pause();
			}
			video.stopPlayback();
			this.finish();
			// super.onBackPressed();
			if(loading.getStatus().toString().equals("RUNNING"))
				loading.cancel(true);
			
			Log.d("ASI", "video_BackPressed");
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void set_valid_url(String url) {
		this.url_ok = url;
	}

	private class get_video_url extends AsyncTask<String, Void, String> {
		private final progress_dialog dialog = new progress_dialog(
				video_view.this,this);

		// // can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Recuperation de l'url de la video");
			this.dialog.show();
		}
		
		protected void onCancelled() {
			Log.d("ASI", "on cancelled");
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				video_url vid = new video_url(args[0]);
				String s = vid.get_relink_adress();
				video_view.this.set_valid_url(s);
			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				return (error);
			}
			return null;
		}

		protected void onPostExecute(String error) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();
			} catch (Exception e) {
				Log.e("ASI", "Erreur d'arret de la boite de dialog");
			}
			if (error == null)
				video_view.this.load_video();
			else {
				new erreur_dialog(video_view.this,"Récupération de l'url",error).show();
			}
		}
	}

	private class loading_video extends AsyncTask<VideoView, Void, String> {
		private final progress_dialog dialog = new progress_dialog(
				video_view.this,this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Vidéo en préparation...");
			this.dialog.show();
		}
		
		protected void onCancelled() {
			if (this.dialog.isShowing()) {
				try {
					this.dialog.dismiss();
				} catch (Exception e) {
					Log.e("ASI", "Erreur d'arret de la boite de dialog");
				}
			}
		}
		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(VideoView... args) {
			// List<String> names =
			// Main.this.application.getDataHelper().selectAll();
			try {
				VideoView vid = args[0];
				while (!vid.isPlaying()) {
					Thread.sleep(1000L);
					// this.wait(1000);
					// on attend la fin de chargement!!
				}

			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				return (error);
			}
			return null;
		}

		protected void onPostExecute(String error) {
			if (this.dialog.isShowing()) {
				try {
					this.dialog.dismiss();
				} catch (Exception e) {
					Log.e("ASI", "Erreur d'arret de la boite de dialog");
				}
			}
			if (error != null)
				new erreur_dialog(video_view.this,"Chargement de la vidéo",error).show();
		}
	}
}
