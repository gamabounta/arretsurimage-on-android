/***************************************************************************
    begin                : jan 01 2013
    copyright            : (C) 2013 by Benoit Valot
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

import java.net.URLEncoder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class FragmentSearch extends FragmentAsiBase implements OnClickListener {
	private OnSearchListener listener;

	private EditText txt_recherche;
	private CheckBox check_emi;
	private CheckBox check_doss;
	private CheckBox check_chro;
	private CheckBox check_vite;

	public interface OnSearchListener {
		public void onSearch(String url);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.listener = (OnSearchListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSearchListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ASI", "onCreateView FragmentSearch");
		View view = inflater.inflate(R.layout.recherche_view, container, false);
		check_emi = (CheckBox) view.findViewById(R.id.check_emi);
		check_doss = (CheckBox) view.findViewById(R.id.check_doss);
		check_chro = (CheckBox) view.findViewById(R.id.check_chro);
		check_vite = (CheckBox) view.findViewById(R.id.check_vite);
		txt_recherche = (EditText) view.findViewById(R.id.recherche_data);
		Button search = (Button) view.findViewById(R.id.recherche_button);
		search.setOnClickListener(this);
		if(savedInstanceState!=null){
			check_emi.setChecked(savedInstanceState.getBoolean("check_emi"));
			check_doss.setChecked(savedInstanceState.getBoolean("check_doss"));
			check_chro.setChecked(savedInstanceState.getBoolean("check_chro"));
			check_vite.setChecked(savedInstanceState.getBoolean("check_vite"));
			txt_recherche.setText(savedInstanceState.getString("search"));
		}
		return (view);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("check_emi", check_emi.isChecked());
		outState.putBoolean("check_doss", check_doss.isChecked());
		outState.putBoolean("check_chro", check_chro.isChecked());
		outState.putBoolean("check_vite", check_vite.isChecked());
		outState.putString("search", txt_recherche.getText().toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View arg0) {
		try{
		String url = this.defined_url();
		listener.onSearch(url);
		}catch (Exception e){
			Log.e("ASI", "Search bad encoding");
			new DialogError(this.getActivity(), "Recherche", e).show();
		}
	}

	private String defined_url() throws Exception {
		if (txt_recherche.getText().toString().equalsIgnoreCase("")) {
			throw new StopException("Aucun élément à rechercher");
			//Log.d("ASI", "rien d'écrit");
			//txt_recherche.setText("valot");
		}
		StringBuilder donnees = new StringBuilder(
				"http://www.arretsurimages.net/recherche.php");
		donnees.append("?t=0&");
			donnees.append(URLEncoder.encode("chaine", "UTF-8"));
			donnees.append("="
					+ URLEncoder.encode(txt_recherche.getText().toString(),
							"UTF-8") + "&");
		if (check_emi.isChecked())
			donnees.append("in_emission=true&");
		if (check_doss.isChecked())
			donnees.append("in_dossiers=true&");
		if (check_chro.isChecked())
			donnees.append("in_chroniques=true&");
		if (check_vite.isChecked())
			donnees.append("in_vites=true&");
		// in_dossiers=true&in_emission=true&in_chroniques=true&in_vites=true&
		donnees.append("periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&orderby=num");
		// t=0&chaine=hortefeux&in_dossiers=true&is_emission=true&in_chroniques=true&in_vites=true&periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&orderby=num
		return donnees.toString();
	}

}
