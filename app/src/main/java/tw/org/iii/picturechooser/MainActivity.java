package tw.org.iii.picturechooser;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    Button picker,camera;
    private static final int REQUEST_EXTERNAL_STORAGE = 200;
    File tmpFile;
    ImageView imageView;
    Uri tmpFileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
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
            }
        });

        picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPictureChooser();
                Log.v("brad","initPictureChooser Click");
            }
        });
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
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        tmpFile = new File(
//                Environment.getExternalStorageDirectory(),"image.jpg");
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
//      相片即時更新

//      相片即時更新
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            Log.v("brad","RESULT_OK");
//            if (data !=null) {
                switch (requestCode){
                    case 100:
                        Log.v("brad","case 100");
                        if (data !=null) {
                            Uri uri = data.getData();
                            ContentResolver cr = this.getContentResolver();
                            try {
                                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                                imageView.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                Log.v("brad", e.toString());
                            }
                        }
                        else{
                            Toast.makeText(this,"無法找到檔案",Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 200:
                        if (data !=null) {
                            Log.v("brad","data!=null:"+tmpFile.getAbsolutePath().toString());
//                            Bitmap bmp = BitmapFactory.decodeFile(tmpFile.getAbsolutePath());
////                            Bitmap bmp = (Bitmap)data.getExtras().get("data");
//                            imageView.setImageBitmap(bmp);
////                            Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+ "/image.jpg");
////                            imageView.setImageBitmap(bmp);
                        }else{
                            if(tmpFile.exists()){
                                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                File f = new File(tmpFile.toString());
                                Uri contentUri = Uri.fromFile(f);
                                mediaScanIntent.setData(contentUri);
                                this.sendBroadcast(mediaScanIntent);
                                Log.v("brad",tmpFile.getAbsolutePath().toString());
                                Bitmap bmp = BitmapFactory.decodeFile(tmpFile.getAbsolutePath());
//                                Bitmap bmp = BitmapFactory.decodeFile(
//                                        Environment.getExternalStorageDirectory()+File.separator+"image.jpg");
                                imageView.setImageBitmap(bmp);
//                                tmpFile.mkdir();
                            }else{
                                Toast.makeText(this,"無法找到檔案",Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                }
//                if(requestCode == 100){
//                    Uri uri = data.getData();
//                    ContentResolver cr = this.getContentResolver();
//                    try {
//                        Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
//                        ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                        imageView.setImageBitmap(bitmap);
//                    } catch (Exception e) {
//                        Log.v("brad", e.toString());
//                    }
//                }
//            }else{
//                Toast.makeText(this,"無法找到檔案",Toast.LENGTH_LONG).show();
//            }
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
}
