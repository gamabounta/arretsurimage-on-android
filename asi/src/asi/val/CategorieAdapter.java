package asi.val;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategorieAdapter extends BaseAdapter {
	
	List<Categorie> categories;
	LayoutInflater inflater;
	boolean desc;
	boolean date;
	SharedDatas shared;

	public CategorieAdapter(Context context, SharedDatas shared, List<Categorie> categories) {
		inflater = LayoutInflater.from(context);
		this.categories = categories;
		desc=shared.isDescriptionEnabled();
		date = shared.isDateEnabled();
		this.shared = shared;
	}
	
	public int getCount() {
		return categories.size();
	}

	public Object getItem(int position) {
		return categories.get(position);
	}

	public long getItemId(int positon) {
		return positon;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		//Mise à jour de la vue ou nouvel vue
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.categorie_listes, null);
			holder.viewColor = convertView.findViewById(R.id.cat_color);
			holder.viewTitre = (TextView) convertView.findViewById(R.id.cat_title);
			holder.viewImage = (ImageView) convertView.findViewById(R.id.cat_image);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Categorie current = categories.get(position);
		holder.viewTitre.setText(current.getTitre());
		holder.viewColor.setBackgroundColor(Color.parseColor(current.getColor()));
		holder.viewImage.setImageResource(current.getImage());

		return convertView;
	}
	
	private class ViewHolder {
		View viewColor;
		TextView viewTitre;
		ImageView viewImage;
	};
}
