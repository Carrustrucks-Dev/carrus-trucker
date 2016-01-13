package com.carrus.trucker.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.carrus.trucker.R;
import com.carrus.trucker.activities.BookingDetailsActivity;
import com.carrus.trucker.interfaces.OnLoadMoreListener;
import com.carrus.trucker.models.Booking;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Transactions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Developer: Saurbhv
 * Dated: 1/13/16.
 */
public class BookingRecyclerAdapter extends RecyclerView.Adapter {
    private ArrayList<Booking> data = new ArrayList<Booking>();
    private LayoutInflater inflater;
    private Context context;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean upcoming = false;

    public BookingRecyclerAdapter(Context context, ArrayList<Booking> myList, RecyclerView recyclerView, boolean upcoming) {
        this.data = myList;
        this.context = context;
        this.upcoming = upcoming;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager
                            .findLastVisibleItemPosition();
                    if (!loading
                            && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }


    public Booking getItem(int position) {
        return data.get(position);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_each_item_layout, parent, false);
            vh = new MyViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar_item, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {

            ((MyViewHolder)holder).date.setText(CommonUtils.getDateFromUTC(data.get(position).getBookingTime()));
            ((MyViewHolder)holder).month.setText(CommonUtils.getShortMonthNameFromUTC(data.get(position).getBookingTime()));
            ((MyViewHolder)holder).name.setText(CommonUtils.toCamelCase(data.get(position).getName()));
            ((MyViewHolder)holder).truckName.setText(data.get(position).getTruckName());
            ((MyViewHolder)holder).shipingJourney.setText(data.get(position).getShipingJourney());
            ((MyViewHolder)holder).timeSlot.setText(CommonUtils.getShortDayNameFromUTC(data.get(position).getBookingTime()) + ", " + data.get(position).getTimeSlot());
            setTextColorOfStatus(data.get(position).getStatus().toUpperCase(), ((MyViewHolder) holder));
            ((MyViewHolder)holder).status.setText(CommonUtils.toCamelCase(data.get(position).getStatus().replace("_", " ")));
            ((MyViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, BookingDetailsActivity.class);
                    intent.putExtra("bookingId", data.get(position).getBooking_id());
                    context.startActivity(intent);
                    Transactions.showNextAnimation((Activity) context);
                }
            });

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public void addMore(ArrayList<Booking> myList) {
        this.data = myList;
        notifyDataSetChanged();

    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView date, month, name, truckName, shipingJourney, timeSlot, status;

        public MyViewHolder(View item) {
            super(item);
            date = (TextView) item.findViewById(R.id.date);
            month = (TextView) item.findViewById(R.id.month);
            name = (TextView) item.findViewById(R.id.name);
            truckName = (TextView) item.findViewById(R.id.truckName);
            shipingJourney = (TextView) item.findViewById(R.id.shipingJourney);
            timeSlot = (TextView) item.findViewById(R.id.timeSlot);
            status = (TextView) item.findViewById(R.id.status);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }
    }

    public void clearAll() {
        data.clear();
        notifyDataSetChanged();
    }

    /**
     * @param String
     * @param ViewHolder
     * Method to set text color of status textView
     * */
    private void setTextColorOfStatus(String status, MyViewHolder mViewHolder){
        switch (status) {
            case "REACHED_DESTINATION":
            case "REACHED_PICKUP_LOCATION":
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.orange));
                break;

            case "ON_GOING":
            case "UP_GOING":
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.blue));
                break;

            case "CONFIRMED":
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.green));
                break;

            case "HALT":
            case "COMPLETED":
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.dark_gery));
                break;

            case "CANCELED":
            case "ON_THE_WAY":
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.red));
                break;
        }
    }
}
