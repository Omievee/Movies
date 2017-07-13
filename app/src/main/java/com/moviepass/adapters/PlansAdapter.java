package com.moviepass.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.listeners.PlanClickListener;
import com.moviepass.model.Plan;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by anubis on 7/12/17.
 */

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.ViewHolder> {

    private final PlanClickListener planClickListener;
    private ArrayList<Plan> plansArrayList;
    private Plan plan;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public PlansAdapter(ArrayList<Plan> plansArrayList, PlanClickListener planClickListener) {
        this.planClickListener = planClickListener;
        this.plansArrayList = plansArrayList;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rel_container)
        RelativeLayout relativeLayout;
        @BindView(R.id.plan_name)
        TextView planName;
        @BindView(R.id.plan_cap)
        TextView planCap;
        @BindView(R.id.price)
        TextView price;
        SparseBooleanArray selectedItems = null;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            relativeLayout = v.findViewById(R.id.relative_layout);
            planName = v.findViewById(R.id.plan_name);
            planCap = v.findViewById(R.id.plan_cap);
            price = v.findViewById(R.id.price);
        }
    }

    @Override
    public PlansAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_plan, parent, false);
        return new PlansAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlansAdapter.ViewHolder holder, final int position) {
        /* if (row_idex == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#c82229"));
            holder.showtime.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.showtime.setTextColor(Color.parseColor("#DE000000"));
        }

        if (position == selectedPosition) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }

        final String time = showtimesArrayList.get(position);

        holder.showtime.setText(time);
        holder.relativeLayout.setSelected(holder.relativeLayout.isSelected());
        holder.relativeLayout.setTag(position);
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int currentPosition = holder.getLayoutPosition();
                if (selectedPosition != currentPosition) {

                    // Show Ripple and then change color
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Temporarily save the last selected position
                            int lastSelectedPosition = selectedPosition;
                            // Save the new selected position
                            selectedPosition = currentPosition;
                            // update the previous selected row
                            notifyItemChanged(lastSelectedPosition);
                            // select the clicked row
                            holder.itemView.setSelected(true);
                        }
                    }, 150);
                }

                showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, time);
            }
        });

        */
    }

    @Override
    public int getItemCount() { return plansArrayList.size(); }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }
}
