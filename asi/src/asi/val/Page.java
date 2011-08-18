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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Page extends AsiActivity {
	/** Called when the activity is first created. */

	private WebView mywebview;

	private String pagedata;

	private String page_title;

	protected ArrayList<VideoUrl> videos;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pageview);

		// Récupération de la listview créée dans le fichier main.xml
		mywebview = (WebView) this.findViewById(R.id.WebViewperso);
		// String data = savedInstanceState.getString("page_data");
		// this.onRestoreInstanceState(savedInstanceState);
		Log.d("ASI", "On_create_page_activity");
		if (savedInstanceState != null)
			Log.d("ASI", "On_create_page_activity_from_old");
		else
			this.loadContent();
		// titre de la page
		setPage_title(this.getIntent().getExtras().getString("titre"));
	}

	public void onSaveInstanceState(final Bundle b) {
		Log.d("ASI", "onSaveInstanceState");
		if (this.pagedata != null) {
			b.putString("page_data", this.pagedata);
		}
		super.onSaveInstanceState(b);
	}

	public void onRestoreInstanceState(final Bundle b) {
		Log.d("ASI", "onRestoreInstanceState");
		super.onRestoreInstanceState(b);
		String name = b.getString("page_data");
		if (name != null) {
			this.pagedata = name;
			Log.d("ASI", "Récupération du contenu de la page");
			this.load_page();
		} else {
			Log.d("ASI", "Rien a récupérer");
			//new get_page_content().execute(this.getIntent().getExtras().getString("url"));
			this.loadContent();
		}
		// titre de la page
		setPage_title(this.getIntent().getExtras().getString("titre"));
	}
	
	public void loadContent(){
		new get_page_content().execute(this.getIntent().getExtras()
				.getString("url"));
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if (videos!=null && !videos.isEmpty()) {
			inflater.inflate(R.layout.emission_menu, menu);
		} else {
			inflater.inflate(R.layout.generic_menu, menu);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.item5:
			telecharger_actes();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void load_page() {
		// ac.replaceView(R.layout.pageview);
		try {

			// les définitions de type mime et de l'encodage
			final String mimeType = "text/html";
			final String encoding = "utf-8";

			// on charge le code HTML dans la webview
			mywebview.loadDataWithBaseURL("http://www.arretsurimages.net",
					this.pagedata, mimeType, encoding, null);
			mywebview.setWebViewClient(new myWebViewClient());
			mywebview.setInitialScale((int) (this.get_datas().getZoomLevel()*mywebview.getScale()));

		} catch (Exception e) {
			new ErrorDialog(this, "Chargement de la page", e).show();
		}

	}

	private class myWebViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
				if (url.matches(".*arretsurimages\\.net.*")) {
					if (url.matches(".*mp3.*")) {
						Log.d("ASI", "Audio-" + url);
						Intent intent = new Intent();
						intent.setAction(android.content.Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse(url), "audio/*");
						startActivity(intent);
					} else if (url
							.matches(".*arretsurimages\\.net\\/contenu.*")) {
						Log.d("ASI", "Chargement arrêt sur image");
						Intent i = new Intent(getApplicationContext(),
								Page.class);
						i.putExtra("url", url);
						Page.this.startActivity(i);
					} else if (url.matches(".*arretsurimages\\.net\\/vite.*")) {
						Log.d("ASI", "Chargement arrêt sur image");
						Intent i = new Intent(getApplicationContext(),
								Page.class);
						i.putExtra("url", url);
						Page.this.startActivity(i);
					} else if (url
							.matches(".*arretsurimages\\.net\\/dossier.*")) {
						Log.d("ASI", "Dossier lancé");
						Intent i = new Intent(getApplicationContext(),
								ArticlesListSearch.class);
						i.putExtra("titre", "DOSSIER");
						i.putExtra("color", "#3399FF");
						i.putExtra("image", "articles");
						i.putExtra("url", url);
						Page.this.startActivity(i);
						// Toast.makeText(
						// page.this,
						// "Les liens vers les dossiers ne sont pas pris en charge !",
						// Toast.LENGTH_LONG).show();
					} else if (url
							.matches(".*arretsurimages\\.net\\/recherche.*")) {
						Log.d("ASI", "Recherche lancé");
						Intent i = new Intent(getApplicationContext(),
								ArticlesListSearch.class);
						i.putExtra("titre", "RECHERCHE");
						i.putExtra("color", "#ACB7C6");
						i.putExtra("image", "recherche");
						i.putExtra("url", url);
						Page.this.startActivity(i);

					} else if (url
							.matches(".*arretsurimages\\.net\\/chroniqueur.*")) {
						Log.d("ASI", "Chronique lancé");
						Intent i = new Intent(getApplicationContext(),
								ArticlesListSearch.class);
						i.putExtra("titre", "CHRONIQUES");
						i.putExtra("color", "#FF398E");
						i.putExtra("image", "kro");
						i.putExtra("url", url);
						Page.this.startActivity(i);

					} else if (url.matches(".*arretsurimages\\.net\\/media.*")) {
						//Intent i = new Intent(Intent.ACTION_VIEW);
//						Uri u = Uri.parse(url);
//						i.setData(u);
						Intent i = new Intent(getApplicationContext(),
								PageImage.class);
						i.putExtra("url", url);
						Page.this.startActivity(i);
					} else if (url
							.matches(".*arretsurimages\\.net\\/emission.*")) {
						Toast.makeText(
								Page.this,
								"Ce lien n'est pas visible sur l'application Android",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(
								Page.this,
								"Ce lien n'est pas visible sur l'application Android : ouverture du navigateur",
								Toast.LENGTH_LONG).show();
						Intent i = new Intent(Intent.ACTION_VIEW);
						Uri u = Uri.parse(url);
						i.setData(u);
						startActivity(i);
					}
					return true;
				} else if (url
						.matches(".*http\\:\\/\\/iphone\\.dailymotion\\.com.*")) {
					Log.d("ASI", "Chargement video");
					Page.this.video_choice(url);
					return (true);
				} else {
					Intent i = new Intent(Intent.ACTION_VIEW);
					Uri u = Uri.parse(url);
					i.setData(u);
					startActivity(i);
					return (true);
				}
			} catch (Exception e) {
				new ErrorDialog(Page.this, "Chargement du lien", e).show();
				return false;
			}
		}
	};

	public void setPagedata(String pagedata) {
		// this.pagedata = "<h2>" + page.this.getPage_title() + "</h2>" + "\n"
		// + pagedata;
		this.pagedata = pagedata;

	}

	public void telecharger_actes() {
		final int nb_actes = videos.size();
		// final CharSequence[] items = new CharSequence[nb_actes];
		// final boolean[] items_selected = new boolean[nb_actes];
		//
		// for(int i = 0; i < nb_actes; i++) {
		// items[i] = "Acte " + (i + 1);
		// items_selected[i] = true;
		// }
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Vidéos de l'article");
		// builder.setMultiChoiceItems(items, items_selected, new
		// DialogInterface.OnMultiChoiceClickListener() {
		//
		// public void onClick(DialogInterface dialog, int which, boolean
		// isChecked) {
		// items_selected[which] = isChecked;
		//
		// }
		// });
		builder.setMessage("Voulez-vous lancer le téléchargement des " + nb_actes
				+ " vidéos de cet article ?");
		builder.setNegativeButton("Non",null);
		builder.setPositiveButton("Oui",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						VideoUrl video_selected = null;
						for (int i = 0; i < nb_actes; i++) {
							// if(items_selected[i]) {
							video_selected = videos.get(i);
							video_selected.setNumber(i + 1);
							video_selected.setTitle(page_title);
							Page.this.get_datas().downloadvideo(video_selected);
							// }
						}
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void video_choice(final String url) {
		final CharSequence[] items = { "Visionner", "Télécharger" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Vidéo android");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Visionner")) {
					// Intent i = new Intent(getApplicationContext(),
					// video_view.class);
					// i.putExtra("url", url);
					// startActivity(i);
					new get_video_url().execute(url);
				} else {
					VideoUrl vid = new VideoUrl(url);
					vid.setTitle(page_title);
					Page.this.get_datas().downloadvideo(vid);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public String getPagedata() {
		return pagedata;
	}

	private class get_page_content extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(Page.this,
				this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Chargement...");
			this.dialog.show();
		}

		protected void onCancelled() {
			Log.d("ASI", "onCancelled");
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			// List<String> names =
			// Main.this.application.getDataHelper().selectAll();
			// this.publishProgress("Chargement...");
			try {
				PageLoad page_d = new PageLoad(args[0]);

				Page.this.setPagedata(page_d.getContent());
				Page.this.setVideo(page_d.getVideos());

				Page.this.get_datas().addArticlesRead(args[0]);
			} catch (Exception e) {
				// String error = e.toString() + "\n" + e.getStackTrace()[0]
				// + "\n" + e.getStackTrace()[1];
				String error = e.getMessage();
				return (error);
			}
			return null;
		}

		protected void onPostExecute(String error) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();
			} catch (Exception e) {
				Log.e("ASI", "Erreur d'arrêt de la boîte de dialogue");
			}
			if (error == null) {
				Page.this.load_page();
			} else {
				//new erreur_dialog(page.this, "Chargement de la page", error).show();
				Page.this.onLoadError(error);
			}
		}
	}

	public void setPage_title(String page_title) {
		if (page_title != null) {
			this.page_title = page_title;
			Log.d("ASI", this.page_title);
		} else {
			Log.d("ASI", "pas de titre");
			this.page_title = "Sans titre";
		}
	}

	public void setVideo(ArrayList<VideoUrl> videos2) {
		this.videos = videos2;
	}

	public String getPage_title() {
		return page_title;
	}

	private class get_video_url extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(Page.this,
				this);
		private String valid_url;

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Recupération de l'URL de la vidéo");
			this.dialog.show();
			valid_url = "";
		}

		protected void onCancelled() {
			Log.d("ASI", "onCancelled");
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				VideoUrl vid = new VideoUrl(args[0]);
				vid.setTitle(page_title);
				valid_url = vid.getRelinkAdress();
			} catch (Exception e) {
				// String error = e.toString() + "\n" + e.getStackTrace()[0]
				// + "\n" + e.getStackTrace()[1];
				String error = e.getMessage();
				return (error);
			}
			return null;
		}

		protected void onPostExecute(String error) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();
			} catch (Exception e) {
				Log.e("ASI", "Erreur d'arrêt de la boîte de dialogue");
			}
			if (error == null) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(valid_url), "video/*");
				startActivity(intent);
			} else {
				new ErrorDialog(Page.this, "Récupération de l'URL", error)
						.show();
			}
		}
	}

}
