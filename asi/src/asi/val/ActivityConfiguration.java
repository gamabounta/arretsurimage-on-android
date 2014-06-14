package asi.val;

import asi.val.FragmentConfiguration.OnConfigSelectedListener;
import asi.val.FragmentLogin.OnAuthenfiedListener;

import com.markupartist.android.widget.ActionBar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class ActivityConfiguration extends ActivityAsiBase implements
		OnConfigSelectedListener, OnAuthenfiedListener {

	private int[] liste = new int[] { R.array.conf_login,
			R.array.conf_autologin, R.array.conf_dlsync, R.array.conf_dlvideo,
			R.array.conf_view_image, R.array.conf_view_desc,
			R.array.conf_view_date, R.array.conf_zoom_enable,
			R.array.conf_zoom_level };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("lemonde", "ActivityConfiguration Created");
		setContentView(R.layout.configuration);

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		getMenuInflater().inflate(R.menu.back_menu_top, actionBar.asMenu());
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setTitle("Préférences");

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
		FragmentConfiguration fragment = FragmentConfiguration
				.newInstance(liste);
		fragmentTransaction.add(R.id.container, fragment, "liste");
		fragmentTransaction.commit();
		fragmentManager.popBackStackImmediate();
	}

	private void loadLogin(String title) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentLogin fragment = new FragmentLogin();
		if (this.isDualMode()) {
			fragmentTransaction.add(R.id.container_little, fragment);
		} else {
			fragmentTransaction.addToBackStack("login");
			fragmentTransaction.replace(R.id.container, fragment);
		}
		fragmentTransaction.commit();
	}

	public void OnConfigSelected(int id) {
		// remove older fragment
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragmentOld = fragmentManager
				.findFragmentById(R.id.container_little);
		if (fragmentOld != null) {
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			fragmentTransaction.remove(fragmentOld);
			fragmentTransaction.commit();
		}
		// Select resources
		Resources res = getResources();
		String[] config = res.getStringArray(id);
		String title = config[1];
		Log.d("lemonde", "View config " + title + ",id=" + id);
		switch (id) {
		case R.array.conf_login:
			this.loadLogin(title);
			return;
		case R.array.conf_zoom_level:
			this.zoomLevel(title);
			return;
		case R.array.conf_dlsync:
			this.setPreferenceDownload(title);
			return;
		case R.array.conf_dlvideo:
			this.setPreferenceActes(title);
			return;
		default:
			// Si aucun, simplement boolean
			this.setPreferenceBoolean(title, config[0]);
		}
	}

	private void setPreferenceBoolean(String title, String key) {
		final CharSequence[] items = { "Oui", "Non" };
		final String finalKey = key;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		int posi = 0;
		if (!this.get_datas().getBooleanPreference(key))
			posi = 1;
		builder.setSingleChoiceItems(items, posi,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						boolean check = items[item].equals("Oui");
						ActivityConfiguration.this.get_datas()
								.setBooleanPreference(finalKey, check);
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void setPreferenceDownload(String title) {
		final CharSequence[] items = { "Série", "Parallèle" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		int posi = 0;
		if (this.get_datas().isDlSync())
			posi = 1;
		builder.setSingleChoiceItems(items, posi,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						boolean check = items[item].equals("Parallèle");
						ActivityConfiguration.this.get_datas().setDlSync(check);
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void setPreferenceActes(String title) {
		final CharSequence[] items = { "Integral", "Actes" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		int posi = 0;
		if (this.get_datas().isDlVideoActe())
			posi = 1;
		builder.setSingleChoiceItems(items, posi,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						boolean check = items[item].equals("Actes");
						ActivityConfiguration.this.get_datas().setDlVideoActe(check);
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void zoomLevel(String title) {
		final CharSequence[] items = new CharSequence[8];
		int zoom = 80;
		int posi = 3;
		int current = this.get_datas().getZoomLevel();
		for (int i = 0; i < items.length; i++) {
			items[i] = "Zoom " + zoom + " %";
			if (zoom == current)
				posi = i;
			zoom += 10;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setSingleChoiceItems(items, posi,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						int zoom = item * 10 + 80;
						ActivityConfiguration.this.get_datas().setZoomLevel(
								zoom);
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onBackPressed() {
		if (this.isDualMode())
			this.finish();
		else
			super.onBackPressed();
	}

	public void OnAuthenfied() {
		// On ne fait rien
	}

}
