package asi.val;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
public class ActivityPageForum extends page {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void load_content() {

		// État de la liste view
		//state = null;

		// récuperation des articles via l'URL
		new get_page_content().execute(this.getIntent().getExtras()
				.getString("url"));
	}
	
	private class get_page_content extends AsyncTask<String, Void, String> {
		private final progress_dialog dialog = new progress_dialog(
				ActivityPageForum.this, this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Chargement...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			// List<String> names =
			// Main.this.application.getDataHelper().selectAll();
			try {
				PageForum re = new PageForum(args[0]);
				ActivityPageForum.this.setPagedata(re.getComment());
			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				return (error);
			}
			return null;
		}

		protected void onPostExecute(String error) {
			if (this.dialog.isShowing()) {
				try {
					this.dialog.dismiss();
				} catch (Exception e) {
					Log.e("ASI", "Erreur d'arrêt de la boîte de dialogue");
				}
			}
			if (error == null)
				ActivityPageForum.this.load_page();
			else {
				//new erreur_dialog(liste_articles_recherche.this,"Chargement des articles", error).show();
				ActivityPageForum.this.erreur_loading(error);
			}
			// Main.this.output.setText(result);
		}
	}

}
