package asi.val;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;

// To be compatible with Android 2.1 need to create
// wrapper class for WrapThumbnailUtils.
public class WrapThumbnailUtils {
	
	@SuppressLint("NewApi")
	public static Bitmap createVideoThumbnail(String filePath) {
		return ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.MICRO_KIND);
	}
}
