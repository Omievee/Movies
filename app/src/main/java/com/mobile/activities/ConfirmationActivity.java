package com.mobile.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import com.helpshift.support.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.application.Application;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.requests.VerificationRequest;
import com.mobile.responses.ChangedMindResponse;
import com.mobile.responses.UserInfoResponse;
import com.mobile.responses.VerificationResponse;
import com.mobile.utils.AppUtils;
import com.moviepass.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.helpshift.util.constants.KeyValueStoreColumns.key;

/**
 * Created by anubis on 6/20/17.
 */

public class ConfirmationActivity extends BaseActivity implements GestureDetector.OnGestureListener {
    public static final int REQUEST_CAMERA_CODE = 0;
    Reservation reservation;
    Screening screening;
    ScreeningToken screeningToken;
    View progress;
    ProgressBar whiteProgress;
    ImageView scanTicket, downArrow;
    String ZIP;
    TransferUtility transferUtility;
    Bitmap photo;
    TextView noCurrentRes, pendingTitle, pendingLocal, pendingTime, pendingSeat, confirmCode, zip, verifyText, noStub;
    Button cancelButton;
    RelativeLayout pendingData, StandardTicket, ETicket, verifyTicketFlag, verifyMsgExpanded;
    GestureDetector gestureScanner;
    String uploadKey;

    private native static String getProductionBucket();

    private native static String getStagingBucket();


    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };

    private static String STORAGE_PERMISSIONS[] = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    protected BottomNavigationView bottomNavigationView;

    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_confirmation);


        bottomNavigationView = findViewById(R.id.CONFIRMED_BOTTOMNAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        transferUtility = TransferUtility.builder()
                .context(getApplicationContext())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(((Application) getApplicationContext()).getAmazonS3Client())
                .build();

        whiteProgress = findViewById(R.id.white_progress);
        progress = findViewById(R.id.confirm_progress);
        screeningToken = Parcels.unwrap(getIntent().getParcelableExtra(Constants.TOKEN));
        screening = screeningToken.getScreening();
        reservation = screeningToken.getReservation();
        String screeningTime = screeningToken.getTime();
        noStub = findViewById(R.id.ConfirmNotStub);
        verifyTicketFlag = findViewById(R.id.VerifyTicketFLag);
        noCurrentRes = findViewById(R.id.NO_Current_Res);
        pendingTitle = findViewById(R.id.PendingRes_Title);
        pendingLocal = findViewById(R.id.PendingRes_Location);
        pendingTime = findViewById(R.id.PendingRes_Time);
        pendingSeat = findViewById(R.id.PendingRes_Seat);
        StandardTicket = findViewById(R.id.STANDARD_TICKET);
        ETicket = findViewById(R.id.E_TICKET);
        confirmCode = findViewById(R.id.ConfirmCode);
        cancelButton = findViewById(R.id.PEndingRes_Cancel);
        zip = findViewById(R.id.PendingZip);
        pendingData = findViewById(R.id.PENDING_DATA);
        scanTicket = findViewById(R.id.TicketScan);
        downArrow = findViewById(R.id.Hide);
        verifyMsgExpanded = findViewById(R.id.VerifyTicketMSG);
        verifyText = findViewById(R.id.smallTextFlag);

        gestureScanner = new GestureDetector(this);


        pendingTitle.setText(screeningToken.getScreening().getTitle());
        pendingLocal.setText(screeningToken.getScreening().getTheaterName());
        pendingTime.setText(screeningTime);
        userData();

        if (screeningToken.getConfirmationCode() != null) {
            ETicket.setVisibility(View.VISIBLE);
            String code = screeningToken.getConfirmationCode();
            confirmCode.setText(code);
            if (screeningToken.getSeatName() != null) {
                pendingSeat.setVisibility(View.VISIBLE);
                pendingSeat.setText("Seat: " + screeningToken.getSeatName());
            }
        } else {
            StandardTicket.setVisibility(View.VISIBLE);
            if (UserPreferences.getProofOfPurchaseRequired() || screeningToken.getScreening().isPopRequired()) {
                verifyTicketFlag.setVisibility(View.VISIBLE);
                expand(verifyMsgExpanded);
                bottomNavigationView.setVisibility(View.GONE);
                verifyTicketFlag.setOnTouchListener((v, event) -> {
                    if (verifyText.getVisibility() == View.INVISIBLE) {
                        collapse(verifyMsgExpanded);
                        fadeIn(verifyText);
                        verifyText.setVisibility(View.VISIBLE);
                    } else {
                        expand(verifyMsgExpanded);
                        fadeOut(verifyText);
                        verifyText.setVisibility(View.INVISIBLE);
                    }
                    return gestureScanner.onTouchEvent(event);
                });

                scanTicket.setOnClickListener(v -> {
                    Log.d(Constants.TAG, "onCreate: ");
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(CAMERA_PERMISSIONS, Constants.REQUEST_CAMERA_CODE);
                        }
                    } else {
                        scan_Ticket();
                    }
                });

                noStub.setOnClickListener(v -> {
                    int res = screeningToken.getReservation().getId();
                    Intent noStubIntent = new Intent(ConfirmationActivity.this, TicketVerification_NoStub.class);
                    noStubIntent.putExtra(Constants.SCREENING, res);
                    startActivity(noStubIntent);
                });
            }
        }


        cancelButton.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            ChangedMindRequest request = new ChangedMindRequest(reservation.getId());
            RestClient.getAuthenticated().changedMind(request).enqueue(new RestCallback<ChangedMindResponse>() {
                @Override
                public void onResponse(Call<ChangedMindResponse> call, Response<ChangedMindResponse> response) {
                    ChangedMindResponse responseBody = response.body();
                    progress.setVisibility(View.GONE);

                    if (responseBody != null && responseBody.getMessage().matches("Failed to cancel reservation: You have already purchased your ticket.")) {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());

                            Toast.makeText(ConfirmationActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG);
                        } catch (Exception e) {
                        }
                    } else if (responseBody != null && responseBody.getMessage().matches("Failed to cancel reservation: You do not have a pending reservation.")) {
                        finish();
                    } else if (responseBody != null && response.isSuccessful()) {
                        Toast.makeText(ConfirmationActivity.this, responseBody.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());

                            Toast.makeText(ConfirmationActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                }

                @Override
                public void failure(RestError restError) {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(ConfirmationActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CAMERA_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scan_Ticket();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "You must grant permissions to continue", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == Constants.REQUEST_STORAGE_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createImageFile();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "You must grant permissions to continue", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            if (data.getExtras() != null) {
                photo = (Bitmap) data.getExtras().get("data");
                if (ContextCompat.checkSelfPermission(ConfirmationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(STORAGE_PERMISSIONS, Constants.REQUEST_STORAGE_CODE);
                    }
                } else {
                    createImageFile();
                }

            }

        }

    }

    /* Bottom Navigation View */

    int getContentViewId() {
        return R.layout.activity_movies;
    }

    int getNavigationMenuItemId() {
        return R.id.action_movies;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    startActivity(new Intent(ConfirmationActivity.this, ProfileActivity.class));
                } else if (itemId == R.id.action_movies) {
                } else if (itemId == R.id.action_theaters) {
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(ConfirmationActivity.this, SettingsActivity.class));
                }
                finish();
            }
        }, 300);
        return true;

