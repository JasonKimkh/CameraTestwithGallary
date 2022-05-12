package com.example.cameratest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.cameratest.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private WebView webView;
    private WebSettings webViewSetting;
    private static final int MY_PERMISSION_STORAGE = 1111;
    private static String filePath;
    String mCurrentPhotoPath;
   // CameraTest cameraTest = new CameraTest(this);

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        checkPermission(); // 권한 획득

        webView = binding.activityMainCamera;
        Log.v("MainActivity Webview", String.valueOf(webView));
        //webview 세팅
        webViewSetting = webView.getSettings();
        webViewSetting.setJavaScriptEnabled(true);
        webViewSetting.setLoadWithOverviewMode(true);
        webViewSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webViewSetting.setUseWideViewPort(true);
        webViewSetting.setLoadsImagesAutomatically(true);
        webViewSetting.setUseWideViewPort(true);
        webViewSetting.setSupportZoom(true);
        webViewSetting.setAllowFileAccess(true);
        webViewSetting.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient(){});
        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(this, "Android");

        webView.loadUrl("file:///android_asset/Camera.html");
        Log.v("MainActivity Webview", String.valueOf(webView));





    }

    public void checkPermission() {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("checkVerify() : ","if문 들어옴");

            //카메라 또는 저장공간 권한 획득 여부 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {

                Toast.makeText(getApplicationContext(),"권한 관련 요청을 허용해 주셔야 카메라 캡처이미지 사용등의 서비스를 이용가능합니다.",Toast.LENGTH_SHORT).show();

            } else {
//
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

   /* public void loadWeb(String url){
        Log.d("##", "loadWeb: webView : "+binding.activityMainCamera);
        binding.activityMainCamera.loadUrl(url);
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_STORAGE:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(MainActivity.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Toast.makeText(MainActivity.this, "해당 권한을 활성화 되었습니다.", Toast.LENGTH_SHORT).show();

                break;
        }


    }

    @JavascriptInterface
    public void takePicture() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;

        try {
            photoFile = createImageFile();
            filePath = String.valueOf(photoFile);
            Log.d("mCurrentPhotoPath", String.valueOf(mCurrentPhotoPath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Uri photoURI = FileProvider.getUriForFile(this,
                "com.example.cameratest.fileprovider", photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(takePictureIntent, 99);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("is requestCode got?", String.valueOf(requestCode));
        if (requestCode == 99) {
            Log.d("is requestCode got?", String.valueOf(requestCode));
            if (resultCode == RESULT_OK) {
                Log.d("is resultcode ResultOK??", String.valueOf(resultCode));
                resizeImage();
                setWebviewImage();

            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void resizeImage() {
        String path = filePath;
        Log.v("reszieImage_filePath:", filePath);
        OutputStream out = null;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            //사진을 1/10크기로 만든다.
            int width = bitmap.getWidth() / 10;
            int height = bitmap.getHeight() / 10;
            Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

            //화면 회전이 된다.
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotateBitmap = Bitmap.createBitmap(resizeBitmap, 0, 0, width, height, matrix, true);

            //결과를 다시 저장한다.
            Bitmap resultBitmap = rotateBitmap;
            File photoFile = new File(path);
            out = new FileOutputStream(photoFile);
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void setWebviewImage() {

        String path = "file://" + filePath;
        Log.v("setWebViewImage_filePath:", path);
        //String script = "javascript:setImage(\"" + path + "\")";
        //Log.d("script 내용", String.valueOf(script));
        Log.d("webview null?", String.valueOf(webView));
        webView.loadUrl("javascript:setImage('"+path+"')");
        webView.loadUrl("javascript:alert(1)");



        //MainActivity mainActivity = new MainActivity();
        //mainActivity.loadWeb(script);
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("##", "onActivityResult: ");

        cameraTest.onActivityResult(requestCode, resultCode, data);

    }*/
}