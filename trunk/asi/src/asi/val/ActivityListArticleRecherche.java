package asi.val;

import java.util.ArrayList;

import com.markupartist.android.widget.ActionBar;

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
import android.view.MenuItem.OnMenuItemClickListener;
import asi.val.FragmentListArticle.OnArticleSelectedListener;
import asi.val.FragmentSearch.OnSearchListener;


public class ActivityListArticleRecherche extends ActivityAsiBase implements
		OnArticleSelectedListener, OnSearchListener {

	protected ArrayList<Article> articles;

	private get_recherche_url asyntask;

	private String searchUrl;

	private ActionBar actionBar;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ASI", "ActivityListArticleRecherche onCreate");
		setContentView(R.layout.main);
		
		// Premier chargement
		if (savedInstanceState == null) {
			searchUrl = this.getIntent().getExtras().getString("url");
			if(searchUrl.equals(""))
				searchUrl=null;
			if(searchUrl!=null)
				this.load_content();
			else 
				this.loadSearch();
		}

		actionBar = (ActionBar) findViewById(R.id.actionbar);
//		getMenuInflater().inflate(R.menu.list_article_menu_top,
//				actionBar.asMenu());
		this.addNavigationToActionBar(actionBar, "RECHERCHE");
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.addAction(actionBar.newAction(R.id.actionbar_item_home)
				.setIcon(R.drawable.recherche));
		
		// dual-mode ou  bouton
		this.loadSearchDual();
	}
	
	public void onSaveInstanceState(final Bundle b) {
		Log.d("ASI", "ActivityListArticleRecherche onSaveInstanceState");
		if (this.articles != null) {
			b.putParcelableArrayList("liste_data", this.articles);
		}
		if(searchUrl != null)
			b.putString("url", searchUrl);
		super.onSaveInstanceState(b);
	}

	public void onRestoreInstanceState(final Bundle b) {
		Log.d("ASI", "ActivityListArticleRecherche onRestoreInstanceState");
		super.onRestoreInstanceState(b);
		this.articles = b.getParcelableArrayList("liste_data");
		this.searchUrl = b.getString("url");
		if (this.searchUrl !=null && this.articles == null) {
			Log.d("lemonde", "arret du chargement");
			asyntask = new get_recherche_url();
			asyntask.execute(searchUrl);
		}
	}

	protected void onDestroy() {
		Log.d("ASI", "ActivityListArticleRecherche onDestroy");
		if (asyntask != null && !asyntask.getStatus().equals(Status.FINISHED)) {
			asyntask.cancel(true);
		}
		super.onDestroy();
	}

	public void load_content() {
		if (asyntask != null && !asyntask.getStatus().equals(Status.FINISHED)) {
			asyntask.cancel(true);
		}
		if (this.searchUrl == null)
			return;
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentLoad fragment = new FragmentLoad();
		fragmentTransaction.replace(R.id.container, fragment);
		fragmentTransaction.commit();
		// recuperation de l'url des flux rss
		// String url = this.getIntent().getExtras().getString("url");
		asyntask = new get_recherche_url();
		asyntask.execute(searchUrl);
	}

	public void loadSearchDual() {
		if (this.isDualMode()) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentSearch fragment = (FragmentSearch) fragmentManager
					.findFragmentByTag("search");
			if (fragment == null) {
				FragmentTransaction fragmentTransaction = fragmentManager
						.beginTransaction();
				fragment = new FragmentSearch();
				fragmentTransaction.add(R.id.container_little, fragment,
						"search");
				fragmentTransaction.commit();
			}
		} else {
			actionBar.addAction(actionBar.newAction()
					.setIcon(R.drawable.recherche_menu_top)
					.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							ActivityListArticleRecherche.this.loadSearch();
							return true;
						}
					}));
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

	public void loadSearch() {
		this.searchUrl=null;
		if (this.isDualMode())
			return;
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentSearch fragment = new FragmentSearch();
		fragmentTransaction.replace(R.id.container, fragment, "search_principal");
		fragmentTransaction.commit();
	}

	protected void do_on_recherche_item(String url) {
		// à faire uniquement dans les recherches
		new get_recherche_url().execute(url);
	}

	public void set_articles(ArrayList<Article> art) {
		// if (articles == null)
		this.articles = art;
		// else {
		// if (articles.size() > 0)
		// this.articles.get(articles.size() - 1);
		// for (int i = 0; i < art.size(); i++) {
		// articles.add(art.get(i));
		// }
		// }
	}

	@Override
	public void onSearch(String url) {
		this.searchUrl = url;
		this.load_content();
	}

	@Override
	public void OnArticleSelected(Article art, int pos) {
		if (art.getUri().contains("recherche.php"))
			this.onSearch(art.getUri());
		else {
			Intent i = new Intent(this, ActivityPage.class);
			i.putExtra("article", art);
			i.putExtra("articles", articles);
			i.putExtra("position", pos);
			this.startActivity(i);
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.full_menu, menu);
		return true;
	}
	

	private class get_recherche_url extends AsyncTask<String, Void, String> {
		Boolean loadImage;
		ArrayList<Article> articles;
		ImageCache cache;

		// can use UI thread here
		protected void onPreExecute() {
			loadImage = ActivityListArticleRecherche.this.get_datas()
					.isLoadImageEnabled();
			cache = ActivityListArticleRecherche.this.get_datas()
					.getImageCache();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				PageRecherche re = new PageRecherche(args[0]);
				articles = re.getArticles();
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
				ActivityListArticleRecherche.this.set_articles(articles);
				ActivityListArticleRecherche.this.load_data();
			} else {
				ActivityListArticleRecherche.this.erreur_loading(error);
			}
		}
	}

}
