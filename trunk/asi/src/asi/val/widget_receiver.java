package asi.val;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;

public class widget_receiver extends AppWidgetProvider {

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			Log.d("ASI", "Widget update:" + appWidgetIds[i]);
			int appWidgetId = appWidgetIds[i];
			// Lien vers la page courante d'ASI
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_asi);
			// On définit les actions sur les éléments du widget
			this.defined_intent(context, views, appWidgetIds);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	private void defined_intent(Context context, RemoteViews views,
			int[] appWidgetIds) {
		//Différentes catégories
		int[] catint = new int[] { R.array.catT, R.array.catE, R.array.catD,
				R.array.catC, R.array.catV};
		Resources res = context.getResources();
		ArrayList<Categorie> cats = new ArrayList<Categorie>();
		for (int i = 0; i < catint.length; i++) {
			String[] categorie = res.getStringArray(catint[i]);
			Categorie cat = new Categorie();
			cat.setTitre(categorie[0]);
			int img = res.getIdentifier(categorie[1],
					"drawable", context.getPackageName());
			cat.setImage(img);
			cat.setUrl(categorie[2]);
			cat.setSubCat(categorie[3]);
			cat.setColor(categorie[4]);
			cats.add(cat);
		}
		
		// Charge les articles de tout le site
		Intent intent = new Intent(context, ActivityListArticle.class);
		intent.setAction("00000");
		intent.putExtra("categories", cats);
		intent.putExtra("cat", cats.get(0));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		views.setOnClickPendingIntent(R.id.widget2_asi, pendingIntent);

		// Charge les articles des émissions
		intent = new Intent(context, ActivityListArticle.class);
		intent.setAction("11111");
		intent.putExtra("categories", cats);
		intent.putExtra("cat", cats.get(1));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pendingIntent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget2_emi, pendingIntent);

		// Charge les articles des dossiers
		intent = new Intent(context, ActivityListArticle.class);
		intent.setAction("22222");
		intent.putExtra("categories", cats);
		intent.putExtra("cat", cats.get(2));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pendingIntent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT // no flags
				);
		views.setOnClickPendingIntent(R.id.widget2_art, pendingIntent);

		// Charge les articles des chroniques
		intent = new Intent(context, ActivityListArticle.class);
		intent.setAction("33333");
		intent.putExtra("categories", cats);
		intent.putExtra("cat", cats.get(3));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pendingIntent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT // no flags
				);
		views.setOnClickPendingIntent(R.id.widget2_chro, pendingIntent);

		// Charge les vidéos téléchargés
		intent = new Intent(context, ActivityVideoOnSd.class);
		intent.setAction("44444");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pendingIntent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget2_video, pendingIntent);
		Log.d("ASI", "Add intent to widget");

	}

	public void onReceive(Context context, Intent intent) {
		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();

		Log.d("ASI", "Action2=" + action);
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			super.onReceive(context, intent);
		}
	}

	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d("ASI", "disabled widget");
	}

	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d("ASI", "enabled widget");
	}

	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Log.d("ASI", "deleted widget");
	}

	public SharedDatas get_datas(Context c) {
		SharedDatas datas = SharedDatas.shared;
		if (datas == null)
			return (new SharedDatas(c));
		datas.setContext(c);
		return datas;
	}
}
