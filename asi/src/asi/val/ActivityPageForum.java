package asi.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.markupartist.android.widget.ActionBar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivityPageForum extends ActivityPage {

	private ForumPost forumPost;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected void actionBarInflateMenu(ActionBar actionBar) {
		getMenuInflater().inflate(R.menu.forum_menu_top, actionBar.asMenu());
		this.addNavigationToActionBar(actionBar, "Forum");
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.addAction(actionBar.newAction(R.id.actionbar_item_home)
				.setIcon(R.drawable.pola_forum));
	}

	public void onSaveInstanceState(final Bundle b) {
		Log.d("ASI", "onSaveInstanceState Forum");
		if (this.forumPost != null) {
			ArrayList<String> Save = new ArrayList<String>();
			for (String key : this.forumPost.getHiddenValue().keySet()) {
				Save.add(key);
				Save.add(this.forumPost.getHiddenValue().get(key));
			}
			b.putStringArrayList("forumPost", Save);
		}
		super.onSaveInstanceState(b);
	}

	public void onRestoreInstanceState(final Bundle b) {
		Log.d("ASI", "onRestoreInstanceState Forum");
		ArrayList<String> Save = b.getStringArrayList("forumPost");
		if (Save != null) {
			Log.d("ASI", "Récupération du forumPost");

			if (!Save.isEmpty()) {
				this.forumPost = new ForumPost();
				for (int i = 0; i < (Save.size() - 1); i = (i + 2)) {
					this.forumPost.addHiddenValue(Save.get(i), Save.get(i + 1));
				}
			}
		}
		super.onRestoreInstanceState(b);
	}

	public void load_content() {
		// récuperation des articles via l'URL
		new get_page_content().execute(this.getIntent().getExtras()
				.getString("url"));
		// Sur le forum de test
		//new get_page_content().execute("http://www.arretsurimages.net/forum/read.php?12,1197521,1198387");
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.post_item:
			this.prepareComment();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void prepareComment() {
		if (this.get_datas().getCookies().equals("phorum_session_v5=deleted")) {
			new DialogError(this, "Envoie du Commentaire",
					"Ce forum n'est accéssible qu'aux abonnés").show();
		} else if (this.forumPost != null) {
			new DialogComment(this).show();
		}
	}

	protected void postComment(String comment) {
		// TODO Auto-generated method stub
		Log.d("asi", "Post a comment");
		if (comment != "") {
			forumPost.addHiddenValue("subject", "Re: "
					+ this.getIntent().getExtras().getString("titre"));
			forumPost.addHiddenValue("body", comment);
			StringBuilder donnees = new StringBuilder("");
			donnees.append("forum_id="
					+ forumPost.getHiddenValue().get("forum_id") + "&");
			try {
				for (String key : forumPost.getHiddenValue().keySet()) {
					donnees.append(URLEncoder.encode(key, "UTF-8"));
					donnees.append("="
							+ URLEncoder.encode(
									forumPost.getHiddenValue().get(key),
									"UTF-8") + "&");
				}
				donnees.append("finish=>%20Envoyer");
				Log.d("asi", donnees.toString());
				new sendPostComment().execute(donnees.toString());
			} catch (Exception e) {
				new DialogError(this, "Envoie du Post", e).show();
			}
		}

	}

	private class get_page_content extends AsyncTask<String, Void, String> {
		private final DialogProgress dialog = new DialogProgress(
				ActivityPageForum.this, this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Chargement...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				PageForum re = new PageForum(args[0]);
				ActivityPageForum.this.setPagedata(re.getComment());
				ActivityPageForum.this.setForumPost(re.getForumPost());
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
				ActivityPageForum.this.erreur_loading(error);
			}
		}
	}

	public void setForumPost(ForumPost post) {
		this.forumPost = post;
	}

	private class sendPostComment extends AsyncTask<String, Void, String> {
		private final DialogProgress dialog = new DialogProgress(
				ActivityPageForum.this, this);

		private BufferedReader in;
		private OutputStreamWriter out;
		private String cookies;

		// can use UI thread here
		protected void onPreExecute() {
			in = null;
			out = null;
			this.dialog.setMessage("Envoie du Commentaire...");
			this.dialog.show();
			cookies = ActivityPageForum.this.get_datas().getCookies();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				URL url_login = new URL(
						"http://www.arretsurimages.net/forum/posting.php");
				HttpURLConnection connection = (HttpURLConnection) url_login
						.openConnection();
				connection.setDoOutput(true);
				connection.setInstanceFollowRedirects(true);

				connection.setRequestProperty("Cookie", cookies);

				// On écrit les données via l'objet OutputStream
				out = new OutputStreamWriter(connection.getOutputStream());
				out.write(args[0]);
				out.flush();
				in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String data;
				while ((data = in.readLine()) != null) {
					if (data.contains("<div class=\"attention\">")) {
						data = data.replaceAll("\\<\\/?div.*?>", "");
						Log.e("ASI", data);
						throw new Exception(data);
					}
					// Log.d("ASI", data);
				}
				connection.disconnect();
				return null;
			} catch (Exception e) {
				String error = e.getMessage();
				return (error);
			} finally {
				// Dans tous les cas on ferme le bufferedReader s'il n'est pas
				// null
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
					}
				}
			}

		}

		protected void onPostExecute(String mess) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();
			} catch (Exception e) {
				Log.e("ASI", "Erreur d'arrêt de la boîte de dialogue");
			}
			if (mess != null) {
				new DialogError(ActivityPageForum.this,
						"Envoie du Commentaire", mess).show();
			} else {
				Log.d("asi", "send with success");
				ActivityPageForum.this.load_content();
			}
		}
	};

	private class DialogComment extends AlertDialog.Builder {

		private EditText txt_comment;

		private Context mContext;

		public DialogComment(Context arg0) {
			super(arg0);
			this.mContext = arg0;
			this.defined_interface();
		}

		private void defined_interface() {

			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.post_comment_dialog, null);
			txt_comment = (EditText) layout.findViewById(R.id.comment_texte);
			txt_comment.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
					| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
					| InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			Button b;
			b = (Button) layout.findViewById(R.id.comment_B);
			b.setOnClickListener(
					new Button.OnClickListener() {
						public void onClick(View arg0) {
							txt_comment.append("[b][/b]");
						}
					});
			b = (Button) layout.findViewById(R.id.comment_I);
			b.setOnClickListener(
					new Button.OnClickListener() {
						public void onClick(View arg0) {
							txt_comment.append("[i][/i]");
						}
					});
			b = (Button) layout.findViewById(R.id.comment_small);
			b.setOnClickListener(
					new Button.OnClickListener() {
						public void onClick(View arg0) {
							txt_comment.append("[small][/small]");
						}
					});
			b = (Button) layout.findViewById(R.id.comment_large);
			b.setOnClickListener(
					new Button.OnClickListener() {
						public void onClick(View arg0) {
							txt_comment.append("[large][/large]");
						}
					});
			b = (Button) layout.findViewById(R.id.comment_url);
			b.setOnClickListener(
					new Button.OnClickListener() {
						public void onClick(View arg0) {
							txt_comment.append("[url=http://][/url]");
						}
					});
			b = (Button) layout.findViewById(R.id.comment_quote);
			b.setOnClickListener(
					new Button.OnClickListener() {
						public void onClick(View arg0) {
							txt_comment.append("[quote=DS][/quote]");
						}
					});
			this.setView(layout);
			// this.setMessage(message+"\n\n"+error);
			this.setTitle("Commentaire");
			this.setPositiveButton("Poster",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Log.d("ASI", "comment-" + txt_comment.getText());
							try {
								ActivityPageForum.this.postComment(""
										+ txt_comment.getText());
							} catch (Exception e) {
								new DialogError(
										mContext,
										"Chargement de la fenetre de dialog de commentaire",
										e).show();
								dialog.cancel();
							}
							dialog.cancel();
						}
					});
		}

	};
}
