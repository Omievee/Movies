package com.moviepass.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.moviepass.R;
import com.moviepass.activities.TheaterActivity;
import com.moviepass.fragments.SynopsisFragment;
import com.moviepass.listeners.ShowtimeClickListener;
import com.moviepass.model.Movie;
import com.moviepass.model.Screening;

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
    public TextView showtime = null;
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
        CardView cinemaCardViewListItem;
        @BindView(R.id.cinema_movieTitle)
        TextView cinemaTItle;
        @BindView(R.id.CINEMAPOSTER)
        SimpleDraweeView cinemaPoster;
        @BindView(R.id.SHOWTIMEGRID)
        GridLayout showtimeGrid;
        @BindView(R.id.cinema_Synopsis)
        ImageButton synopsis;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            cinemaCardViewListItem = v.findViewById(R.id.list_item_cinemaposterCARDVIEW);
            cinemaTItle = v.findViewById(R.id.cinema_movieTitle);
            cinemaPoster = v.findViewById(R.id.CINEMAPOSTER);
            showtimeGrid = v.findViewById(R.id.SHOWTIMEGRID);
            synopsis = v.findViewById(R.id.cinema_Synopsis);

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
                        if (screening.getFormat().equals("2D")) {
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
                        } else {
                            finalShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                            finalShowtime.setPadding(50, 50, 50, 50);
                            Toast.makeText(holder.itemView.getContext(), R.string.Not_Supportd, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            if (!screening.getFormat().equals("2D")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.cinemaCardViewListItem.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));

                }
            }

            holder.synopsis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Movie movie = new Movie();
                    String synopsis = movie.getSynopsis();
                    String title = movie.getTitle();
                    Bundle bundle = new Bundle();
                    bundle.putString(MOVIE, synopsis);
                    bundle.putString(TITLE, title);

                    SynopsisFragment fragobj = new SynopsisFragment();
                    fragobj.setArguments(bundle);
                    FragmentManager fm = ((TheaterActivity) context).getSupportFragmentManager();
                    fragobj.show(fm, "fr_dialogfragment_synopsis");

                }
            });
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