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

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import asi.val.FragmentCategorie.OnCategorieSelectedListener;

public class ActivityCategorie extends ActivityAsiBase implements
		OnCategorieSelectedListener {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// récupération de l'actionBar
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		getMenuInflater()
				.inflate(R.menu.categorie_menu_top, actionBar.asMenu());
		actionBar.setDisplayShowHomeEnabled(true);

		ArrayList<Categorie> cats = this.getCategories(this.getIntent()
				.getExtras().getBoolean("gratuit"));
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		if (savedInstanceState != null) {
			Fragment fragmentOld = fragmentManager
					.findFragmentById(R.id.container_little);
			if (fragmentOld != null)
				fragmentTransaction.remove(fragmentOld);
			Fragment fragmentOld2 = fragmentManager
					.findFragmentById(R.id.container);
			if (fragmentOld2 != null)
				fragmentTransaction.remove(fragmentOld2);
		}
		FragmentCategorie fragment = FragmentCategorie.newInstance(cats);
		if (this.isDualMode())
			fragmentTransaction.add(R.id.container_little, fragment,
					"categories");
		else {
			fragmentTransaction.replace(R.id.container, fragment, "categories");
		}
		fragmentTransaction.commit();
		fragmentManager.popBackStackImmediate();

	}

	private ArrayList<Categorie> getCategories(boolean gratuit) {
		int[] liste = null;
		if (!gratuit) {
			liste = new int[] { R.array.catT, R.array.catE, R.array.catD,
					R.array.catC, R.array.catV, R.array.catR };
		} else {
			liste = new int[] { R.array.catT, R.array.catE, R.array.catD,
					R.array.catC, R.array.catG, R.array.catR };
		}
		ArrayList<Categorie> cats = new ArrayList<Categorie>();
		Resources res = getResources();
		for (int i = 0; i < liste.length; i++) {
			String[] categorie = res.getStringArray(liste[i]);
			Categorie cat = new Categorie();
			cat.setTitre(categorie[0]);
			int img = res.getIdentifier(categorie[1],
					"drawable", this.getPackageName());
			cat.setImage(img);
			cat.setUrl(categorie[2]);
			cat.setSubCat(categorie[3]);
			cat.setColor(categorie[4]);
			cats.add(cat);
		}
		return (cats);

	}
	
	private ArrayList<Categorie> getSubCategories(Categorie cat) {
		String subid = cat.getSubCat();
		if(subid.equals(""))
			return null;
		
		Resources res = getResources();
		int id = res.getIdentifier(subid, "array", this.getPackageName());
		ArrayList<Categorie> cats = new ArrayList<Categorie>();
		
		String[] subcategorie = res.getStringArray(id);
		for (int i = 0; i < subcategorie.length; i += 3) {
			Categorie subcat = new Categorie();
			subcat.setTitre(subcategorie[i]);
			int img = res.getIdentifier(subcategorie[i + 2],
					"drawable", this.getPackageName());
			subcat.setImage(img);
			if(subcategorie[i + 1].contains("rss"))
				subcat.setUrl(subcategorie[i + 1]);
			else
				subcat.setSubCat(subcategorie[i + 1]);
			subcat.setColor(cat.getColor());
			cats.add(subcat);
		}
		return cats;
	}
	
	public void onCategorieSelected(ArrayList<Categorie> cats, int pos) {
		Categorie cat = cats.get(pos);
		ArrayList<Categorie> subcats = this.getSubCategories(cat);
		FragmentManager fragmentManager = getSupportFragmentManager();
		if(subcats!=null){
			//On charge la nouvelle list dans un Fragment
			fragmentManager.popBackStackImmediate();
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			FragmentCategorie fragment = FragmentCategorie.newInstance(subcats);
			fragmentTransaction.replace(R.id.container, fragment, "subcategories");
			fragmentTransaction.addToBackStack("sub");
			fragmentTransaction.commit();
		}else{
			if(cat.getUrl().equalsIgnoreCase("recherche")){
				Intent i = new Intent(this, ActivityListArticleRecherche.class);
				i.putExtra("url", "");
				this.startActivity(i);
				Log.d("ASI","load recherche");
			}else{
				//ListArticle Activity
				Intent i = new Intent(this, ActivityListArticle.class);
				i.putExtra("cat", cat);
				i.putExtra("categories", cats);
				this.startActivity(i);
				Log.d("ASI","load listArticle");
			}
		}
	}

	protected void do_recherche(String titre, String color, String image) {
		new DialogRecherche(this, titre, color, image).show();

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.full_menu, menu);
		return true;
	}

}
