package com.carrustruckerapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.carrustruckerapp.R;
import com.carrustruckerapp.entities.ExpandableChildItem;
import com.carrustruckerapp.interfaces.AppConstants;
import com.carrustruckerapp.interfaces.WebServices;
import com.carrustruckerapp.utils.CommonUtils;
import com.carrustruckerapp.utils.GlobalClass;

import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Saurbhv on 10/30/15.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter implements AppConstants{

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<ExpandableChildItem>> _listDataChild;
    public WebServices webServices;
    public GlobalClass globalClass;
    public String orderId;
    public SharedPreferences sharedPreferences;

    public ExpandableListAdapter(Context context, String orderId,List<String> listDataHeader,
                                 HashMap<String, List<ExpandableChildItem>> listChildData) {
        this.orderId=orderId;
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        globalClass=(GlobalClass)_context.getApplicationContext();
        webServices=globalClass.getWebServices();
        sharedPreferences = _context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final ExpandableChildItem expandableChildItem = (ExpandableChildItem) getChild(groupPosition, childPosition);
        LayoutInflater infalInflater = (LayoutInflater) this._context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView name, details;
        switch (groupPosition) {
            case 0:
                convertView = infalInflater.inflate(R.layout.list_item, null);
                name = (TextView) convertView.findViewById(R.id.name);
                details = (TextView) convertView.findViewById(R.id.details);
                name.setText(expandableChildItem.getName());
                details.setText(expandableChildItem.getDetail());


                break;
            case 1:
                convertView = infalInflater.inflate(R.layout.upload_documents_layout, null);
                TextView uploadButtonText=(TextView)convertView.findViewById(R.id.upload_button_text);
                uploadButtonText.setText(expandableChildItem.getName());
                convertView.findViewById(R.id.upload_document).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(expandableChildItem.getName().equalsIgnoreCase(_context.getString(R.string.pod))){

                        }else if (expandableChildItem.getName().equalsIgnoreCase(_context.getString(R.string.invoice))) {

                        }else if(expandableChildItem.getName().equalsIgnoreCase(_context.getString(R.string.consignment))){

                        }
                        Toast.makeText(v.getContext(), "under construction:" + expandableChildItem.getName(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case 2:
                convertView = infalInflater.inflate(R.layout.list_item, null);
                name = (TextView) convertView.findViewById(R.id.name);
                details = (TextView) convertView.findViewById(R.id.details);
                name.setText(expandableChildItem.getName());
                details.setText(expandableChildItem.getDetail());

                break;
            case 3:
                convertView = infalInflater.inflate(R.layout.simple_textview_layout, null);
                final TextView notes=(TextView)convertView.findViewById(R.id.notes);
                notes.setText(expandableChildItem.getName());
                break;
            case 4:
                convertView = infalInflater.inflate(R.layout.my_notes_layout, null);
                final EditText myNotes= (EditText) convertView.findViewById(R.id.notes);
                myNotes.setText(expandableChildItem.getName());
                convertView.findViewById(R.id.submit_notes_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (myNotes.getText().toString().isEmpty()) {
                            CommonUtils.showSingleButtonPopup(_context,"Please enter notes");
                        } else {
                            CommonUtils.showLoadingDialog((Activity) _context,"Sending...");
                            webServices.addNotes(sharedPreferences.getString(ACCESS_TOKEN, ""), orderId, myNotes.getText().toString(), new Callback<String>() {
                                @Override
                                public void success(String s, Response response) {
                                    CommonUtils.dismissLoadingDialog();
                                    CommonUtils.showSingleButtonPopup(_context, "Saved Successfully");
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    CommonUtils.showRetrofitError((Activity) _context,error);
                                    CommonUtils.showSingleButtonPopup(_context, "Oops!! Some error occurred. Please try again. ");
                                    CommonUtils.dismissLoadingDialog();
                                }
                            });
                        }
                    }
                });
                break;

        }
//        if (expandableChildItem.getType() == 1) {
////            TextView name = (TextView) convertView.findViewById(R.id.name);
////            TextView details = (TextView) convertView.findViewById(R.id.details);
////            name.setText(expandableChildItem.getName());
////            details.setText(expandableChildItem.getDetail());
//        } else if (expandableChildItem.getType() == 2) {
//
//        } else if (expandableChildItem.getType() == 3) {
//
//        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        ImageView indicator=(ImageView) convertView.findViewById(R.id.indicator);
        lblListHeader.setText(headerTitle);
        int imageResourceId = isExpanded ? R.mipmap.icon_cross
                : R.mipmap.icon_add;
        indicator.setImageResource(imageResourceId);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
