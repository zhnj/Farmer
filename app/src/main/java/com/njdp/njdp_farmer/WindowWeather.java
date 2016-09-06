package com.njdp.njdp_farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.njdp.njdp_farmer.adpter.MAdapterForMListView;
import com.njdp.njdp_farmer.bean.Data;
import com.njdp.njdp_farmer.util.HttpGetData;
import com.njdp.njdp_farmer.util.GetDataListener;
import com.njdp.njdp_farmer.util.AirQualityShowView;
import com.njdp.njdp_farmer.util.ChooseImageWeather;
import com.njdp.njdp_farmer.util.JsonToBean;
import com.njdp.njdp_farmer.util.SaveDataToPhone;
import com.njdp.njdp_farmer.viewpage.MListViewForScrollView;

//import com.example.poptest.adapter.MAdapterForLifeListView;

/**
 * Created by Administrator on 2016/7/7.
 * 天气弹窗
 */
public class WindowWeather extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,AppBarLayout.OnOffsetChangedListener{
    private String city;
    private String country;
    private JsonToBean jsonToBean;
    private HttpGetData httpGetData;
    private String jsonStr;
    private SaveDataToPhone saveDataToPhone;
    private SwipeRefreshLayout mRefreshLayout;
    private Data mdata;
    private CoordinatorLayout rootView;
    private NestedScrollView mScrollView;
    //futureItem的listview
    private MListViewForScrollView mlistView;
    //life Listview
    //private MListViewForScrollView mLifeListView;
    //relatimeWeather的View
    private RelativeLayout root;
    private TextView tv;
    private ImageView img;
    private TextView tvCityname;
    private TextView tvdict;
    private TextView tvspeed;
    private TextView tvHumidity;
    private TextView tvWeather;
    private boolean isrefresh=false;
    private boolean isUseCity=false;

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
        setContentView(R.layout.activity_weather);
        //设置查询天气的城市
        city = getIntent().getStringExtra("city");
        country = getIntent().getStringExtra("country");
        if(city == null || country == null){
            Toast.makeText(getApplicationContext(), R.string.parameterErr, Toast.LENGTH_SHORT).show();
            finish();
        }
        //TextView textView = (TextView)findViewById(R.id.register_title);
        //assert textView != null;
        //textView.setText(city + country + " 天气预报");
        //加载界面布局
        initView();

        jsonToBean=new JsonToBean(this);
        saveDataToPhone=new SaveDataToPhone(this);

        httpGetData=HttpGetData.getInstance();
        httpGetData.setOngetDataLintener(new GetDataListener() {
            @Override
            public void success() {
                jsonStr = httpGetData.getJsonStr();
                successLink();
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void failed() {
                Snackbar.make(rootView, "请求失败", Snackbar.LENGTH_SHORT).show();
                mRefreshLayout.setRefreshing(false);
            }
        });
        //检查地址正确性，并查询天气
        if(country.equals("")) {
            country=GetCityName(this); //传递过来的city数据为空，从上次记录中取数
        }
        if(country.equals("")){
            httpGetData.setCityName("北京");
        }else{
            if(country.contains("县")&&country.length()>2){
                country = country.replace("县", "");
            }
            httpGetData.setCityName(country);
        }

        Update();
    }

    //更新数据
    public void Update(){
        //需要更新先展示sd卡中的数据然后进行更新
        jsonStr=saveDataToPhone.getJsonStr();
        if(jsonStr!=null&&jsonStr.length()>100){
            //解析对象
            mdata=jsonToBean.toBean(jsonStr);
            showView();
        }
        getDataFromHttp();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        city=intent.getStringExtra("city");
        country=intent.getStringExtra("country");
        httpGetData.setCityName(city);
        SaveCityName(city,WindowWeather.this);
        onRefresh();
        //getDataFromHttp();
    }

