package com.org.lsa.custom;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;
import com.org.lsa.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utility {
    private final static String TAG = Utility.class.getSimpleName();
    // 0-show, 1-not show
    public static final int showLogs
            = 0;
//            = 1;

    public static String imageWtsAppFilePath;
    public static String BASE_URL="https://ramkyapi.beulahsoftware.com/api/PaymentsDetails/";
    public static Uri imageWtsAppFilePathUri;
    public static String imageFilePath;
    public static String latitudeUStr = "0.0", longitudeUStr = "0.0";
    ;

    public static final SharedPreferences getSharedPreferences(Context context) {

        return context.getApplicationContext().getSharedPreferences("prefLSA", 0);

    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
       /* int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;*/
        int MAX_HEIGHT = 512;
        int MAX_WIDTH = 512;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage);
        return img;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(imageFilePath);
//        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static boolean isGrantedPermissionWRITE_EXTERNAL_STORAGE(Activity activity) {
        int version = Build.VERSION.SDK_INT;
        if (version <= 32) {
            boolean isAllowPermissionApi28 = ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            Log.i("general_em", "isGrantedPermissionWRITE_EXTERNAL_STORAGE() - isAllowPermissionApi28: " + isAllowPermissionApi28);
            return isAllowPermissionApi28;
        } else {
            boolean isAllowPermissionApi33 = Environment.isExternalStorageManager();
            Log.i("general_em", "isGrantedPermissionWRITE_EXTERNAL_STORAGE() - isAllowPermissionApi33: " + isAllowPermissionApi33);
            return isAllowPermissionApi33;
        }
    }

    public static String getDateTime_DD_MM_YYYY() {
//        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        /*String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
        DateFormat dateFormat = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            dateFormat = new SimpleDateFormat(pattern);
        }*/
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        Date date = new Date();
//        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

        String date = dateFormat.format(Calendar.getInstance().getTime());
        if (Utility.showLogs == 0)
            Log.d(TAG, "date " + date);

        return date;
//        return dateFormat.format(date);
    }

    public static Uri saveImageToInternalStorage(Context mContext, Bitmap bitmap) {

        String mTimeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());

        String mImageName = "snap_" + mTimeStamp + ".jpg";

        ContextWrapper wrapper = new ContextWrapper(mContext);

        File file = wrapper.getDir("Images", Context.MODE_PRIVATE);

        file = new File(file, "snap_" + mImageName);
//        file = new File(file, "snap_"+ mImageName+".jpg");

        try {

            OutputStream stream = null;

            stream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            stream.flush();

            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri mImageUri = Uri.parse(file.getAbsolutePath());

        return mImageUri;
    }

    public static void assignArrayAdpListToSpin(Context context, List array,
                                                Spinner spin) {
//        ArrayAdapter<String> arrayAdp = new ArrayAdapter<String>(context,
//                android.R.layout.simple_spinner_dropdown_item, array);
        ArrayAdapter<String> arrayAdp = new ArrayAdapter<String>(context,
                android.R.layout.simple_dropdown_item_1line, array);
        spin.setAdapter(arrayAdp);
        arrayAdp.setDropDownViewResource(com.org.lsa.R.layout.spinner_textview);
    }


    public static void checkGPSEnabled(Context context) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(100);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        LocationServices.getSettingsClient(context).checkLocationSettings(builder.build()).addOnCompleteListener(new CheckLocationSettingsListener(context));
    }

    static void checkGPSEnabled(Context context, Task task) {
        try {
            LocationSettingsResponse locationSettingsResponse = (LocationSettingsResponse) task.getResult(ApiException.class);
        } catch (ApiException e) {
            switch (e.getStatusCode()) {
                case 6:
                    try {
                        ((ResolvableApiException) e).startResolutionForResult((Activity) context, 1005);
                        return;
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                        return;
                    }
                default:
                    return;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static boolean isGPSEnabled1(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled("gps");
    }
    public static void showAlertDialog(final Context context, String title,
                                       String message, Boolean status) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Specify the alert dialog title
//        String titleText = "Say Hello!";

        // Initialize a new foreground color span instance
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.BLUE);

        // Initialize a new spannable string builder instance
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(title);

        // Apply the text color span
        ssBuilder.setSpan(
                foregroundColorSpan,
                0,
                title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Set the alert dialog title using spannable string builder
        builder.setTitle(ssBuilder);

//        builder.setTitle(title);

        if (status == true) {
            builder.setIcon(R.drawable.success_48x48);
        } else {
            builder.setIcon(R.drawable.fail_48x48);
        }

        builder.setMessage(message).setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
//                        ((Activity) context).startActivity(new Intentent(context,Menulist.class));
//                        ((Activity) context).finish();


                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {


                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);

            }
        });

        alert.show();


    }

    public static String getDate_DD_MM_YYYY() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
//        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        Date date = new Date();
//        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        if (Utility.showLogs == 0)
            Log.d(TAG, "date " + date);

        return date;
//        return dateFormat.format(date);
    }
}
