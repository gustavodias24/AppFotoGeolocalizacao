package benicio.soluces.aplicativotestebencio;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    public static String imageToBase64(Uri imageUri, Context c) {
        try {
            InputStream inputStream = c.getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
