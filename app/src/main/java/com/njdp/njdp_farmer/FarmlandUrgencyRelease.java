package com.njdp.njdp_farmer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.MyClass.FarmlandInfoUrgency;
import com.njdp.njdp_farmer.MyClass.UrgencyInfo;
import com.njdp.njdp_farmer.address.AddressSelect;
import com.njdp.njdp_farmer.conent_frament.FarmlandManager;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FarmlandUrgencyRelease extends AppCompatActivity {
    private final String TAG = "FarmlandUrgencyRelease";
    private static final int ADDRESSEDIT = 1;
    private final DateFormat yyyymmdd_DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private final String[] crops = new String[]{"小麦", "玉米", "水稻", "谷物", "其他"};
    private final String[] crops1 = new String[]{"WH", "CO", "RC", "GR", "OT"};
    private boolean isEdit = false;
    private String token;
    private FarmlandInfoUrgency farmlandInfoUrgency;
    private ProgressDialog pDialog;

    private EditText croptype, area, address, person_name, phone, deadline;
    private Button releaseEditFinish;
    private ImageButton getback=null;
    private boolean firstSearchBaiduGPS;


    ////////////////根据地址的经纬度变量///////////////
    GeoCoder mSearch;
    MyOnGetGeoCoderResultListener myOnGetGeoCoderResultListener;

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
        setContentView(R.layout.activity_farmland_urgency_release);

        farmlandInfoUrgency = (FarmlandInfoUrgency)getIntent().getSerializableExtra("farmlandInfoUrgency");
        if (farmlandInfoUrgency == null) {
            farmlandInfoUrgency = new FarmlandInfoUrgency();
            isEdit = false;
        }else {
            isEdit = true;
        }

        token = getIntent().getStringExtra("token");
        if (token == null) {
            error_hint("参数传递错误！");
            finish();
        }
        initView();

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        myOnGetGeoCoderResultListener = new MyOnGetGeoCoderResultListener();
        mSearch.setOnGetGeoCodeResultListener(new MyOnGetGeoCoderResultListener());
    }

    private void initView() {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        croptype = (EditText) this.findViewById(R.id.crops_kind);
        area = (EditText) this.findViewById(R.id.area);
        address = (EditText) this.findViewById(R.id.address);
        person_name = (EditText) this.findViewById(R.id.person_name);
        phone = (EditText) this.findViewById(R.id.phone);
        deadline = (EditText) this.findViewById(R.id.deadline);
        releaseEditFinish = (Button) this.findViewById(R.id.btn_editFinish);
        if (phone != null) {
            phone.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        getback=(ImageButton) this.findViewById(R.id.getback);
        TextView top_title = (TextView) this.findViewById(R.id.tv_top_title);
        croptype.setText("小麦");

        //如果是编辑的话，初始化数据
        if(isEdit){
            assert top_title != null;
            top_title.setText("修改农田信息");
            croptype.setText(crops[indexArry(crops1, farmlandInfoUrgency.getCrops_kind())]);
            area.setText(String.valueOf(farmlandInfoUrgency.getArea()));
            address.setText(farmlandInfoUrgency.getProvince() + "-" + farmlandInfoUrgency.getCity() + "-" + farmlandInfoUrgency.getCounty() + "-" +
                    farmlandInfoUrgency.getTown() + "-" + farmlandInfoUrgency.getVillage());
            person_name.setText(farmlandInfoUrgency.getPerson());
            phone.setText(farmlandInfoUrgency.getPhone());
            deadline.setText(farmlandInfoUrgency.getDeadline());
            releaseEditFinish.setText("确认修改");
        }

        initSpinner();
        initOnClick();
    }

    private void initSpinner(){
        //绑定适配器
        UrgencySpinnerAdapter adapter = new UrgencySpinnerAdapter(this);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if(isEdit){
            UrgencyInfo urgencyInfo = null;
            for(UrgencyInfo u : AgentApplication.urgencyInfos){
                if(u.getId().equals(farmlandInfoUrgency.getDisaster_id())) {
                    urgencyInfo = u;
                    break;
                }
            }
            if(urgencyInfo == null){
                error_hint("未能找到相匹配的紧急灾情项！");
                return;
            }
            adapter.add(urgencyInfo);
        }else {
            ArrayList<UrgencyInfo> urgencyInfos = new ArrayList<>();
            for (UrgencyInfo u : AgentApplication.urgencyInfos) {
                try {
                    Date date = yyyymmdd_DateFormat.parse(u.getDisaster_time());
                    if (date.after(new Date())){
                        urgencyInfos.add(u);
                    }
                }catch (ParseException px){
                    Log.d(TAG, "DateParseException: " + u.getDisaster_time());
                }
            }
            adapter.addAll(urgencyInfos);
            if(urgencyInfos.size() == 0){
                error_hint("未找到可用的紧急灾情项！");
                return;
            }
        }
        //获取紧急灾情选择下拉窗
        Spinner spinner = (Spinner) findViewById(R.id.sp_urgency);
        //为spinner添加适配器
        assert spinner != null;
        spinner.setAdapter(adapter);
        //设置Spinner下拉列表的标题
        spinner.setPrompt("选择要查询的灾情批次");
        //为spinner绑定监听器
        spinner.setOnItemSelectedListener(new SpinnerListener());
        spinner.setSelection(0);
    }

    private void initOnClick() {
        croptype.setOnClickListener(handler);
        address.setOnClickListener(handler);
        getback.setOnClickListener(handler);

        releaseEditFinish.setOnClickListener(handler);
        releaseEditFinish.setEnabled(false);
        releaseEditFinish.setClickable(false);
        editTextIsNull();
    }

    //该监听器用于监听用户多spinner的操作
    class SpinnerListener implements AdapterView.OnItemSelectedListener {
        //当用户选择先拉列表中的选项时会调用这个方法
        /**
         *参数说明：
         *第一个：当前的下拉列表，也就是第三个参数的父view
         *第二个：当前选中的选项
         *第三个：所选选项的位置
         *第四个： 所选选项的id
         */
        public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                   long id) {
            //获取用户所选的选项内容
            String urgency_id = AgentApplication.urgencyInfos.get(position).getId();
            farmlandInfoUrgency.setDisaster_id(urgency_id);
            farmlandInfoUrgency.setDeadline(AgentApplication.urgencyInfos.get(position).getDisaster_time());
            deadline.setText(farmlandInfoUrgency.getDeadline());
        }
        //当用户不做选择时调用的该方法
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

    View.OnClickListener handler = new View.OnClickListener()
    {
        public void onClick (View v) {
            switch (v.getId()) {
                // TODO: 根据点击进行不同的处理
                case R.id.crops_kind:
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(FarmlandUrgencyRelease.this);
                    builder.setTitle("作物类型");
                    int a = indexArry(crops, croptype.getText().toString());
                    builder.setSingleChoiceItems(crops, a, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            croptype.setText(crops[position]);
                            farmlandInfoUrgency.setCrops_kind(crops1[position]);
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    break;
                case R.id.address:
                    Intent intent1 = new Intent(FarmlandUrgencyRelease.this, AddressSelect.class);
                    intent1.putExtra("address", address.getText().toString());
                    startActivityForResult(intent1, ADDRESSEDIT);
                    break;
                case R.id.getback:
                    mSearch.destroy();
                    finish();
                case R.id.btn_editFinish:
                    Log.e("------------->", "点击发布紧急农田信息");
                    checkRelease();
                    break;
            }
        }
    };


    //这是跳转到另一个布局页面返回来的操作
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            return;
        }
        switch (requestCode) {
            case ADDRESSEDIT:
                String add = data.getStringExtra("address");
                address.setText(add);
                break;
        }
    }

    //发布紧急农田信息
    public void checkRelease() {

        String tag_string_req = "req_farmland_urgency_release";

        pDialog.setMessage("正在发布 ...");
        showDialog();
        Log.i("GGGG", farmlandInfoUrgency.getLongitude() + ":" + farmlandInfoUrgency.getLatitude());
        if (!NetUtil.checkNet(this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {

            if (farmlandInfoUrgency.getLongitude().length() == 0 || farmlandInfoUrgency.getLatitude().length() == 0) {
                hideDialog();
                error_hint("发布失败，没有获取到有效的GPS位置信息！");
                return;
            }
            String ReqUrl; //需要连接的URL
            if(isEdit){
                ReqUrl = AppConfig.URL_FARMLAND_URGENCY_EDIT;
            }else {
                ReqUrl = AppConfig.URL_FARMLAND_URGENCY_RELEASE;
            }
            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    ReqUrl, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<>();
                    params.put("token", token);
                    if(isEdit){
                        params.put("urgencyFarmlandID", String.valueOf(farmlandInfoUrgency.getId()));
                    }else {
                        params.put("disaster_id", String.valueOf(farmlandInfoUrgency.getDisaster_id()));
                    }
                    String crops_kind;
                    crops_kind = crops1[indexArry(crops, croptype.getText().toString())];
                    farmlandInfoUrgency.setCrops_kind(crops_kind);
                    params.put("Urge_Farmlands_crops", farmlandInfoUrgency.getCrops_kind());
                    params.put("Urge_Farmerland_area", String.valueOf(farmlandInfoUrgency.getArea()));
                    params.put("Urge_Farmlands_province", farmlandInfoUrgency.getProvince());
                    params.put("Urge_Farmlands_city", farmlandInfoUrgency.getCity());
                    params.put("Urge_Farmlands_county", farmlandInfoUrgency.getCounty());
                    params.put("Urge_Farmlands_town", farmlandInfoUrgency.getTown());
                    params.put("Urge_Farmlands_village", farmlandInfoUrgency.getVillage());
                    params.put("Urge_Farmlands_longitude", farmlandInfoUrgency.getLongitude());
                    params.put("Urge_Farmlands_Latitude", farmlandInfoUrgency.getLatitude());
                    params.put("Urge_person", farmlandInfoUrgency.getPerson());
                    params.put("Urge_phone", farmlandInfoUrgency.getPhone());
                    params.put("Urge_deadline", farmlandInfoUrgency.getDeadline());
                    return params;
                }
            };
            strReq.setRetryPolicy(new DefaultRetryPolicy(2000,1,1.0f)); //请求超时时间2S，重复1次
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    //响应服务器成功
    private Response.Listener<String> mSuccessListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i("tagconvertstr", "[" + response + "]");
            Log.d(TAG, "Release Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");

                // Check for error node in json
                if (status == 0) {
                    // user successfully logged in
                    if(isEdit) {
                        error_hint("农田信息修改成功！");
                    }
                    else {
                        error_hint("发布成功！");
                    }
                    //clean frament
                    setContentNUll();

                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmlandUrgencyRelease.this, login.class);
                    startActivity(intent);
                    setContentNUll();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmlandUrgencyRelease.this, login.class);
                    startActivity(intent);
                    setContentNUll();
                }else if(status == 29){
                    error_hint("农田信息不完整！");
                }else  if(status == 31){
                    error_hint("农田数据已过期，不能修改！");
                } else if(status == 32){
                    error_hint("非法访问，无此权限。");
                }else{
                    error_hint("其他未知错误！");
                }
            } catch (JSONException e) {
                empty_hint(R.string.connect_error);
                // JSON error
                e.printStackTrace();
                Log.e(TAG, "Json error：response错误！" + e.getMessage());
            }
        }
    };

    //响应服务器失败
    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Release Error: " + error.getMessage());
            error_hint("服务器连接失败");
            hideDialog();
        }
    };

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //错误信息提示1
    private void error_hint(String str) {
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //错误信息提示2
    private void empty_hint(int in) {
        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //查找数组中的位置
    private int indexArry(String[] source, String str) {
        int i = -1;
        for (String s : source) {
            i++;
            if (str.equals(s)) {
                return i;
            }
        }
        return -1;
    }

    //清空发布界面的录入信息
    private void setContentNUll() {
        mSearch.destroy();
        //返回结果
        Intent intent = new Intent(FarmlandUrgencyRelease.this, FarmerLandList.class);
        intent.putExtra("farmlandInfoUrgency", farmlandInfoUrgency);

        setResult(RESULT_OK, intent);
        finish();
    }

    private Location getLocalGPS() {
        //地理位置服务提供者
        String locationProvider;
        //获取并返回Location
        if (ActivityCompat.checkSelfPermission(FarmlandUrgencyRelease.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            error_hint("请在权限设置管理界面开放定位权限。");
            return null;
        }
        //获取地理位置管理器
        LocationManager locationManager = (LocationManager) FarmlandUrgencyRelease.this.getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
            if(locationManager.getLastKnownLocation(locationProvider) != null){
                return locationManager.getLastKnownLocation(locationProvider);
            }
        }
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
            if(locationManager.getLastKnownLocation(locationProvider) != null){
                return locationManager.getLastKnownLocation(locationProvider);
            } else {
                error_hint("没有可用的位置提供器，请检查本设备是否支持定位功能。");
                return null;
            }
        }

        return null;
    }

    //输入是否为空，判断是否禁用按钮
    private void editTextIsNull(){

        croptype.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(address.getText()) && !TextUtils.isEmpty(person_name.getText())
                        && !TextUtils.isEmpty(phone.getText()) && !TextUtils.isEmpty(deadline.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
            }
        });

        area.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(address.getText())
                        && !TextUtils.isEmpty(person_name.getText()) && !TextUtils.isEmpty(phone.getText()) && !TextUtils.isEmpty(deadline.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                if(s.length() > 0) {
                    farmlandInfoUrgency.setArea(Float.valueOf(s.toString()));
                }else {
                    farmlandInfoUrgency.setArea(0);
                }
            }
        });

        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(person_name.getText())
                        && !TextUtils.isEmpty(phone.getText()) && !TextUtils.isEmpty(deadline.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                String temp[] = s.toString().split("-");
                if (temp.length > 4) {
                    farmlandInfoUrgency.setProvince(temp[0]);
                    farmlandInfoUrgency.setCity(temp[1]);
                    farmlandInfoUrgency.setCounty(temp[2]);
                    farmlandInfoUrgency.setTown(temp[3]);
                    farmlandInfoUrgency.setVillage(temp[4]);

                    //通过村名查找坐标位置
                    firstSearchBaiduGPS = true;
                    mSearch.geocode(new GeoCodeOption().city(farmlandInfoUrgency.getCity())
                            .address(farmlandInfoUrgency.getCounty() + " " + farmlandInfoUrgency.getVillage()));
                }
            }
        });

        person_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(address.getText())
                        && !TextUtils.isEmpty(phone.getText()) && !TextUtils.isEmpty(deadline.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                if(s.length() > 0) {
                    farmlandInfoUrgency.setPerson(s.toString());
                }else{
                    farmlandInfoUrgency.setPerson("");
                }
            }
        });

        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(address.getText())
                        && !TextUtils.isEmpty(person_name.getText()) && !TextUtils.isEmpty(deadline.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                farmlandInfoUrgency.setPhone(s.toString());
            }
        });

        deadline.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(address.getText())
                        && !TextUtils.isEmpty(person_name.getText()) && !TextUtils.isEmpty(phone.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                if (s.toString().length() > 0) {
                    farmlandInfoUrgency.setDeadline(s.toString());
                }
            }
        });

    }

    ////////////////////////根据地址获取经纬度代码////////////////////////////
    class MyOnGetGeoCoderResultListener implements OnGetGeoCoderResultListener {
        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                //有时因为村名包含“村”字而搜索不到位置，进行二次搜索
                if(firstSearchBaiduGPS){
                    firstSearchBaiduGPS = false;
                    mSearch.geocode(new GeoCodeOption().city(farmlandInfoUrgency.getCity())
                            .address(farmlandInfoUrgency.getCounty() + " " + farmlandInfoUrgency.getVillage().substring(0, farmlandInfoUrgency.getVillage().length()-1)));
                    return;
                }
                //未找到村庄位置，显示提示信息
                new AlertDialog.Builder(FarmlandUrgencyRelease.this)
                        .setTitle("系统提示")
                        .setMessage("未能找到村庄位置，请确认输入是否正确，否则将要定位本地位置。")
                        .setIcon(R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作
                                //获取地址经纬度失败，获取本地GPS经纬度
                                Location location;
                                if(AgentApplication.permission_ACCESS_COARSE_LOCATION && AgentApplication.permission_ACCESS_COARSE_LOCATION) {
                                    location = getLocalGPS();
                                }else {
                                    error_hint("请在权限设置管理界面开放定位权限。");
                                    return;
                                }
                                if(location == null){
                                    error_hint("GPS定位信息获取异常。");
                                    return;
                                }
                                // 将GPS设备采集的原始GPS坐标转换成百度坐标
                                CoordinateConverter converter  = new CoordinateConverter();
                                converter.from(CoordinateConverter.CoordType.GPS);
                                // sourceLatLng待转换坐标
                                converter.coord(new LatLng(location.getLatitude(), location.getLongitude()));
                                LatLng point = converter.convert();
                                farmlandInfoUrgency.setLatitude(String.valueOf(point.latitude));
                                farmlandInfoUrgency.setLongitude(String.valueOf(point.longitude));
                            }
                        })
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里返回到地址录入界面
                                Intent intent = new Intent(FarmlandUrgencyRelease.this, AddressSelect.class);
                                intent.putExtra("address", address.getText().toString());
                                startActivityForResult(intent, ADDRESSEDIT);
                            }
                        }).show();
                return;
            }
            farmlandInfoUrgency.setLatitude(String.valueOf(geoCodeResult.getLocation().latitude));
            farmlandInfoUrgency.setLongitude(String.valueOf(geoCodeResult.getLocation().longitude));
            Log.i("ccccccccccc", "纬度" + String.valueOf(farmlandInfoUrgency.getLatitude() + "经度" + farmlandInfoUrgency.getLongitude()));
        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

        }
    }

    /**
     * 紧急灾情的下拉框适配器
     */
    private class UrgencySpinnerAdapter extends ArrayAdapter<UrgencyInfo> {

        public UrgencySpinnerAdapter(Context context) {
            super(context, R.layout.spinner_list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UrgencyInfo urgencyInfo = getItem(position);
            LayoutInflater inflater = getLayoutInflater();
            //View view = inflater.inflate(R.layout.spinner_list, null, true);
            View view = inflater.inflate(R.layout.spinner_list, null);
            TextView list_textView = (TextView) view.findViewById(R.id.list_textView);

            list_textView.setText(urgencyInfo.getDisaster_remark());

            return view;
        }
    }
}
