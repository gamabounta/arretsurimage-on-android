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

import com.markupartist.android.widget.ActionBar;

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
import android.view.MenuItem.OnMenuItemClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ActivityPage extends ActivityAsiBase {
	/** Called when the activity is first created. */

	private WebView mywebview;

	private String pagedata;

	private String page_title;

	private String forum_link;

	protected ArrayList<Video> videos;

	protected ActionBar actionBar;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pageview);

		// titre de la page
		setPage_title(this.getIntent().getExtras().getString("titre"));

		// Récupération de la listview créée dans le fichier main.xml
		mywebview = (WebView) this.findViewById(R.id.WebViewperso);

		this.actionBar = (ActionBar) findViewById(R.id.actionbar);
		this.actionBarInflateMenu(actionBar);

		Log.d("ASI", "On_create_page_activity");
		if (savedInstanceState != null)
			Log.d("ASI", "On_create_page_activity_from_old");
		else
			this.load_content();
	}

	protected void actionBarInflateMenu(ActionBar actionBar) {
		getMenuInflater().inflate(R.menu.page_menu_top, actionBar.asMenu());
		this.addNavigationToActionBar(actionBar, this.getPage_title());
		// actionBar.setDisplayShowHomeEnabled(true);
	}

	public void onSaveInstanceState(final Bundle b) {
		Log.d("ASI", "onSaveInstanceState");
		if (this.pagedata != null) {
			b.putString("page_data", this.pagedata);
		}
		if (this.videos != null && !this.videos.isEmpty()) {
			ArrayList<String> videoSave = new ArrayList<String>();
			for (Video v : this.videos) {
				videoSave.add(v.getTitle());
				videoSave.add("" + v.getNumber());
				videoSave.add(v.getURL());
			}
			b.putStringArrayList("video_links", videoSave);
		}
		if (this.forum_link != null) {
			b.putString("forum_link", forum_link);
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
			this.forum_link = b.getString("forum_link");
			
			ArrayList<String> videoSave = b.getStringArrayList("video_links");
			if (videoSave != null && !videoSave.isEmpty()) {
				this.videos = new ArrayList<Video>();
				Video v;
				for (int i = 0; i < (videoSave.size()-2); i = (i + 3)) {
					v = new Video();
					v.setTitle(videoSave.get(i));
					v.setNumber(Integer.parseInt(videoSave.get(i + 1)));
					v.setURL(videoSave.get(i + 2));
					this.videos.add(v);
				}
			}
			this.load_page();

		} else {
			Log.d("ASI", "Rien a récupérer");
			this.load_content();
		}
		// titre de la page
		setPage_title(this.getIntent().getExtras().getString("titre"));
	}

	public void load_content() {
		new get_page_content().execute(this.getIntent().getExtras()
				.getString("url"));
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if (videos != null && !videos.isEmpty()) {
			inflater.inflate(R.layout.emission_menu, menu);
		} else {
			inflater.inflate(R.layout.full_menu, menu);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.telechargement_video_item:
			telecharger_actes();
			return true;
		case R.id.itemshare:
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.putExtra(Intent.EXTRA_TEXT,
					"Un article interessant sur le site arretsurimage.net :\n"
							+ this.page_title + "\n" + this.getIntent().getExtras().getString("url"));
			emailIntent.setType("text/plain");
			startActivity(Intent.createChooser(emailIntent,
					"Partager cet article"));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void load_page() {
		// ac.replaceView(R.layout.pageview);
		try {
			// On ajoute les boutons si videos ou forum
			if (this.forum_link != null) {
				actionBar.addAction(actionBar
						.newAction()
						.setIcon(R.drawable.forum_menu_top)
						.setOnMenuItemClickListener(
								new OnMenuItemClickListener() {
									public boolean onMenuItemClick(MenuItem item) {
										Intent i = new Intent(
												getApplicationContext(),
												ActivityPageForum.class);
										i.putExtra("titre",
												ActivityPage.this.page_title);
										i.putExtra("color", "#B4DC45");
										i.putExtra("image", "forum");
										i.putExtra("url",
												ActivityPage.this.forum_link);
										ActivityPage.this.startActivity(i);
										return true;
									}
								}));
			}
			if (this.videos != null && !this.videos.isEmpty()) {
				actionBar.addAction(actionBar.newAction()
						.setIcon(R.drawable.telechargement_video_menu_top)
						.setOnMenuItemClickListener(new OnMenuItemClickListener() {
							public boolean onMenuItemClick(MenuItem item) {
								ActivityPage.this.telecharger_actes();
								return true;
							}
						}));
			}

			// les définitions de type mime et de l'encodage
			final String mimeType = "text/html";
			final String encoding = "utf-8";

			// on charge le code HTML dans la webview
			mywebview.loadDataWithBaseURL("http://www.arretsurimages.net",
					this.pagedata, mimeType, encoding, null);
			mywebview.setWebViewClient(new myWebViewClient());
			mywebview
					.setInitialScale((int) (this.get_datas().getZoomLevel() * mywebview
							.getScale()));
			
			mywebview.getSettings().setBuiltInZoomControls(this.get_datas().isZoomEnable());

		} catch (Exception e) {
			new DialogError(this, "Chargement de la page", e).show();
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
								ActivityPage.class);
						i.putExtra("url", url);
						ActivityPage.this.startActivity(i);
					} else if (url.matches(".*arretsurimages\\.net\\/vite.*")) {
						Log.d("ASI", "Chargement arrêt sur image");
						Intent i = new Intent(getApplicationContext(),
								ActivityPage.class);
						i.putExtra("url", url);
						ActivityPage.this.startActivity(i);
					} else if (url
							.matches(".*arretsurimages\\.net\\/dossier.*")) {
						Log.d("ASI", "Dossier lancé");
						Intent i = new Intent(getApplicationContext(),
								ActivityListArticleRecherche.class);
						i.putExtra("titre", "DOSSIER");
						i.putExtra("color", "#3399FF");
						i.putExtra("image", "articles");
						i.putExtra("url", url);
						ActivityPage.this.startActivity(i);
						// Toast.makeText(
						// page.this,
						// "Les liens vers les dossiers ne sont pas pris en charge !",
						// Toast.LENGTH_LONG).show();
					} else if (url
							.matches(".*arretsurimages\\.net\\/recherche.*")) {
						Log.d("ASI", "Recherche lancé");
						Intent i = new Intent(getApplicationContext(),
								ActivityListArticleRecherche.class);
						i.putExtra("titre", "RECHERCHE");
						i.putExtra("color", "#ACB7C6");
						i.putExtra("image", "recherche");
						i.putExtra("url", url);
						ActivityPage.this.startActivity(i);

					} else if (url
							.matches(".*arretsurimages\\.net\\/chroniqueur.*")) {
						Log.d("ASI", "Chronique lancé");
						Intent i = new Intent(getApplicationContext(),
								ActivityListArticleRecherche.class);
						i.putExtra("titre", "CHRONIQUES");
						i.putExtra("color", "#FF398E");
						i.putExtra("image", "kro");
						i.putExtra("url", url);
						ActivityPage.this.startActivity(i);

					} else if (url.matches(".*arretsurimages\\.net\\/media.*")) {
						Intent i = new Intent(getApplicationContext(),
								ActivityPageImage.class);
						i.putExtra("url", url);
						ActivityPage.this.startActivity(i);

					} else if (url.matches(".*arretsurimages\\.net\\/forum.*")) {
						Intent i = new Intent(getApplicationContext(),
								ActivityPageForum.class);
						i.putExtra("titre", ActivityPage.this.page_title);
						i.putExtra("color", "#B4DC45");
						i.putExtra("image", "forum");
						i.putExtra("url", url);
						ActivityPage.this.startActivity(i);

					} else if (url
							.matches(".*arretsurimages\\.net\\/emission.*")) {
						Toast.makeText(
								ActivityPage.this,
								"Ce lien n'est pas visible sur l'application Android",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(
								ActivityPage.this,
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
					ActivityPage.this.video_choice(url);
					return (true);
				} else {
					Intent i = new Intent(Intent.ACTION_VIEW);
					Uri u = Uri.parse(url);
					i.setData(u);
					startActivity(i);
					return (true);
				}
			} catch (Exception e) {
				new DialogError(ActivityPage.this, "Chargement du lien", e)
						.show();
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

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Vidéos de l'article");

		builder.setMessage("Voulez-vous lancer le téléchargement des "
				+ nb_actes + " vidéos de cette article?");
		builder.setNegativeButton("Non", null);
		builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Video video_selected = null;
				for (int i = 0; i < nb_actes; i++) {
					// if(items_selected[i]) {
					video_selected = videos.get(i);
					video_selected.setNumber(i + 1);
					video_selected.setTitle(page_title);
					ActivityPage.this.get_datas().downloadvideo(video_selected);
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
					new get_video_url().execute(url);
				} else {
					Video vid = new Video(url);
					vid.setTitle(page_title);
					ActivityPage.this.get_datas().downloadvideo(vid);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
		// TODO Auto-generated method stub
	}

	public String getPagedata() {
		return pagedata;
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

	public void setForumLink(String link) {
		this.forum_link = link;
	}

	public void setVideo(ArrayList<Video> videos2) {
		// AJoute le bouton si video n'est pas vide
		this.videos = videos2;
	}

	public String getPage_title() {
		return page_title;
	}

	private class get_page_content extends AsyncTask<String, Void, String> {
		private final DialogProgress dialog = new DialogProgress(
				ActivityPage.this, this);

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
			try {
				PageLoading page_d = new PageLoading(args[0]);

				ActivityPage.this.setPagedata(page_d.getContent());
				ActivityPage.this.setVideo(page_d.getVideos());
				ActivityPage.this.setForumLink(page_d.getForum_link());

				ActivityPage.this.get_datas().add_articles_lues(args[0]);
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
				ActivityPage.this.load_page();
			} else {
				// new erreur_dialog(page.this, "Chargement de la page",
				// error).show();
				Log.e("asi", error);
				ActivityPage.this.erreur_loading(error);
			}
		}
	}

	private class get_video_url extends AsyncTask<String, Void, String> {
		private final DialogProgress dialog = new DialogProgress(
				ActivityPage.this, this);
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
				Video vid = new Video(args[0]);
				vid.setTitle(page_title);
				valid_url = vid.get_relink_adress();
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
				new DialogError(ActivityPage.this, "Récupération de l'URL",
						error).show();
			}
		}
	}

}
