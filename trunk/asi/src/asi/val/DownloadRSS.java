/***************************************************************************
    begin                : aug 01 2010
    copyright            : (C) 2010 by Benoit Valot
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

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

public class DownloadRSS {

	private URL url;

	private Document dom;

	private ArrayList<Article> articles;

	public DownloadRSS(String rss_url) throws Exception {

		if (rss_url == null)
			url = new URL("http://www.arretsurimages.net/tous-les-contenus.rss");
		else
			url = new URL(rss_url);

		articles = new ArrayList<Article>();
	}

	public void get_rss_articles() throws Exception {
		// Récupération de la page en dom
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		dom = db.parse(new InputSource(url.openStream()));
		// on parcourt les items
		NodeList items = dom.getElementsByTagName("item");
		for (int i = 0; i < items.getLength(); i++) {
			Article ar = new Article();
			Node item = items.item(i);
			NodeList artis = item.getChildNodes();
			try {
				for (int j = 0; j < artis.getLength(); j++) {
					Node arti = artis.item(j);
					if (arti.getNodeName().equalsIgnoreCase("title"))
						ar.setTitle(arti.getFirstChild().getNodeValue());
					else if (arti.getNodeName().equalsIgnoreCase("description"))
						ar.setDescriptionOnRSS(arti.getFirstChild()
								.getNodeValue());
					else if (arti.getNodeName().equalsIgnoreCase("link"))
						ar.setUri(arti.getFirstChild().getNodeValue());
					else if (arti.getNodeName().equalsIgnoreCase("pubDate"))
						ar.setDate(arti.getFirstChild().getNodeValue());
					else if (arti.getNodeName().equals("enclosure"))
						ar.setImageUrl(arti.getAttributes().getNamedItem("url")
								.getNodeValue());
				}
				articles.add(ar);
			} catch (Exception e) {
				Log.d("ASI", "bad article:" + (i + 1));
			}
		}
	}

	public void setArticles(ArrayList<Article> articles) {
		this.articles = articles;
	}

	public ArrayList<Article> getArticles() {
		return articles;
	}

}
