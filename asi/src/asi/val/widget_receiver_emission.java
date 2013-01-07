package asi.val;

import java.util.ArrayList;
import java.util.Vector;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class widget_receiver_emission extends AppWidgetProvider {

	public static final String SHOW_FIRST = "asi.val.action.SHOW_FIRST";

	public static final String SHOW_SECOND = "asi.val.action.SHOW_SECOND";

	public static final String DOWNLOAD_FIRST = "asi.val.action.DOWNLOAD_FIRST";

	public static final String DOWNLOAD_SECOND = "asi.val.action.DOWNLOAD_SECOND";

	public static final String UPDATE_WIDGET = "asi.val.action.UPDATE_WIDGET";

	private Vector<Article> articles;

	private String url = "http://www.arretsurimages.net/emissions.rss";

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			Log.d("ASI", "Widget update:" + appWidgetIds[i]);
			int appWidgetId = appWidgetIds[i];
			// Lien vers la page courante d'ASI
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_emi_asi);
			// On définit les actions sur les éléments du widget
			this.defined_intent(context, views, appWidgetIds);

			views.setTextViewText(R.id.widget_message1, "Mise à jour en cours");
			views.setTextViewText(R.id.widget_message2, "");
			views.setViewVisibility(R.id.widget_check_image1, View.INVISIBLE);
			views.setViewVisibility(R.id.widget_check_image2, View.INVISIBLE);
			appWidgetManager.updateAppWidget(appWidgetId, views);

			try {
				Log.d("ASI", "Lancement widget téléchargement");
				if (i == 0) {
					GetArticleWidget getArticleWidget = new GetArticleWidget(
							context, appWidgetIds);
					getArticleWidget.execute(this.url);
				}
			} catch (Exception e) {
				views.setTextViewText(R.id.widget_message1,
						"Erreur de mise à jour");
				appWidgetManager.updateAppWidget(appWidgetId, views);
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				Log.e("ASI", "Error widget " + error);
			}
		}
	}

	public void finishAccessVideo(ArrayList<Video> videos, Context context,
			int[] appWidgetIds) {
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_emi_asi);
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		articles = this.get_datas(context).get_widget_emission();
		this.defined_first_article(views, context);
		this.defined_second_article(views, context);
		this.defined_intent(context, views, appWidgetIds);
		appWidgetManager.updateAppWidget(appWidgetIds, views);
		if (videos.size() != 0) {
			Toast.makeText(context,
					"Ajouts de " + videos.size() + " vidéos à télécharger",
					Toast.LENGTH_SHORT).show();
			for (Video vid : videos) {
				Intent i = new Intent(context, ServiceDownload.class);
				i.putExtra("titre", vid.getTitle());
				i.putExtra("dlsync", this.get_datas(context).isDlSync());
				i.putExtra("url", vid.getLinkURL());
				context.startService(i);
			}
		} else {
			Toast.makeText(
					context,
					"Aucune vidéo trouvée\nRéessayer de lancer le téléchargement",
					Toast.LENGTH_LONG).show();
		}
	}

	public void updateArticles(Vector<Article> articlesBackground,
			Context context, int[] appWidgetIds) {
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_emi_asi);
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		this.defined_intent(context, views, appWidgetIds);
		int appWidgetId;
		articles = articlesBackground;

		for (int i = 0; i < appWidgetIds.length; i++) {
			Log.d("ASI", "Widget update article:" + appWidgetIds[i]);
			appWidgetId = appWidgetIds[i];
			try {
				articles = articlesBackground;
				if (articles == null || articles.size() < 3)
					throw new Exception("Erreur de telechargement");

				Toast.makeText(context, "ASI émission à jour",
						Toast.LENGTH_SHORT).show();
				this.defined_first_article(views, context);
				this.defined_second_article(views, context);
				appWidgetManager.updateAppWidget(appWidgetId, views);

			} catch (Exception e) {
				views.setTextViewText(R.id.widget_message1,
						"Erreur de mise à jour");
				views.setTextViewText(R.id.widget_message2, "");
				appWidgetManager.updateAppWidget(appWidgetId, views);
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				Log.e("ASI", "Error widget " + error);
			} finally {
				if (articles == null) {
					articles = new Vector<Article>();
				}
				if (i == 0) {
					this.get_datas(context).save_widget_emission(articles);
				}
			}
		}
	}

	private void defined_intent(Context context, RemoteViews views,
			int[] appWidgetIds) {

		// update du widget
		Intent intent = new Intent(context, widget_receiver_emission.class);
		intent.setAction(UPDATE_WIDGET);
		intent.putExtra("IDS", appWidgetIds);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT // no flags
				);
		views.setOnClickPendingIntent(R.id.widget_update, pendingIntent);

		intent = new Intent(context, widget_receiver_emission.class);
		intent.setAction(SHOW_FIRST);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_emi1, pendingIntent);

		intent = new Intent(context, widget_receiver_emission.class);
		intent.setAction(SHOW_SECOND);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_emi2, pendingIntent);

		intent = new Intent(context, widget_receiver_emission.class);
		intent.setAction(DOWNLOAD_FIRST);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_download1, pendingIntent);

		intent = new Intent(context, widget_receiver_emission.class);
		intent.setAction(DOWNLOAD_SECOND);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_download2, pendingIntent);

	}

	private void defined_first_article(RemoteViews views, Context context) {
		if (articles.size() >= 1) {
			views.setTextViewText(R.id.widget_message1, articles.elementAt(0)
					.getTitle());
			if (this.get_datas(context).contain_articles_lues(
					articles.elementAt(0).getUri()))
				views.setViewVisibility(R.id.widget_check_image1, View.VISIBLE);
			else
				views.setViewVisibility(R.id.widget_check_image1,
						View.INVISIBLE);
		} else {
			views.setTextViewText(R.id.widget_message1, "Aucun article");
			views.setViewVisibility(R.id.widget_check_image1, View.INVISIBLE);
		}
	}

	private void defined_second_article(RemoteViews views, Context context) {
		if (articles.size() >= 2) {
			views.setTextViewText(R.id.widget_message2, articles.elementAt(1)
					.getTitle());
			if (this.get_datas(context).contain_articles_lues(
					articles.elementAt(1).getUri()))
				views.setViewVisibility(R.id.widget_check_image2, View.VISIBLE);
			else
				views.setViewVisibility(R.id.widget_check_image2,
						View.INVISIBLE);
		} else {
			views.setTextViewText(R.id.widget_message2, "Aucun article");
			views.setViewVisibility(R.id.widget_check_image2, View.INVISIBLE);
		}
	}

	public void onReceive(Context context, Intent intent) {
		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();

		Log.d("ASI", "Action=" + action);
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else if (SHOW_FIRST.equals(action)) {
			articles = this.get_datas(context).get_widget_emission();
			intent = new Intent(context, ActivityPage.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (articles.size() > 0) {
				intent.putExtra("url", articles.elementAt(0).getUri());
				intent.putExtra("titre", articles.elementAt(0).getTitle());
				context.startActivity(intent);
			}
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_emi_asi);
			// On met l'article courant lu et on rend visible l'image check
			this.defined_first_article(views, context);
			views.setViewVisibility(R.id.widget_check_image1, View.VISIBLE);

			ComponentName thisWidget = new ComponentName(context,
					widget_receiver_emission.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			manager.updateAppWidget(thisWidget, views);
		} else if (SHOW_SECOND.equals(action)) {
			articles = this.get_datas(context).get_widget_emission();
			intent = new Intent(context, ActivityPage.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (articles.size() > 1) {
				intent.putExtra("url", articles.elementAt(1).getUri());
				intent.putExtra("titre", articles.elementAt(1).getTitle());
				context.startActivity(intent);
			}
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_emi_asi);
			// On met l'article courant lu et on rend visible l'image check
			this.defined_first_article(views, context);
			views.setViewVisibility(R.id.widget_check_image2, View.VISIBLE);

			ComponentName thisWidget = new ComponentName(context,
					widget_receiver_emission.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			manager.updateAppWidget(thisWidget, views);
		} else if (DOWNLOAD_FIRST.equals(action)) {
			articles = this.get_datas(context).get_widget_emission();
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_emi_asi);
			ComponentName thisWidget = new ComponentName(context,
					widget_receiver_emission.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// on lance le téléchargement
			if (articles.size() >= 1) {
				// On indique téléchargement en cours
				views.setTextViewText(R.id.widget_message1,
						"Récupération des liens vidéos");
				views.setViewVisibility(R.id.widget_check_image1,
						View.INVISIBLE);
				GetEmissionVideo emission = new GetEmissionVideo(context,
						manager.getAppWidgetIds(thisWidget), articles.get(0)
								.getTitle());
				emission.execute(articles.get(0).getUri());
				this.get_datas(context).add_articles_lues(
						articles.get(0).getUri());
			}
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			manager.updateAppWidget(thisWidget, views);
		} else if (DOWNLOAD_SECOND.equals(action)) {
			articles = this.get_datas(context).get_widget_emission();
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_emi_asi);
			ComponentName thisWidget = new ComponentName(context,
					widget_receiver_emission.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// on lance le téléchargement
			if (articles.size() >= 2) {
				// On indique téléchargement en cours
				views.setTextViewText(R.id.widget_message2,
						"Récupération des liens vidéos");
				views.setViewVisibility(R.id.widget_check_image2,
						View.INVISIBLE);
				GetEmissionVideo emission = new GetEmissionVideo(context,
						manager.getAppWidgetIds(thisWidget), articles.get(1)
								.getTitle());
				emission.execute(articles.get(1).getUri());
				this.get_datas(context).add_articles_lues(
						articles.get(1).getUri());
			}
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			manager.updateAppWidget(thisWidget, views);
		} else if (UPDATE_WIDGET.equals(action)) {
			int[] ids = intent.getIntArrayExtra("IDS");
			this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
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

	private class GetArticleWidget extends AsyncTask<String, Void, Void> {

		private Vector<Article> articlesBackground;

		private Context context;

		private int[] appWidgetIds;

		public GetArticleWidget(Context c, int[] Ids) {
			context = c;
			appWidgetIds = Ids;
		}

		// can use UI thread here
		protected void onPreExecute() {
			articlesBackground = new Vector<Article>();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Void doInBackground(String... args) {
			try {
				DownloadRSS d = new DownloadRSS(args[0]);
				d.get_rss_articles();
				articlesBackground = d.getArticles();
				Log.d("ASI", "widget telechargement termine");

			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				Log.e("ASI", error);
			}
			return (null);

		}

		protected void onPostExecute(Void result) {
			widget_receiver_emission.this.updateArticles(articlesBackground,
					context, appWidgetIds);
		}

	}

	private class GetEmissionVideo extends AsyncTask<String, Void, Void> {
		protected ArrayList<Video> videos;

		private Context context;

		private String title;

		private int[] appWidgetId;

		public GetEmissionVideo(Context c, int[] Id, String tit) {
			context = c;
			appWidgetId = Id;
			title = tit;
		}

		// can use UI thread here
		protected void onPreExecute() {
			videos = new ArrayList<Video>();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Void doInBackground(String... args) {
			try {
				PageLoading page_d = new PageLoading(args[0]);
				page_d.getContent();
				videos = page_d.getVideos();
				Log.d("ASI", "widget emission recupérer");

			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				Log.e("ASI", error);
			}
			return (null);

		}

		protected void onPostExecute(Void result) {
			for (Video vid : videos) {
				vid.setTitle(title);
			}
			widget_receiver_emission.this.finishAccessVideo(videos, context,
					appWidgetId);
		}

	}

}
