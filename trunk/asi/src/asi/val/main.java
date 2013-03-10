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

import com.markupartist.android.widget.ActionBar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import asi.val.FragmentLogin.OnAuthenfiedListener;

public class main extends ActivityAsiBase implements
		OnAuthenfiedListener {

	private boolean autologin;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Log.d("ASI", "Main create");

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		getMenuInflater().inflate(R.menu.param_menu_top, actionBar.asMenu());
		actionBar.setDisplayShowHomeEnabled(true);

		this.button_load();

		// autologin activé
		this.autologin = this.get_datas().isAutologin();
		//this.autologin = false;
		// on teste la version de l'application, si mise à jour, alors ajout
		// d'un message sur les nouveautés
		// int old_version = settings.getInt("old_version", 34);
		// if (old_version < 43) {
		// this.show_news_dialog();
		// SharedPreferences.Editor editor = settings.edit();
		// editor.putInt("old_version", 43);
		// editor.commit();
		// this.autologin=false;
		// }
	}

	// private void show_news_dialog() {
	// // TODO Auto-generated method stub
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// builder.setTitle("Nouveautés");
	// builder.setMessage(R.string.news);
	// builder.setCancelable(false);
	// builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	// dialog.cancel();
	// }
	// });
	// builder.create().show();
	// }

	public void onStart() {
		super.onStart();
		// démarrage de l'autologin
		if (!this.get_datas().getCookies().equals("phorum_session_v5=deleted")
				&& this.autologin) {
			// this.get_datas().setCookies(Cookies);
			this.load_page(false);
			Toast.makeText(this, this.get_datas().getUsername() + " connecté.",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void connect_to_gratuit() {
		// this.setCookies("phorum_session_v5=deleted");
		this.get_datas().setAuthentification("", "",
				"phorum_session_v5=deleted");
		this.load_page(true);
	}

	public void OnAuthenfied() {
		this.load_page(false);
	}

	private void load_page(boolean gratuit) {
		Intent i = new Intent(this, ActivityCategorie.class);
		i.putExtra("gratuit", gratuit);
		this.startActivity(i);
		if (this.datas.isAutologin())
			this.finish();
	}

	private void button_load() {
		Button button_gratuit = (Button) findViewById(R.id.gratuit_button);

		button_gratuit.setOnClickListener(new OnClickListener() {
			public void onClick(View viewParam) {
				main.this.connect_to_gratuit();
			}
		});

		Button button_abonnement = (Button) findViewById(R.id.abonnement_button);

		button_abonnement.setOnClickListener(new OnClickListener() {
			public void onClick(View viewParam) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				Uri u = Uri
						.parse("http://www.arretsurimages.net/abonnements.php");
				i.setData(u);
				startActivity(i);
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.info_menu, menu);
		return true;
	}
}
