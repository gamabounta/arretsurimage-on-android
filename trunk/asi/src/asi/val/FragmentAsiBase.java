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

import android.support.v4.app.Fragment;

public class FragmentAsiBase extends Fragment {
	
	public SharedDatas get_datas() {
		SharedDatas datas = SharedDatas.shared;
		if (datas == null){
			return (new SharedDatas(this.getActivity().getApplicationContext()));
		}
		datas.setContext(this.getActivity().getApplicationContext());
		return datas;
	}

}
