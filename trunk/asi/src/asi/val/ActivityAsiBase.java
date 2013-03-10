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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public class ActivityAsiBase extends FragmentActivity {

	protected SharedDatas datas;
	
	protected boolean hasFocus = false;
	
	
	public void onResume() {
		super.onResume();
		hasFocus=true;
		ActionBar actionBar = (ActionBar) this.findViewById(R.id.actionbar);
		if (actionBar != null && actionBar.getNavigationMode()==ActionBar.NAVIGATION_MODE_LIST) {
			Log.d("ASI", "Navigation mode onResume");
			actionBar.setSelectedNavigationItem(0);
		}
	}
	
	public void onPause() {
		super.onPause();
		hasFocus=false;
	}
	

	public SharedDatas get_datas() {
		datas = SharedDatas.shared;
		if (datas == null)
			return (new SharedDatas(this));
		datas.setContext(this);
		return datas;
	}

	public void load_content() {

	}
	
	protected boolean isDualMode(){
		return(this.findViewById(R.id.container_little)!=null);
	}
	
	public void selfReload(View view){
		ActivityAsiBase.this.load_content();
	}

	protected void erreur_loading(String error) {
		Log.e("ASI", error);
		Log.e("lemonde", error);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentReload fragment = FragmentReload.newInstance(error);
		fragmentTransaction.replace(R.id.container, fragment);
		fragmentTransaction.commitAllowingStateLoss();
	}

	public void addNavigationToActionBar(ActionBar actionBar, String title) {
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		String[] option = new String[] { title, "Vidéos téléchargées" , "Paramètres"};
		SpinnerAdapter adapter = new ArrayAdapter<String>(this,
				R.layout.actionbar_list_dropdown_item, option);
		actionBar.setListNavigationCallbacks(adapter,
				new ActionBar.OnNavigationListener() {
					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {
						if (itemPosition == 1) {
							Intent i = new Intent(ActivityAsiBase.this, ActivityVideoOnSd.class);
							ActivityAsiBase.this.startActivity(i);
						} else if (itemPosition == 2){
							Intent i = new Intent(ActivityAsiBase.this, ActivityConfiguration.class);
							ActivityAsiBase.this.startActivity(i);
						}
						return true;
					}
				});
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.actionbar_item_home:
			Intent i = new Intent(Intent.ACTION_VIEW);
			Uri u = Uri.parse("http://www.arretsurimages.net/");
			i.setData(u);
			startActivity(i);
			return true;
		case R.id.itemback:
			this.onBackPressed();
			return true;
		case R.id.video_item:
			i = new Intent(this, ActivityVideoOnSd.class);
			this.startActivity(i);
			return true;
		case R.id.param_item:
			i = new Intent(this, ActivityConfiguration.class);
			this.startActivity(i);
			return true;
		case R.id.info_item:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("À propos");
			builder.setMessage(R.string.apropos);

			builder.setPositiveButton("ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			builder.show();
			return true;
		case R.id.close_item:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
