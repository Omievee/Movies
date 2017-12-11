package com.moviepass.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.moviepass.R;
import com.moviepass.listeners.ShowtimeClickListener;
import com.moviepass.model.Screening;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class TheaterMoviesAdapter extends RecyclerView.Adapter<TheaterMoviesAdapter.ViewHolder> {

    View root;
    public static final String TAG = "found";
    ShowtimeClickListener showtimeClickListener;
    private ArrayList<Screening> screeningsArrayList;
    ArrayList<String> showtimesArrayList;
    List<String> startTimes;
    private boolean qualifiersApproved;
    private final int TYPE_ITEM = 0;
    public TextView showtime = null;


    public TheaterMoviesAdapter(Context context, ArrayList<String> showtimesArrayList, ArrayList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener, boolean qualifiersApproved) {
        this.showtimeClickListener = showtimeClickListener;
        this.screeningsArrayList = screeningsArrayList;
        this.qualifiersApproved = qualifiersApproved;
        this.showtimesArrayList = showtimesArrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_cinemaposterCARDVIEW)
        CardView cinemaCardViewListItem;
        @BindView(R.id.cinema_movieTitle)
        TextView cinemaTItle;
        @BindView(R.id.CINEMAPOSTER)
        SimpleDraweeView cinemaPoster;
        @BindView(R.id.SHOWTIMEGRID)
        GridLayout showtimeGrid;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            cinemaCardViewListItem = v.findViewById(R.id.list_item_cinemaposterCARDVIEW);
            cinemaTItle = v.findViewById(R.id.cinema_movieTitle);
            cinemaPoster = v.findViewById(R.id.CINEMAPOSTER);
            showtimeGrid = v.findViewById(R.id.SHOWTIMEGRID);


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
        final Uri imgUrl = Uri.parse(screening.getImageUrl());
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).build();
        holder.cinemaPoster.setImageURI(imgUrl);
        holder.cinemaPoster.getHierarchy().setFadeDuration(500);
        holder.cinemaTItle.setText(screening.getTitle());
        holder.cinemaPoster.setController(controller);

        //onBind set up Gridlayout & begin a loop to create a new TextView for each showtime in the respective Array.
        holder.showtimeGrid.setRowCount(1);
        holder.showtimeGrid.setColumnCount(screening.getStartTimes().size());
        holder.showtimeGrid.removeAllViews();
//        Date currentTime = Calendar.getInstance().getTime();


        if (screening.getStartTimes() != null) {
            for (int i = 0; i < screening.getStartTimes().size(); i++) {
                showtime = new TextView(root.getContext());
                showtime.setText(screening.getStartTimes().get(i));
                holder.showtimeGrid.addView(showtime);
                showtime.setTextSize(20);
                showtime.setTextColor(root.getResources().getColor(R.color.white));
                showtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                showtime.setPadding(50, 50, 50, 50);
//                ViewGroup.MarginLayoutParams llp = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT,ViewGroup.MarginLayoutParams.WRAP_CONTENT );
//                llp.setMargins(10, 0, 10, 0);
//                showtime.setLayoutParams(llp);
                final TextView finalShowtime = showtime;
                finalShowtime.setSelected(false);
                //onclick on each showtime will execute the showtimelistener & create reservtion if possible.
                showtime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!finalShowtime.isSelected()) {
                            finalShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background_selected));
                            finalShowtime.setPadding(50, 50, 50, 50);
                            String selectedShowTime = finalShowtime.getText().toString();
                            showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, selectedShowTime);
                            finalShowtime.setSelected(true);
                        } else {
                            finalShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                            finalShowtime.setPadding(50, 50, 50, 50);
                            String selectedShowTime = finalShowtime.getText().toString();
                            showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, selectedShowTime);
                            finalShowtime.setSelected(false);
                        }
                    }
                });

            }
        }
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