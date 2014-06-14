package asi.val;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

public class SharedDatas {

	public static SharedDatas shared;

	private static final String FILENAME = "article_lus";

	private static final String FILENAME_WIDGET = "widget_articles";

	private static final String FILENAME_WIDGET_EMISSION = "widget_emission";

	public static final String PREFERENCE = "asi_pref";

	private ArrayList<String> articles_lues;

	private Context activity;

	private ImageCache cache;

	public SharedDatas(Context a) {
		Log.d("ASI", "create shared");
		this.articles_lues = new ArrayList<String>();
		SharedDatas.shared = this;
		activity = a;
		cache = new ImageCache(activity);
		this.set_articles_lues();
	}

	public void setContext(Context a) {
		activity = a;
	}

	public Context getContext() {
		return activity;
	}

	public ImageCache getImageCache() {
		return cache;
	}

	public Bitmap createVideoThumbnail(String filePath) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				Bitmap bit = WrapThumbnailUtils.createVideoThumbnail(filePath);
				return bit;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	public void setAuthentification(String username, String password,
			String cookies) {
		Log.d("ASI", "set_cookies " + cookies);
		SharedPreferences settings = this.getContext().getSharedPreferences(
				PREFERENCE, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("user", username);
		editor.putString("pass", password);
		editor.putString("cookies", cookies);

		// Commit the edits!
		editor.commit();
	}

	public String getCookies() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getString("cookies", "phorum_session_v5=deleted");
	}

