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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class page_recherche {

	private URL url;

	private String post;

	private Vector<article> articles;
	
	private HTMLEntities convert = new HTMLEntities();

	public page_recherche(String u) throws MalformedURLException {
		// lancer directement une recherche depuis un lien
		url = new URL(u);
		articles = new Vector<article>();
		post="";
	}

	public page_recherche() throws MalformedURLException {
		// créer une recherche
		articles = new Vector<article>();
		url = new URL("http://www.arretsurimages.net/recherche.php");
		post="";
	}
	
	public void set_post(String p){
		post=p;
	}

	public Vector<article> getArticles() throws Exception {
		BufferedReader in = null;
		OutputStreamWriter out = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);

			// On écrit le post si il y a des choses à écrire ?
			out = new OutputStreamWriter(conn.getOutputStream());
			out.write(post);
			out.flush();
			
			//on lit la réponse
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));

			String ligneCodeHTML;
			boolean desc = false;
			StringBuffer description = new StringBuffer("");
			boolean start = false;
			boolean nextpage=false;
			article article = new article();
			//élément de recherche
			Matcher m ;
			Pattern date = Pattern.compile(".*\\<span class\\=\"typo-date\"\\>(.*?)\\<\\/span\\>.*");
			Pattern url = Pattern.compile(".*\\<a href\\=\"(.*?)\".*");
			Pattern titre = Pattern.compile(".*class\\=\"typo-titre\"\\>(.*?)\\<\\/a\\>.*");
			Pattern link_next = Pattern.compile("\\<a href\\=\"(.*?)\"\\>(.*?)\\<\\/a\\>");
			
			while ((ligneCodeHTML = in.readLine()) != null) {
				ligneCodeHTML = " " + ligneCodeHTML;
				
				if(articles.size()>30&&ligneCodeHTML.contains("rech-filtres-droite")){
					nextpage=true;
					article = new article();
					article.setTitle("Plus de résultats");
					article.setDate(">");
					start = false;
				}
				
				if (nextpage&&ligneCodeHTML.contains("</div>")){
					nextpage=false;
				}
				//récupérer le lien des pages suivantes et le nombre de résultats comme un article ???
				if(nextpage){
					if(ligneCodeHTML.contains("typo-info")){
						article.setDescriptionOnRecherche(this.convert_html_to_string(ligneCodeHTML));
					}
					m = link_next.matcher(ligneCodeHTML);
					while(m.find()){
						Log.d("ASI", "link-"+m.group(1));//lien
						Log.d("ASI", "num-"+m.group(2));//nom du lien
						if(m.group(2).equalsIgnoreCase("&gt;")){
							Log.d("ASI", "ok");
							article.setUri("http://www.arretsurimages.net"+m.group(1));
							articles.add(article);
						}
					}		
				}
					
				/*		<div class="rech-filtres-droite">
				<span class="typo-date">212</span> <span class="typo-info">résultat(s) - page</span> <span class="typo-date">1</span><span class="typo-info"> / 4</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

				
				<span class="typo-mono">&nbsp;&nbsp;			&nbsp;</span>&nbsp;
				1&nbsp;<a href="/recherche.php?t=0&chaine=hortefeux&in_dossiers=true&is_emission=true&in_chroniques=true&in_vites=true&periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&p=2&orderby=num">2</a>&nbsp;<a href="/recherche.php?t=0&chaine=hortefeux&in_dossiers=true&is_emission=true&in_chroniques=true&in_vites=true&periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&p=3&orderby=num">3</a>&nbsp;<a href="/recherche.php?t=0&chaine=hortefeux&in_dossiers=true&is_emission=true&in_chroniques=true&in_vites=true&periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&p=4&orderby=num">4</a>&nbsp;			<span class="typo-mono"><a href="/recherche.php?t=0&chaine=hortefeux&in_dossiers=true&is_emission=true&in_chroniques=true&in_vites=true&periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&p=2&orderby=num">&gt;</a>	
				<a href="/recherche.php?t=0&chaine=hortefeux&in_dossiers=true&is_emission=true&in_chroniques=true&in_vites=true&periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&p=4&orderby=num">&gt;&gt;</a></span>
				</span>
			</div>
			*/
				
			//Log.d("ASI",ligneCodeHTML);
				
			//on récupère les informations de chaque article;
				if (ligneCodeHTML.contains("bloc-contenu-5")||ligneCodeHTML.contains("bloc-contenu-6")){//bloc-rech
					start = true;
					article = new article();
					article.set_color_from_recherche(ligneCodeHTML);
				}
				if (ligneCodeHTML.contains("rech-filtres-gauche"))//recherche.php
					start=false;
				if (ligneCodeHTML.contains("\"col_right\""))//dossier
					start=false;

				if(start){
					//recherche des éléments
					m = date.matcher(ligneCodeHTML);
					if(m.find())
						article.setDate(m.group(1));
					
					if(ligneCodeHTML.contains("typo-titre")){
						m = url.matcher(ligneCodeHTML);
						if(m.find())
							article.setUri("http://www.arretsurimages.net"+m.group(1));
						else
							Log.e("ASI","Pas d'URL");
						m = titre.matcher(ligneCodeHTML);
						if(m.find()){
							String d = m.group(1).replaceFirst("<span.*?</span>", "");
							article.setTitle(this.convert_html_to_string(d));
						}
						else
						article.setTitle(ligneCodeHTML);
						//enlever les parties HTML du titre
					}
					
					if(ligneCodeHTML.contains("typo-description")){
						desc=true;
					}
					if(desc){
						//String d = ligneCodeHTML.replaceAll("<.*?>", "");
						//d = d.replaceFirst("</div>", "");
						description.append(ligneCodeHTML);
					}
					
					if(desc&&ligneCodeHTML.contains("</div>")){
						desc=false;
						article.setDescriptionOnRecherche(this.convert_html_to_string(description.toString()));
						description  = new StringBuffer("");
						articles.add(article);
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
		return articles;
	}
	
	private String convert_html_to_string(String html){
		html = html.replaceAll("<.*?>", "");
		html = html.replaceAll("\\s+", " ");
		return (convert.unhtmlentities(html));
	}
}
