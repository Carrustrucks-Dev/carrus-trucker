package com.carrus.trucker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.carrus.trucker.R;
import com.carrus.trucker.models.Booking;
import com.carrus.trucker.utils.CommonUtils;

import java.util.ArrayList;

/**
 * Created by Saurbhv on 10/29/15.
 */
public class BookingAdapter extends BaseAdapter {
    private ArrayList<Booking> data = new ArrayList<Booking>();
    private LayoutInflater inflater;
    private Context context;

    public BookingAdapter(Context context, ArrayList<Booking> myList) {
        this.data = myList;
        this.context = context;
        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Booking getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder mViewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_each_item_layout, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

            mViewHolder.date.setText(CommonUtils.getDateFromUTC(data.get(position).getBookingTime()));
            mViewHolder.month.setText(CommonUtils.getShortMonthNameFromUTC(data.get(position).getBookingTime()));
            mViewHolder.name.setText(CommonUtils.toCamelCase(data.get(position).getName()));
            mViewHolder.truckName.setText(data.get(position).getTruckName());
            mViewHolder.shipingJourney.setText(data.get(position).getShipingJourney());
            mViewHolder.timeSlot.setText(CommonUtils.getShortDayNameFromUTC(data.get(position).getBookingTime()) + ", " + data.get(position).getTimeSlot());
            setTextColorOfStatus(data.get(position).getStatus().toUpperCase(),mViewHolder);
//            switch (data.get(position).getStatus().toUpperCase()) {
//                case "REACHED_DESTINATION":
//                case "REACHED_PICKUP_LOCATION":
//                    mViewHolder.status.setTextColor(context.getResources().getColor(R.color.orange));
//                    break;
//
//                case "ON_GOING":
//                case "UP_GOING":
//                    mViewHolder.status.setTextColor(context.getResources().getColor(R.color.blue));
//                    break;
//
//                case "CONFIRMED":
//                    mViewHolder.status.setTextColor(context.getResources().getColor(R.color.green));
//                    break;
//
//                case "HALT":
//                case "COMPLETED":
//                    mViewHolder.status.setTextColor(context.getResources().getColor(R.color.dark_gery));
//                    break;
//
//                case "CANCELED":
//                case "ON_THE_WAY":
//                    mViewHolder.status.setTextColor(context.getResources().getColor(R.color.red));
//                    break;
//            }
//            if(data.get(position).getStatus().equals("REACHED_DESTINATION")||data.get(position).getStatus().equals("REACHED_PICKUP_LOCATION"))
//                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.orange));
//            if(data.get(position).getStatus().equals("ON_GOING")||data.get(position).getStatus().equals("UP_GOING"))
//                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.blue));
//            if(data.get(position).getStatus().equals("CONFIRMED"))
//                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.green));
//            if(data.get(position).getStatus().equals("HALT")||data.get(position).getStatus().equals("COMPLETED"))
//                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.dark_gery));
//            if(data.get(position).getStatus().equals("CANCELED"))
//                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.red));
            mViewHolder.status.setText(CommonUtils.toCamelCase(data.get(position).getStatus().replace("_", " ")));
        return convertView;
    }

    private class MyViewHolder {
        TextView date, month, name, truckName, shipingJourney, timeSlot, status;


        public MyViewHolder(View item) {
            date = (TextView) item.findViewById(R.id.date);
            month = (TextView) item.findViewById(R.id.month);
            name = (TextView) item.findViewById(R.id.name);
            truckName = (TextView) item.findViewById(R.id.truckName);
            shipingJourney = (TextView) item.findViewById(R.id.shipingJourney);
            timeSlot = (TextView) item.findViewById(R.id.timeSlot);
            status = (TextView) item.findViewById(R.id.status);
        }
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

