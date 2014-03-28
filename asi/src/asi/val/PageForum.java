/***************************************************************************
begin                : dec 12 2011
copyright            : (C) 2011 by Benoit Valot
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class PageForum {

	private URL url;

	private HTMLEntities convert = new HTMLEntities();
	
	private ForumPost forumPost = new ForumPost();

	public PageForum(String u) throws MalformedURLException {
		// lancer directement une recherche depuis un lien
		url = new URL(u);
	}

	public String getComment() throws Exception {
		BufferedReader in = null;
		OutputStreamWriter out = null;
		StringBuffer parsehtml = new StringBuffer("");
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);

			conn.setRequestProperty("Cookie", SharedDatas.shared.getCookies());

			// on lit la réponse
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));

			String ligneCodeHTML;
			boolean desc = false;
			boolean start = false;
			boolean post = false;
			Article article = new Article();
			//élément de recherche
			Matcher m ;
			Pattern postPattern = Pattern.compile(".*\\<input type\\=\"hidden\" name\\=\"(.*?)\" value\\=\"(.*?)\".*");
			//<input type="hidden" name="forum_id" value="12" />

			while ((ligneCodeHTML = in.readLine()) != null) {
				ligneCodeHTML = " " + ligneCodeHTML;
				
				//titre du forum
				//<title>&quot;Non, Sarkozy n'a pas d'ordinateur. Et alors ?&quot;</title>
				if(ligneCodeHTML.contains("<title>")){
					//<h1 class="typo-titre">
					ligneCodeHTML = ligneCodeHTML.replaceFirst("<title>", "<h3 class=\"typo-titre\">");
					ligneCodeHTML = ligneCodeHTML.replaceFirst("</title>", "</h3>");			
					parsehtml.append(ligneCodeHTML);
				}

				// lien vers le commentaire
				if (ligneCodeHTML.contains("<a name=\"msg-")) {
					start = true;
				}
				
				if (ligneCodeHTML.contains("<div id=\"post\">")) {
					start = false;
					post=true;
				}

				if (start) {
					// recherche des éléments
				
					// auteur
					if (ligneCodeHTML.contains("class=\"message-infos\"")) {
						ligneCodeHTML = in.readLine();
						article.setTitle(this
								.convert_html_to_string(ligneCodeHTML));
					}

					// date					
					if (ligneCodeHTML.contains("class=\"message-user-info\"")) {
						ligneCodeHTML = in.readLine();
						article.setDate(this
								.convert_html_to_string(ligneCodeHTML));
					}
					if (ligneCodeHTML.contains("<div class=\"content\">")) {
						parsehtml.append("<div class=\"forum-comment\">\n");
						parsehtml.append("<p class=\"forum-name\">"+article.getTitle()+"</p>\n");
						desc = true;
						ligneCodeHTML = ligneCodeHTML.replaceFirst("content", "forum-content");
					}
					if (desc && ligneCodeHTML.contains("class=\"message-options\">")) {
						desc = false;
						parsehtml.append("<p class=\"forum-date\">"+article.getDate()+"</p>\n");
						parsehtml.append("</div>\n");
						parsehtml.append("<hr />\n");
					}
					if (desc) {
						parsehtml.append(ligneCodeHTML);
					}

				}
				
				if(post==true){
					m = postPattern.matcher(ligneCodeHTML);
					while(m.find()){
						Log.d("asi","type="+m.group(1)+",value="+m.group(2));
						this.forumPost.addHiddenValue(m.group(1),m.group(2));
					}
				}

			}
		} catch (java.net.ProtocolException e) {
			throw new StopException("Problème de connexion");
		} catch (Exception e) {
			throw e;
		} finally {
			// Dans tous les cas on ferme le bufferedReader s'il n'est pas null
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
		return this.get_style() + parsehtml.toString();
	}

	private String convert_html_to_string(String html) {
		html = html.replaceAll("<.*?>", "");
		html = html.replaceAll("\\s+", " ");
		return (convert.unhtmlentities(html));
	}
	
	public String get_style() {
		StringBuffer sb2 = new StringBuffer("");
		sb2.append("<style type=\"text/css\">");
		sb2.append(new PageCssStyle().get_css_data());
		sb2.append("</style>");
		return (sb2.toString());
	}
	
	public ForumPost getForumPost() {
		return forumPost;
	}
}
