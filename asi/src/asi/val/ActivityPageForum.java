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

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import asi.val.FragmentPage.OnLinkSelectedListener;
import asi.val.FragmentPostComment.OnPostCommentListener;

public class ActivityPageForum extends ActivityAsiBase implements
		OnLinkSelectedListener, OnPostCommentListener {

	private ForumPost forumPost;

	private String pagedata;

	private get_page_content contentTask;

	private sendPostComment commentTask;

	private ActionBar actionBar;
	
	private Categorie forum;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		forum = this.getIntent().getExtras().getParcelable("forum");

		if (savedInstanceState == null) {
			this.load_content();
		}

		actionBar = (ActionBar) findViewById(R.id.actionbar);
		this.addNavigationToActionBar(actionBar, "Forum");
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.addAction(actionBar.newAction(R.id.actionbar_item_home)
				.setIcon(R.drawable.pola_forum));

		// dual-mode ou bouton
		this.loadCommentDual();
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
		if (pagedata != null)
			b.putString("pagedata", pagedata);
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
		pagedata = b.getString("pagedata");
		if (pagedata == null) {
			Log.d("ASI", "Rien a recuperer");
			contentTask = new get_page_content();
			contentTask.execute(forum.getUrl());
		}
		super.onRestoreInstanceState(b);
	}

	@Override
	protected void onDestroy() {
		Log.d("ASI", "ActivityPageForum onDestroy");
		if (contentTask != null
				&& !contentTask.getStatus().equals(Status.FINISHED)) {
			contentTask.cancel(true);
		}
		if (commentTask != null
				&& !commentTask.getStatus().equals(Status.FINISHED)) {
			commentTask.cancel(true);
		}
		super.onDestroy();
	}

	public void load_content() {
		// On arrete l'ancien
		if (contentTask != null
				&& !contentTask.getStatus().equals(Status.FINISHED)) {
			contentTask.cancel(true);
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentLoad fragment = new FragmentLoad();
		fragmentTransaction.replace(R.id.container, fragment);
		fragmentTransaction.commit();

		contentTask = new get_page_content();
		contentTask.execute(forum.getUrl());
		// Sur le forum de test
		// new
		// get_page_content().execute("http://www.arretsurimages.net/forum/read.php?12,1197521,1198387");
	}

	public void load_data() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentPage fragment = FragmentPage.newInstance(this.pagedata);
		fragmentTransaction.replace(R.id.container, fragment, "page");
		fragmentTransaction.commitAllowingStateLoss();
	}

	private void loadCommentDual() {
		// Non abonnée, pas acces au post
		if (this.get_datas().getCookies().equals("phorum_session_v5=deleted"))
			return;
		if (this.isDualMode()) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentPostComment fragment = (FragmentPostComment) fragmentManager
					.findFragmentByTag("comment");
			if (fragment == null) {
				FragmentTransaction fragmentTransaction = fragmentManager
						.beginTransaction();
				fragment = new FragmentPostComment();
				fragmentTransaction.add(R.id.container_little, fragment,
						"comment");
				fragmentTransaction.commit();
			}
		} else {
			actionBar.addAction(actionBar.newAction()
					.setIcon(R.drawable.post_menu_top)
					.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							ActivityPageForum.this.prepareComment();
							return true;
						}
					}));
		}
	}

	protected void setPageData(String html) {
		pagedata = html;
	}

	private void prepareComment() {
		if (this.isDualMode())
			return;
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		FragmentPostComment fragment = new FragmentPostComment();
		fragmentTransaction.addToBackStack("comment");
		fragmentTransaction.replace(R.id.container, fragment,
				"comment_principal");
		fragmentTransaction.commit();
	}
	

	protected void finishSendComment() {
		if(!this.isDualMode()){
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.popBackStackImmediate();
		}
		this.load_content();
	};

	@Override
	public void onVideoLink(String url) {
		// On fait rien
	}

	@Override
	public void onForumLink(String url) {
		// On fait rien
	};

	@Override
	public void OnPostComment(String comment) {
		// TODO Auto-generated method stub
		Log.d("asi", "Post a comment");
		forumPost.addHiddenValue("subject", "Re: "
				+ forum.getTitre());
		forumPost.addHiddenValue("body", comment);
		StringBuilder donnees = new StringBuilder("");
		donnees.append("forum_id=" + forumPost.getHiddenValue().get("forum_id")
				+ "&");
		try {
			for (String key : forumPost.getHiddenValue().keySet()) {
				donnees.append(URLEncoder.encode(key, "UTF-8"));
				donnees.append("="
						+ URLEncoder.encode(
								forumPost.getHiddenValue().get(key), "UTF-8")
						+ "&");
			}
			donnees.append("finish=>%20Envoyer");
			Log.d("asi", donnees.toString());
			commentTask = new sendPostComment();
			commentTask.execute(donnees.toString());
		} catch (Exception e) {
			new DialogError(this, "Envoie du Post", e).show();
		}

	}

	private class get_page_content extends AsyncTask<String, Void, String> {
		private String data;
		private ForumPost forumPost;

		// can use UI thread here
		protected void onPreExecute() {
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				PageForum re = new PageForum(args[0]);
				data = re.getComment();
				forumPost = re.getForumPost();
			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				return (error);
			}
			return null;
		}

		protected void onPostExecute(String error) {
			if (error == null) {
				ActivityPageForum.this.setPageData(data);
				ActivityPageForum.this.setForumPost(forumPost);
				ActivityPageForum.this.load_data();
			} else {
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
				ActivityPageForum.this.finishSendComment();
			}
		}
	}

}