	public String getUsername() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getString("user", "");
	}

	public String getPassword() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getString("pass", "");
	}

	private void set_articles_lues() {
		try {
			FileInputStream fos = activity.openFileInput(FILENAME);
			InputStreamReader isr = new InputStreamReader(fos);
			BufferedReader objBufferReader = new BufferedReader(isr);
			String strLine;
			while ((strLine = objBufferReader.readLine()) != null) {
				this.articles_lues.add(strLine);
			}
			;
			fos.close();
			this.test_length_article_lu();
		} catch (java.io.FileNotFoundException e) {
			Toast.makeText(activity, "Création du fichier de sauvegarde",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			new DialogError(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCÈS aux données partagées " + e.getMessage());
		}
	}

	private void test_length_article_lu() {
		try {
			if (this.articles_lues.size() > 2000) {
				ArrayList<String> temp = new ArrayList<String>();
				Log.d("ASI", "diminue la longeur de la sauvegarde");
				FileOutputStream fos = activity.openFileOutput(FILENAME,
						Context.MODE_PRIVATE);
				for (int i = 1000; i < this.articles_lues.size(); i++) {
					String url_article = this.articles_lues.get(i) + "\n";
					temp.add(this.articles_lues.get(i));
					fos.write(url_article.getBytes());
				}
				fos.flush();
				fos.close();
				this.articles_lues = temp;
			}
		} catch (Exception e) {
			new DialogError(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCÈS aux données partagées " + e.getMessage());
		}
	}

	public void add_articles_lues(String url_article) {
		try {
			if (!this.articles_lues.contains(url_article)) {
				this.articles_lues.add(url_article);
				FileOutputStream fos = activity.openFileOutput(FILENAME,
						Context.MODE_APPEND);
				url_article = url_article + "\n";
				fos.write(url_article.getBytes());
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			new DialogError(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCÈS aux données partagées " + e.getMessage());
		}
	}

	public void remove_articles_lues(String url_article) {
		try {
			if (this.articles_lues.contains(url_article)) {
				this.articles_lues.remove(url_article);
				FileOutputStream fos = activity.openFileOutput(FILENAME,
						Context.MODE_PRIVATE);
				for (String url : this.articles_lues) {
					url = url + "\n";
					fos.write(url.getBytes());
				}
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			new DialogError(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCÈS aux données partagées " + e.getMessage());
		}
	}

	public boolean contain_articles_lues(String url_article) {
		if (this.articles_lues.contains(url_article)) {
			// Log.d("ASI","ok_url_lu="+url_article);
			return (true);
		} else {
			// Log.d("ASI","no_url_lu="+url_article);
			return (false);
		}
	}

	public void setDlSync(boolean dlsync) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putBoolean("dlsync", dlsync);
		editor.commit();
	}

	public boolean isDlSync() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getBoolean("dlsync", false);
	}
	
	public void setDlVideoActe(boolean dlvideo) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putBoolean("dlvideo", dlvideo);
		editor.commit();
	}

	public boolean isDlVideoActe() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getBoolean("dlvideo", true);
	}

	public void setAutologin(boolean autologin) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putBoolean("autologin", autologin);
		editor.commit();
	}

	public boolean isAutologin() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getBoolean("autologin", true);
	}

	public void setZoomLevel(int posi) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putInt("zoom_level", posi);
		editor.commit();
	}

	public int getZoomLevel() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getInt("zoom_level", 100);
	}

	public void setBooleanPreference(String key, boolean pref) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putBoolean(key, pref);
		editor.commit();
	}

	public boolean getBooleanPreference(String key) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getBoolean(key, true);
	}

	public void setZoomEnable(boolean ena) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putBoolean("zoom_enable", ena);
		editor.commit();
	}

	public boolean isZoomEnable() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getBoolean("zoom_enable", true);
	}

	public boolean isLoadImageEnabled() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getBoolean("image_enabled", true);
	}

	public boolean isDescriptionEnabled() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getBoolean("description_enabled", true);
	}

	public boolean isDateEnabled() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getBoolean("date_enabled", true);
	}

	public void save_widget_posi(int posi) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putInt("posi_widget", posi);
		editor.commit();
	}

	public int get_widget_posi() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getInt("posi_widget", 0);
	}

	public void save_widget_article(ArrayList<Article> arts) {
		try {
			FileOutputStream fos = activity.openFileOutput(FILENAME_WIDGET,
					Context.MODE_PRIVATE);
			Parcel p1 = Parcel.obtain();
			p1.writeList(arts);
			fos.write(p1.marshall());
			fos.flush();
			fos.close();
		} catch (java.io.FileNotFoundException e) {
			Log.d("ASI", "sauver données partagées" + e.getMessage());
		} catch (Exception e) {
			new DialogError(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCES aux données partagées " + e.getMessage());
		}
	}

	@SuppressWarnings("finally")
	public ArrayList<Article> get_widget_article() {
		ArrayList<Article> temp = new ArrayList<Article>();
		try {
			FileInputStream fos = activity.openFileInput(FILENAME_WIDGET);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			do {
				int numread = fos.read(b);
				if (numread <= 0)
					break;
				bos.write(b, 0, numread);
			} while (true);
			byte[] bytes = bos.toByteArray();
			Log.d("lemonde", "Taille : " + bytes.length);
			Parcel p2 = Parcel.obtain();
			p2.unmarshall(bytes, 0, bytes.length);
			p2.setDataPosition(0);
			@SuppressWarnings("unchecked")
			ArrayList<Article> arts = p2.readArrayList(Article.class
					.getClassLoader());
			if (arts != null) {
				temp = arts;
			}
			fos.close();
			this.test_length_article_lu();
		} catch (java.io.FileNotFoundException e) {
			Log.d("ASI", "sauver données partagées" + e.getMessage());
		} catch (Exception e) {
			new DialogError(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCÈS aux données partagées " + e.getMessage());
		} finally {
			return (temp);
		}
	}

	public void save_widget_emission(ArrayList<Article> arts) {
		try {
			FileOutputStream fos = activity.openFileOutput(
					FILENAME_WIDGET_EMISSION, Context.MODE_PRIVATE);
			Parcel p1 = Parcel.obtain();
			p1.writeList(arts);
			fos.write(p1.marshall());
			fos.flush();
			fos.close();
		} catch (java.io.FileNotFoundException e) {
			Log.d("ASI", "sauver données partagées" + e.getMessage());
		} catch (Exception e) {
			new DialogError(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCES aux données partagées " + e.getMessage());
		}
	}

	@SuppressWarnings("finally")
	public ArrayList<Article> get_widget_emission() {
		ArrayList<Article> temp = new ArrayList<Article>();
		try {
			FileInputStream fos = activity
					.openFileInput(FILENAME_WIDGET_EMISSION);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			do {
				int numread = fos.read(b);
				if (numread <= 0)
					break;
				bos.write(b, 0, numread);
			} while (true);
			byte[] bytes = bos.toByteArray();
			Log.d("lemonde", "Taille : " + bytes.length);
			Parcel p2 = Parcel.obtain();
			p2.unmarshall(bytes, 0, bytes.length);
			p2.setDataPosition(0);
			@SuppressWarnings("unchecked")
			ArrayList<Article> arts = p2.readArrayList(Article.class
					.getClassLoader());
			if (arts != null) {
				temp = arts;
			}
			fos.close();
			this.test_length_article_lu();
		} catch (java.io.FileNotFoundException e) {
			Log.d("ASI", "sauver données partagées" + e.getMessage());
		} catch (Exception e) {
			new DialogError(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCÈS aux données partagées " + e.getMessage());
		} finally {
			return (temp);
		}
	}
}
