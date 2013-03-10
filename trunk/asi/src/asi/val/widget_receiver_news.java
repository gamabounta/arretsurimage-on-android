package asi.val;

import java.util.ArrayList;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class widget_receiver_news extends AppWidgetProvider {

	public static final String SHOW_CURRENT = "asi.val.action.SHOW_CURRENT";

	public static final String SHOW_NEXT = "asi.val.action.SHOW_NEXT";

	public static final String CHECK_CURRENT = "asi.val.action.CHECK_CURRENT";

	public static final String UPDATE_WIDGET = "asi.val.action.UPDATE_WIDGET";

	private ArrayList<Article> articles;

	private String url = "http://www.arretsurimages.net/rss/tous-les-contenus.rss";

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			Log.d("ASI", "Widget update:" + appWidgetIds[i]);
			int appWidgetId = appWidgetIds[i];
			// Lien vers la page courante d'ASI
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_new_asi);
			// On définit les actions sur les éléments du widget
			this.defined_intent(context, views, appWidgetIds);

			views.setTextViewText(R.id.widget_message, "Mise à jour en cours");
			views.setImageViewResource(R.id.widget_color, R.drawable.color_asi);
			views.setTextViewText(R.id.widget_next_texte, "0/0");
			views.setViewVisibility(R.id.widget_check_image, View.INVISIBLE);
			views.setViewVisibility(R.id.widget_vignette, View.INVISIBLE);
			views.setImageViewBitmap(R.id.widget_vignette, BitmapFactory
					.decodeResource(context.getResources(), R.drawable.vide));
			appWidgetManager.updateAppWidget(appWidgetId, views);

			try {
				Log.d("ASI", "Lancement widget téléchargement");
				if (i == 0) {
					GetArticleWidget getArticleWidget = new GetArticleWidget(
							context, appWidgetIds);
					getArticleWidget.execute(this.url);
				}
			} catch (Exception e) {
				views.setTextViewText(R.id.widget_message,
						"Erreur de mise à jour");
				appWidgetManager.updateAppWidget(appWidgetId, views);
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				Log.e("ASI", "Error widget " + error);
			}
		}
	}

	public void updateArticles(ArrayList<Article> articlesBackground,
			Context context, int[] appWidgetIds) {
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_new_asi);
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		this.defined_intent(context, views, appWidgetIds);
		int appWidgetId;
		articles = articlesBackground;
		// Erreur de téléchargement
		if (articles == null || articles.size() == 0) {
			for (int i = 0; i < appWidgetIds.length; i++) {
				Log.d("ASI", "Widget update article:" + appWidgetIds[i]);
				appWidgetId = appWidgetIds[i];
				views.setTextViewText(R.id.widget_message,
						"Erreur de mise à jour");
				appWidgetManager.updateAppWidget(appWidgetId, views);
			}
			Log.e("ASI", "Error widget update");
			return;
		}

		articles = this.get_new_articles(articles, context);
		this.get_datas(context).save_widget_article(articles);
		this.get_datas(context).save_widget_posi(0);
		Toast.makeText(context, "ASI widget à jour", Toast.LENGTH_SHORT).show();

		// Pas de nouvel article
		if (articles.size() == 0) {
			for (int i = 0; i < appWidgetIds.length; i++) {
				Log.d("ASI", "Widget update article:" + appWidgetIds[i]);
				appWidgetId = appWidgetIds[i];
				views.setTextViewText(R.id.widget_message,
						"Pas de nouvel article");
				appWidgetManager.updateAppWidget(appWidgetId, views);
			}
		} else {
			// Présenter le premier article
			for (int i = 0; i < appWidgetIds.length; i++) {
				Log.d("ASI", "Widget update article:" + appWidgetIds[i]);
				appWidgetId = appWidgetIds[i];
				this.defined_article(views, context, 0);
				appWidgetManager.updateAppWidget(appWidgetId, views);
			}
		}
	}

	private void defined_intent(Context context, RemoteViews views,
			int[] appWidgetIds) {

		// update du widget
		Intent intent = new Intent(context, widget_receiver_news.class);
		intent.setAction(UPDATE_WIDGET);
		intent.putExtra("IDS", appWidgetIds);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT // no flags
				);
		views.setOnClickPendingIntent(R.id.widget_update, pendingIntent);

		// Check de l'article en cours
		intent = new Intent(context, widget_receiver_news.class);
		intent.setAction(CHECK_CURRENT);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_check, pendingIntent);

		intent = new Intent(context, widget_receiver_news.class);
		intent.setAction(SHOW_CURRENT);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_mes, pendingIntent);

		intent = new Intent(context, widget_receiver_news.class);
		intent.setAction(SHOW_NEXT);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_next, pendingIntent);
	}

	private void defined_article(RemoteViews views, Context context, int posi) {
		if (articles.size() != 0) {
			views.setTextViewText(R.id.widget_message, articles.get(posi)
					.getTitle());
			this.setArticleColor(views, articles.get(posi));
			this.get_datas(context).save_widget_posi(posi);
			views.setTextViewText(R.id.widget_next_texte, (posi + 1) + "/"
					+ articles.size());
			if (this.get_datas(context).contain_articles_lues(
					articles.get(posi).getUri()))
				views.setViewVisibility(R.id.widget_check_image, View.VISIBLE);
			else
				views.setViewVisibility(R.id.widget_check_image, View.INVISIBLE);
			Bitmap bit = this.get_datas(context).getImageCache()
					.getBitmapForFile(articles.get(posi).getImageUrl());
			if (bit != null) {
				Log.d("ASI", "Widget View image");
				views.setViewVisibility(R.id.widget_vignette, View.VISIBLE);
				views.setImageViewBitmap(R.id.widget_vignette, bit);
			} else {
				Log.d("ASI", "Widget no image");
				views.setViewVisibility(R.id.widget_vignette, View.INVISIBLE);
				views.setImageViewBitmap(R.id.widget_vignette,
						BitmapFactory.decodeResource(context.getResources(),
								R.drawable.vide));
			}
		} else {
			views.setTextViewText(R.id.widget_message, "Aucun article non lu");
			views.setImageViewResource(R.id.widget_color, R.drawable.color_asi);
			views.setTextViewText(R.id.widget_next_texte, "0/0");
			views.setViewVisibility(R.id.widget_check_image, View.INVISIBLE);
			views.setViewVisibility(R.id.widget_vignette, View.INVISIBLE);
			views.setImageViewBitmap(R.id.widget_vignette, BitmapFactory
					.decodeResource(context.getResources(), R.drawable.vide));
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
		} else if (SHOW_CURRENT.equals(action)) {
			articles = this.get_datas(context).get_widget_article();
			int posi = this.get_datas(context).get_widget_posi();
			intent = new Intent(context, ActivityPage.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (posi < articles.size()) {
				intent.putExtra("articles", articles);
				intent.putExtra("article", articles.get(posi));
				context.startActivity(intent);
				this.get_datas(context).add_articles_lues(
						articles.get(posi).getUri());
			}
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_new_asi);
			// On met l'article courant lu et on rend visible l'image check
			this.defined_article(views, context, posi);

			ComponentName thisWidget = new ComponentName(context,
					widget_receiver_news.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			manager.updateAppWidget(thisWidget, views);
		} else if (SHOW_NEXT.equals(action)) {
			articles = this.get_datas(context).get_widget_article();
			int posi = this.get_datas(context).get_widget_posi();

			if ((posi + 1) == articles.size())
				posi = 0;
			else
				posi++;
			Log.d("ASI", "position widget;" + posi);
			Log.d("ASI", "save_articles:" + articles.size());
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_new_asi);
			this.defined_article(views, context, posi);

			ComponentName thisWidget = new ComponentName(context,
					widget_receiver_news.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			// int[] temp = manager.getAppWidgetIds(thisWidget);
			// for(int z=0;z<temp.length;z++)
			// Log.d("ASI","intent update of:"+temp[z]);
			manager.updateAppWidget(thisWidget, views);
			// appWidgetManager.updateAppWidget(appWidgetId, views);
		} else if (CHECK_CURRENT.equals(action)) {
			articles = this.get_datas(context).get_widget_article();
			int posi = this.get_datas(context).get_widget_posi();
			if (posi < articles.size())
				this.get_datas(context).add_articles_lues(
						articles.get(posi).getUri());
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_new_asi);
			// On met l'article courant lu et on rend visible l'image check
			// views.setViewVisibility(R.id.widget_check_image, View.VISIBLE);
			this.defined_article(views, context, posi);

			ComponentName thisWidget = new ComponentName(context,
					widget_receiver_news.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
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

	private void setArticleColor(RemoteViews views, Article art) {
		if (art.isArticle())
			views.setImageViewResource(R.id.widget_color, R.drawable.color_art);
		else if (art.isChronique())
			views.setImageViewResource(R.id.widget_color, R.drawable.color_chro);
		else if (art.isEmission())
			views.setImageViewResource(R.id.widget_color, R.drawable.color_emi);
		else if (art.isViteDit())
			views.setImageViewResource(R.id.widget_color, R.drawable.color_vite);
	}

	private ArrayList<Article> get_new_articles(ArrayList<Article> articles2,
			Context c) {
		ArrayList<Article> ar = new ArrayList<Article>();
		for (int i = 0; i < articles2.size(); i++) {
			if (!this.get_datas(c).contain_articles_lues(
					articles2.get(i).getUri()))
				ar.add(articles2.get(i));
		}
		return (ar);
	}

	public SharedDatas get_datas(Context c) {
		SharedDatas datas = SharedDatas.shared;
		if (datas == null)
			return (new SharedDatas(c));
		datas.setContext(c);
		return datas;
	}

	private class GetArticleWidget extends AsyncTask<String, Void, Void> {

		private ArrayList<Article> articlesBackground;

		private Context context;

		private int[] appWidgetIds;

		private SharedDatas shared;

		public GetArticleWidget(Context c, int[] Ids) {
			shared = widget_receiver_news.this.get_datas(c);
			context = c;
			appWidgetIds = Ids;
		}

		// can use UI thread here
		protected void onPreExecute() {
			articlesBackground = null;
		}

		// automatically done on worker thread (separate from UI thread)
		protected Void doInBackground(String... args) {
			try {
				DownloadRSS d = new DownloadRSS(args[0]);
				d.get_rss_articles();
				articlesBackground = d.getArticles();
				Log.d("ASI", "widget telechargement termine");
				shared.getImageCache().clearMemoryCache();
				for (Article art : articlesBackground) {
					shared.getImageCache().addBitmapToCache(art.getImageUrl());
				}

			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				Log.e("ASI", error);
			}
			return (null);

		}

		protected void onPostExecute(Void result) {
			widget_receiver_news.this.updateArticles(articlesBackground,
					context, appWidgetIds);
		}

	}

}
