package asi.val;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class ServiceDownload extends Service {

	private NotificationManager mNM;

	protected AutoUpdated update;

	private ArrayList<DownloadVideoThread> downloading;

	private DownloadVideoThread currentDownload;

	private int currentView;

	private boolean dlsync;

	private boolean firstStart;

	private int idFinish = 1000;

	private final int STATUS_BAR_NOTIFICATION = 1;

	private Notification noti;

	private String titre = "Téléchargement vidéo ASI";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		Log.d("ASI", "Service download onCreate");
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		downloading = new ArrayList<DownloadVideoThread>();
		currentDownload = null;
		dlsync = false;
		currentView = 0;
		firstStart = true;

		// ceration de la notification
		this.noti = new Notification(R.drawable.telechargement_menu_top, titre,
				System.currentTimeMillis());
		Intent notiIntent = new Intent(getApplicationContext(),
				ServiceDownload.class);
		// notiIntent.setAction(STOP_DOWNLOAD);
		notiIntent.setAction(Long.toString(System.currentTimeMillis()));
		notiIntent.putExtra("Stop", "hh");
		PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0,
				notiIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.notification_progress);
		contentView.setTextViewText(R.id.status_text,
				"Téléchargement vidéo ASI");
		contentView.setTextViewText(R.id.desc_text, "");
		contentView.setTextViewText(R.id.desc_title, "En preparation");
		contentView.setProgressBar(R.id.status_progress, 100, 0, false);
		noti.contentView = contentView;
		noti.contentIntent = pi;
		noti.flags |= Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_NO_CLEAR;
		mNM.notify(STATUS_BAR_NOTIFICATION, noti);

		// demarrage du thread de mise à jour
		update = new AutoUpdated();
		update.start();
	}

	@Override
	public void onDestroy() {
		Log.d("ASI", "Service download onDestroy");
		update.stop_update();
		mNM.cancel(STATUS_BAR_NOTIFICATION);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		handleStartCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		handleStartCommand(intent);
		return (START_STICKY);
	}

	private void handleStartCommand(Intent intent) {
		if (intent == null)
			;
		else if (intent.getExtras().getString("Stop") != null) {
			Log.d("ASI", "Intent to Stop download");
			for (DownloadVideoThread dvid : this.downloading)
				dvid.Stop_download();
			if (this.currentDownload != null) {
				this.currentDownload.Stop_download();
				this.currentDownload=null;
			}
			Toast.makeText(getApplicationContext(), "Arrêt des téléchargements",
			Toast.LENGTH_LONG).show();
			this.stopSelf();
		} else {
			Log.d("ASI", "Service download onStart");
			if (firstStart) {
				dlsync = intent.getExtras().getBoolean("dlsync");
				firstStart = false;
			}
			Video vid = intent.getExtras().getParcelable("video");
			DownloadVideoThread d = new DownloadVideoThread(vid);
			this.downloading.add(d);
			if (dlsync)
				d.start();
		}
	}
	
	private void notifiedFinishVideo(DownloadVideoThread dvid, boolean success) {
		String desc = "";
		String titre = dvid.get_download_video().getShortTitle_and_number();
		if (success) {
			desc = "Téléchargement terminé";
			NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(dvid.get_download_path()),
					"video/*");
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					intent, 0);
			notification.setSmallIcon(R.drawable.video_menu_top);
			notification.setContentTitle(titre);
			notification.setContentText(desc);
			notification.setAutoCancel(false);
			notification.setContentIntent(contentIntent);
			mNM.notify(idFinish, notification.build());
		} else {
			desc = "Téléchargement intérompu";
			NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
			Intent i = new Intent(getApplicationContext(),
					ServiceDownload.class);
			i.setAction(Long.toString(System.currentTimeMillis()));
			i.putExtra("video", dvid.get_download_video());
			i.putExtra("dlsync", dlsync);
			PendingIntent contentIntent = PendingIntent.getService(this, 0, i,
					PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setSmallIcon(R.drawable.telechargement_video_menu_top);
			notification.setContentTitle(titre);
			notification.setContentText(desc);
			notification.setAutoCancel(true);
			notification.setContentIntent(contentIntent);
			mNM.notify(idFinish, notification.build());
		}
		idFinish++;
	}

	private void addVideoToMedia(DownloadVideoThread dvid){
		// Ajout de la vidéo au système de lecture
		try {
			MediaScannerConnection medconn = new MediaScannerConnection(
					this, null);
			medconn.connect();
			medconn.scanFile(dvid.get_download_path().getAbsolutePath(),
					null);
			medconn.disconnect();
		} catch (Exception e) {
			Log.e("ASI", "ERREUR d'ajout de la vidéo\n" + e.toString());
		}
	}

	protected void updatedNotification() {
		if (dlsync)
			this.updateNotificationOnParallel();
		else {
			this.updateNotificationOnSerie();
		}
	}

	private void updateNotificationOnParallel() {
		if (downloading.size() == 0) {
			this.stopSelf();
		} else {
			for (DownloadVideoThread dvid : downloading) {
				if (!dvid.isAlive()) {
					boolean state = dvid.get_error() == null;
					downloading.remove(dvid);
					this.notifiedFinishVideo(dvid, state);
					this.addVideoToMedia(dvid);
				}
			}
			if (this.currentView < downloading.size()) {
				DownloadVideoThread dvid = downloading.get(currentView);
				noti.contentView.setTextViewText(R.id.status_text, titre + ": "
						+ (this.currentView+1) + "/" + downloading.size());
				noti.contentView.setTextViewText(R.id.desc_text,
						dvid.get_pourcentage_download());
				noti.contentView.setTextViewText(R.id.desc_title, dvid
						.get_download_video().getShortTitle_and_number());
				noti.contentView.setProgressBar(R.id.status_progress, 100,
						dvid.getProgress(), false);
				mNM.notify(STATUS_BAR_NOTIFICATION, noti);
				this.currentView++;
			} else {
				this.currentView = 0;
			}
		}
	}

	private void updateNotificationOnSerie() {
		if (this.currentDownload == null) {
			// on arrete le service à la fin du téléchargement
			if (downloading.size() == 0) {
				this.stopSelf();
			} else {
				this.currentDownload = downloading.get(0);
				downloading.remove(0);
				this.currentDownload.start();
			}
		} else {
			if (this.currentDownload.isAlive()) {
				noti.contentView.setTextViewText(R.id.status_text, titre
						+ ": 1/" + (downloading.size() + 1));
				noti.contentView.setTextViewText(R.id.desc_text,
						this.currentDownload.get_pourcentage_download());
				noti.contentView.setTextViewText(R.id.desc_title,
						this.currentDownload.get_download_video()
								.getShortTitle_and_number());
				noti.contentView.setProgressBar(R.id.status_progress, 100,
						this.currentDownload.getProgress(), false);
				mNM.notify(STATUS_BAR_NOTIFICATION, noti);
			} else {
				boolean state = this.currentDownload.get_error() == null;
				DownloadVideoThread dvid = this.currentDownload;
				this.currentDownload = null;
				this.notifiedFinishVideo(dvid, state);
				this.addVideoToMedia(dvid);
			}
		}
	}

	class AutoUpdated extends Thread {

		private Boolean stop;

		public AutoUpdated() {
			this.stop = false;
		}

		public void run() {
			// Code exécuté dans le nouveau thread
			Log.d("ASI", "update_start");
			while (!stop) {
				try {
					Thread.sleep(3000);
					ServiceDownload.this.updatedNotification();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.e("ASI", "Error_thread" + e.toString());
				}
			}
			Log.d("ASI", "update_stop");
		}

		public void stop_update() {
			this.stop = true;
		}
	}

}
