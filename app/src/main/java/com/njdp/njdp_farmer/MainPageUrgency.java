package com.njdp.njdp_farmer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.db.SessionManager;

public class MainPageUrgency extends AppCompatActivity implements View.OnClickListener{
    private final int ACCESS_COARSE_LOCATION = 1;
    private final int ACCESS_FINE_LOCATION = 2;
    private String token;
    private Button urgency, farmland, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置沉浸模式
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main_page_urgency);

        token = getIntent().getStringExtra("TOKEN");
        //判断参数传递是否正确
        if (token == null) {
            error_hint("参数传递错误！");
            return;
        }

        initView();
        //申请相关权限
        RequestPermission(ACCESS_COARSE_LOCATION);
    }

    //加载控件布局
    private void initView() {
        urgency = (Button) this.findViewById(R.id.bt_urgency_manager);
        farmland = (Button) this.findViewById(R.id.bt_farmland_manager);
        logout = (Button) this.findViewById(R.id.btn_logout);

        initOnClick();
    }

    //设置点击操作
    private void initOnClick() {
        urgency.setOnClickListener(this);
        farmland.setOnClickListener(this);
        logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_urgency_manager:
                Log.e("------------->", "点击查看发布的紧急灾情信息");
                Intent intent1 = new Intent(MainPageUrgency.this, UrgencyList.class);
                intent1.putExtra("token", token);
                startActivity(intent1);
                break;
            case R.id.bt_farmland_manager:
                Log.e("------------->", "点击查看发布的紧急农田信息");
                Intent intent2 = new Intent(MainPageUrgency.this, FarmlandUrgencyList.class);
                intent2.putExtra("token", token);
                startActivity(intent2);
                break;
            case R.id.btn_logout:
                Log.e("------------->", "用户退出登录");
                SessionManager session = new SessionManager(getApplicationContext());
                session.setLogin(false, false, "");
                Intent intent3 = new Intent(MainPageUrgency.this, login.class);
                startActivity(intent3);
                finish();
        }
    }

    //逐项申请定位权限、扩展卡SD卡读写权限、读取手机当前的状态权限
    public void RequestPermission(int type) {
        switch (type){
            case ACCESS_COARSE_LOCATION:
                //检查网络定位权限
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {

                        new AlertDialog.Builder(MainPageUrgency.this)
                                .setMessage("应用需要使用网络定位权限，请授权")
                                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        RequestPermission(ACCESS_FINE_LOCATION);
                                    }
                                })
                                .create()
                                .show();
                    } else {
                        //申请权限
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                ACCESS_COARSE_LOCATION);
                    }
                } else {
                    //已经拥有权限，检查下一项权限
                    AgentApplication.permission_ACCESS_COARSE_LOCATION = true;
                    RequestPermission(ACCESS_FINE_LOCATION);
                }
                break;
            case ACCESS_FINE_LOCATION:
                //检查GPS定位权限
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                        new AlertDialog.Builder(MainPageUrgency.this)
                                .setMessage("应用需要使用GPS定位权限，请授权")
                                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        AgentApplication.permission_ACCESS_FINE_LOCATION = false;
                                    }
                                })
                                .create()
                                .show();
                    } else {
                        //申请权限
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                ACCESS_FINE_LOCATION);
                    }
                } else {
                    //已经拥有权限，检查下一项权限
                    AgentApplication.permission_ACCESS_FINE_LOCATION = true;
                }
                break;
        }
    }

    //对申请权限的结果进行处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_COARSE_LOCATION: {
                //存储授权结果
                AgentApplication.permission_ACCESS_COARSE_LOCATION = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                RequestPermission(ACCESS_FINE_LOCATION);
                break;
            }
            case ACCESS_FINE_LOCATION:{
                //存储授权结果
                AgentApplication.permission_ACCESS_FINE_LOCATION = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            }
        }
    }

    //错误信息提示1
    private void error_hint(String str) {
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    private long timeMillis;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - timeMillis) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                timeMillis = System.currentTimeMillis();
            } else {
                AgentApplication.ExitApp();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //释放背景图片
        View view = findViewById(R.id.top_layout);
        assert view != null;
        view.setBackgroundResource(0);
        urgency.setBackgroundResource(0);
        farmland.setBackgroundResource(0);
    }
}
