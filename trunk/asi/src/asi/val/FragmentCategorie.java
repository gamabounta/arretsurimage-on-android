package asi.val;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FragmentCategorie extends FragmentAsiBase  implements OnItemClickListener {
    private OnCategorieSelectedListener listener;
	private ListView maListViewPerso;
	private ArrayList<Categorie> categories;

    public interface OnCategorieSelectedListener {
        public void onCategorieSelected(ArrayList<Categorie> cats, int pos);
    }
    
	public static FragmentCategorie newInstance(ArrayList<Categorie> categories) {
		FragmentCategorie fragment = new FragmentCategorie();
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("categorie_data", categories);
		fragment.setArguments(bundle);
		return fragment;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	this.listener = (OnCategorieSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCategorieSelectedListener");
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("asi", "OnCreateView FragmentCategorie");
		View view = inflater.inflate(R.layout.list_view,
                container, false);
		categories = this.getArguments().getParcelableArrayList("categorie_data");
		maListViewPerso = (ListView) view.findViewById(R.id.listviewperso);
		this.load_content();
		return(view);
	}

	public void load_content() {
		// Création de l'adaptateur
		CategorieAdapter adap = new CategorieAdapter(this.getActivity(), this.get_datas(), categories);

		// On attribut à notre listView l'adapter que l'on vient de créer
		maListViewPerso.setAdapter(adap);

		// Enfin on met un écouteur d'évènement sur notre listView
		maListViewPerso.setOnItemClickListener(this); 
	}
	
	public ListView getListView(){
		return maListViewPerso;
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {				
		listener.onCategorieSelected(categories, position);
	}
	
	
}
