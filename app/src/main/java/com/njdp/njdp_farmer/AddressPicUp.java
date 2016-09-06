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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;
import com.tdroid.imageselector.library.imageselelctor.ImageSelectorImageFromDialog;
import com.tdroid.imageselector.library.imageselelctor.ImageSelectorView;
import com.tdroid.imageselector.library.imageselelctor.SelectorBean;
import com.tdroid.imageselector.library.imageselelctor.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class AddressPicUp extends AppCompatActivity {
    private String token, id;
    private final int CAMERA = 7;
    private ArrayList<String> picsPath;
    private ImageSelectorView imageSelectorViewEditModel;
    private ImageSelectorView imageSelectorViewViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置沉浸模式
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        setContentView(R.layout.activity_address_pic_up);
        imageSelectorViewEditModel = (ImageSelectorView) findViewById(R.id.imageselector_editmodel);
        imageSelectorViewViewModel = (ImageSelectorView) findViewById(R.id.imageselector_viewmodel);
        TextView viewTitle = (TextView) findViewById(R.id.viewTitle);

        boolean isEdit = getIntent().getBooleanExtra("isEdit", false);

        if (AgentApplication.permission_READ_EXTERNAL_STORAGE && AgentApplication.permission_WRITE_EXTERNAL_STORAGE
                && AgentApplication.permission_MOUNT_UNMOUNT_FILESYSTEMS) {

            setImageSelectorConfig();
            if (isEdit) {
                //TODO: 编辑时显示已上传的图片
                token = getIntent().getStringExtra("token");
                id = getIntent().getStringExtra("id");
                if (token == null || id == null) {
                    error_hint("参数传递错误！");
                    finish();
                }
                picsPath = getIntent().getStringArrayListExtra("pics");
                if(picsPath != null){
                    loadImageSelectorSource();
                }
            } else {
                assert viewTitle != null;
                viewTitle.setVisibility(View.INVISIBLE);
                imageSelectorViewViewModel.setVisibility(View.INVISIBLE);
            }
        } else {
            error_hint("请在权限设置管理界面开放存储卡读取权限。");
        }
        //申请相关权限
        RequestPermission(CAMERA);

        ImageButton getback = (ImageButton) this.findViewById(R.id.getback);
        assert getback != null;
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //初始化图片选择模块
    private void setImageSelectorConfig() {
        //初始化
        imageSelectorViewEditModel.initSelector(this);
        imageSelectorViewViewModel.initSelector(this);
        //显示图片描述
        imageSelectorViewEditModel.setShowDisc(false);
        //显示图片描述
        imageSelectorViewViewModel.setShowDisc(false);
        //可编辑的imageviewselector监听设置
        imageSelectorViewEditModel.setImageSelectorViewListener(new ImageSelectorView.ImageSelectorViewListener() {
            @Override
            public void onChoice(Intent intent, int intentTag, Uri uri) {
                //该方法会调用系统相机. intentTag=ImageSelectorImageFromDialog.CAMERA_INTENT_REQUEST
                //需要处理onActivityResult
                //防止部分手机调用系统相机时，activity会被销毁而不能获取到传入的图片路径.
                //会在onActivityResult里获取该值
                if (AgentApplication.permission_CAMERA) {
                    SharedPreferencesUtils.setParam(AddressPicUp.this, "CameraPath", uri.getPath());
                    startActivityForResult(intent, intentTag);
                } else {
                    error_hint("不能调用相机，请在权限设置管理界面开放相机权限。");
                }
            }

            @Override
            public void onDelete(int position, SelectorBean selectorBean) {
                //删除图片操作时，需要调用
                imageSelectorViewEditModel.refreshDeleteResult(position);
            }
        });
    }

    //显示已经上传的街景图片
    private void loadImageSelectorSource() {
        //需要手动构建该集合
        List<SelectorBean> images = new ArrayList<>();
        //测试2张数据

        for (int i = 0; i < picsPath.size(); i++) {
            String path = "";
            String dis = "图片描述";
            SelectorBean selectorBean = new SelectorBean();

            if(picsPath.get(i).length()>5 && picsPath.get(i).charAt(0)=='/') {
                path = AppConfig.URL_IP + picsPath.get(i).substring(1);
            }else {
                path = AppConfig.URL_IP + picsPath.get(i);
            }
            //path="http://www.fenmr.com/uploads/allimg/141112/6-141112103607.jpg";
            dis+=(i+1);

            selectorBean.setDisc(dis);
            selectorBean.setPath(path);
            images.add(selectorBean);
        }
        imageSelectorViewViewModel.setSelectorData(images);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //相机
        if (resultCode == RESULT_OK && requestCode == ImageSelectorImageFromDialog.CAMERA_INTENT_REQUEST) {
            try {
                String path = (String) SharedPreferencesUtils.getParam(AddressPicUp.this, "CameraPath", "");
                //String path = "调用系统相继返回的图片路径";
                assert path != null;
                if (!path.equals("")) {
                    File file = new File(path);
                    if (file.exists()) {
                        //需要手动给imageSelectorView 传入值显示相机返回图片
                        imageSelectorViewEditModel.setImageFromCamera(path);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //上传图片操作
    public void UpPicClick(View v) {
        //获取imageselector里最终的数据
        List<SelectorBean> imageList = imageSelectorViewEditModel.getFinalImageList();
        if (imageList.size() == 0) {
            error_hint("没有选择图片！");
        }
        ArrayList<String> imageStrings = new ArrayList<>();
        for (int i = 0; i < imageList.size(); i++) {
            imageStrings.add(imageList.get(i).getPath());
        }
        Intent intent = new Intent(AddressPicUp.this, FarmerRelease.class);
        intent.putStringArrayListExtra("imagList", imageStrings);
        setResult(RESULT_OK, intent);
        finish();
    }

    //申请权限
    public void RequestPermission(int type) {
        switch (type) {
            case CAMERA:
                //检查相机权限
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA)) {

                        new AlertDialog.Builder(AddressPicUp.this)
                                .setMessage("应用需要使用相机权限，请授权")
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
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA);
                    }
                } else {
                    //已经拥有权限，检查下一项权限
                    AgentApplication.permission_CAMERA = true;
                }
                break;
        }
    }

    //对申请权限的结果进行处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意了授权
                    AgentApplication.permission_CAMERA = true;
                } else {
                    //用户拒绝了授权
                    AgentApplication.permission_CAMERA = false;
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
        imageSelectorViewEditModel.destroyDrawingCache();
        imageSelectorViewEditModel = null;
        imageSelectorViewViewModel.destroyDrawingCache();
        imageSelectorViewViewModel = null;
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
