package com.mobile.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.model.Screening;
import com.moviepass.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class TheaterMoviesAdapter extends RecyclerView.Adapter<TheaterMoviesAdapter.ViewHolder> {

    public static final String MOVIE = "movie";
    public static final String TITLE = "title";

    View root;
    public static final String TAG = "found";
    ShowtimeClickListener showtimeClickListener;
    private ArrayList<Screening> screeningsArrayList;
    ArrayList<String> showtimesArrayList;
    List<String> startTimes;
    private boolean qualifiersApproved;
    private final int TYPE_ITEM = 0;
    public RadioButton showtime;
    public RadioButton currentTime;
    RadioButton checked;

    Context context;


    public TheaterMoviesAdapter(Context context, ArrayList<String> showtimesArrayList, ArrayList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener, boolean qualifiersApproved) {
        this.showtimeClickListener = showtimeClickListener;
        this.screeningsArrayList = screeningsArrayList;
        this.qualifiersApproved = qualifiersApproved;
        this.showtimesArrayList = showtimesArrayList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_cinemaposterCARDVIEW)
        RelativeLayout cinemaCardViewListItem;
        @BindView(R.id.cinema_movieTitle)
        TextView cinemaTItle;
        @BindView(R.id.CINEMAPOSTER)
        SimpleDraweeView cinemaPoster;
        @BindView(R.id.SHOWTIMEGRID)
        RadioGroup showtimeGrid;

        @BindView(R.id.Not_Supported)
        TextView notSupported;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            cinemaCardViewListItem = v.findViewById(R.id.list_item_cinemaposterCARDVIEW);
            cinemaTItle = v.findViewById(R.id.cinema_movieTitle);
            cinemaPoster = v.findViewById(R.id.CINEMAPOSTER);
            showtimeGrid = v.findViewById(R.id.SHOWTIMEGRID);
            notSupported = v.findViewById(R.id.Not_Supported);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        root = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cinemaposter, parent, false);
        return new ViewHolder(root);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Screening screening = screeningsArrayList.get(position);

        startTimes = screening.getStartTimes();

        //FRESCO code..
        final Uri imgUrl = Uri.parse(screening.getLandscapeImageUrl());
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).build();
        holder.cinemaPoster.setImageURI(imgUrl);
        holder.cinemaPoster.getHierarchy().setFadeDuration(500);
        holder.cinemaTItle.setText(screening.getTitle());
        holder.cinemaPoster.setController(controller);
        holder.showtimeGrid.removeAllViews();

        if (screening.getStartTimes() != null) {
            for (int i = 0; i < screening.getStartTimes().size(); i++) {

                showtime = new RadioButton(root.getContext());
                showtime.setText(screening.getStartTimes().get(i));
                holder.showtimeGrid.addView(showtime);
                showtime.setTextSize(16);
                showtime.setTextColor(root.getResources().getColor(R.color.white));
                showtime.setPadding(30, 20, 30, 20);
                showtime.setBackground((root.getResources().getDrawable(R.drawable.showtime_background)));
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 50, 30);
                showtime.setLayoutParams(params);
                showtime.setButtonDrawable(null);
                currentTime = showtime;
                if (screening.getFormat().equals("2D")) {
                    holder.showtimeGrid.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {

                            Log.d(TAG, "onCheckedChanged: ");
                            checked = group.findViewById(checkedId);

                            if (currentTime != null) {
                                currentTime.setChecked(false);
                            }
                            currentTime = checked;
                            String selectedShowTime = currentTime.getText().toString();
                            showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, selectedShowTime);
                        }

                    });
                }

                if (screening.getFormat().equals("3D") || screening.getFormat().equals("IMAX 3D") || screening.getFormat().equals("IMAX")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        currentTime.setClickable(false);
                        holder.notSupported.setVisibility(View.VISIBLE);
                        holder.cinemaCardViewListItem.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));
                    }
                }
            }
        }

        //TODO:
//        holder.synopsis.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Movie movie = new Movie();
//                String synopsis = movie.getSynopsis();
//                String title = movie.getTitle();
//                Bundle bundle = new Bundle();
//                bundle.putString(MOVIE, synopsis);
//                bundle.putString(TITLE, title);
//
//                SynopsisFragment fragobj = new SynopsisFragment();
//                fragobj.setArguments(bundle);
//                FragmentManager fm = ((TheaterActivity) context).getSupportFragmentManager();
//                fragobj.show(fm, "fr_dialogfragment_synopsis");
//
//            }
//        });

    }


    @Override
    public int getItemCount() {
        return screeningsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }


}