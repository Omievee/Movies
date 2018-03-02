package com.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.requests.VerificationLostRequest;
import com.mobile.responses.ChangedMindResponse;
import com.mobile.responses.VerificationLostResponse;
import com.moviepass.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by anubis on 7/16/17.
 */

public class VerificationActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    public static final String RESERVATION = "reservation";
    public static final String SCREENING = "screeningObject";

    Screening screening;
    Reservation reservation;

    RelativeLayout relativeLayout;
    ImageView poster;
    TextView posterTitle;
    TextView title;
    TextView genres;
    TextView runTime;

    FrameLayout frameLayout;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    private Camera camera;
    private ImageButton buttonOpenCamera;

    private boolean isPreview;
    private boolean isActivityFinished;

    FloatingActionMenu fab;
    View progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Bundle extras = getIntent().getExtras();
        screening = Parcels.unwrap(getIntent().getParcelableExtra(SCREENING));
        reservation = Parcels.unwrap(getIntent().getParcelableExtra(RESERVATION));

        frameLayout = findViewById(R.id.camera_frame_layout);
        surfaceView = findViewById(R.id.surface_view);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        relativeLayout = findViewById(R.id.relative_layout);
        poster = findViewById(R.id.poster);
        posterTitle = findViewById(R.id.poster_movie_title);
        title = findViewById(R.id.movie_title);
        genres = findViewById(R.id.movie_genre);
        runTime = findViewById(R.id.text_run_time);

        fab = findViewById(R.id.fab);
        progress = findViewById(R.id.progress);

        if (screening != null) {
            String imgUrl = screening.getImageUrl();

            Picasso.Builder builder = new Picasso.Builder(this);
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    exception.printStackTrace();
                }
            });
            builder.build()
                    .load(imgUrl)
                    .placeholder(R.drawable.ticket_top_red_dark)
                    .error(R.drawable.ticket_top_red_dark)
                    .into(poster, new Callback() {
                        @Override
                        public void onSuccess() {
                            supportStartPostponedEnterTransition();
                        }

                        @Override
                        public void onError() {
                            supportStartPostponedEnterTransition();
                        }
                    });

            title.setText(screening.getTitle());

            FloatingActionButton buttonChangeReservation = new FloatingActionButton(this);
            buttonChangeReservation.setLabelText(getText(R.string.activity_verification_change_reservation).toString());
            buttonChangeReservation.setImageResource(R.drawable.icon_reset);
            buttonChangeReservation.setButtonSize(FloatingActionButton.SIZE_MINI);
            buttonChangeReservation.setColorNormalResId(R.color.red);
            buttonChangeReservation.setColorPressedResId(R.color.red_dark);
            fab.addMenuButton(buttonChangeReservation);

            buttonChangeReservation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progress.setVisibility(View.VISIBLE);
                    ChangedMindRequest request = new ChangedMindRequest(reservation.getId());

                    RestClient.getAuthenticated().changedMind(request).enqueue(new RestCallback<ChangedMindResponse>() {
                        @Override
                        public void onResponse(Call<ChangedMindResponse> call, Response<ChangedMindResponse> response) {
                            ChangedMindResponse responseBody = response.body();
                            progress.setVisibility(View.GONE);

                            if (responseBody != null) {
                                Snackbar snackbar = Snackbar.make(relativeLayout, responseBody.getMessage(), BaseTransientBottomBar.LENGTH_LONG);
                                snackbar.show();
                                finish();
                            }
                        }

                        @Override
                        public void failure(RestError restError) {
                            progress.setVisibility(View.GONE);
                            fab.setEnabled(true);
                            Snackbar snackbar = Snackbar.make(relativeLayout, restError.getMessage(), BaseTransientBottomBar.LENGTH_LONG);
                            snackbar.show();
                            finish();
                        }
                    });
                }
            });
        } else {
            Intent intent = getIntent();

            String movieTitle = intent.getStringExtra("mSelectedMovieTitle");
            String theaterName = intent.getStringExtra("mTheaterSelected");
            String showtime = intent.getStringExtra("showtime");

            posterTitle.setText(movieTitle);
            title.setText(movieTitle);

            FloatingActionButton buttonLostTicket = new FloatingActionButton(this);
            buttonLostTicket.setLabelText(getText(R.string.activity_verification_lost_ticket).toString());
            buttonLostTicket.setImageResource(R.drawable.icon_reset);
            buttonLostTicket.setButtonSize(FloatingActionButton.SIZE_MINI);
            buttonLostTicket.setColorNormalResId(R.color.red);
            buttonLostTicket.setColorPressedResId(R.color.red_dark);
            fab.addMenuButton(buttonLostTicket);

            buttonLostTicket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(VerificationActivity.this, R.style.AlertDialogCustom);

                    View layout = View.inflate(VerificationActivity.this, R.layout.dialog_lost_ticket, null);
                    final EditText reason = layout.findViewById(R.id.edit_reason);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(reason, InputMethodManager.SHOW_IMPLICIT);

                    alert.setView(layout);
                    alert.setTitle(R.string.activity_verification_lost_ticket_title);
                    alert.setMessage(R.string.activity_verification_lost_ticket_message);
                    alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String reasonString = reason.getText().toString();


                            if (reasonString.length() == 0) {
                                Toast.makeText(VerificationActivity.this, R.string.activity_verification_empty_reason, Toast.LENGTH_LONG).show();
                            } else {
                                sendLostTicketReason(reasonString);
                            }
                        }
                    });
                    alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String reasonString = reason.getText().toString();

                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
            });
        }

        buttonOpenCamera = findViewById(R.id.button_open_camera);
        buttonOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (screening != null) {
                    Intent intent = new Intent(VerificationActivity.this, VerificationPictureActivity.class);
                    intent.putExtra(SCREENING, Parcels.wrap(screening));
                    startActivity(intent);
                } else {
                    String movieTitle = getIntent().getStringExtra("mSelectedMovieTitle");
                    String theaterName = getIntent().getStringExtra("mTheaterSelected");
                    String showtime = getIntent().getStringExtra("showtime");
                    int reservationId = getIntent().getIntExtra("reservationId", 0);
                    String tribuneMovieId = getIntent().getStringExtra("tribuneMovieId");
                    String tribuneTheaterId = getIntent().getStringExtra("tribuneTheaterId");

                    Intent intent = new Intent(VerificationActivity.this, VerificationPictureActivity.class);
                    intent.putExtra("reservationId", reservationId);
                    intent.putExtra("mSelectedMovieTitle", movieTitle);
                    intent.putExtra("tribuneMovieId", tribuneMovieId);
                    intent.putExtra("mTheaterSelected", theaterName);
                    intent.putExtra("tribuneTheaterId", tribuneTheaterId);
                    intent.putExtra("showtime", showtime);
                    startActivity(intent);

                }
            }
        });
    }

    @Override
    public void onPause() {
        surfaceView.setVisibility(View.GONE);
        if (camera != null) {
            releaseCamera();
        }

        if (this.isFinishing()) {
            isActivityFinished = true;
        } else {
            isActivityFinished = false;
        }

        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            releaseCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.camera == null) {
            try {
                this.camera = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
            }
            surfaceView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                buttonOpenCamera.setEnabled(true);
            }
        }
    }

    private void releaseCamera() {
        if (camera != null) {

            isPreview = false;
            camera.stopPreview();
            camera.lock();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            setCameraDisplayOrientation(this, 0, camera);

            Camera.Parameters p = camera.getParameters();

            List<Camera.Size> sizes = p.getSupportedPreviewSizes();
            Camera.Size optimalSize = getOptimalPreviewSize(sizes, frameLayout.getWidth(), frameLayout.getHeight());

            List<String> focusModes = p.getSupportedFocusModes();
            if (p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            if (p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            //p.setPreviewSize(camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height);
            p.setPreviewSize(optimalSize.width, optimalSize.height);
            camera.setParameters(p);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                camera.autoFocus(mAutoFocusTakePictureCallback);
            }
            isPreview = true;
        } catch (IOException e) {
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        // stop preview before making changes
        try {
            stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    public void stopPreview() {
        if (camera != null) {

            isPreview = false;
            camera.stopPreview();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {

            isPreview = false;
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            Camera.Parameters p = camera.getParameters();

            List<Camera.Size> sizes = p.getSupportedPreviewSizes();
            Camera.Size optimalSize = getOptimalPreviewSize(sizes, frameLayout.getWidth(), frameLayout.getHeight());

            Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            if (display.getRotation() == Surface.ROTATION_0) {
                camera.setDisplayOrientation(90);
            }

            if (display.getRotation() == Surface.ROTATION_270) {
                camera.setDisplayOrientation(180);
            }

            setCameraDisplayOrientation(this, 0, camera);

            if (p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            if (p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            p.setPreviewSize(optimalSize.width, optimalSize.height);
            camera.setParameters(p);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

            isPreview = true;
        } catch (Exception e) {
        }
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
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

        camera.setDisplayOrientation(result);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
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

    public void sendLostTicketReason(String reason) {
        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            //IGNORED PICTURE
            final int reservationId = intent.getIntExtra("reservationId", 0);

            VerificationLostRequest ticketVerificationLostRequest = new VerificationLostRequest(reason);

            RestClient.getAuthenticated().lostTicket(reservationId, ticketVerificationLostRequest).enqueue(new RestCallback<VerificationLostResponse>() {
                @Override
                public void onResponse(Call<VerificationLostResponse> call, Response<VerificationLostResponse> response) {
                    VerificationLostResponse ticketVerificationLostResponse = response.body();

                    if (ticketVerificationLostResponse != null) {

                        progress.setVisibility(View.GONE);

                        displayWarning();
                    } else {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());

                            Snackbar snackbar = Snackbar.make(relativeLayout, jObjError.getString("message"), BaseTransientBottomBar.LENGTH_LONG);
                            snackbar.show();

                            progress.setVisibility(View.GONE);
                        } catch (Exception e) {
                            Toast.makeText(VerificationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void failure(RestError restError) {
                    progress.setVisibility(View.GONE);

                }
            });

        }
    }

    public void displayWarning() {
        AlertDialog.Builder alert = new AlertDialog.Builder(VerificationActivity.this, R.style.AlertDialogCustom);
        alert.setTitle(R.string.activity_verification_lost_ticket_title_post);
        alert.setMessage(R.string.activity_verification_lost_ticket_message_post);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(VerificationActivity.this, BrowseActivity.class);
                startActivity(intent);
            }
        });
        alert.show();
    }
}
