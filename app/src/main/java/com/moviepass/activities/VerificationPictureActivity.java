package com.moviepass.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.application.Application;
import com.moviepass.model.Screening;
import com.moviepass.model.ScreeningToken;
import com.moviepass.network.RestClient;
import com.moviepass.requests.VerificationRequest;
import com.moviepass.responses.VerificationResponse;
import com.moviepass.utils.AppUtils;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 7/17/17.
 */

public class VerificationPictureActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    private native static String getProductionBucket();
    private native static String getStagingBucket();

    private static final String TAG = "TAG";
    public static final String TOKEN = "token";

    private boolean isPreview;
    private boolean isActivityFinished;

    private Camera camera;

    private AmazonS3 s3;
    private TransferUtility transferUtility;

    Screening screening;
    ScreeningToken token;

    RelativeLayout relativeLayout;
    ImageView buttonTakePicture;
    ImageView buttonRetakePicture;
    ImageView buttonSubmitPicture;
    View progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_picture);

        s3 = ((Application) getApplicationContext()).getAmazonS3Client();
        transferUtility = new TransferUtility(s3, getApplicationContext());

        Bundle extras = getIntent().getExtras();
        token = Parcels.unwrap(getIntent().getParcelableExtra(TOKEN));

        buttonTakePicture = findViewById(R.id.take_picture);
        buttonRetakePicture = findViewById(R.id.retake_picture);
        buttonSubmitPicture = findViewById(R.id.submit_picture);

        progress = findViewById(R.id.progress);
    }


    void addViewAndRemove(float x, float y) {
        mRelFocusViewContainer.removeAllViews();

        View view = getLayoutInflater().inflate(R.layout.view_focus_circle, null);
        view.setX(x - 30);
        view.setY(y - 30);
        mRelFocusViewContainer.addView(view);
        view.startAnimation(startFadeOutAnimation);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRelFocusViewContainer.removeAllViews();
            }
        }, 1000);
    }

    public static int getRotationAngle(Activity mContext, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;

        result = (info.orientation - degrees + 360) % 360;

        return result;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public void takePicture() {
        camera.takePicture(null, null, mPicture);

        buttonRetakePicture.setVisibility(View.VISIBLE);
        buttonSubmitPicture.setVisibility(View.VISIBLE);

        buttonTakePicture.setVisibility(View.GONE);
    }

    public void reTakePicture() {
        refreshCamera();

        buttonRetakePicture.setVisibility(View.GONE);
        buttonSubmitPicture.setVisibility(View.GONE);

        buttonTakePicture.setVisibility(View.VISIBLE);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            camera.stopPreview();
            isPreview = false;
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

            isPreview = true;
        } catch (Exception e) {

        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            int angleToRotate = getRotationAngle(VerificationPictureActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK);
            // Solve image inverting problem
            Bitmap orignalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap bitmapImage = rotate(orignalImage, angleToRotate);
            int nh = (int) (bitmapImage.getHeight() * (1024.0 / bitmapImage.getWidth()));
            Bitmap scaledImage = Bitmap.createScaledBitmap(bitmapImage, 1024, nh, true);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            scaledImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);


            final byte[] bitmapdata = bos.toByteArray();

            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bitmapdata);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            //Turn into file
            final File getPictureFile = getOutputMediaFile();
            if (getPictureFile == null) {
                return;
            }

            buttonSubmitPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progress.setVisibility(View.VISIBLE);
                    buttonSubmitPicture.setEnabled(false);

                    final Intent intent = getIntent();

                    //MetaData
                    final ObjectMetadata objectMetadata = new ObjectMetadata();

                    if (screening != null) {

                        final String fileKey = String.valueOf(token.getReservation().getId());

                        //AWS S3 upload
                        try {
                            String reservationId = String.valueOf(token.getReservation().getId());
                            String showTime = String.valueOf(token.getTime());
                            String movieTitle = URLEncoder.encode(token.getScreening() != null ? token.getScreening().getTitle() : "", "UTF-8");
                            String theaterName = URLEncoder.encode(token.getScreening().getTheaterName(), "UTF-8");
                            URLEncoder.encode(Build.MODEL, "UTF-8");
                            String reservationKind = token.getScreening().getKind();
                            String movieId = String.valueOf(token.getScreening().getMoviepassId());
                            String theaterId = String.valueOf(token.getScreening().getTribuneTheaterId());

                            //Setting MetaData
                            objectMetadata.setUserMetadata(metaDataMap(reservationId, showTime, movieId, movieTitle, theaterId, theaterName, reservationKind));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        //Upload
                        uploadAWSFile(getPictureFile, fileKey, objectMetadata);
                    } else {

                        Log.d("intextras", "intextras" + intent.getExtras());

                        final String fileKey = String.valueOf(intent.getIntExtra("reservationId", 0));

                        //AWS S3 upload
                        try {
                            String reservationId = String.valueOf(intent.getIntExtra("reservationId", 0));
                            String showTime = String.valueOf(intent.getStringExtra("showtime"));
                            String movieTitle = URLEncoder.encode(intent.getStringExtra("movieTitle"), "UTF-8");
                            String theaterName = URLEncoder.encode(intent.getStringExtra("theaterName"), "UTF-8");
                            URLEncoder.encode(Build.MODEL, "UTF-8");
                            String reservationKind = URLEncoder.encode(String.valueOf("STANDARD"), "UTF-8");
                            String movieId = String.valueOf(intent.getStringExtra("tribuneMovieId"));
                            String theaterId = String.valueOf(intent.getStringExtra("tribuneTheaterId"));

                            //Setting MetaData
                            objectMetadata.setUserMetadata(metaDataMap(reservationId, showTime, movieId, movieTitle, theaterId, theaterName, reservationKind));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        //Upload
                        uploadAWSFile(getPictureFile, fileKey, objectMetadata);
                    }
                }
            });
        }
    };

    private HashMap<String, String> metaDataMap(@NonNull String reservationId, @NonNull String showTime, @NonNull String movieId, @NonNull String movieTitle,
                                                @NonNull String theaterId, @NonNull String theaterName, String reservationKind ) {
        HashMap<String, String> meta = new HashMap<>();
        meta.put("reservation_id", reservationId);//reservationId
        meta.put("showtime", showTime);//ShowTime
        meta.put("movie_id", movieId);//Movie Id
        meta.put("movie_title", movieTitle);// MovieTitle
        meta.put("theater_id", theaterId);//TheaterId
        meta.put("theater_name", theaterName);//TheaterName
        meta.put("reservation_kind", reservationKind);//reservationKind
        meta.put("device_name", AppUtils.getDeviceName());//Device Name
        meta.put("os_version", AppUtils.getOsCodename());//OS VERSION
        meta.put("user_id", String.valueOf(UserPreferences.getUserId()));//UserId

        return  meta;
    }

    private void uploadAWSFile(File file, final String fileKey, ObjectMetadata objectMetadata) {
        buttonSubmitPicture.setEnabled(false);

        //Staging Bucket
        //TransferObserver observer = transferUtility.upload(getStagingBucket(), fileKey, file, objectMetadata);

        //Production Bucket
        TransferObserver observer = transferUtility.upload(getProductionBucket(), fileKey, file, objectMetadata);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (isActivityFinished)
                    return;

                if (state == TransferState.COMPLETED) {
                    Log.i(TAG, "id: " + id + " State: " + state);
                    progress.setVisibility(View.GONE);

                    Intent intent = getIntent();

                    if (screening != null) {
                        int reservationId = token.getReservation().getId();
                        VerificationRequest ticketVerificationRequest = new VerificationRequest();

                        RestClient.getAuthenticated().verifyTicket(reservationId, ticketVerificationRequest).enqueue(new Callback<VerificationResponse>() {
                            @Override
                            public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                                Intent confirmationIntent = new Intent(VerificationPictureActivity.this, VerificationConfirmationActivity.class);
                                startActivity(confirmationIntent);
                                finish();
                            }

                            @Override
                            public void onFailure(Call<VerificationResponse> call, Throwable t) {
                            }
                        });
                    } else if (intent.getExtras() != null) {
                        int reservationId = intent.getIntExtra("reservationId", 0);
                        VerificationRequest ticketVerificationRequest = new VerificationRequest();

                        RestClient.getAuthenticated().verifyTicket(reservationId, ticketVerificationRequest).enqueue(new Callback<VerificationResponse>() {
                            @Override
                            public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                                Intent confirmationIntent = new Intent(VerificationPictureActivity.this, VerificationConfirmationActivity.class);
                                startActivity(confirmationIntent);
                                finish();
                            }

                            @Override
                            public void onFailure(Call<VerificationResponse> call, Throwable t) {
                            }
                        });
                    }

                } else if (state == TransferState.FAILED) {
                    Snackbar snackbar = Snackbar.make(relativeLayout, "Uploading Failed", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    progress.setVisibility(View.GONE);
                    buttonSubmitPicture.setEnabled(true);
                } else if (state == TransferState.CANCELED) {
                    Snackbar snackbar = Snackbar.make(relativeLayout, "Uploading Cancelled", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    progress.setVisibility(View.GONE);
                    buttonSubmitPicture.setEnabled(true);
                } else if (state == TransferState.IN_PROGRESS) {
                    Snackbar snackbar = Snackbar.make(relativeLayout, "Uploading", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.i(TAG, " id:" + id + " Bytes Current:" + bytesCurrent + " Total" + bytesTotal);

            }

            @Override
            public void onError(int id, Exception ex) {
                if (isActivityFinished)
                    return;

                Log.e(TAG, "id:" + id);
                ex.printStackTrace();
                progress.setVisibility(View.GONE);
                buttonSubmitPicture.setEnabled(true);
            }
        });

    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MoviePass");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MoviePass", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                timeStamp + ".jpg");

        return mediaFile;
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                Log.i("tap_to_focus", "success!");
            } else {
                // do something...
                Log.i("tap_to_focus", "fail!");

            }
        }
    };
}
