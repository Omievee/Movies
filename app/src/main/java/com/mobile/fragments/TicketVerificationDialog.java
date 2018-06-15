package com.mobile.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.helpshift.support.Support;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.activities.TicketVerification_NoStub;
import com.mobile.application.Application;
import com.mobile.helpers.ContextSingleton;
import com.mobile.helpers.LogUtils;
import com.mobile.model.PopInfo;
import com.mobile.network.RestClient;
import com.mobile.requests.VerificationRequest;
import com.mobile.responses.VerificationResponse;
import com.mobile.utils.AppUtils;
import com.moviepass.BuildConfig;
import com.moviepass.R;

import org.json.JSONException;
import org.json.JSONObject;

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

import static android.app.Activity.RESULT_OK;

/**
 * Created by o_vicarra on 2/26/18.
 */

public class TicketVerificationDialog extends BottomSheetDialogFragment {
    public final String APP_TAG = "TicketVerification";
    ImageView ticketScan;
    View root;
    BitmapFactory.Options bmOptions;

    ProgressBar progress;
    TextView noStub, FAQs;
    ObjectMetadata objectMetadata;
    String key;
    Activity myActivity;
    Context myContext;
    File photoFile;
    String photoFileName = "TicketVerification.jpg";

    private TransferUtility transferUtility;
    private PopInfo popInfo;


    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };

    private static String STORAGE_PERMISSIONS[] = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public TicketVerificationDialog() {
    }

    public static TicketVerificationDialog newInstance(PopInfo pop) {
        TicketVerificationDialog fragment = new TicketVerificationDialog();
        Bundle args = new Bundle();
        args.putParcelable("popInfo", pop);
        fragment.setCancelable(false);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fr_ticketverification_banner, container, false);

        ticketScan = root.findViewById(R.id.TicketScan);
        progress = root.findViewById(R.id.progress);
        noStub = root.findViewById(R.id.NoStub);
        FAQs = root.findViewById(R.id.FAQs);
        transferUtility = TransferUtility.builder()
                .context(myActivity.getApplicationContext())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(((Application) myActivity.getApplicationContext()).getAmazonS3Client())
                .build();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        popInfo = getArguments().getParcelable("popInfo");

        ticketScan.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(CAMERA_PERMISSIONS, Constants.REQUEST_CAMERA_CODE);
            } else {
                scanTicket();
            }
        });

        FAQs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Support.showFAQSection(getActivity(), Constants.TICKET_VERIFICATION_FAQ_SECTION);
            }
        });

        noStub.setOnClickListener(v -> {
            Intent intent = new Intent(myActivity, TicketVerification_NoStub.class);
            int res = popInfo.getReservationId();
            intent.putExtra(Constants.SCREENING, res);
            startActivity(intent);
        });


    }


    public void scanTicket() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);
        Uri fileProvider = FileProvider.getUriForFile(myContext, getResources().getString(R.string.authority_file_provider), photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(myContext.getPackageManager()) != null) {
            LogUtils.newLog(Constants.TAG, "scanTicket: ");
            startActivityForResult(intent, Constants.REQUEST_CAMERA_CODE);

        }
    }

    private File getPhotoFileUri(String photoFileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(myContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            LogUtils.newLog(APP_TAG, "failed to create directory");
        }

        return new File(mediaStorageDir.getPath() + File.separator + photoFileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {

            if (photoFile != null) {
                bmOptions = new BitmapFactory.Options();

                Log.d(Constants.TAG, "onActivityResult: " + bmOptions);
                BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                int scaleFactor = Math.min(photoW / 1024, photoH / 1024);
                if (scaleFactor != 1) {
                    bmOptions.inSampleSize = scaleFactor;
                }
                bmOptions.inJustDecodeBounds = false;
                Bitmap image = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(photoFile);
                    LogUtils.newLog("compressing file " + photoFile.getAbsolutePath());
                    image.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                } catch (Exception ignored) {

                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    image.recycle();
                }


            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(STORAGE_PERMISSIONS, Constants.REQUEST_STORAGE_CODE);
                } else {
                    createFileForUpload();
                }
            } else {
                Log.d(Constants.TAG, "onActivityResult: " + bmOptions);
                createFileForUpload();
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Check permissions results.. one for camera, the other for storage
        if (requestCode == Constants.REQUEST_CAMERA_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanTicket();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(myActivity, "You must grant permissions to continue", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == Constants.REQUEST_STORAGE_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createFileForUpload();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(myActivity, "You must grant permissions to continue", Toast.LENGTH_SHORT).show();

        }
    }

    public void createFileForUpload() {
        Handler handler = new Handler();
        progress.setVisibility(View.VISIBLE);
        ticketScan.setVisibility(View.INVISIBLE);

        handler.postDelayed(() -> {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            final byte[] bitmapdata = bos.toByteArray();

            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bitmapdata);
                fos.close();
            } catch (FileNotFoundException e) {
                LogUtils.newLog(Constants.TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {

                LogUtils.newLog(Constants.TAG, "Error accessing file: " + e.getMessage());

            }
            //Turn into file
            final File getPictureFile = getOutputMediaFile();
            if (getPictureFile == null) {
                Toast.makeText(myActivity, "Failed to create File", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadToAWS(getPictureFile);
        }, 4000);
    }

    private void uploadToAWS(File ticketPhoto) {
        objectMetadata = new ObjectMetadata();
        key = String.valueOf(popInfo.getReservationId());
        String reservationId = String.valueOf(popInfo.getReservationId());
        String showTime = popInfo.getShowtime();
        String movieTitle = popInfo.getMovieTitle();
        String theaterName = popInfo.getTheaterName();
        String reservationKind = "reskind";
        String tribuneMovieId = popInfo.getTribuneMovieId();
        String tribuneTheaterId = popInfo.getTribuneTheaterId();
        objectMetadata.setUserMetadata(metaDataMap(reservationId, showTime, tribuneMovieId, movieTitle, tribuneTheaterId, theaterName, reservationKind));


        TransferObserver observer = transferUtility.upload(BuildConfig.BUCKET, key, ticketPhoto, objectMetadata);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    int reservationId = getArguments().getInt("reservationId");
                    VerificationRequest ticketVerificationRequest = new VerificationRequest();
                    RestClient.getAuthenticated().verifyTicket(reservationId, ticketVerificationRequest).enqueue(new Callback<VerificationResponse>() {
                        @Override
                        public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                            if (response != null && response.isSuccessful()) {
                                progress.setVisibility(View.GONE);
                                Toast.makeText(myActivity, "Your ticket stub has been submitted", Toast.LENGTH_LONG).show();
                                dismiss();
                            } else {
                                JSONObject jObjError = null;
                                try {
                                    jObjError = new JSONObject(response.errorBody().string());
                                    if (jObjError.getString("message").equals("Verification status is different from PENDING_SUBMISSION")) {
                                        progress.setVisibility(View.GONE);
                                        Toast.makeText(myActivity, "Your ticket stub has been submitted", Toast.LENGTH_LONG).show();
                                        dismiss();
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VerificationResponse> call, Throwable t) {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(myActivity, "Server Error. Try Again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                LogUtils.newLog(Constants.TAG, "onError: ");
            }
        });
    }

    private HashMap<String, String> metaDataMap(@NonNull String reservationId, @NonNull String
            showTime, @NonNull String movieId, @NonNull String movieTitle,
                                                @NonNull String theaterId, @NonNull String theaterName, String reservationKind) {
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
        meta.put("version_code", String.valueOf(BuildConfig.VERSION_CODE));
        meta.put("version_name", BuildConfig.VERSION_NAME);
        meta.put("os", "android");

        return meta;
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MoviePass");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                LogUtils.newLog("MoviePass", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg");
        return mediaFile;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
        myActivity = getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myContext = null;
    }
}



