package com.carrus.trucker.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.carrus.trucker.R;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Log;
import com.flurry.android.FlurryAgent;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by Saurbhv on 11/17/15.
 */
public class ShowImageActivity extends BaseActivity  implements View.OnClickListener{

    private ImageView ivCloseButton, ivDocument;
    private Button btnUploadNew;
    private String orderId, documentName, imagePath, url;
    private Map<String, TypedFile> images;
    private WebView webView;
    private boolean uploadFlag=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_image);
        setupUI(getWindow().getDecorView().getRootView());

        init();

    }

    /**
     * Method to initialize all the {@link View}s inside the Layout of this
     * {@link Activity}
     */
    private void init(){
        //Get values from intent
        url = getIntent().getStringExtra("url");
        orderId = getIntent().getStringExtra("orderId");
        documentName = getIntent().getStringExtra("documentName");

        //Get Resource ID from XML
        ivCloseButton = (ImageView) findViewById(R.id.ivCloseButton);
        btnUploadNew = (Button) findViewById(R.id.btnUploadNew);
        ivDocument = (ImageView) findViewById(R.id.ivDocument);
        webView = (WebView) findViewById(R.id.webView);

        //Set Listener
        ivCloseButton.setOnClickListener(this);
        btnUploadNew.setOnClickListener(this);

        //Initialize variables
        images = new HashMap<String, TypedFile>();

        showDocument(url);
    }

    /**
     * Method to show dialog fof document upload
     * */
    void selectImage() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getResources().getString(R.string.upload_documents));
        dialog.setItems(R.array.image_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                switch (item) {
                    case 0:
                        try {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intent, LOAD_IMAGE_RESULTS);
                        } catch (Exception ex) {
                            Toast.makeText(ShowImageActivity.this, getResources().getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 1:
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/pdf");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, LOAD_PDF_FILE);
                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            }
        });
        dialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Matrix mat = new Matrix();
        if (requestCode == LOAD_IMAGE_RESULTS && resultCode == -1 && data != null) {

            try {
                imagePath = CommonUtils.getPath(this, data.getData());
                mat.postRotate(CommonUtils.getCameraPhotoOrientation(imagePath));
                Log.e("Imagepath", "" + imagePath);
                filenameAndUpload();
            } catch (Exception ne) {
                Toast.makeText(ShowImageActivity.this, getResources().getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == LOAD_PDF_FILE && resultCode == -1 && data != null) {
            try {
                imagePath = CommonUtils.getPath(this, data.getData());
                mat.postRotate(CommonUtils.getCameraPhotoOrientation(imagePath));
                Log.e("Imagepath", "" + imagePath);
                filenameAndUpload();
            } catch (Exception ne) {
                Toast.makeText(ShowImageActivity.this, getResources().getString(R.string.unable_to_perform_action), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra("uploadFlag", uploadFlag);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void showDocument(String url) {
        String extension = url.substring(url.lastIndexOf("."));
        if (extension.equalsIgnoreCase(".pdf")) {
            ivDocument.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + url);


        } else {
            ivDocument.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            Picasso.with(this).
                    load(url).
                    placeholder(R.drawable.loading_placeholder)
                    .into(ivDocument);
        }
    }

    private void filenameAndUpload() {
        switch (documentName) {
            case "POD":
                images.put("pod", new TypedFile("*/*", new File(imagePath)));
                break;
            case "Invoice":
                images.put("invoice", new TypedFile("*/*", new File(imagePath)));
                break;
            case "Consignment Notes":
                images.put("consigmentNote", new TypedFile("*/*", new File(imagePath)));
                break;
        }
        uploadDocumentApi();

    }

    private void uploadDocumentApi() {
        CommonUtils.showLoadingDialog(ShowImageActivity.this, getString(R.string.uploading_msg));
        RestClient.getWebServices().uploadDocument(accessToken, new TypedString(orderId), images, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                try {
                    FlurryAgent.onEvent("Upload document mode");
                    JSONObject jsonObject = new JSONObject(s);
                    CommonUtils.dismissLoadingDialog();
                    CommonUtils.showSingleButtonPopup(ShowImageActivity.this, jsonObject.getString("message"));
                    switch (documentName) {
                        case "POD":
                            url = jsonObject.getJSONObject("data").getString("podUpload");
                            break;
                        case "Invoice":
                            url = jsonObject.getJSONObject("data").getString("invoiceUpdate");
                            break;
                        case "Consignment Notes":
                            url = jsonObject.getJSONObject("data").getString("consigmentNoteUpdate");
                            break;
                    }
                    showDocument(url);
                    uploadFlag = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    CommonUtils.dismissLoadingDialog();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                uploadFlag = false;
                CommonUtils.dismissLoadingDialog();
                CommonUtils.showRetrofitError(ShowImageActivity.this, error);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivCloseButton:
                Intent intent=new Intent();
                intent.putExtra("uploadFlag",uploadFlag);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            case R.id.btnUploadNew:
                selectImage();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, MY_FLURRY_APIKEY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}