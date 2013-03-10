package asi.val;

import android.os.Parcel;
import android.os.Parcelable;

public class Categorie implements Parcelable {

	private String titre;

	private int image;

	private String url;
	
	private String subcat;
	
	private String color;

	public Categorie() {
		this.titre = "";
		this.url = "";
		this.subcat = "";
		this.color = "";
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSubCat() {
		return subcat;
	}

	public void setSubCat(String subcat) {
		this.subcat = subcat;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public int getImage() {
		return image;
	}
	
	public void setImage(int image) {
		this.image = image;
	}

	public String toString() {
		return (this.titre);
	}

	public static final Parcelable.Creator<Categorie> CREATOR = new Parcelable.Creator<Categorie>() {
		public Categorie createFromParcel(Parcel in) {
			return new Categorie(in);
		}

		public Categorie[] newArray(int size) {
			return new Categorie[size];
		}
	};

	private Categorie(Parcel in) {
		this.titre = in.readString();
		this.image = in.readInt();
		this.url = in.readString();
		this.color = in.readString();
		this.subcat = in.readString(); 
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(this.titre);
		out.writeInt(this.image);
		out.writeString(this.url);
		out.writeString(this.color);
		out.writeString(this.subcat);
	}

}
