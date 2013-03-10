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

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class FragmentConfiguration extends FragmentAsiBase implements OnItemClickListener {
	protected OnConfigSelectedListener listener;
	private ListView maListViewPerso;
	private int[] liste;

	public interface OnConfigSelectedListener {
		public void OnConfigSelected(int id);
	}

	public static FragmentConfiguration newInstance(int[] liste) {
		FragmentConfiguration fragment = new FragmentConfiguration();
		Bundle bundle = new Bundle();
		bundle.putIntArray("liste_data", liste);
		fragment.setArguments(bundle);
		return fragment;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	this.listener = (OnConfigSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnConfigSelectedListener");
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("lemonde", "onCreateView FragmentConfiguration");
		View view = inflater.inflate(R.layout.list_view, container, false);
		liste = this.getArguments().getIntArray("liste_data");
		maListViewPerso = (ListView) view.findViewById(R.id.listviewperso);
		this.load_content();
		return (view);
	}
	
	public void load_content(){
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		
		Resources res = getResources();
		for (int i = 0; i < liste.length; i++) {
			String[] config = res.getStringArray(liste[i]);
			map = new HashMap<String, String>();
			map.put("titre", config[1]);
			map.put("description", config[2]);
			listItem.add(map);
		}

		// Création d'un SimpleAdapter qui se chargera de mettre les items
		// présent dans notre list (listItem) dans la vue affichageitem
		SimpleAdapter mSchedule = new SimpleAdapter(this.getActivity().getBaseContext(),
				listItem, R.layout.config_listes, new String[] { "titre", "description" }, new int[] { R.id.titre, R.id.description });
		// on ajoute le viewbinder
		//mSchedule.setViewBinder(new bind_color());

		// On attribut à notre listView l'adapter que l'on vient de créer
		maListViewPerso.setAdapter(mSchedule);

		// Enfin on met un écouteur d'évènement sur notre listView
		maListViewPerso.setOnItemClickListener(this);
	}
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		listener.OnConfigSelected(liste[position]);
	}
}
