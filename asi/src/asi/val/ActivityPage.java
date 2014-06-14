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
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import asi.val.FragmentListArticle.OnArticleSelectedListener;
import asi.val.FragmentPage.OnLinkSelectedListener;

public class ActivityPage extends ActivityAsiBase implements
		OnArticleSelectedListener, OnLinkSelectedListener {
	/** Called when the activity is first created. */

	private String pagedata;

	private get_page_content asyntask;

	protected Article article;

	private ArrayList<Article> articles;

	protected String forum_link;

	protected ArrayList<Video> videos;

	private ActionBar actionBar;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ASI", "ActivityPage onCreate");
		articles = this.getIntent().getExtras()
				.getParcelableArrayList("articles");

		setContentView(R.layout.main);

		if (savedInstanceState != null) {
			Log.d("ASI", "On_create_page_activity_from_old");
			article = savedInstanceState.getParcelable("article");
		} else {
			article = this.getIntent().getExtras().getParcelable("article");
			this.load_content();
		}
		// Chargement des articles si dual-mode et pas déjà instancier
		this.loadArticles();

		actionBar = (ActionBar) findViewById(R.id.actionbar);
		getMenuInflater().inflate(R.menu.page_menu_top, actionBar.asMenu());
		actionBar.setDisplayShowHomeEnabled(true);
		this.addNavigationToActionBar(actionBar, article.getTitle());

	}

	public void loadArticles() {
		if (!this.isDualMode() || this.articles == null) {
			Log.d("lemonde", "ActivityPage Without FragmentList");
			return;
		}
		Log.d("ASI", "ActivityPage Load FragmentList");
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentListArticle fragment = (FragmentListArticle) fragmentManager
				.findFragmentByTag("articles");
		if (fragment == null) {
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragment = FragmentListArticle.newInstance(articles);
			fragmentTransaction.replace(R.id.container_little, fragment,
					"articles");
			fragmentTransaction.commit();
		}
	}

	public void onSaveInstanceState(final Bundle b) {
		Log.d("ASI", "ActivityPage onSaveInstanceState");
		if (this.pagedata != null) {
			b.putString("page_data", this.pagedata);
			//Ajout video et forum
			if (this.videos != null && !this.videos.isEmpty()) {
				b.putParcelableArrayList("video_links", this.videos);
			}
			if (this.forum_link != null) {
				b.putString("forum_link", forum_link);
			}
		}
		b.putParcelable("article", article);
		super.onSaveInstanceState(b);
	}

	public void onRestoreInstanceState(final Bundle b) {
		Log.d("ASI", "ActivityPage onRestoreInstanceState");
		super.onRestoreInstanceState(b);
		String data = b.getString("page_data");
		if (data != null) {
			this.pagedata = data;
			Log.d("ASI", "Recuperation du content de la page");
			this.forum_link = b.getString("forum_link");
			this.videos = b.getParcelableArrayList("video_links");
			//on ajoute les boutons si nécessaire
			this.loadActionbarButton();
		} else {
			Log.d("ASI", "Rien a recuperer");
			asyntask = new get_page_content();
			asyntask.execute(article.getUri());
		}
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
		// On arrete l'ancien
		if (asyntask != null && !asyntask.getStatus().equals(Status.FINISHED)) {
			asyntask.cancel(true);
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentLoad fragment = new FragmentLoad();
		fragmentTransaction.replace(R.id.container, fragment);
		fragmentTransaction.commit();

		asyntask = new get_page_content();
		asyntask.execute(article.getUri());
	}

	public void load_data() {
		this.loadActionbarButton();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentPage fragment = FragmentPage.newInstance(this.pagedata);
		fragmentTransaction.replace(R.id.container, fragment, "page");
		fragmentTransaction.commitAllowingStateLoss();
	}

	public void loadActionbarButton() {
		if (this.forum_link != null) {
			actionBar.addAction(actionBar.newAction()
					.setIcon(R.drawable.forum_menu_top)
					.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							ActivityPage.this
									.onForumLink(ActivityPage.this.forum_link);
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
	}

	public void setPagedata(String data) {
		this.pagedata = data;
	}

	public String getPagedata() {
		return pagedata;
	}

	public void setForumLink(String link) {
		this.forum_link = link;
	}

	public void setVideo(ArrayList<Video> videos2) {
		// AJoute le bouton si video n'est pas vide
		if (videos2 != null){
			ArrayList<Video> tmp = new ArrayList<Video>();
			boolean isDlActe = ActivityPage.this.get_datas().isDlVideoActe();
			for (Video vid : videos2){
				if (vid.isActe()==isDlActe){
					tmp.add(vid);
				}
			}
			if (tmp.isEmpty()){
				this.videos = videos2;
			}else{
				this.videos = tmp;
			}
		}
	}

	public void telecharger_actes() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Vidéos de l'article");

		builder.setMessage("Voulez-vous lancer le téléchargement des "
				+ videos.size() + " vidéos de cette article?");
		builder.setNegativeButton("Non", null);
		builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				for (Video vid : videos) {
					vid.setTitle(article.getTitle());
					Intent intent = new Intent(getApplicationContext(),
							ServiceDownload.class);
					intent.putExtra("dlsync", ActivityPage.this.get_datas()
							.isDlSync());
					intent.putExtra("video", vid);
					ActivityPage.this.startService(intent);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void OnArticleSelected(Article art, int pos) {
		this.article = art;
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(article.getTitle());
		this.addNavigationToActionBar(actionBar, article.getTitle());
		this.pagedata=null;
		this.forum_link=null;
		this.videos=null;
		while(this.actionBar.getActionCount()>1)
			this.actionBar.removeActionAt(1);
		this.load_content();
	}

	public void onForumLink(String url) {
		Intent i = new Intent(getApplicationContext(), ActivityPageForum.class);
		Categorie cat = new Categorie();
		cat.setTitre(article.getTitle());
		cat.setColor("#B4DC45");
		cat.setUrl(url);
		int img = this.getResources().getIdentifier("forum", "drawable",
				this.getPackageName());
		cat.setImage(img);
		i.putExtra("forum", cat);
		ActivityPage.this.startActivity(i);
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
							+ this.article.getTitle() + "\n"
							+ this.article.getUri());
			emailIntent.setType("text/plain");
			startActivity(Intent.createChooser(emailIntent,
					"Partager cet article"));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class get_page_content extends AsyncTask<String, Void, String> {
		String data;
		ArrayList<Video> videos;
		String forumLink;

		// // can use UI thread here
		protected void onPreExecute() {
		}

		protected void onCancelled() {
			Log.d("lemonde", "On Cancel");
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				PageLoading page_d = new PageLoading(args[0]);
				data = page_d.getContent();
				videos = page_d.getVideos();
				forumLink = page_d.getForum_link();
			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				return (error);
			}
			return null;
		}

		protected void onPostExecute(String error) {
			if (error == null) {
				ActivityPage.this.setPagedata(data);
				ActivityPage.this.setVideo(videos);
				ActivityPage.this.setForumLink(forumLink);
				ActivityPage.this.load_data();
			} else {
				ActivityPage.this.erreur_loading(error);
			}
		}
	}

	public void onVideoLink(final String url) {
		final CharSequence[] items = { "Visionner", "Télécharger", "Aller sur Dailymotion" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Vidéo android");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Visionner")) {
					new get_video_url().execute(url);
				} else if (items[item].equals("Télécharger")) {
					Log.d("ASI", "DownloadVideo");
					Intent i = new Intent(ActivityPage.this
							.getApplicationContext(), ServiceDownload.class);
					i.putExtra("dlsync", ActivityPage.this.get_datas()
							.isDlSync());
					Video vid = new Video(url);
					vid.setTitle(ActivityPage.this.article.getTitle());
					i.putExtra("video", vid);
					ActivityPage.this.startService(i);
				} else {
					Intent i = new Intent(Intent.ACTION_VIEW);
					Video vid2 = new Video(url);
					Uri u = Uri.parse(vid2.getURL());
					i.setData(u);
					startActivity(i);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
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
				vid.setTitle("");
				valid_url = vid.get_relink_adress();
			} catch (Exception e) {
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