    private void initView() {
        //RelatimeWeather的view
        tv= (TextView) findViewById(R.id.tv);
        img= (ImageView) findViewById(R.id.img);
        root = (RelativeLayout) findViewById(R.id.root);
        tvCityname= (TextView) findViewById(R.id.tv_cityname);
        tvdict= (TextView) findViewById(R.id.tv_dirct);
        tvHumidity= (TextView) findViewById(R.id.tv_humidity);
        tvspeed= (TextView) findViewById(R.id.tv_speed);
        tvWeather= (TextView) findViewById(R.id.tv_weather);
        mScrollView= (NestedScrollView) findViewById(R.id.nestedscrollView);
        rootView= (CoordinatorLayout) findViewById(R.id.root_view);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        assert mRefreshLayout != null;
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_dark);
        //futureitem的listview
        mlistView = (MListViewForScrollView) findViewById(R.id.future_listview);
        //life ListView
        //mLifeListView= (MListViewForScrollView) findViewById(R.id.life_listview);
        ImageButton getback=(ImageButton) super.findViewById(R.id.getback);
        //返回上一界面
        assert getback != null;
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //隐藏root
        root.setVisibility(View.INVISIBLE);
    }

    //展示数据
    private void showView() {
        MAdapterForMListView mAdapter = new MAdapterForMListView(mdata.getWeather(), WindowWeather.this);
        //MAdapterForLifeListView mLifeAdapter = new MAdapterForLifeListView(mdata.getLife().getInfo(), com.njdp.njdp_farmer.WindowWeather.this);
        //利用Util为air的布局设置数据
        new AirQualityShowView(WindowWeather.this,mdata.getPm25());

        tv.setText(mdata.getRealtime().getWeather().getTemperature());
        img.setImageResource(ChooseImageWeather.getImageWeather(mdata.getRealtime().getWeather().getImg()));
        tvCityname.setText(mdata.getRealtime().getCity_name());
        tvdict.setText(mdata.getRealtime().getWind().getDirect());
        tvspeed.setText(mdata.getRealtime().getWind().getPower());
        tvHumidity.setText(mdata.getRealtime().getWeather().getHumidity());
        tvWeather.setText(mdata.getRealtime().getWeather().getInfo());
        //future Adapter
        mlistView.setAdapter(mAdapter);
        //取消listview的焦点
        mlistView.setFocusable(false);
        //life Adapter
        //mLifeListView.setAdapter(mLifeAdapter);
        //取消listview的焦点
        //mLifeListView.setFocusable(false);

        root.setVisibility(View.VISIBLE);
        //滑动到scorllview的顶部
        mScrollView.smoothScrollTo(0, 0);
        if(isrefresh){
            Snackbar.make(rootView, "天气数据更新成功", Snackbar.LENGTH_SHORT).show();
            isrefresh=false;
        }
    }


    //联网获取数据
    private void getDataFromHttp(){
        try {
            httpGetData.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //联网请求成功调用
    private void successLink() {
        Log.d("tag", jsonStr);
        if(jsonStr.length()<100){
            if(isUseCity){
                Snackbar.make(rootView, "暂时不支持该城市或地区。", Snackbar.LENGTH_SHORT).show();
            }else {
                Snackbar.make(rootView, "暂不支持该城市或地区，使用上级城市进行查询。", Snackbar.LENGTH_SHORT).show();
                isUseCity = true;
                if (city.equals("")) {
                    httpGetData.setCityName("北京");
                } else {
                    httpGetData.setCityName(city);
                }
                getDataFromHttp();
            }
            return;
        }
        saveDataToPhone.saveJsonStr(jsonStr);

        if(jsonStr!=null&&!jsonStr.equals("")){
            //解析并保存对象
            mdata=jsonToBean.toBean(jsonStr);
            showView();
        }
    }

    //下拉刷新时调用
    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        isrefresh=true;
        getDataFromHttp();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            mRefreshLayout.setEnabled(true);
        } else {
            mRefreshLayout.setEnabled(false);
        }

    }

    /**
     * 储存cityname
     * @param cityName 城市名
     * @param context 上下文
     */
    public void SaveCityName(String cityName,Context context){
        SharedPreferences sp =context.getSharedPreferences("CITY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("CityName", cityName);
        editor.apply();
    }

    /**
     * 返回cityname用于下次进入直接从内存中得到cityname,如果为空则进行定位
     * @param context 上下文
     * @return 城市名
     */
    public String GetCityName(Context context){
        SharedPreferences sp =context.getSharedPreferences("CITY", Context.MODE_PRIVATE);
        city= sp.getString("CityName", "");

        //如果cityname==null,调用GPS定位功能进行定位

        return city;
    }

}