//        else if (itemId == R.id.action_reservations) {
//            startActivity(new Intent(ConfirmationActivity.this, ReservationsActivity.class));
    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);

    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }

    public void userData() {
        int userId = UserPreferences.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                userInfoResponse = response.body();
                if (userInfoResponse != null) {
                    String address = userInfoResponse.getShippingAddressLine2();
                    List<String> addressList = Arrays.asList(address.split(",", -1));

                    for (int i = 0; i < addressList.size(); i++) {
                        ZIP = addressList.get(2);
                        zip.setText(ZIP);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
            }
        });
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }


    public void scan_Ticket() {
        Intent ticketVerif = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(ticketVerif, Constants.REQUEST_TICKET_VERIF);
    }

    public void createImageFile() {
        Handler handler = new Handler();
        whiteProgress.setVisibility(View.VISIBLE);
        scanTicket.setVisibility(View.INVISIBLE);
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
        ObjectMetadata objectMetadata = new ObjectMetadata();

        Log.d(Constants.TAG, "uploadToAWS:  " + screeningToken);
        if (screeningToken != null) {
            uploadKey = String.valueOf(screeningToken.getReservation().getId());

            try {
                String reservationId = String.valueOf(screeningToken.getReservation().getId());
                String showTime = screeningToken.getTime();
                String movieTitle = screeningToken.getScreening().getTitle();
                String theaterName = screeningToken.getScreening().getTheaterName();
                URLEncoder.encode(Build.MODEL, "UTF-8");
                String reservationKind = screeningToken.getScreening().getKind();
                String movieId = String.valueOf(screeningToken.getScreening().getMoviepassId());
                String theaterId = String.valueOf(screeningToken.getScreening().getTribuneTheaterId());

                //Setting MetaData
                objectMetadata.setUserMetadata(metaDataMap(reservationId, showTime, movieId, movieTitle, theaterId, theaterName, reservationKind));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        TransferObserver observer = transferUtility.upload(getProductionBucket(), uploadKey, ticketPhoto, objectMetadata);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(Constants.TAG, "STATUS???: " + state);
                if (state == TransferState.COMPLETED) {
                    int reservationId = screeningToken.getReservation().getId();
                    VerificationRequest ticketVerificationRequest = new VerificationRequest();
                    RestClient.getAuthenticated().verifyTicket(reservationId, ticketVerificationRequest).enqueue(new Callback<VerificationResponse>() {
                        @Override
                        public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                            if (response != null && response.isSuccessful()) {
                                whiteProgress.setVisibility(View.GONE);
                                Toast.makeText(ConfirmationActivity.this, "You ticketIconFinal stub has been submitted", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                JSONObject jObjError = null;
                                try {
                                    jObjError = new JSONObject(response.errorBody().string());
                                    if (jObjError.getString("message").equals("Verification status is different from PENDING_SUBMISSION")) {
                                        progress.setVisibility(View.GONE);
                                        Toast.makeText(ConfirmationActivity.this, "You ticketIconFinal stub has been submitted", Toast.LENGTH_LONG).show();
                                        finish();
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
                            whiteProgress.setVisibility(View.GONE);
                            Toast.makeText(ConfirmationActivity.this, "Server Error. Try Again", Toast.LENGTH_SHORT).show();
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

    private HashMap<String, String> metaDataMap(@NonNull String reservationId, @NonNull String showTime, @NonNull String movieId, @NonNull String movieTitle,
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
