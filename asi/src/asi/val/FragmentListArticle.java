package asi.val;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class FragmentListArticle extends FragmentAsiBase implements
		OnItemClickListener, OnItemLongClickListener {
	protected OnArticleSelectedListener listener;
	private ListView maListViewPerso;
	private ArrayList<Article> articles;
	private Parcelable state;

	public interface OnArticleSelectedListener {
		public void OnArticleSelected(Article art, int pos);
	}

	public static FragmentListArticle newInstance(ArrayList<Article> articles) {
		FragmentListArticle fragment = new FragmentListArticle();
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("liste_data", articles);
		fragment.setArguments(bundle);
		return fragment;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	this.listener = (OnArticleSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ASI", "onCreateView FragmentListArticle");
		View view = inflater.inflate(R.layout.list_view, container, false);
		articles = this.getArguments().getParcelableArrayList("liste_data");
		maListViewPerso = (ListView) view.findViewById(R.id.listviewperso);
		this.load_content();
		return (view);
	}

	public void load_content() {
		// on sauve
		state = maListViewPerso.onSaveInstanceState();

		// On attribut à notre listView l'adapter que l'on vient de créer
		maListViewPerso.setAdapter(new ArticleAdapter(this.getActivity(), this.get_datas() ,articles));

		// Enfin on met un écouteur d'évènement sur notre listView
		maListViewPerso.setOnItemClickListener(this);
		maListViewPerso.setOnItemLongClickListener(this);
		maListViewPerso.onRestoreInstanceState(state);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		this.marquer_comme_lu(articles.get(pos).getUri(),true);
		Article art = articles.get(pos);
		Log.d("ASI", "Load article : " + art.getUri());
		listener.OnArticleSelected(articles.get(pos), pos);
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos,
			long arg3) {
		final int position = pos;
		final Article art = articles.get(pos);
		boolean is_vue = this.get_datas().contain_articles_lues(art.getUri());
		String texte = "Marquer comme lu";
		if (is_vue)
			texte = "Marquer comme non lu";
		final CharSequence[] items = { "Visualiser", "Partager",
				texte};
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this.getActivity());
		builder.setTitle(art.getTitle());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Visualiser")) {
					FragmentListArticle.this.marquer_comme_lu(art.getUri(),true);
					FragmentListArticle.this.listener.OnArticleSelected(art,
							position);
				} else if (items[item].equals("Partager")) {
					FragmentListArticle.this.partage(art.getUri(),
							art.getTitle());
				} else if (items[item].equals("Marquer comme lu")) {
					FragmentListArticle.this.marquer_comme_lu(art.getUri(),true);
				}else{
					FragmentListArticle.this.marquer_comme_lu(art.getUri(),false);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
		return false;
	}

	private void partage(String url, String titre) {
		// TODO Auto-generated method stub
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_TEXT,
				"Un article interessant sur le site arretsurimage.net :\n" + titre
						+ "\n" + url);
		emailIntent.setType("text/plain");
		startActivity(Intent.createChooser(emailIntent, "Partager cet article"));
	}

	private void marquer_comme_lu(String url,boolean sens) {
		if(sens)
			this.get_datas().add_articles_lues(url);
		else
			this.get_datas().remove_articles_lues(url);
		state = maListViewPerso.onSaveInstanceState();
		this.load_content();
		maListViewPerso.onRestoreInstanceState(state);
	}
	
}
