package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.MyClass.FarmlandInfoUrgency;
import com.njdp.njdp_farmer.MyClass.UrgencyInfo;
import com.njdp.njdp_farmer.adpter.FarmUrgencyAdapter;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FarmlandUrgencyList extends AppCompatActivity {
    private static final int FARMLANDURGENCY_ADD = 1;
    private static final int FARMLANDURGENCY_EDIT = 2;
    private final String TAG = "FarmLandUrgencyList";
    private final DateFormat yyyymmdd_DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private final String[][] cropsType = new String[][]{{"WH", "小麦"}, {"CO", "玉米"}, {"RC", "水稻"}, {"GR", "谷物"}, {"OT", "其他"}};
    private ExpandableListView listView;
    private List<String> group;
    private List<List<FarmlandInfoUrgency>> child;
    FarmUrgencyAdapter adapter;
    private ArrayList<FarmlandInfoUrgency> farmlandInfoUrgencys; //根据批次筛选后的数据
    List<UrgencyInfo> urgencyInfos = new ArrayList<>();
    private ProgressDialog pDialog;
    private String token;
    String urgency_id;
    private int isEditNow=-1;       //当前编辑信息的位置
    private double pageCount = 0;  //分页总数
    private int curPage = 0;        //当前页数
    private boolean isBottom;      //是否已滑动到底部

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
        setContentView(R.layout.activity_farmland_urgency_list);

        token = getIntent().getStringExtra("token");
        //判断参数传递是否正确
        if (token == null) {
            error_hint("参数传递错误！");
            finish();
        }

        //初始化参数及控件
        farmlandInfoUrgencys = new ArrayList<>();
        //获取扩展列表
        listView = (ExpandableListView) findViewById(R.id.expandableListView);
        listView.setOnItemLongClickListener(new OnItemLongClickListenerImpl()); // 长按事件
        //滚动监听器
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView v, int scrollState) {
            //scrollState值：0，滑动结束；1，正在滚动；2，手指做了抛的滑动动作，屏幕产生惯性滑动；
                if (isBottom) {
                    if (curPage < pageCount && pageCount > 1) {
                        curPage++;
                        getFarmlandUrgencyInfos(urgency_id, curPage);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
                isBottom = (firstVisibleItem + visibleItemCount == totalItemCount);
            }
        });
        this.registerForContextMenu(listView); // 为所有列表项注册上下文菜单

        //获取背景
        View farmlandBackground = findViewById(R.id.root_div);
        assert farmlandBackground != null;
        farmlandBackground.getBackground().setAlpha(180);

        //进度条
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //新建按钮
        Button add = (Button)findViewById(R.id.btn_add);
        assert add != null;
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmlandUrgencyList.this, FarmlandUrgencyRelease.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, FARMLANDURGENCY_ADD);
            }
        });
        //返回上一界面
        ImageButton getback = (ImageButton) super.findViewById(R.id.getback);
        assert getback != null;
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //获取紧急灾情信息
        getUrgencyInfos(0);
    }

    private void initSpinner(){
        //绑定适配器
        UrgencySpinnerAdapter adapter = new UrgencySpinnerAdapter(this);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(urgencyInfos);
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

    private void initData() {
        group = new ArrayList<>();
        child = new ArrayList<>();
        int i = 1;
        for(int j = 0; j < farmlandInfoUrgencys.size(); j++ ){
            addInfo(i+"."+farmlandInfoUrgencys.get(j).getVillage() + "-" + ConvertToCHS(farmlandInfoUrgencys.get(j).getCrops_kind())
                    + "-" + farmlandInfoUrgencys.get(j).getArea() + "亩", new FarmlandInfoUrgency[]{farmlandInfoUrgencys.get(j)});
            i++;
        }
        //刷新界面
        if(group.size() >= 0) {
            if(curPage == 1) {
                adapter = new FarmUrgencyAdapter(FarmlandUrgencyList.this, group, child);
                listView.setAdapter(adapter);
                listView.setGroupIndicator(null);  //不显示向下的箭头
            }else{
                adapter.RefreshData(group, child);
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 添加数据信息
     * @param g 标题信息
     * @param c 发布的内容
     */
    private void addInfo(String g, FarmlandInfoUrgency[] c) {
        group.add(g);
        List<FarmlandInfoUrgency> list = Arrays.asList(c);
        child.add(list);
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
            urgency_id = urgencyInfos.get(position).getId();
            getFarmlandUrgencyInfos(urgency_id, 0);
        }
        //当用户不做选择时调用的该方法
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

    private class OnItemLongClickListenerImpl implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if(view.getTag(R.id.flag)!=null){
                int groupPos = (Integer) view.getTag(R.id.flag); //参数值是在setTag时使用的对应资源id号
                Log.i("LongClickListener----", "触发长按事件，触发的是第" + groupPos + "项！");
            }else {
                return true; //返回TRUE不会弹出菜单选项
            }

            return false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, view, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info =(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView
                .getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP )
        {
            try {
                Date date = yyyymmdd_DateFormat.parse(child.get(ExpandableListView.getPackedPositionGroup(info.packedPosition)).get(0).getDeadline());
                if (date.before(new Date())) {
                    error_hint("此项信息已过期，不允许修改或删除！");
                    return;
                }
            }catch (ParseException px){
                Log.d(TAG, "DateParseException: " + child.get(ExpandableListView.getPackedPositionGroup(info.packedPosition)).get(0).getDeadline());
            }
            menu.add(0, 1, 0, "修改");
            menu.add(1, 2, 0, "删除" );
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // 得到当前被选中的item信息
        //AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
        final int groupposion = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
        isEditNow = groupposion;

        switch(item.getItemId()) {
            case 1:
                // 修改
                Log.e("------------->", "修改我的发布信息");
                Intent intent = new Intent(FarmlandUrgencyList.this, FarmlandUrgencyRelease.class);
                intent.putExtra("token", token);
                intent.putExtra("farmlandInfoUrgency", child.get(groupposion).get(0));
                startActivityForResult(intent, FARMLANDURGENCY_EDIT);
                break;
            case 2:
                // 删除
                new AlertDialog.Builder(FarmlandUrgencyList.this)
                        .setTitle("系统提示")
                        .setMessage("将要删除【" + group.get(groupposion) + "】，删除后将无法恢复，确定删除吗？")
                        .setIcon(R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作，需要配合后台返回的结果执行下面的3行代码
                                DeleteFarmlandInfos(child.get(groupposion).get(0).getId());

                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“取消”后的操作
                            }
                        }).show();
                break;

            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    //这是跳转到另一个布局页面返回来的操作
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            return;
        }
        switch (requestCode) {
            case FARMLANDURGENCY_ADD:
                int i = 0;
                FarmlandInfoUrgency f = (FarmlandInfoUrgency)data.getSerializableExtra("farmlandInfoUrgency");
                if(f != null){
                    for (UrgencyInfo u : urgencyInfos) {
                        if(u.getId().equals(f.getDisaster_id())) {
                            //获取紧急灾情选择下拉窗
                            Spinner spinner = (Spinner) findViewById(R.id.sp_urgency);
                            //为spinner添加适配器
                            assert spinner != null;
                            spinner.setSelection(i);
                            break;
                        }
                        i++;
                    }
                }
                break;
            case FARMLANDURGENCY_EDIT:
                if(isEditNow >= 0){
                    //更新原始数据
                    int index = farmlandInfoUrgencys.indexOf(child.get(isEditNow).get(0));
                    if(index != -1)
                        farmlandInfoUrgencys.set(index, (FarmlandInfoUrgency)data.getSerializableExtra("farmlandInfoUrgency"));
                    //刷新显示
                    initData();
                    isEditNow = -1;
                }
                break;
        }
    }

    //获取发布的紧急灾情信息
    public void getUrgencyInfos(final int pageNum) {

        String tag_string_req = "req_urgency_get";

        pDialog.setMessage("正在获取发布的紧急灾情数据 ...");
        showDialog();

        if (!NetUtil.checkNet(FarmlandUrgencyList.this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            StringRequest strReq;
            if(pageNum == 0) {
                curPage = 1;
                pageCount = 1;
                //服务器请求
                strReq = new StringRequest(Request.Method.POST,
                        AppConfig.URL_URGENCY_GET, mgSuccessListener, mErrorListener) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to url
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        return params;
                    }
                };
            }else {
                strReq = new StringRequest(Request.Method.POST,
                        AppConfig.URL_URGENCY_GET_BY_PAGE + pageNum, mgSuccessListener, mErrorListener){
                    @Override
                    protected Map<String, String> getParams(){
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        return params;
                    }
                };
            }

            strReq.setRetryPolicy(new DefaultRetryPolicy(2000,1,1.0f)); //请求超时时间2S，重复1次
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    //响应服务器成功
    private Response.Listener<String> mgSuccessListener = new Response.Listener<String>() {

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
                    //清空旧数据
                    if(curPage == 1)
                        AgentApplication.urgencyInfos.clear();
                    //此处引入JSON jar包
                    JSONObject result = jObj.getJSONObject("result");
                    JSONArray jObjs = result.getJSONArray("data");
                    for(int i = 0; i < jObjs.length(); i++){
                        UrgencyInfo temp = new UrgencyInfo();
                        JSONObject object = (JSONObject)jObjs.opt(i);
                        temp.setId(object.getString("disaster_id"));
                        temp.setDisaster_time(object.getString("disaster_time"));
                        temp.setDisaster_remark(object.getString("disaster_remark") + "  ");
                        AgentApplication.urgencyInfos.add(temp);
                    }
                    //判断分页的情况
                    pageCount = result.getDouble("total")/result.getDouble("per_page");
                    if(pageCount > 1){
                        if(curPage < pageCount) {
                            curPage++;
                            getUrgencyInfos(curPage);
                            return;
                        }
                    }
                    urgencyInfos = AgentApplication.urgencyInfos;
                    initSpinner();
                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmlandUrgencyList.this, login.class);
                    startActivity(intent);
                    finish();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmlandUrgencyList.this, login.class);
                    startActivity(intent);
                    finish();
                }else if(status == 32){
                    error_hint("非法访问，无此权限。");
                } else{
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

    //获取发布的紧急调配农田信息
    public void getFarmlandUrgencyInfos(final String id, final int pageNum) {

        String tag_string_req = "req_farmlandurgency_get";

        pDialog.setMessage("正在获取发布的紧急调配农田数据 ...");
        showDialog();

        if (!NetUtil.checkNet(FarmlandUrgencyList.this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            //服务器请求
            StringRequest strReq;
            if(pageNum == 0){
                curPage = 1;
                pageCount = 1;
                //服务器请求
                strReq= new StringRequest(Request.Method.POST,
                        AppConfig.URL_FARMLAND_URGENCY_GET, mfSuccessListener, mErrorListener) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to url
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        params.put("disaster_id", id);
                        return params;
                    }
                };
            }else {
                strReq= new StringRequest(Request.Method.POST,
                        AppConfig.URL_FARMLAND_URGENCY_GET_BY_PAGE + pageNum, mfSuccessListener, mErrorListener) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to url
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        params.put("disaster_id", id);
                        return params;
                    }
                };
            }

            strReq.setRetryPolicy(new DefaultRetryPolicy(2000,1,1.0f)); //请求超时时间2S，重复1次
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    //响应服务器成功
    private Response.Listener<String> mfSuccessListener = new Response.Listener<String>() {

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
                    //清空旧数据
                    if(curPage == 1)
                        AgentApplication.farmlandInfoUrgencies.clear();
                    //此处引入JSON jar包
                    JSONObject result = jObj.getJSONObject("result");
                    JSONArray jObjs = result.getJSONArray("data");

                    for(int i = 0; i < jObjs.length(); i++){
                        FarmlandInfoUrgency temp = new FarmlandInfoUrgency();
                        JSONObject object = (JSONObject)jObjs.opt(i);
                        temp.setId(object.getInt("id"));
                        temp.setDisaster_id(object.getString("disaster_id"));
                        temp.setCrops_kind(object.getString("Urge_Farmlands_crops"));
                        temp.setArea(Float.parseFloat(object.getString("Urge_Farmerland_area")));
                        temp.setProvince(object.getString("Urge_Farmlands_province"));
                        temp.setCity(object.getString("Urge_Farmlands_city"));
                        temp.setCounty(object.getString("Urge_Farmlands_county"));
                        temp.setTown(object.getString("Urge_Farmlands_town"));
                        temp.setVillage(object.getString("Urge_Farmlands_village"));
                        temp.setLongitude(object.getString("Urge_Farmlands_longitude"));
                        temp.setLatitude(object.getString("Urge_Farmlands_Latitude"));
                        temp.setPerson(object.getString("Urge_person"));
                        temp.setPhone(object.getString("Urge_phone"));
                        temp.setDeadline(object.getString("Urge_deadline"));
                        temp.setCreatetime(object.getString("created_at"));
                        temp.setUpdatetime(object.getString("updated_at"));
                        AgentApplication.farmlandInfoUrgencies.add(temp);
                    }
                    //判断分页的情况
                    pageCount = result.getDouble("total")/result.getDouble("per_page");
                    farmlandInfoUrgencys = AgentApplication.farmlandInfoUrgencies;
                    initData();
                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmlandUrgencyList.this, login.class);
                    startActivity(intent);
                    finish();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmlandUrgencyList.this, login.class);
                    startActivity(intent);
                    finish();
                }else if(status == 29){
                    error_hint("输入的信息不完整。");
                }else if(status == 32){
                    error_hint("非法访问，无此权限。");
                } else{
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

    //删除发布的农田信息
    public void DeleteFarmlandInfos(final int id) {

        String tag_string_req = "req_farmlandurgency_Delete";

        pDialog.setMessage("正在删除农田数据 ...");
        showDialog();

        if (!NetUtil.checkNet(this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            //服务器请求
            StringRequest strReq;
            //id=-1删除全部，否则删除单条记录
            if(id >= 0){
                strReq = new StringRequest(Request.Method.POST,
                        AppConfig.URL_FARMLAND_URGENCY_DEL, dSuccessListener, mErrorListener) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to url
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        params.put("urgencyFarmlandID", String.valueOf(id));
                        return params;
                    }
                };
            } else {
                strReq = new StringRequest(Request.Method.POST,
                        AppConfig.URL_FARMLAND_DEL_ALL, dSuccessListener, mErrorListener) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to url
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        return params;
                    }
                };
            }
            strReq.setRetryPolicy(new DefaultRetryPolicy(2000,1,1.0f)); //请求超时时间2S，重复1次
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    //响应服务器成功
    private Response.Listener<String> dSuccessListener = new Response.Listener<String>() {

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
                    farmlandInfoUrgencys.remove(child.get(isEditNow).get(0));
                    getFarmlandUrgencyInfos(urgency_id, 0);

                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmlandUrgencyList.this, login.class);
                    startActivity(intent);
                    finish();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmlandUrgencyList.this, login.class);
                    startActivity(intent);
                    finish();
                } else if(status == 31){
                    error_hint("农田数据已过期，不能删除！");
                }else if(status == 32){
                    error_hint("非法访问，无此权限。");
                } else{
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
            Log.e(TAG, "DeleteFarmLandInfo Error: " + error.getMessage());
            error_hint("服务器连接超时");
            hideDialog();
        }
    };

    //作物类型编码转换为中文
    private String ConvertToCHS(String s){
        String crop = "";

        if(s.length() == 2){
            for (String[] aCropsType : cropsType) {
                if (aCropsType[0].equals(s)) {
                    crop = aCropsType[1];
                    break;
                }
            }
            if(crop.isEmpty()){
                crop = "未知";
            }
        }else {
            crop = "未知";
        }
        return crop;
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        assert listView != null;
        this.unregisterForContextMenu(listView);
        View view = findViewById(R.id.root_div);
        assert view != null;
        view.setBackgroundResource(0); //释放背景图片
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
