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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentReload extends FragmentAsiBase {
	TextView title;
	TextView desc;
	
	public static FragmentReload newInstance(String error) {
		FragmentReload fragment = new FragmentReload();
		Bundle bundle = new Bundle();
		bundle.putString("error", error);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.reload_view, container, false);
		title = (TextView) view.findViewById(R.id.error_title);
		desc = (TextView) view.findViewById(R.id.error_tip);
		// Ajouter le titre depuis le Bundle
		title.setText("Une erreur réseau s'est produite lors du chargement.");
		desc.setText(this.getArguments().getString("error"));
		return (view);
	}

}
