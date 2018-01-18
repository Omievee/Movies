package com.mobile.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.model.MoviePassCard;
import com.moviepass.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by anubis on 8/1/17.
 */

public class MoviePassCardAdapter extends RecyclerView.Adapter<MoviePassCardAdapter.ViewHolder> {

    private ArrayList<MoviePassCard> arrayListMoviePassCard;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;

    private Context context;

    public MoviePassCardAdapter(ArrayList<MoviePassCard> arrayListMoviePassCard) {
        this.arrayListMoviePassCard = arrayListMoviePassCard;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.linear_layout)
        LinearLayout listItemMoviePassCard;
        @BindView(R.id.card_number)
        TextView cardNumber;
        @BindView(R.id.expiration_date)
        TextView expirationDate;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            listItemMoviePassCard = v.findViewById(R.id.linear_layout);
            cardNumber = v.findViewById(R.id.card_number);
            expirationDate = v.findViewById(R.id.expiration_date);
        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_moviepass_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final MoviePassCard moviePassCard = arrayListMoviePassCard.get(position);

        holder.cardNumber.setText(String.format(Locale.getDefault(), "%s %s", "MoviePass Card: ", moviePassCard.getMaskedNumber()));

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            String expirationDate = moviePassCard.getExpirationDate();

            if (moviePassCard.getExpirationDate() != null) {
                Date date = format.parse(expirationDate);
                System.out.println(date);

                format = new SimpleDateFormat("MM/yyyy ", Locale.US);
                String newDate = format.format(date);

                holder.expirationDate.setText(String.format(Locale.getDefault(), "%s %s", "Expiration Date: ", newDate));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        setSlideAnimation(holder.listItemMoviePassCard);

        holder.listItemMoviePassCard.setTag(position);
    }

    @Override
    public int getItemCount() {
        return arrayListMoviePassCard.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    private void setSlideAnimation(View view) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_up);
        view.startAnimation(animation);
    }
}