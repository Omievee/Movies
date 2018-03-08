package com.mobile.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.activities.ConfirmationActivity;
import com.mobile.activities.MovieActivity;
import com.mobile.activities.TicketVerification_NoStub;
import com.mobile.application.Application;
import com.mobile.helpers.ContextSingleton;
import com.mobile.network.RestClient;
import com.mobile.requests.VerificationRequest;
import com.mobile.responses.VerificationResponse;
import com.mobile.utils.AppUtils;
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

    String mCurrentPhotoPath;
    private Uri imageUri;
    ImageView ticketScan;
    View root;
    int PERMISSION_ALL = 1;
    Bitmap photo;
    ProgressBar progress;
    TextView noStub;
    ObjectMetadata objectMetadata;
    String key;

    private native static String getProductionBucket();

    private native static String getStagingBucket();

    private TransferUtility transferUtility;

    public static final int REQUEST_CAMERA_CODE = 0;


    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };

    private static String STORAGE_PERMISSIONS[] = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public TicketVerificationDialog() {
    }

    public static TicketVerificationDialog newInstance(int resID, String movieTitle, String tribuneMovieId, String theaterName, String tribuneTheaterId, String showtime) {
        TicketVerificationDialog fragment = new TicketVerificationDialog();
        Bundle args = new Bundle();
        args.putInt("reservationId", resID);
        args.putString("mSelectedMovieTitle", movieTitle);
        args.putString("tribuneMovieId", tribuneMovieId);
        args.putString("mTheaterSelected", theaterName);
        args.putString("tribuneTheaterId", tribuneTheaterId);
        args.putString("showtime", showtime);
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
        transferUtility = TransferUtility.builder()
                .context(getActivity().getApplicationContext())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(((Application) getContext().getApplicationContext()).getAmazonS3Client())
                .build();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ContextSingleton.getInstance(getActivity()).getGlobalContext();


        ticketScan.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(CAMERA_PERMISSIONS, Constants.REQUEST_CAMERA_CODE);
            } else {
                scanTicket();
            }
        });

        noStub.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TicketVerification_NoStub.class);
            int res = getArguments().getInt("reservationId");
            intent.putExtra(Constants.SCREENING, res);
            startActivity(intent);
        });
    }


    public void scanTicket() {
        Intent ticketVerif = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(ticketVerif, Constants.REQUEST_TICKET_VERIF);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            if (data.getExtras() != null) {
                photo = (Bitmap) data.getExtras().get("data");
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(STORAGE_PERMISSIONS, Constants.REQUEST_STORAGE_CODE);
                } else {
                    createImageFile();
                }
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
            Toast.makeText(getActivity(), "You must grant permissions to continue", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == Constants.REQUEST_STORAGE_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createImageFile();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getActivity(), "You must grant permissions to continue", Toast.LENGTH_SHORT).show();

        }
    }

    public void createImageFile() {
        Handler handler = new Handler();
        progress.setVisibility(View.VISIBLE);
        ticketScan.setVisibility(View.INVISIBLE);
        handler.postDelayed(() -> {


            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            final byte[] bitmapdata = bos.toByteArray();

            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(Constants.TAG, "Error creating media file, test storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bitmapdata);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(Constants.TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {

                Log.d(Constants.TAG, "Error accessing file: " + e.getMessage());

            }
            //Turn into file
            final File getPictureFile = getOutputMediaFile();
            if (getPictureFile == null) {
                return;
            }
            Log.d(Constants.TAG, "onActivityResult: " + getPictureFile.getAbsolutePath());
            uploadToAWS(getPictureFile);
        }, 4000);

    }

    private void uploadToAWS(File ticketPhoto) {
        objectMetadata = new ObjectMetadata();
        key = String.valueOf(getArguments().getInt("reservationId"));

        try {
            if (getArguments() != null) {
                String reservationId = String.valueOf(getArguments().getInt("reservationId"));
                String showTime = getArguments().getString("showtime");
                String movieTitle = getArguments().getString("mSelectedMovieTitle");
                String theaterName = getArguments().getString("mTheaterSelected");
                URLEncoder.encode(Build.MODEL, "UTF-8");
                String reservationKind = "reskind";
                String tribuneMovieId = getArguments().getString("tribuneMovieId");
                String tribuneTheaterId = getArguments().getString("tribuneTheaterId");
                objectMetadata.setUserMetadata(metaDataMap(reservationId, showTime, tribuneMovieId, movieTitle, tribuneTheaterId, theaterName, reservationKind));
            }


            //Setting MetaData

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        TransferObserver observer = transferUtility.upload(getProductionBucket(), key, ticketPhoto, objectMetadata);
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
                                Toast.makeText(getActivity(), "You ticket stub has been submitted", Toast.LENGTH_LONG).show();
                                dismiss();
                            } else {
                                JSONObject jObjError = null;
                                try {
                                    jObjError = new JSONObject(response.errorBody().string());
                                    if (jObjError.getString("message").equals("Verification status is different from PENDING_SUBMISSION")) {
                                        progress.setVisibility(View.GONE);
                                        Toast.makeText(getActivity(), "You ticket stub has been submitted", Toast.LENGTH_LONG).show();
                                        dismiss();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VerificationResponse> call, Throwable t) {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), "Server Error. Try Again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d(Constants.TAG, "onError: ");
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

        return meta;
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MoviePass");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MoviePass", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg");

        return mediaFile;
    }


}



