/***************************************************************************
    begin                : jan 01 2013
    copyright            : (C) 2013 by Benoit Valot
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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class FragmentPage extends FragmentAsiBase {
	private OnLinkSelectedListener listener;
	private String html;
	private WebView mywebview;

	public interface OnLinkSelectedListener {
		public void onVideoLink(final String url);
		public void onForumLink(String url);
	}

	public static FragmentPage newInstance(String html) {
		FragmentPage fragment = new FragmentPage();
		Bundle bundle = new Bundle();
		bundle.putString("data", html);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.listener = (OnLinkSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnLinkSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ASI", "onCreateView FragmentPage");
		View view = inflater.inflate(R.layout.pageview, container, false);
		this.html = this.getArguments().getString("data");
		mywebview = (WebView) view.findViewById(R.id.WebViewperso);
		this.load_content();
		return (view);
	}
	
	private void load_content() {
		final String mimeType = "text/html";
		final String encoding = "UTF-8";
		// on charge mon code html dans ma webview
		mywebview.loadDataWithBaseURL("http://www.arretsurimages.net", this.html,
				mimeType, encoding, null);
		mywebview.setWebViewClient(new myWebViewClient());
		mywebview
				.setInitialScale((int) (this.get_datas().getZoomLevel() * mywebview
						.getScale()));
		mywebview.getSettings().setBuiltInZoomControls(this.get_datas().isZoomEnable());
	}

	private class myWebViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
				Log.d("ASI", "Open url : " + url);
				if (url.matches(".*arretsurimages\\.net.*")) {
					if (url.matches(".*mp3.*")) {
						Log.d("ASI", "Audio-" + url);
						Intent intent = new Intent();
						intent.setAction(android.content.Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse(url), "audio/*");
						FragmentPage.this.startActivity(intent);
					} else if (url
							.matches(".*arretsurimages\\.net\\/chroniques.*")) {
						Log.d("ASI", "Chargement arrêt sur image");
						Intent i = new Intent(FragmentPage.this.getActivity(),
								ActivityPage.class);
						Article art = new Article();
						art.setUri(url);
						i.putExtra("article", art);
						FragmentPage.this.startActivity(i);
					} else if (url
							.matches(".*arretsurimages\\.net\\/emissions.*")) {
						Log.d("ASI", "Chargement arrêt sur image");
						Intent i = new Intent(FragmentPage.this.getActivity(),
								ActivityPage.class);
						Article art = new Article();
						art.setUri(url);
						i.putExtra("article", art);
						FragmentPage.this.startActivity(i);
					} else if (url
							.matches(".*arretsurimages\\.net\\/articles.*")) {
						Log.d("ASI", "Chargement arrêt sur image");
						Intent i = new Intent(FragmentPage.this.getActivity(),
								ActivityPage.class);
						Article art = new Article();
						art.setUri(url);
						i.putExtra("article", art);
						FragmentPage.this.startActivity(i);
					} else if (url.matches(".*arretsurimages\\.net\\/breves.*")) {
						Log.d("ASI", "Chargement arrêt sur image");
						Intent i = new Intent(FragmentPage.this.getActivity(),
								ActivityPage.class);
						Article art = new Article();
						art.setUri(url);
						i.putExtra("article", art);
						FragmentPage.this.startActivity(i);
					} else if (url
							.matches(".*arretsurimages\\.net\\/dossier.*")) {
						Log.d("ASI", "Dossier lancé");
						Intent i = new Intent(FragmentPage.this.getActivity(),
								ActivityListArticle.class);
						Categorie cat = new Categorie();
						cat.setTitre("DOSSIER");
						cat.setColor("#3399FF");
						cat.setUrl(url);
						int img = FragmentPage.this.getResources().getIdentifier("articles",
								"drawable", FragmentPage.this.getActivity().getPackageName());
						cat.setImage(img);
						i.putExtra("cat", cat);
						FragmentPage.this.startActivity(i);
					} else if (url
							.matches(".*arretsurimages\\.net\\/recherche.*")) {
						Log.d("ASI", "Recherche lancé");
						Intent i = new Intent(FragmentPage.this.getActivity(),
								ActivityListArticleRecherche.class);
						i.putExtra("url", url);
						FragmentPage.this.startActivity(i);

					} else if (url
							.matches(".*arretsurimages\\.net\\/chroniqueur.*")) {
						Log.d("ASI", "Chronique lancé");
						Intent i = new Intent(FragmentPage.this.getActivity(),
								ActivityListArticle.class);
						Categorie cat = new Categorie();
						cat.setTitre("CHRONIQUES");
						cat.setColor("#FF398E");
						cat.setUrl(url);
						int img = FragmentPage.this.getResources().getIdentifier("kro",
								"drawable", FragmentPage.this.getActivity().getPackageName());
						cat.setImage(img);
						i.putExtra("cat", cat);
						FragmentPage.this.startActivity(i);

					} else if (url.matches(".*arretsurimages\\.net\\/media.*")) {
						Intent i = new Intent(FragmentPage.this.getActivity(),
								ActivityPageImage.class);
						i.putExtra("url", url);
						FragmentPage.this.startActivity(i);

					} else if (url.matches(".*arretsurimages\\.net\\/forum.*")) {
						FragmentPage.this.listener.onForumLink(url);
/*					} else if (url
							.matches(".*arretsurimages\\.net\\/emission.*")) {
						Toast.makeText(
								FragmentPage.this.getActivity(),
								"Ce lien n'est pas visible sur l'application Android",
								Toast.LENGTH_LONG).show();*/
					} else {
						Toast.makeText(
								FragmentPage.this.getActivity(),
								"Ce lien n'est pas visible sur l'application Android : ouverture du navigateur",
								Toast.LENGTH_LONG).show();
						Intent i = new Intent(Intent.ACTION_VIEW);
						Uri u = Uri.parse(url);
						i.setData(u);
						FragmentPage.this.startActivity(i);
					}
					return true;
				} else if (url
						.matches("http\\:\\/\\/www\\.dailymotion\\.com\\/video.*")) {
					Log.d("ASI", "Chargement video");
					FragmentPage.this.listener.onVideoLink(url);
					return (true);
				} else {
					Intent i = new Intent(Intent.ACTION_VIEW);
					Uri u = Uri.parse(url);
					i.setData(u);
					startActivity(i);
					return (true);
				}
			} catch (Exception e) {
				new DialogError(FragmentPage.this.getActivity(), "Chargement du lien", e)
						.show();
				return false;
			}
		}
	};
}
