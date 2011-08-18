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
import java.util.HashMap;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class liste_articles extends asi_activity {
	protected ListView maListViewPerso;

	protected Vector<article> articles;

	protected String color;

	protected int image;

	protected Parcelable state;
	
	public Vector<String> test;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Récupération de la listview créée dans le fichier main.xml
		maListViewPerso = (ListView) findViewById(R.id.listviewperso);
		
		TextView text = (TextView) findViewById(R.id.list_text);
		text.setText(this.getIntent().getExtras().getString("titre")
				.replaceFirst(">", ""));
		this.color = this.getIntent().getExtras().getString("color");
		text.setBackgroundColor(Color.parseColor(color));

		// récupération de l'image
		image = this.getResources().getIdentifier(
				this.getIntent().getExtras().getString("image"), "drawable",
				this.getPackageName());
		ImageView v = (ImageView) findViewById(R.id.cat_image);
		if (image != 0) {
			v.setImageResource(image);
		} else
			v.setImageResource(R.drawable.toutlesite);

		this.load_content();

	}

	public void load_content() {
		// TODO Auto-generated method stub
		// récupération de l'URL des flux RSS
		String url = this.getIntent().getExtras().getString("url");
		new get_rss_url().execute(url);
		// État de la liste view
		state = null;
	}

	public void onResume() {
		super.onResume();
		Log.d("ASI", "liste_article onResume");
		if (articles != null) {
			this.load_data();
		}
		//if (state != null)
			//maListViewPerso.onRestoreInstanceState(state);
	}

	public void onSaveInstanceState(final Bundle b) {
		Log.d("ASI", "liste_article onSaveInstanceState");
		state = maListViewPerso.onSaveInstanceState();
		super.onSaveInstanceState(b);
	}

	public ArrayList<HashMap<String, String>> get_listitem() {
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();

		// chargement des articles
		HashMap<String, String> map;
		for (int i = 0; i < articles.size(); i++) {
			articles.elementAt(i).setColor(color);
			map = new HashMap<String, String>();
			map.put("titre", articles.elementAt(i).getTitle());
			map.put("description", articles.elementAt(i).getDescription());
			map.put("date", articles.elementAt(i).getDate());
			map.put("url", articles.elementAt(i).getUri());
			map.put("color", articles.elementAt(i).getColor());
			if (this.get_datas()
					.contain_articles_lues(articles.elementAt(i).getUri()))
				map.put("griser", "enabled-true");
			else
				map.put("griser", "enabled-false");
			listItem.add(map);
		}
		return listItem;
	}

	public void load_data() {

		// Création de la ArrayList qui nous permettra de remplir la listView
		ArrayList<HashMap<String, String>> listItem = this.get_listitem();

		// Création d'un SimpleAdapter qui se chargera de mettre les items
		// présent dans notre list (listItem) dans la vue affichageitem
		SimpleAdapter mSchedule = new SimpleAdapter(this.getBaseContext(),
				listItem, R.layout.listview, new String[] { "griser", "color",
						"titre", "description", "date" }, new int[] {
						R.id.griser, R.id.color, R.id.titre, R.id.description,
						R.id.date });

		// on ajoute le binder
		mSchedule.setViewBinder(new bind_color());
		
		//on sauve
		state = maListViewPerso.onSaveInstanceState();

		// On attribue à notre listView l'adapter que l'on vient de créer
		maListViewPerso.setAdapter(mSchedule);

		// Enfin on met un écouteur d'évènement sur notre listView
		maListViewPerso.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				// on récupère la HashMap contenant les infos de notre item
				// (titre, description, img)
				HashMap<String, String> map = (HashMap<String, String>) maListViewPerso
						.getItemAtPosition(position);
				if (map.get("url").contains("recherche.php"))
					liste_articles.this.do_on_recherche_item(map.get("url"));
				else
					liste_articles.this.load_page(map.get("url"),
							map.get("titre"));
			}
		});
		maListViewPerso
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@SuppressWarnings("unchecked")
					public boolean onItemLongClick(AdapterView<?> a, View v,
							int position, long id) {
						// TODO Auto-generated method stub
						HashMap<String, String> map = (HashMap<String, String>) maListViewPerso
								.getItemAtPosition(position);
						if (map.get("url").contains("recherche.php"))
							liste_articles.this.do_on_recherche_item(map.get("url"));
						else
							liste_articles.this.choice_action_item(
									map.get("url"), map.get("titre"));

						return false;
					}

				});
		maListViewPerso.onRestoreInstanceState(state);
	}

	private void choice_action_item(final String url, final String titre) {
		final CharSequence[] items = { "Visualiser", "Partager",
				"Marquer comme lu" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titre);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Visualiser")) {
					liste_articles.this.load_page(url, titre);
				} else if (items[item].equals("Partager")) {
					liste_articles.this.partage(url, titre);
				} else {
					liste_articles.this.marquer_comme_lu(url);
				}
				// download_view.this.load_data();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	protected void do_on_recherche_item(String url){
		//à faire uniquement dans les recherches
	}

	private void partage(String url, String titre) {
		// TODO Auto-generated method stub
		try {
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.putExtra(Intent.EXTRA_TEXT,
					"Un article interessant sur le site arretsurimage.net :\n"
							+ titre + "\n" + url);
			emailIntent.setType("text/plain");
			// emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(Intent.createChooser(emailIntent,
					"Partager cet article"));
		} catch (Exception e) {
			new erreur_dialog(this, "Chargement de la page", e).show();
		}
	}

	private void marquer_comme_lu(String url) {
		try {
			this.get_datas().add_articles_lues(url);
			state = maListViewPerso.onSaveInstanceState();
			this.load_data();
			maListViewPerso.onRestoreInstanceState(state);
		} catch (Exception e) {
			new erreur_dialog(this, "Chargement de la page", e).show();
		}
		// new page(main.this);
	}

	private void load_page(String url, String titre) {
		try {
			Intent i = new Intent(this, page.class);
			i.putExtra("url", url);
			i.putExtra("titre", titre);
			this.startActivity(i);
		} catch (Exception e) {
			new erreur_dialog(this, "Chargement de la page", e).show();
		}
		// new page(main.this);
	}

	public void set_articles(Vector<article> art) {
		this.articles = art;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.liste_article_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.item4:
			for(int i =0;i<articles.size();i++)
				this.get_datas().add_articles_lues(articles.elementAt(i).getUri());
			if(articles.size()>0)
				this.marquer_comme_lu(articles.elementAt(0).getUri());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class get_rss_url extends AsyncTask<String, Void, String> {
		private final progress_dialog dialog = new progress_dialog(
				liste_articles.this, this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Chargement...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			// List<String> names =
			// Main.this.application.getDataHelper().selectAll();
			try {
				rss_download rss = new rss_download(args[0]);
				rss.get_rss_articles();
				liste_articles.this.set_articles(rss.getArticles());
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
					Log.e("ASI", "Erreur d'arrêt de la boîte de dialogue");
				}
			}
			if (error == null)
				liste_articles.this.load_data();
			else {
				//new erreur_dialog(liste_articles.this, "Chargement des articles", error).show();
				liste_articles.this.erreur_loading(error);
			}
			// Main.this.output.setText(result);
		}
	}

}