package com.jason.andfix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

  private static int REQ_PERMISSION_CODE = 1001;
  private static final String[] PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE};

  private TextView resultTv;
  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("native-lib");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    checkAndRequestPermissions();

    // Example of a call to a native method
    resultTv= findViewById(R.id.sample_text);
    resultTv.setText(stringFromJNI());
    DexFileManager.getInstance().setContext(this);
  }

  /**
   * 权限检测以及申请
   */
  private void checkAndRequestPermissions() {
    // Manifest.permission.WRITE_EXTERNAL_STORAGE 和  Manifest.permission.READ_PHONE_STATE是必须权限，允许这两个权限才会显示广告。

    if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

    } else {
      ActivityCompat.requestPermissions(this, PERMISSIONS, REQ_PERMISSION_CODE);
    }

  }


  /**
   * 权限判断
   * @param permissionName
   * @return
   */
  private boolean hasPermission(String permissionName) {
    return ActivityCompat.checkSelfPermission(this, permissionName)
        == PackageManager.PERMISSION_GRANTED;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    if (requestCode == REQ_PERMISSION_CODE) {
      checkAndRequestPermissions();
    }

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }



  /**
   * A native method that is implemented by the 'native-lib' native library, which is packaged with
   * this application.
   */
  public native String stringFromJNI();

  public void calculate(View view) {
    Calculator caclutor=new Calculator();

    Calculator caclutor1=new Calculator();
    Calculator caclutor3=new Calculator();
    caclutor.calculate();
    caclutor1.calculate();
    caclutor3.calculate();
    resultTv.setText(" 结果  "+caclutor3.calculate());
    Log.i("tuch", "jisuan: "+caclutor1.calculate());
  }

  public void fix(View view) {
    Log.i("tuch", " 路劲  ：  " + Environment.getExternalStorageDirectory());
    DexFileManager.getInstance().loadDexFile(new File(Environment.getExternalStorageDirectory(),"out.dex"));
  }
}
