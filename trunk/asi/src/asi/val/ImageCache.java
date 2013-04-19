package asi.val;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageCache {
	private ArrayList<File> currentCacheFile;

	private HashMap<String, Bitmap> currentCacheMemory;

	private Context context;

	private File cacheDir;

//	private int limitOfMemoryFile = 30;

	private int limitOfCacheFile = 100;

	public ImageCache(Context c) {
		this.context = c;
		this.currentCacheFile = new ArrayList<File>();
		this.currentCacheMemory = new HashMap<String, Bitmap>();
		// Load cacheFile
		this.cacheDir = context.getCacheDir();
		//load cacheDisk file in older date
		this.initializedCacheFile();
	}
	
	private void initializedCacheFile(){
		File[] files = this.cacheDir.listFiles();
		Arrays.sort( files, new Comparator<File>()
		{
		    public int compare(File o1, File o2) {

		        if (o1.lastModified() > o2.lastModified()) {
		            return -1;
		        } else if (o1.lastModified() < o2.lastModified()) {
		            return +1;
		        } else {
		            return 0;
		        }
		    }

		});
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains(".png")
					|| files[i].getName().contains(".jpg")) {
				currentCacheFile.add(cacheDir);
				Log.d("ASI", "Cache file : " + files[i].getName());
			}
		}
	}

	private File getFileforUrl(String url) {
		String name = url.substring(url.lastIndexOf('/') + 1);
		File cache = new File(cacheDir, name);
		return cache;
	}

	public void addBitmapToCache(String url) {
		if(url=="")
			return;
		if (!url.contains(".jpg") && !url.contains(".png"))
			return;
		// Déjà dans le cache?
		if (this.getBitmapForFile(url) != null)
			return;

		try {
			Log.d("ASI","load bitmap \n"+url);
			// telechargement
			URL myFileUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			Bitmap image = BitmapFactory.decodeStream(is);
			is.close();
			if (image == null)
				throw new Exception("Erreur lecture image");
			Bitmap scale = Bitmap.createScaledBitmap(image, 100, 75, false);
			// sauvegarde
			File current = this.getFileforUrl(url);
			current.createNewFile();
			FileOutputStream fos = new FileOutputStream(current);
			if (current.getName().contains(".jpg"))
				scale.compress(CompressFormat.JPEG, 100, fos);
			else
				scale.compress(CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();

			// Ajout au cache
			this.currentCacheFile.add(current);
			this.currentCacheMemory.put(url, scale);
			// on verifie la taille
			this.checkMemoryUsage();

		} catch (Exception e) {
			Log.e("ASI", e.toString());
		}
	}

	private void checkMemoryUsage() {
		while (currentCacheFile.size() > limitOfCacheFile) {
			File first = currentCacheFile.get(0);
			Log.d("ASI","remove image : "+first.getName());
			currentCacheFile.remove(0);
			first.delete();
		}
		currentCacheMemory.clear();
	}

	public void clearMemoryCache() {
		currentCacheMemory.clear();
	}

	public Bitmap getBitmapForFile(String url) {
		Bitmap image = null;
		if(url=="")
			return (image);
		if (this.currentCacheMemory.containsKey(url)) {
			image = currentCacheMemory.get(url);
		} else {
			File current = this.getFileforUrl(url);
			if (current.exists()) {
				image = BitmapFactory.decodeFile(current.getAbsolutePath());
				// Si pas null, on l'ajoute au cache
				if (image != null)
					this.currentCacheMemory.put(url, image);
			}
		}
		return (image);
	}
}
