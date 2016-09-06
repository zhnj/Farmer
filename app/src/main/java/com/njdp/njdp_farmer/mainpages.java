package com.njdp.njdp_farmer;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.njdp.njdp_farmer.CostomProgressDialog.CustomProgressDialog;
import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.conent_frament.*;
import com.njdp.njdp_farmer.viewpage.ContentViewPager;

import java.util.ArrayList;
import java.util.List;

public class mainpages extends AppCompatActivity {
    private String token;
    private int openModule;
    private CustomProgressDialog progressDialog;
    private ContentViewPager contentViewPager;
    private final int ACCESS_COARSE_LOCATION = 1;
    private final int ACCESS_FINE_LOCATION = 2;
    private final int READ_PHONE_STATE = 3;
    private final int WRITE_EXTERNAL_STORAGE = 4;
    private final int READ_EXTERNAL_STORAGE = 5;
    private final int MOUNT_UNMOUNT_FILESYSTEMS = 6;

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
        setContentView(R.layout.activity_mainpage);
        AgentApplication.addActivity(this);

        if(progressDialog != null) {
            progressDialog.cancel();
        }
        progressDialog = new CustomProgressDialog(this,"数据正在请求中...", R.anim.donghua_frame);
        progressDialog.show();

        //checkNetState();//检查网络状态
        //获取参数
        token = getIntent().getStringExtra("TOKEN");
        openModule = getIntent().getIntExtra("openModule", 0);
        //申请相关权限
        RequestPermission(ACCESS_COARSE_LOCATION);
    }

    private List<Fragment> content_list = null;

    private void initdata() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //要传递的参数
        Bundle bundle1 = new Bundle();
        bundle1.putString("token", token);
        if(content_list == null)
            content_list = new ArrayList<>();
        else
            content_list.clear();
        //农户发布界面
        FarmlandManager farmlandManager = new FarmlandManager();
        farmlandManager.setArguments(bundle1);
        content_list.add(farmlandManager);
        //农机查询界面
        progressDialog.setContent("正在准备农机信息！");
        FarmMachineSearch farmMachineSearch = new FarmMachineSearch();
        farmMachineSearch.setArguments(bundle1);
        content_list.add(farmMachineSearch);
        //个人信息界面，需要用到农田发布的数据，先加载
        progressDialog.setContent("正在准备个人数据！");
        PersonalInfoFrame personalInfoFrame = new PersonalInfoFrame();
        personalInfoFrame.setArguments(bundle1);
        content_list.add(personalInfoFrame);
        transaction.commit();
    }

    private void initview(int page) {
        if (content_list == null) {
            return;
        }
        contentViewPager = (ContentViewPager) findViewById(R.id.content_viewpager);
        RadioGroup contentradiogroup = (RadioGroup) findViewById(R.id.content_radiogroup);
        //预加载一页
        contentViewPager.setOffscreenPageLimit(3);
        contentViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return content_list.get(i);
            }

            @Override
            public int getCount() {
                return content_list.size();
            }

        });
        assert contentradiogroup != null;
        contentradiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_release:
                        contentViewPager.setCurrentItem(0);
                        break;
                    case R.id.rb_search:
                        contentViewPager.setCurrentItem(1);
                        break;
                    case R.id.rb_userInfo:
                        contentViewPager.setCurrentItem(2);
                        break;
                }
            }
        });
        if(page == 1) {
            contentradiogroup.check(R.id.rb_release);
        }else if(page == 2){
            contentradiogroup.check(R.id.rb_search);
        }else {
            contentradiogroup.check(R.id.rb_userInfo);
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

                        new AlertDialog.Builder(mainpages.this)
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

                        new AlertDialog.Builder(mainpages.this)
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
                                        RequestPermission(WRITE_EXTERNAL_STORAGE);
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
                    RequestPermission(WRITE_EXTERNAL_STORAGE);
                }
                break;
            case WRITE_EXTERNAL_STORAGE:
                //写扩展卡的权限
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        new AlertDialog.Builder(mainpages.this)
                                .setMessage("应用需要使用读写扩展卡的权限，请授权")
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
                                        RequestPermission(READ_EXTERNAL_STORAGE);
                                    }
                                })
                                .create()
                                .show();
                    } else {
                        //申请权限
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_STORAGE);
                    }
                } else {
                    //已经拥有权限，检查下一项权限
                    AgentApplication.permission_WRITE_EXTERNAL_STORAGE = true;
                    RequestPermission(READ_EXTERNAL_STORAGE);
                }
                break;
            case READ_EXTERNAL_STORAGE:
                //读扩展卡的权限
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        new AlertDialog.Builder(mainpages.this)
                                .setMessage("应用需要使用读写扩展卡的权限，请授权")
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
                                        RequestPermission(MOUNT_UNMOUNT_FILESYSTEMS);
                                    }
                                })
                                .create()
                                .show();
                    } else {
                        //申请权限
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                READ_EXTERNAL_STORAGE);
                    }
                } else {
                    //已经拥有权限，检查下一项权限
                    AgentApplication.permission_READ_EXTERNAL_STORAGE = true;
                    RequestPermission(MOUNT_UNMOUNT_FILESYSTEMS);
                }
                break;
            case MOUNT_UNMOUNT_FILESYSTEMS:
                //读写SD卡的权限
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)) {

                        new AlertDialog.Builder(mainpages.this)
                                .setMessage("应用需要使用读写SD卡的权限，请授权")
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
                                        RequestPermission(READ_PHONE_STATE);
                                    }
                                })
                                .create()
                                .show();
                    } else {
                        //申请权限
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS},
                                MOUNT_UNMOUNT_FILESYSTEMS);
                    }
                } else {
                    //已经拥有权限，检查下一项权限
                    AgentApplication.permission_MOUNT_UNMOUNT_FILESYSTEMS = true;
                    RequestPermission(READ_PHONE_STATE);
                }
                break;
            case READ_PHONE_STATE:
                //检查读取手机当前的状态权限
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_PHONE_STATE)) {

                        new AlertDialog.Builder(mainpages.this)
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
                                        //填充数据
                                        initdata();
                                        if(openModule == 1 || openModule == 2 || openModule == 3){
                                            initview(openModule);//填充布局
                                        }
                                        else{
                                            error_hint("参数传输错误！");
                                            finish();
                                        }
                                        progressDialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    } else {
                        //申请权限
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                READ_PHONE_STATE);
                    }
                } else {
                    //已经拥有权限
                    AgentApplication.permission_READ_PHONE_STATE = true;
                    //填充数据
                    initdata();
                    if(openModule == 1 || openModule == 2 || openModule == 3){
                        initview(openModule);//填充布局
                    }
                    else{
                        error_hint("参数传输错误！");
                        finish();
                    }
                    progressDialog.dismiss();
                }
                break;
        }
    }

    //对申请权限的结果进行处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    AgentApplication.permission_ACCESS_COARSE_LOCATION = true;
                } else {
                    //用户拒绝了授权
                    AgentApplication.permission_ACCESS_COARSE_LOCATION = false;
                }
                RequestPermission(ACCESS_FINE_LOCATION);
                break;
            }
            case ACCESS_FINE_LOCATION:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    AgentApplication.permission_ACCESS_FINE_LOCATION = true;
                } else {
                    //用户拒绝了授权
                    AgentApplication.permission_ACCESS_FINE_LOCATION = false;
                }
                RequestPermission(WRITE_EXTERNAL_STORAGE);
                break;
            }
            case WRITE_EXTERNAL_STORAGE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    AgentApplication.permission_WRITE_EXTERNAL_STORAGE = true;
                } else {
                    //用户拒绝了授权
                    AgentApplication.permission_WRITE_EXTERNAL_STORAGE = false;
                }
                RequestPermission(READ_EXTERNAL_STORAGE);
                break;
            }
            case READ_EXTERNAL_STORAGE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    AgentApplication.permission_READ_EXTERNAL_STORAGE = true;
                } else {
                    //用户拒绝了授权
                    AgentApplication.permission_READ_EXTERNAL_STORAGE = false;
                }
                RequestPermission(MOUNT_UNMOUNT_FILESYSTEMS);
                break;
            }
            case MOUNT_UNMOUNT_FILESYSTEMS:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    AgentApplication.permission_MOUNT_UNMOUNT_FILESYSTEMS = true;
                } else {
                    //用户拒绝了授权
                    //AgentApplication.permission_MOUNT_UNMOUNT_FILESYSTEMS = false;
                    AgentApplication.permission_MOUNT_UNMOUNT_FILESYSTEMS = true; //Android 6.0已禁用此权限
                }
                RequestPermission(READ_PHONE_STATE);
                break;
            }
            case READ_PHONE_STATE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    AgentApplication.permission_READ_PHONE_STATE = true;
                } else {
                    //用户拒绝了授权
                    AgentApplication.permission_READ_PHONE_STATE = false;
                }
                //填充数据
                initdata();
                if(openModule == 1 || openModule == 2 || openModule == 3){
                    initview(openModule);//填充布局
                }
                else{
                    error_hint("参数传输错误！");
                    finish();
                }
                progressDialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //释放资源
        content_list.clear();
        content_list = null;
        View view = findViewById(R.id.top_layout);
        assert view != null;
        view.setBackgroundResource(0); //释放背景图片
        AgentApplication.removeActivity(this);

        getRunningAppProcessInfo();
        //setBackgroundResource和 android:background → setBackgroundResource(0);
        //setBackgroundDrawable( background) → setBackgroundDrawable (null)
        //setBackground ( background ) → setBackground ( null )
    }

    //查看系统内存占用情况
    private void getRunningAppProcessInfo() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        //获得系统里正在运行的所有进程
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessesList = mActivityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessesList) {
            // 进程ID号
            int pid = runningAppProcessInfo.pid;
            // 用户ID
            int uid = runningAppProcessInfo.uid;
            // 进程名
            String processName = runningAppProcessInfo.processName;
            // 占用的内存
            int[] pids = new int[] {pid};
            Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(pids);
            int memorySize = memoryInfo[0].dalvikPrivateDirty;

            System.out.println("processName=" + processName + ",pid=" + pid + ",uid=" + uid + ",memorySize=" + memorySize + "kb");
        }
    }

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //不跟随系统变化字体大小
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
}
