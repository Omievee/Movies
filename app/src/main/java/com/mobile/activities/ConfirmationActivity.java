package com.mobile.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.GestureDetector;
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
import com.helpshift.support.Support;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.application.Application;
import com.mobile.helpers.LogUtils;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.network.RestClient;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.requests.VerificationRequest;
import com.mobile.responses.UserInfoResponse;
import com.mobile.responses.VerificationResponse;
import com.mobile.utils.AppUtils;
import com.moviepass.BuildConfig;
import com.moviepass.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.File;
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

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.text.TextUtils.isEmpty;

/**
 * Created by anubis on 6/20/17.
 */

public class ConfirmationActivity extends BaseActivity implements GestureDetector.OnGestureListener {
    public static final int REQUEST_CAMERA_CODE = 0;
    Reservation reservation;
    Screening screening;
    public final String APP_TAG = "TicketVerification";

    ScreeningToken screeningToken;
    View progress;
    ProgressBar whiteProgress;
    ImageView scanTicket, downArrow;
    String ZIP;
    TransferUtility transferUtility;
    Bitmap photo;
    TextView noCurrentRes, pendingTitle, pendingLocal, pendingTime, pendingSeat, confirmCode, zip, verifyText, noStub, FAQs;
    Button cancelButton;
    RelativeLayout pendingData, StandardTicket, verifyTicketFlag, verifyMsgExpanded;

    ConstraintLayout ETicket;
    GestureDetector gestureScanner;
    String uploadKey;
    File photoFile;
    String photoFileName = "TicketVerification.jpg";
    BitmapFactory.Options bmOptions;

    private static String PERMISSIONS[] = new String[]{
            CAMERA,
            WRITE_EXTERNAL_STORAGE
    };

    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_confirmation);

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
        String screeningTime = screeningToken.getAvailability().getStartTime();
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
        FAQs = findViewById(R.id.FAQs);

        gestureScanner = new GestureDetector(this);

        pendingTitle.setText(screeningToken.getScreening().getTitle());
        pendingLocal.setText(screeningToken.getScreening().getTheaterName());
        pendingTime.setText(screeningTime);
        userData();

        if (screeningToken != null && screeningToken.getConfirmationCode() != null && !isEmpty(screeningToken.getConfirmationCode().getConfirmationCode())) {
            ETicket.setVisibility(View.VISIBLE);
            String code = screeningToken.getConfirmationCode().getConfirmationCode();
            confirmCode.setText(code);
            if (screeningToken.getSeatSelected() != null && screeningToken.getSeatSelected().size()>0) {
                pendingSeat.setVisibility(View.VISIBLE);
                pendingSeat.setText("Seats: " + screeningToken.getSeatSelected().get(0).getSeatName());
            }
        } else {
            StandardTicket.setVisibility(View.VISIBLE);
            if (UserPreferences.INSTANCE.getRestrictions().getProofOfPurchaseRequired() || screeningToken.getScreening().getPopRequired()) {
                verifyTicketFlag.setVisibility(View.VISIBLE);
                expand(verifyMsgExpanded);
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
                    if (ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(PERMISSIONS, Constants.REQUEST_CAMERA_CODE);
                        } else {
                            scan_Ticket();
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

                FAQs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Support.showFAQSection(ConfirmationActivity.this, Constants.TICKET_VERIFICATION_FAQ_SECTION);
                    }
                });
            }
        }


        cancelButton.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CAMERA_CODE && ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, CAMERA) == PERMISSION_GRANTED) {
            scan_Ticket();
        } else {
            Toast.makeText(this, "You must grant permissions to continue", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            if (photoFile != null) {
                bmOptions = new BitmapFactory.Options();

                android.util.Log.d(Constants.TAG, "onActivityResult: " + bmOptions);
                BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                int scaleFactor = Math.min(photoW / 1024, photoH / 1024);
                if (scaleFactor != 1) {
                    bmOptions.inSampleSize = scaleFactor;
                }
                bmOptions.inJustDecodeBounds = false;
                Bitmap image = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(photoFile);
                    LogUtils.newLog("compressing file " + photoFile.getAbsolutePath());
                    image.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
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
            uploadToAWS(photoFile);
        }

    }

    private File getPhotoFileUri(String photoFileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            android.util.Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        android.util.Log.d(Constants.TAG, "getPhotoFileUri: " + new File(mediaStorageDir.getPath() + File.separator + photoFileName));

        return new File(mediaStorageDir.getPath() + File.separator + photoFileName);
    }

    public void userData() {
        int userId = UserPreferences.INSTANCE.getUserId();
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
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);
        Uri fileProvider = FileProvider.getUriForFile(this, getString(R.string.authority_file_provider), photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(getPackageManager()) != null) {
            android.util.Log.d(Constants.TAG, "scanTicket: ");
            startActivityForResult(intent, Constants.REQUEST_CAMERA_CODE);
        }
    }

    private void uploadToAWS(File ticketPhoto) {
        whiteProgress.setVisibility(View.VISIBLE);
        scanTicket.setVisibility(View.INVISIBLE);
        ObjectMetadata objectMetadata = new ObjectMetadata();

        LogUtils.newLog(Constants.TAG, "uploadToAWS:  " + screeningToken);
        if (screeningToken != null) {
            uploadKey = String.valueOf(screeningToken.getReservation().getId());

            try {
                String reservationId = String.valueOf(screeningToken.getReservation().getId());
                String showTime = screeningToken.getAvailability().getStartTime();
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


        TransferObserver observer = transferUtility.upload(BuildConfig.BUCKET, uploadKey, ticketPhoto, objectMetadata);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                LogUtils.newLog(Constants.TAG, "STATUS???: " + state);
                if (state == TransferState.COMPLETED) {
                    int reservationId = screeningToken.getReservation().getId();
                    VerificationRequest ticketVerificationRequest = new VerificationRequest();
                    RestClient.getAuthenticated().verifyTicket(reservationId, ticketVerificationRequest).enqueue(new Callback<VerificationResponse>() {
                        @Override
                        public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                            if (response != null && response.isSuccessful()) {
                                whiteProgress.setVisibility(View.GONE);
                                Toast.makeText(ConfirmationActivity.this, "Your ticket stub has been submitted", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                JSONObject jObjError = null;
                                try {
                                    jObjError = new JSONObject(response.errorBody().string());
                                    if (jObjError.getString("message").equals("Verification status is different from PENDING_SUBMISSION")) {
                                        progress.setVisibility(View.GONE);
                                        Toast.makeText(ConfirmationActivity.this, "Your stub has been submitted", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                } catch (JSONException | IOException e) {
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
                LogUtils.newLog(Constants.TAG, "onError: ");
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
        meta.put("user_id", String.valueOf(UserPreferences.INSTANCE.getUserId()));//UserId
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
}
