package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.MyClass.UrgencyInfo;
import com.njdp.njdp_farmer.adpter.UrgencyAdapter;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrgencyList extends AppCompatActivity {
    private final int URGENCY_ADD = 1;
    private final int URGENCY_EDIT = 2;
    private final String TAG = "FarmLandList";
    private ExpandableListView listView;
    private ArrayList<UrgencyInfo> urgencyInfoList;
    private List<String> group;
    private List<List<UrgencyInfo>> child;
    private ProgressDialog pDialog;
    private String token;
    private boolean isFirst = true;
    private int isEditPosion=-1;
    private double pageCount;
    private int curPage;

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
        setContentView(R.layout.activity_urgency_list);

        //初始化参数及控件
        urgencyInfoList = new ArrayList<>();

        token = getIntent().getStringExtra("token");
        //判断参数传递是否正确
        if (token == null) {
            error_hint("参数传递错误！");
            finish();
        }

        //获取扩展列表
        listView = (ExpandableListView) findViewById(R.id.expandableListView);
        listView.setOnItemLongClickListener(new OnItemLongClickListenerImpl()); // 长按事件
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true; //false展开，true不展开
            }
        });
        this.registerForContextMenu(listView); // 为所有列表项注册上下文菜单
        //获取背景
        View farmlandlist = findViewById(R.id.root_div);
        assert farmlandlist != null;
        farmlandlist.getBackground().setAlpha(180);

        //进度条
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //新建按钮
        Button add = (Button)findViewById(R.id.btn_add);
        assert add != null;
        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UrgencyList.this, UrgencyRelease.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, URGENCY_ADD);
                isEditPosion = -1;  //新建信息，编辑状态置为-1
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

        //获取紧急灾情数据
        getUrgencyInfos(0);
    }

    //填充数据
    private void initData() {
        group = new ArrayList<>();
        child = new ArrayList<>();
        int i = 1;
        for(int j = 0; j < urgencyInfoList.size(); j++ ){
            addInfo(i+"."+urgencyInfoList.get(j).getDisaster_time(), new UrgencyInfo[]{urgencyInfoList.get(j)});
            i++;
        }
        //刷新界面
        if(group.size() >= 0) {
            UrgencyAdapter adapter = new UrgencyAdapter(UrgencyList.this, group, child);
            listView.setAdapter(adapter);
            listView.setGroupIndicator(null);  //不显示向下的箭头
        }
    }

    /**
     * 添加数据信息
     * @param g 标题信息
     * @param c 发布的内容
     */
    private void addInfo(String g, UrgencyInfo[] c) {
        group.add(g);
        List<UrgencyInfo> list = Arrays.asList(c);
        child.add(list);
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

    //长按弹出菜单选项
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, view, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info =(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView
                .getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP )
        {
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
        isEditPosion = groupposion;

        switch(item.getItemId()) {
            case 1:
                // 修改
                Log.e("------------->", "修改紧急灾情信息");
                Intent intent = new Intent(UrgencyList.this, UrgencyRelease.class);
                intent.putExtra("token", token);
                intent.putExtra("urgencyInfo", child.get(groupposion).get(0));
                startActivityForResult(intent, URGENCY_EDIT);
                break;
            case 2:
                // 删除
                new AlertDialog.Builder(UrgencyList.this)
                        .setTitle("系统提示")
                        .setMessage("将要删除【" + group.get(groupposion) + "】，删除后将无法恢复，确定删除吗？")
                        .setIcon(R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作，需要配合后台返回的结果执行下面的3行代码
                                DeleteUrgencyInfos(child.get(groupposion).get(0).getId());

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
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            return;
        }
        switch (requestCode) {
            case URGENCY_ADD:
            case URGENCY_EDIT:
                if(isEditPosion >= 0){
                    //更新原始数据
                    int index = urgencyInfoList.indexOf(child.get(isEditPosion).get(0));
                    if(index != -1)
                        urgencyInfoList.set(index, (UrgencyInfo)data.getSerializableExtra("urgencyInfo"));
                    //刷新显示
                    initData();
                    isEditPosion = -1;
                }else {
                    getUrgencyInfos(0);
                }

                break;
        }
    }

    /**
     * 获取发布的紧急灾情信息
     * @param pageNum 页数
     */
    public void getUrgencyInfos(final int pageNum) {

        String tag_string_req = "req_urgency_get";

        if(isFirst) {
            pDialog.setMessage("正在获取发布的紧急灾情数据 ...");
            showDialog();
            isFirst = false;
        }

        if (!NetUtil.checkNet(UrgencyList.this)) {
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
                        temp.setDisaster_remark(object.getString("disaster_remark"));
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
                    urgencyInfoList = AgentApplication.urgencyInfos;
                    if(urgencyInfoList == null || urgencyInfoList.size()==0)
                    {
                        error_hint("没有发布信息！");
                    }else {
                        initData();
                    }
                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(UrgencyList.this, login.class);
                    startActivity(intent);
                    finish();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(UrgencyList.this, login.class);
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

    //删除发布的农田信息
    public void DeleteUrgencyInfos(final String id) {

        String tag_string_req = "req_urgency_Delete";

        pDialog.setMessage("正在更新紧急灾情数据 ...");
        showDialog();

        if (!NetUtil.checkNet(this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            //服务器请求
            StringRequest strReq;
            //删除单条记录
            if(id.length() > 0){
                strReq = new StringRequest(Request.Method.POST,
                        AppConfig.URL_URGENCY_DEL, mSuccessListener, mErrorListener) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to url
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        params.put("disaster_id", id);
                        return params;
                    }
                };
                strReq.setRetryPolicy(new DefaultRetryPolicy(2000,1,1.0f)); //请求超时时间2S，重复1次
                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
            }
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
                    //此处引入JSON jar包
                    //String result = jObj.getString("result");
                    if(isEditPosion == -1){ //全部删除
                        urgencyInfoList.clear();
                    }else {
                        urgencyInfoList.remove(child.get(isEditPosion).get(0));
                    }
                    error_hint("删除成功！");
                    initData();

                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(UrgencyList.this, login.class);
                    startActivity(intent);
                    finish();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(UrgencyList.this, login.class);
                    startActivity(intent);
                    finish();
                } else if(status == 30){
                    error_hint("该灾情存在关联农田，不可被删除！");
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
}
