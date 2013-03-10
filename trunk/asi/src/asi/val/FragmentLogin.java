package asi.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class FragmentLogin extends FragmentAsiBase implements OnClickListener {

	private EditText txt_username;

	private EditText txt_password;

	private TextView txt_message;

	private ViewSwitcher viewSwitch;

	private OnAuthenfiedListener listener;
	
	public interface OnAuthenfiedListener {
		public void OnAuthenfied();
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	this.listener = (OnAuthenfiedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnAuthenfiedListener");
        }
    }
    
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putCharSequence("user", txt_username.getText());
		outState.putCharSequence("pass", txt_password.getText());
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("lemonde", "onCreateView FragmentListArticle");
		View view = inflater.inflate(R.layout.login_abonne, container, false);

		txt_username = (EditText) view.findViewById(R.id.txt_username);
		txt_password = (EditText) view.findViewById(R.id.txt_password);
		viewSwitch = (ViewSwitcher) view.findViewById(R.id.loadSwitcher);
		txt_message = (TextView) view.findViewById(R.id.message_login);
		Button button_login = (Button) view.findViewById(R.id.login_button);
		button_login.setOnClickListener(this);

		// récupération des préférences
		if (savedInstanceState == null) {
			txt_username.setText(this.get_datas().getUsername());
			txt_password.setText(this.get_datas().getPassword());
		}else{
			txt_username.setText(savedInstanceState.getCharSequence("user"));
			txt_password.setText(savedInstanceState.getCharSequence("pass"));
		}
		//viewSwitch.showPrevious();
		return (view);
	}

	public void onClick(View arg0) {
		try {
			txt_message.setText("");
			txt_message.setTextColor(getResources()
					.getColor(R.color.color_text));
			if (txt_username.getText().toString().equalsIgnoreCase(""))
				throw new StopException("Login vide");
			if (txt_password.getText().toString().equalsIgnoreCase(""))
				throw new StopException("Mot de passe vide");

			StringBuilder donnees = new StringBuilder("");
			donnees.append(URLEncoder.encode("username", "UTF-8"));
			donnees.append("="
					+ URLEncoder.encode(txt_username.getText().toString(),
							"UTF-8") + "&");
			donnees.append(URLEncoder.encode("password", "UTF-8"));
			donnees.append("="
					+ URLEncoder.encode(txt_password.getText().toString(),
							"UTF-8"));
			String donneeStr = donnees.toString();
			new get_cookies_value().execute(donneeStr);
			viewSwitch.showNext();
			txt_message.setText("Connexion...");
			txt_username.setEnabled(false);
			txt_password.setEnabled(false);
		} catch (StopException e) {
			txt_message.setText(e.toString());
			txt_message.setTextColor(getResources().getColor(
					R.color.color_rouge));
		} catch (Exception e) {
			new DialogError(this.getActivity(), "Connexion au site", e).show();
		}
	}

	protected void finishAutentification(String cookies) {
		viewSwitch.showPrevious();
		txt_username.setEnabled(true);
		txt_password.setEnabled(true);
		// On vérifie l'authentification
		if (!cookies.matches(".*phorum_session_v5.*")) {
			txt_message.setText(cookies);
			txt_message.setTextColor(getResources().getColor(
					R.color.color_rouge));
			this.get_datas().setAuthentification("", "", "phorum_session_v5=deleted");
			return;
		}
		// on sauve les préférences car le login/pass ok
		txt_message.setText("Connecté");
		txt_message.setTextColor(getResources().getColor(
				R.color.color_text));
		this.get_datas().setAuthentification(txt_username.getText().toString(),
				txt_password.getText().toString(), cookies);
		// on lance les actions
		this.listener.OnAuthenfied();
	}


	private class get_cookies_value extends AsyncTask<String, Void, String> {

		private BufferedReader in;
		private OutputStreamWriter out;

		// can use UI thread here
		protected void onPreExecute() {
			in = null;
			out = null;
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				URL url_login = new URL(
						"http://www.arretsurimages.net/forum/login.php");
				HttpURLConnection connection = (HttpURLConnection) url_login
						.openConnection();
				connection.setDoOutput(true);
				connection.setInstanceFollowRedirects(false);

				// On écrit les données via l'objet OutputStream
				out = new OutputStreamWriter(connection.getOutputStream());
				out.write(args[0]);
				out.flush();

				in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				// sb.append("<p>"+connection.getHeaderField("Location")+"</p>");
				// récuperation des cookies
				String cookies = connection.getHeaderField("Set-Cookie");
				if (cookies == null)
					throw new StopException("Problème de cookies");
				else {
					StringTokenizer st = new StringTokenizer(cookies, ";");
					if (st.hasMoreTokens()) {
						String token = st.nextToken();

						String name = token.substring(0, token.indexOf("="))
								.trim();

						String value = token.substring(token.indexOf("=") + 1,
								token.length()).trim();
						if (name.equalsIgnoreCase("phorum_session_v5")) {
							if (value.equalsIgnoreCase("deleted"))
								throw new StopException(
										"Login / mot de passe incorrect");
							// else
							// throw new
							// StopException("Login / mot de passe ok");
						}

					}
				}
				connection.disconnect();
				return cookies;
			} catch (StopException e) {
				return (e.toString());
			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				return (error);
			} finally {
				// Dans tous les cas on ferme le bufferedReader s'il n'est pas
				// null
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

		}

		protected void onPostExecute(String mess) {
			FragmentLogin.this.finishAutentification(mess);
		}
	}

}
