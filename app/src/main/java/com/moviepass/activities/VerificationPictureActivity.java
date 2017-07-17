package com.moviepass.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.moviepass.R;
import com.moviepass.network.RestClient;

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

/**
 * Created by anubis on 7/17/17.
 */

public class VerificationPictureActivity extends AppCompatActivity {

    private Camera camera;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_picture);
    }

    /*
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


    public void takePicture() {
        camera.takePicture(null, null, mPicture);

        mReTakePictureButton.setVisibility(View.VISIBLE);
        mSubmitPictureButton.setVisibility(View.VISIBLE);

        mTakePictureButton.setVisibility(View.GONE);
        mBottomText.setText(R.string.activity_ticket_verification_ticket_camera_info_bottom_alt);
    }

    public void reTakePicture() {
        refreshCamera();

        mReTakePictureButton.setVisibility(View.GONE);
        mSubmitPictureButton.setVisibility(View.GONE);

        mTakePictureButton.setVisibility(View.VISIBLE);
        mBottomText.setText(R.string.activity_ticket_verification_ticket_camera_info_bottom);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            int angleToRotate = getRoatationAngle(VerificationActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK);
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

            mSubmitPictureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mProgress.setVisibility(View.VISIBLE);
                    mSubmitPictureButton.setEnabled(false);

                    final Intent intent = getIntent();

                    //MetaData
                    final ObjectMetadata objectMetadata = new ObjectMetadata();

                    if (mToken != null) {

                        final String fileKey = String.valueOf(mToken.getReservation().getId());

                        //AWS S3 upload
                        try {
                            String reservationId = String.valueOf(mToken.getReservation().getId());
                            String showTime = String.valueOf(mToken.getTime());
                            String movieTitle = URLEncoder.encode(mToken.getScreening() != null ? mToken.getScreening().getTitle() : "", "UTF-8");
                            String theaterName = URLEncoder.encode(mToken.getScreening().getTheaterName(), "UTF-8");
                            URLEncoder.encode(Build.MODEL, "UTF-8");
                            String reservationKind = mToken.getScreening().getKind();
                            String movieId = String.valueOf(mToken.getScreening().getMovieId());
                            String theaterId = String.valueOf(mToken.getScreening().getTribuneTheaterId());

                            //Setting MetaData
                            objectMetadata.setUserMetadata(metaDataMap(reservationId, showTime, movieId, movieTitle, theaterId, theaterName, reservationKind));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        //Upload
                        uploadAWSFile(getPictureFile, fileKey, objectMetadata);
                    }

                    //Test if value from get extras is null
                    String test = String.valueOf(intent.getStringExtra("movieTitle"));

                    if (mToken == null && test != null) {

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
        mSubmitPictureButton.setEnabled(false);

        //Staging Bucket
        //TransferObserver observer = transferUtility.upload(Constants.STAGING_BUCKET, fileKey, file, objectMetadata);

        //Production Bucket
        TransferObserver observer = transferUtility.upload(Constants.PRDUCTION_BUCKET, fileKey, file, objectMetadata);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if(isActivityFinished)
                    return;

                if (state == TransferState.COMPLETED) {
                    Log.i(TAG, "id: " + id + " State: " + state);
                    mProgress.setVisibility(View.GONE);

                    Intent intent = getIntent();

                    if (mToken != null) {
                        int reservationId = mToken.getReservation().getId();
                        VerificationRequest ticketVerificationRequest = new VerificationRequest();

                        RestClient.getAuthenticated().verifyTicket(reservationId, ticketVerificationRequest).enqueue(new Callback<VerificationResponse>() {
                            @Override
                            public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                                Intent confirmationIntent = new Intent(VerificationActivity.this, VerificationConfirmationActivity.class);
                                startActivity(confirmationIntent);
                                finish();
                            }

                            @Override
                            public void onFailure(Call<VerificationResponse> call, Throwable t) {
                            }
                        });
                    } else if (intent.getExtras() != null) {
                        int reservationId = intent.getIntExtra("reservationId", 0);
                        VerificationRequest ticketVerificationRequest = new TicketVerificationRequest();

                        RestClient.getAuthenticated().verifyTicket(reservationId, ticketVerificationRequest).enqueue(new Callback<VerificationResponse>() {
                            @Override
                            public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                                Intent confirmationIntent = new Intent(VerificationActivity.this, VerificationConfirmationActivity.class);
                                startActivity(confirmationIntent);
                                finish();
                            }

                            @Override
                            public void onFailure(Call<VerificationResponse> call, Throwable t) {
                            }
                        });
                    }

                } else if (state == TransferState.FAILED) {
                    Toast.makeText(VerificationActivity.this, "Uploading Failed", Toast.LENGTH_LONG).show();
                    mProgress.setVisibility(View.GONE);
                    mSubmitPictureButton.setEnabled(true);
                } else if (state == TransferState.CANCELED) {
                    Toast.makeText(VerificationActivity.this, "Uploading Cancelled", Toast.LENGTH_LONG).show();
                    mProgress.setVisibility(View.GONE);
                    mSubmitPictureButton.setEnabled(true);
                } else if (state == TransferState.IN_PROGRESS) {
                    Toast.makeText(VerificationActivity.this, "Uploading", Toast.LENGTH_LONG).show();
                }


            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.i(TAG, " id:" + id + " Bytes Current:" + bytesCurrent + " Total" + bytesTotal);

            }

            @Override
            public void onError(int id, Exception ex) {
                if(isActivityFinished)
                    return;

                Log.e(TAG, "id:" + id);
                ex.printStackTrace();
                mProgress.setVisibility(View.GONE);
                mSubmitPictureButton.setEnabled(true);

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
