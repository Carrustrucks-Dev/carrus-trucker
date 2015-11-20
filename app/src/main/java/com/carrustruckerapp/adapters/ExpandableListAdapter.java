package com.carrustruckerapp.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.carrustruckerapp.R;
import com.carrustruckerapp.activities.ShowImageActivity;
import com.carrustruckerapp.entities.ExpandableChildItem;
import com.carrustruckerapp.interfaces.ActivityResultCallback;
import com.carrustruckerapp.interfaces.AppConstants;
import com.carrustruckerapp.retrofit.RestClient;
import com.carrustruckerapp.utils.CommonUtils;
import com.carrustruckerapp.utils.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;


/**
 * Created by Saurbhv on 10/30/15.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter implements AppConstants {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<ExpandableChildItem>> _listDataChild;
    public String orderId;
    public SharedPreferences sharedPreferences;
    public String imagePath;
    ActivityResultCallback resultCallback;
    public String documentName;
    public Map<String, TypedFile> images;
    ExpandableChildItem expandableChildItem;

    public ExpandableListAdapter(Context context, String orderId, List<String> listDataHeader,
                                 HashMap<String, List<ExpandableChildItem>> listChildData) {
        this.orderId = orderId;
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        sharedPreferences = _context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        this.resultCallback = (ActivityResultCallback) context;
        images = new HashMap<String, TypedFile>();
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

        expandableChildItem = (ExpandableChildItem) getChild(groupPosition, childPosition);
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
                if (expandableChildItem.getDetail().equalsIgnoreCase("null")) {
                    convertView = infalInflater.inflate(R.layout.upload_documents_layout, null);
                    TextView uploadButtonText = (TextView) convertView.findViewById(R.id.upload_button_text);
                    uploadButtonText.setText(expandableChildItem.getName());
//                    convertView.findViewById(R.id.upload_document).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
////                            selectImage();
//                            documentName=expandableChildItem.getName();
//                            Log.e("Document Name",documentName);
//                            Toast.makeText(v.getContext(), expandableChildItem.getName(), Toast.LENGTH_LONG).show();
//                        }
//                    });
                } else {
                    convertView = infalInflater.inflate(R.layout.uploaded_document_layout, null);
                    TextView uploadButtonText = (TextView) convertView.findViewById(R.id.upload_button_text);
                    uploadButtonText.setText(expandableChildItem.getName());
                  /*  convertView.findViewById(R.id.uploaded_document).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            expandableChildItem.getDetail();
                        }
                    });*/

                }
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
                final TextView notes = (TextView) convertView.findViewById(R.id.notes);
                notes.setText(expandableChildItem.getName());
                break;
            case 4:
                convertView = infalInflater.inflate(R.layout.my_notes_layout, null);
                final EditText myNotes = (EditText) convertView.findViewById(R.id.notes);
                myNotes.setText(expandableChildItem.getName());
                convertView.findViewById(R.id.submit_notes_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (myNotes.getText().toString().trim().isEmpty()) {
                            CommonUtils.showSingleButtonPopup(_context, "Please enter notes");
                        } else {
                            final String myNotesData = myNotes.getText().toString().trim();
                            CommonUtils.showLoadingDialog((Activity) _context, "Sending...");
                            RestClient.getWebServices().addNotes(sharedPreferences.getString(ACCESS_TOKEN, ""), orderId, myNotes.getText().toString().trim(), new Callback<String>() {
                                @Override
                                public void success(String s, Response response) {
                                    expandableChildItem.setName(myNotesData);
                                    CommonUtils.dismissLoadingDialog();
                                    CommonUtils.showSingleButtonPopup(_context, "Saved Successfully");
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    CommonUtils.showRetrofitError((Activity) _context, error);
//                                    CommonUtils.showSingleButtonPopup(_context, "Oops!! Some error occurred. Please try again. ");
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
        ImageView indicator = (ImageView) convertView.findViewById(R.id.indicator);
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

    void selectImage() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(_context);
        dialog.setTitle(_context.getString(R.string.upload_documents));
        dialog.setItems(R.array.image_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    try {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        resultCallback.startActivityResult(intent, LOAD_IMAGE_RESULTS, 10);
//                        startActivityForResult(Intent.createChooser(intent, _context.getString(R.string.select_picture)), LOAD_IMAGE_RESULTS);
                    } catch (Exception ex) {
                        Toast.makeText(_context, _context.getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
                    }

                } else if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    resultCallback.startActivityResult(intent, LOAD_PDF_FILE, 10);

                } else {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (groupPosition == 1) {

            ExpandableChildItem childItem = (ExpandableChildItem) getChild(groupPosition, childPosition);
            if (childItem.getDetail().equalsIgnoreCase("null")) {
                documentName = childItem.getName();
                selectImage();
                Log.e("Document Name", documentName);
            } else {
                Intent intent = new Intent(_context, ShowImageActivity.class);
                intent.putExtra("url", childItem.getDetail());
                intent.putExtra("orderId", orderId);
                intent.putExtra("documentName", childItem.getName());
                _context.startActivity(intent);

            }
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Matrix mat = new Matrix();
        if (requestCode == LOAD_IMAGE_RESULTS && resultCode == -1 && data != null) {

            try {
                imagePath = CommonUtils.getPath(_context, data.getData());
                mat.postRotate(CommonUtils.getCameraPhotoOrientation(imagePath));
                Log.e("Imagepath", "" + imagePath);
                if (documentName.equalsIgnoreCase(_context.getString(R.string.pod))) {
                    images.put("pod", new TypedFile("image/*", new File(imagePath)));
                } else if (documentName.equalsIgnoreCase(_context.getString(R.string.invoice))) {
                    images.put("invoice", new TypedFile("image/*", new File(imagePath)));
                } else if (documentName.equalsIgnoreCase(_context.getString(R.string.consignment))) {
                    images.put("consigmentNote", new TypedFile("image/*", new File(imagePath)));
                }
                CommonUtils.showLoadingDialog((Activity) _context, "Uploading...");
                RestClient.getWebServices().uploadDocument(sharedPreferences.getString(ACCESS_TOKEN, ""), new TypedString(orderId), images, new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            CommonUtils.dismissLoadingDialog();
                            CommonUtils.showSingleButtonPopup(_context, jsonObject.getString("message"));
                            resultCallback.getOrderDetails();
//                            ExpandableChildItem childItem;
//                            switch (documentName) {
//                                case "POD":
//                                    childItem = (ExpandableChildItem) getChild(1, 0);
//                                    childItem.setDetail(jsonObject.getJSONObject("data").getString("podUpload"));
//
//                                    break;
//                                case "Invoice":
//                                    childItem = (ExpandableChildItem) getChild(1, 1);
//                                    childItem.setDetail(jsonObject.getJSONObject("data").getString("invoiceUpdate"));
//                                    break;
//                                case "Consignment Notes":
//                                    childItem = (ExpandableChildItem) getChild(1, 2);
//                                    childItem.setDetail(jsonObject.getJSONObject("data").getString("consigmentNoteUpdate"));
//                                    break;
//                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            CommonUtils.dismissLoadingDialog();
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        CommonUtils.showRetrofitError((Activity) _context, error);
                        CommonUtils.showSingleButtonPopup(_context, "Oops!! Some error occurred. Please try again. ");
                        CommonUtils.dismissLoadingDialog();
                    }
                });
            } catch (Exception ne) {
                Toast.makeText(_context, _context.getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == LOAD_PDF_FILE && resultCode == -1 && data != null) {
            try {
                imagePath = CommonUtils.getPath(_context, data.getData());
                mat.postRotate(CommonUtils.getCameraPhotoOrientation(imagePath));
                Log.e("Imagepath", "" + imagePath);
                if (documentName.equalsIgnoreCase(_context.getString(R.string.pod))) {
                    images.put("pod", new TypedFile("*/*", new File(imagePath)));
                } else if (documentName.equalsIgnoreCase(_context.getString(R.string.invoice))) {
                    images.put("invoice", new TypedFile("*/*", new File(imagePath)));
                } else if (documentName.equalsIgnoreCase(_context.getString(R.string.consignment))) {
                    images.put("consigmentNote", new TypedFile("*/*", new File(imagePath)));
                }
                CommonUtils.showLoadingDialog((Activity) _context, "Uploading...");
                RestClient.getWebServices().uploadDocument(sharedPreferences.getString(ACCESS_TOKEN, ""), new TypedString(orderId), images, new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            CommonUtils.dismissLoadingDialog();
                            CommonUtils.showSingleButtonPopup(_context, jsonObject.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            CommonUtils.dismissLoadingDialog();
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        CommonUtils.showRetrofitError((Activity) _context, error);
                        CommonUtils.showSingleButtonPopup(_context, "Oops!! Some error occurred. Please try again. ");
                        CommonUtils.dismissLoadingDialog();
                    }
                });
            } catch (Exception ne) {
                Toast.makeText(_context, _context.getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
            }

        }
    }

}
