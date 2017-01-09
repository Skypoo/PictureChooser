package tw.org.iii.picturechooser;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Button picker,camera;
//    Button grey1,test;
    private static final int REQUEST_EXTERNAL_STORAGE = 200;
    File tmpFile,chooserFile,path;
//    ImageView imageView;
    Uri tmpFileUri,chooserFileUri;
    Bitmap bmp;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        imageView = (ImageView) findViewById(R.id.imageView);
        camera = (Button)findViewById(R.id.camera);
        picker = (Button)findViewById(R.id.chooser);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED )
        {
            //未取得權限，向使用者要求允許權限
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    REQUEST_EXTERNAL_STORAGE);
        }
        else {}

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("brad","initCamera Click");
                initCamera();
//                grey1.setEnabled(true);
            }
        });

        picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPictureChooser();
//                grey1.setEnabled(true);
                Log.v("brad","initPictureChooser Click");
            }
        });


//
//        test.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GcodeThread gt1 = new GcodeThread();
//                gt1.start();
//                test.setEnabled(false);
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.v("brad","onRequestPermissionResult");
                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){}
        else{}
    }

    public void initCamera(){
        Log.v("brad","initCamera");
//        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        tmpFile = new File(
//                Environment.getExternalStorageDirectory(),"image.jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File path = getExtermalStoragePublicDir("picturechooser");
        Log.v("brad",path.toString());
        if(!path.exists()){
            path.mkdir();
        }
        tmpFile = new File(path,"image"+System.currentTimeMillis()+".jpg");
        Log.v("brad",tmpFile.toString());
        tmpFileUri = Uri.fromFile(tmpFile);
        Log.v("brad",tmpFileUri.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpFileUri);
        startActivityForResult(intent,200);
        Log.v("brad","intent:"+intent.toString());
    }

    public void initPictureChooser (){
        Log.v("brad","initPictureChooser");
        Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
        picker.setType("image/*");
        picker.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        Intent desIntent = Intent.createChooser(picker,null);
        startActivityForResult(desIntent,100);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 100:
                Log.v("brad","case 100");
                if (data !=null) {
                    Uri uri = data.getData();
                    String path = getPath(MainActivity.this, uri);
                    CropDialog mCropDialog = new CropDialog(MainActivity.this, path);
                    mCropDialog.show();
                    mCropDialog.setOnCropFinishListener(new CropDialog.OnCropFinishListener() {
                        @Override
                        public void onCrop(String path) {
                            if(path != null && !path.equals("")) {
                                bmp = BitmapFactory.decodeFile(path);
                                new DisplayImageDialog(MainActivity.this, bmp).show();
                            }
                        }
                    });
                }
                else{
                    Log.v("brad","data =null");
                    Toast.makeText(this,"無法找到檔案",Toast.LENGTH_LONG).show();
                }

                break;
            case 200:
                if (resultCode == Activity.RESULT_OK) {
                        if (tmpFile.exists()) {
//--------------------------更新相片資料到外部儲存裝置上-------------------------------------------------
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            File f = new File(tmpFile.toString());
                            Uri contentUri = Uri.fromFile(f);
                            mediaScanIntent.setData(contentUri);
                            this.sendBroadcast(mediaScanIntent);
//--------------------------更新相片資料到外部儲存裝置上-------------------------------------------------
                            String path = getPath(MainActivity.this, tmpFileUri);
                            CropDialog mCropDialog = new CropDialog(MainActivity.this, path);
                            mCropDialog.show();
                            mCropDialog.setOnCropFinishListener(new CropDialog.OnCropFinishListener() {
                                @Override
                                public void onCrop(String path) {
                                    if(path != null && !path.equals("")) {
                                        bmp = BitmapFactory.decodeFile(path);
                                        new DisplayImageDialog(MainActivity.this, bmp).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(this, "無法找到檔案", Toast.LENGTH_LONG).show();
                        }
                    break;
                }
        }
    }

    private File getExtermalStoragePublicDir(String albumName) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if(path.mkdir()){
            File f = new File(path, albumName);
            if(f.mkdir()){
                return f;
            }
        }
        return new File(path, albumName);
    }



    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }



//-------------------------------------------------------------------------------------






}
