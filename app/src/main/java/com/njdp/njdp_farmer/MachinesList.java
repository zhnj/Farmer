package com.njdp.njdp_farmer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.MyClass.MachineInfo;
import com.njdp.njdp_farmer.adpter.MachineAdapter;
import com.njdp.njdp_farmer.conent_frament.FarmMachineSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MachinesList extends AppCompatActivity implements AdapterView.OnItemClickListener, Window.Callback {
    private List<String[]> group;
    private List<List<MachineInfo>> child;
    private List<MachineInfo> machineInfos;
    private final int CALL_PHONE = 8;
    private Intent intent;
    private ImageButton getback = null;

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
        setContentView(R.layout.activity_machines_list);

        //初始化参数及控件
        machineInfos = new ArrayList<>();
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.expandableListView);
        View machinelist = findViewById(R.id.root_div);
        assert machinelist != null;
        machinelist.getBackground().setAlpha(180);

        //获取农机数据
        machineInfos = AgentApplication.machinesToShow;

        /**
         * 初始化列表数据
         */
        initData();
        if (group.size() >= 0) {
            MachineAdapter adapter = new MachineAdapter(MachinesList.this, group, child, mListener);
            assert listView != null;
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
            listView.setGroupIndicator(null);  //不显示向下的箭头
        }

        getback = (ImageButton) super.findViewById(R.id.getback);
        //返回上一界面
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        group = new ArrayList<>();
        child = new ArrayList<>();
        //没有农机信息，直接返回
        if(machineInfos == null) {
            error_hint("没有周边农机信息！");
            return;
        }

        for (MachineInfo f : machineInfos) {
            addInfo(new String[]{f.getImage(), f.getName(), f.getRange(), f.getTelephone()}, new MachineInfo[]{f});
        }
    }

    /**
     * 添加数据信息
     * @param g 标题信息
     * @param c 发布的内容
     */
    private void addInfo(String[] g, MachineInfo[] c) {
        group.add(g);
        List<MachineInfo> list = Arrays.asList(c);
        child.add(list);
    }

    /**
     * 响应ListView中item的点击事件
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        Toast.makeText(this, "listview的item被点击了！，点击的位置是-->" + position,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 实现类，响应按钮点击事件
     */
    private MachineAdapter.MyClickListener mListener = new MachineAdapter.MyClickListener() {
        @Override
        public void myOnClick(int position, View v) {
            //Toast.makeText(MachinesList.this, "listview的内部的按钮被点击了！，位置是-->" + position + ",内容是-->" + group.get(position)[3], Toast.LENGTH_SHORT).show();
            intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + group.get(position)[3]));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //确认拨打电话的权限
            RequestPermission(CALL_PHONE);
        }
    };

    //申请权限
    public void RequestPermission(int type) {
        switch (type) {
            case CALL_PHONE:
                //检查拨打电话权限
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CALL_PHONE)) {

                        new AlertDialog.Builder(MachinesList.this)
                                .setMessage("应用需要使用拨打电话的权限，请授权")
                                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .create()
                                .show();
                    } else {
                        //申请权限
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.CALL_PHONE},
                                CALL_PHONE);
                    }
                } else {
                    //已经拥有权限，检查下一项权限
                    AgentApplication.permission_CALL_PHONE = true;
                    startActivity(intent);
                }
                break;
        }
    }

    //对申请权限的结果进行处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CALL_PHONE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    AgentApplication.permission_CALL_PHONE = true;
                    startActivity(intent);
                } else {
                    //用户拒绝了授权
                    AgentApplication.permission_CALL_PHONE = false;
                }
                break;
            }
        }
    }

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        View view = findViewById(R.id.root_div);
        assert view != null;
        view.setBackgroundResource(0); //释放背景图片
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
