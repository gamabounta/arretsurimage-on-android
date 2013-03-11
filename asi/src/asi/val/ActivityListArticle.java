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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import asi.val.FragmentCategorie.OnCategorieSelectedListener;
import asi.val.FragmentListArticle.OnArticleSelectedListener;

public class ActivityListArticle extends ActivityAsiBase implements
		OnArticleSelectedListener, OnCategorieSelectedListener {

	private Categorie cat;

	private ArrayList<Categorie> categories;

	protected ArrayList<Article> articles;

	private get_rss_url asyntask;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("ASI", "ActivityListeArticles onCreate");
		setContentView(R.layout.main);

		categories = this.getIntent().getExtras()
				.getParcelableArrayList("categories");
		// On verifie si il n'y a pas la liste d'article
		if (savedInstanceState != null) {
			Log.d("ASI", "On_create_liste_article_activity_from_old");
			cat = savedInstanceState.getParcelable("cat");
		} else {
			cat = this.getIntent().getExtras().getParcelable("cat");
			this.load_content();
		}
		// dual-mode
		this.loadCategorie();

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		getMenuInflater().inflate(R.menu.list_article_menu_top,
				actionBar.asMenu());
		// actionBar.setTitle(this.getIntent().getExtras().getString("titre").replaceFirst(">",
		// ""));
		this.addNavigationToActionBar(actionBar, cat.getTitre());
		actionBar.setDisplayShowHomeEnabled(true);

		if (cat.getImage() != 0) {
			actionBar.addAction(actionBar.newAction(R.id.actionbar_item_home)
					.setIcon(cat.getImage()));
		}

	}

	public void load_data() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentListArticle fragment = FragmentListArticle
				.newInstance(articles);
		fragmentTransaction.replace(R.id.container, fragment, "articles");
		fragmentTransaction.commitAllowingStateLoss();
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
		// recuperation de l'url des flux rss
		// String url = this.getIntent().getExtras().getString("url");
		asyntask = new get_rss_url();
		asyntask.execute(cat.getUrl());
	}

	public void loadCategorie() {
		if (!this.isDualMode() || categories == null) {
			return;
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentCategorie fragment = (FragmentCategorie) fragmentManager
				.findFragmentByTag("categories");
		if (fragment == null) {
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragment = FragmentCategorie.newInstance(categories);
			fragmentTransaction.add(R.id.container_little, fragment,
					"categories");
			fragmentTransaction.commit();
		}
	}

	public void onSaveInstanceState(final Bundle b) {
		Log.d("ASI", "ActivityListeArticles onSaveInstanceState");
		if (this.articles != null) {
			b.putParcelableArrayList("liste_data", this.articles);
		}
		b.putParcelable("cat", cat);
		super.onSaveInstanceState(b);
	}

	public void onRestoreInstanceState(final Bundle b) {
		Log.d("ASI", "ActivityListeArticles onRestoreInstanceState");
		super.onRestoreInstanceState(b);
		this.articles = b.getParcelableArrayList("liste_data");
		if (this.articles == null) {
			Log.d("lemonde", "Rien a recuperer");
			asyntask = new get_rss_url();
			asyntask.execute(cat.getUrl());
		}
	}

	protected void onDestroy() {
		Log.d("ASI", "ActivityListeArticles onDestroy");
		if (asyntask != null && !asyntask.getStatus().equals(Status.FINISHED)) {
			asyntask.cancel(true);
		}
		super.onDestroy();
	}

	public void onCategorieSelected(ArrayList<Categorie> cats, int pos) {
		//On lance une nouvelle activity si recherche
		if (cats.get(pos).getUrl().equalsIgnoreCase("recherche")) {
			Intent i = new Intent(this, ActivityListArticleRecherche.class);
			i.putExtra("url", "");
			this.startActivity(i);
			Log.d("ASI", "load recherche");
			return;
		}
		//Sinon, on mets à jour
		this.cat = cats.get(pos);
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(cat.getTitre());
		this.addNavigationToActionBar(actionBar, cat.getTitre());
		if (cat.getImage() != 0) {
			actionBar.addAction(actionBar.newAction(R.id.actionbar_item_home)
					.setIcon(cat.getImage()));
		}
		this.articles=null;
		this.load_content();
	}

	public void OnArticleSelected(Article art, int pos) {
		Intent i = new Intent(this, ActivityPage.class);
		i.putExtra("article", art);
		i.putExtra("articles", articles);
		i.putExtra("position", pos);
		this.startActivity(i);
	}

	private void choixMultipleLecture() {
		//Si aucun article, fais rien
		if(articles==null)
			return;
		// Demande si tout marquer ou demarquer ou annuler
		final CharSequence[] items = { "Comme lu", "Comme non Lu", "Annuler" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Marquer tout les articles");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Comme lu")) {
					ActivityListArticle.this.marquerMultipleLecture(true);
				} else if (items[item].equals("Comme non Lu")) {
					ActivityListArticle.this.marquerMultipleLecture(false);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void marquerMultipleLecture(boolean sens) {
		for (int i = 0; i < articles.size(); i++) {
			if (sens)
				this.get_datas().add_articles_lues(articles.get(i).getUri());
			else
				this.get_datas().remove_articles_lues(articles.get(i).getUri());
		}
		if (articles.size() > 0)
			this.load_data();
	}

	protected void load_page(String url, String titre) {
		try {
			Intent i = new Intent(this, ActivityPage.class);
			i.putExtra("url", url);
			i.putExtra("titre", titre);
			this.startActivity(i);
		} catch (Exception e) {
			new DialogError(this, "Chargement de la page", e).show();
		}
		// new page(main.this);
	}

	public void set_articles(ArrayList<Article> arts) {
		this.articles = arts;
		for (Article art : articles)
			art.setColor(cat.getColor());
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.full_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.check_item:
			this.choixMultipleLecture();
			return true;
		case R.id.update_item:
			this.load_content();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class get_rss_url extends AsyncTask<String, Void, String> {
		Boolean loadImage;
		ArrayList<Article> articles;
		ImageCache cache;

		// can use UI thread here
		protected void onPreExecute() {
			loadImage = ActivityListArticle.this.get_datas()
					.isLoadImageEnabled();
			cache = ActivityListArticle.this.get_datas().getImageCache();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				// Si flux RSS
				if (args[0].contains(".rss")) {
					DownloadRSS rss = new DownloadRSS(args[0]);
					rss.get_rss_articles();
					articles = rss.getArticles();
				} else {
					// Sinon chargement article en mode recherche
					PageRecherche re = new PageRecherche(args[0]);
					articles = re.getArticles();
				}
				// Si image activée
				if (loadImage) {
					cache.clearMemoryCache();
					for (Article art : articles) {
						cache.addBitmapToCache(art.getImageUrl());
						if (this.isCancelled())
							return "stop";
					}
				}
			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				return (error);
			}
			return null;
		}

		protected void onPostExecute(String error) {
			if (error == null) {
				ActivityListArticle.this.set_articles(articles);
				ActivityListArticle.this.load_data();
			} else {
				ActivityListArticle.this.erreur_loading(error);
			}
		}
	}

}
