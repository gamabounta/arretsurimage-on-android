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
import java.util.ArrayList;
import java.util.Collections;
import com.markupartist.android.widget.ActionBar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ActivityVideoOnSd extends ActivityAsiBase implements
		OnItemClickListener {
	private GridView maGridViewPerso;

	private ArrayList<File> video_sd;

	private final File path = new File(
			Environment.getExternalStorageDirectory() + "/ASI");

	private createThumbnails asyntask;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_view);

		// Récupération de la listview créée dans le fichier main.xml
		maGridViewPerso = (GridView) findViewById(R.id.maGridView);

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		getMenuInflater().inflate(R.menu.back_menu_top, actionBar.asMenu());
		actionBar.setTitle("Vidéos téléchargées");
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.addAction(actionBar.newAction(R.id.actionbar_item_home)
				.setIcon(R.drawable.telechargement));
		video_sd = new ArrayList<File>();
		this.load_content();
		asyntask = new createThumbnails(video_sd, this.get_datas());
		asyntask.execute();
	}

	@Override
	protected void onDestroy() {
		Log.d("ASI", "ActivityPage onDestroy");
		if (asyntask != null && !asyntask.getStatus().equals(Status.FINISHED)) {
			asyntask.cancel(true);
		}
		super.onDestroy();
	}

	public void load_content() {
		video_sd.clear();
		try {
			// on vérifie que l'on peut enregistrer
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state))
				throw new StopException("La carte SD n'est pas montée");

			// recupération de la liste de vidéos dans le dossier
			if (path.exists()) {
				File[] liste = path.listFiles();
				for (int i = 0; i < liste.length; i++) {
					if (liste[i].getName().endsWith(".mp4")){
						video_sd.add(liste[i]);
//						File current = new File(liste[i].getAbsolutePath().replace(".mp4",
//								".jpg"));
//						if(current.exists())
//							current.delete();
					}
				}
			}
		} catch (StopException e) {
			new DialogError(this, "Lecture de la carte SD", e.toString())
					.show();
			// this.update.stop_update();
		} catch (Exception e) {
			new DialogError(this, "Lecture de la carte SD", e).show();
			// this.update.stop_update();
		}
		// On ordonne la liste
		Collections.sort(video_sd);

		this.update_content();

	}

	public void update_content() {
		//on mets à jour depuis l'asynctask
		Parcelable state = maGridViewPerso.onSaveInstanceState();

		// On attribue à notre listView l'adapter que l'on vient de créer
		maGridViewPerso.setAdapter(new VideoAdapter(this, this.get_datas(),
				video_sd));

		// Enfin on met un écouteur d'évènement sur notre listView
		maGridViewPerso.setOnItemClickListener(this);

		maGridViewPerso.onRestoreInstanceState(state);
	}

	private void do_on_video(final File vid) throws Exception {
		final CharSequence[] items = { "Visualiser", "Supprimer" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(vid.getName().replaceAll("_", ""));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Visualiser")) {
					// démarrer la video
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(vid), "video/*");
					ActivityVideoOnSd.this.startActivity(intent);
				} else if (items[item].equals("Supprimer")) {
					if (vid.exists())
						vid.delete();
					File img = new File(vid.getAbsolutePath().replace(".mp4",
							".jpg"));
					if (img.exists())
						img.delete();
					Toast.makeText(ActivityVideoOnSd.this, "Fichier supprimé",
							Toast.LENGTH_SHORT).show();
					ActivityVideoOnSd.this.load_content();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		try {
			ActivityVideoOnSd.this.do_on_video(video_sd.get(position));
		} catch (Exception e) {
			new DialogError(this, "Traitement de la vidéo", e).show();
		}
	}

	private class createThumbnails extends AsyncTask<Void, Void, Integer> {

		private ArrayList<File> videos;
		private SharedDatas shared;

		public createThumbnails(ArrayList<File> videos, SharedDatas shared) {
			this.videos = videos;
			this.shared = shared;
		}

		// // can use UI thread here
		protected void onPreExecute() {
		}

		protected void onCancelled() {
			Log.d("lemonde", "On Cancel");
		}

		// automatically done on worker thread (separate from UI thread)
		protected Integer doInBackground(Void... args) {
			int count = 0;
			for (File vid : videos) {
				Bitmap bit = null;
				File current = new File(vid.getAbsolutePath().replace(".mp4",
						".jpg"));
				if (vid.exists() && !current.exists())
					bit = shared.createVideoThumbnail(vid.getAbsolutePath());
				if (bit != null) {
					try {
						current.createNewFile();
						FileOutputStream fos = new FileOutputStream(current);
						bit.compress(CompressFormat.JPEG, 100, fos);
						fos.flush();
						fos.close();
						count++;
						// On mets à jour l'interface
						Log.d("ASI", "Thumbnails video download : " + count);
						if(this.isCancelled())
							return 0;
						this.publishProgress();
					} catch (Exception e) {
						Log.e("ASI", "Thumbnails video : " + vid.getName());
					}
				}
				// this.publishProgress();
			}
			return count;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			ActivityVideoOnSd.this.update_content();
		}

		protected void onPostExecute(Integer vid) {
			// On fait rien
			Log.d("ASI", "Final Thumbnails video download : " + vid);
		}
	}
}
