package com.carrustruckerapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.carrustruckerapp.R;
import com.carrustruckerapp.entities.Booking;
import com.carrustruckerapp.utils.CommonUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Saurbhv on 10/29/15.
 */
public class BookingAdapter extends BaseAdapter {
    ArrayList<Booking> data = new ArrayList<Booking>();
    LayoutInflater inflater;
    Context context;

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

        try {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            f.setTimeZone(TimeZone.getTimeZone("ISO"));
            Date d = f.parse(String.valueOf(data.get(position).getBookingTime()));
            DateFormat date = new SimpleDateFormat("dd");
            DateFormat month = new SimpleDateFormat("MMM");
            DateFormat dayName = new SimpleDateFormat("EEE");
            mViewHolder.date.setText(date.format(d));
            mViewHolder.month.setText(month.format(d));
            mViewHolder.name.setText(CommonUtils.toCamelCase(data.get(position).getName()));
            mViewHolder.truckName.setText(data.get(position).getTruckName());
            mViewHolder.shipingJourney.setText(data.get(position).getShipingJourney());
            mViewHolder.timeSlot.setText(dayName.format(d)+", "+data.get(position).getTimeSlot());
            if(data.get(position).getStatus().equals("REACHED_DESTINATION")||data.get(position).getStatus().equals("REACHED_PICKUP_LOCATION"))
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.orange));
            if(data.get(position).getStatus().equals("ON_GOING")||data.get(position).getStatus().equals("UP_GOING"))
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.blue));
            if(data.get(position).getStatus().equals("CONFIRMED"))
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.green));
            if(data.get(position).getStatus().equals("HALT")||data.get(position).getStatus().equals("COMPLETED"))
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.dark_gery));
            if(data.get(position).getStatus().equals("CANCELED"))
                mViewHolder.status.setTextColor(context.getResources().getColor(R.color.red));
            mViewHolder.status.setText(data.get(position).getStatus().replace("_"," "));

        }catch (Exception e){
            e.printStackTrace();
        }
        return convertView;
    }

    private class MyViewHolder {
        TextView date,month,name,truckName,shipingJourney,timeSlot,status;


        public MyViewHolder(View item) {
            date = (TextView)item.findViewById(R.id.date);
            month = (TextView)item.findViewById(R.id.month);
            name = (TextView)item.findViewById(R.id.name);
            truckName = (TextView)item.findViewById(R.id.truckName);
            shipingJourney = (TextView) item.findViewById(R.id.shipingJourney);
            timeSlot = (TextView) item.findViewById(R.id.timeSlot);
            status = (TextView) item.findViewById(R.id.status);
        }
    }
}

