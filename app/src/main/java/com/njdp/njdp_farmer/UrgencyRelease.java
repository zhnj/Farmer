package com.njdp.njdp_farmer;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.MyClass.MyDialog;
import com.njdp.njdp_farmer.MyClass.UrgencyInfo;
import com.njdp.njdp_farmer.conent_frament.FarmlandManager;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UrgencyRelease extends AppCompatActivity {
    private final String TAG = "UrgencyRelease";
    private boolean isEdit = false;
    private String token;
    private UrgencyInfo urgencyInfo;
    private EditText time, remark;
    private Button releaseEditFinish;
    private ImageButton getback=null;
    private ProgressDialog pDialog;

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
        setContentView(R.layout.activity_urgency_release);

        urgencyInfo = (UrgencyInfo)getIntent().getSerializableExtra("urgencyInfo");
        if (urgencyInfo == null) {
            urgencyInfo = new UrgencyInfo();
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
    }

    private void initView() {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        time = (EditText) this.findViewById(R.id.disaster_time);
        remark = (EditText) this.findViewById(R.id.disaster_remark);
        releaseEditFinish = (Button) this.findViewById(R.id.btn_editFinish);
        if (remark != null) {
            remark.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        getback=(ImageButton) this.findViewById(R.id.getback);
        TextView top_title = (TextView) this.findViewById(R.id.tv_top_title);

        //如果是编辑的话，初始化数据
        if(isEdit){
            assert top_title != null;
            top_title.setText("修改紧急灾情信息");
            time.setText(urgencyInfo.getDisaster_time());
            time.setClickable(false);
            remark.setText(urgencyInfo.getDisaster_remark());
            releaseEditFinish.setText("确认修改");
        }

        initOnClick();
    }

    private void initOnClick() {
        if(!isEdit) {
            time.setOnClickListener(handler);
        }
        getback.setOnClickListener(handler);
        releaseEditFinish.setOnClickListener(handler);
        releaseEditFinish.setEnabled(false);
        releaseEditFinish.setClickable(false);
        editTextIsNull();
    }

    View.OnClickListener handler = new View.OnClickListener()
    {
        public void onClick (View v) {
            //1.得到InputMethodManager对象
            //InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            //2.调用toggleSoftInput方法，实现切换显示软键盘的功能。
            //imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            switch (v.getId()) {
                // TODO: 根据点击进行不同的处理
                case R.id.btn_editFinish:
                    Log.e("------------->", "点击发布紧急灾情描述");
                    checkRelease();
                    break;
                case R.id.disaster_time:
                    // 选择发生日期
                    MyDialog dialogFragment = MyDialog.newInstance(
                            "灾情发生日期", "紧急灾情描述");
                    dialogFragment.show(getFragmentManager(), "选择日期");
                    break;
                case R.id.getback:
                    finish();
            }
        }
    };

    //紧急灾情描述
    public void checkRelease() {

        String tag_string_req = "req_urgency_release";

        pDialog.setMessage("正在发布 ...");
        showDialog();
        Log.i("GGGG", urgencyInfo.getDisaster_time() + ": " + urgencyInfo.getDisaster_remark());
        if (!NetUtil.checkNet(this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            String ReqUrl; //需要连接的URL
            if(isEdit){
                ReqUrl = AppConfig.URL_URGENCY_EDIT;
            }else {
                ReqUrl = AppConfig.URL_URGENCY_RELEASE;
            }
            urgencyInfo.setDisaster_time(time.getText().toString());
            urgencyInfo.setDisaster_remark(remark.getText().toString());
            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    ReqUrl, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<>();
                    params.put("token", token);
                    if(isEdit){
                        params.put("disaster_id", String.valueOf(urgencyInfo.getId()));
                    }else {
                        params.put("disaster_time", urgencyInfo.getDisaster_time());
                    }
                    params.put("disaster_remark", urgencyInfo.getDisaster_remark());
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
                        error_hint("紧急灾情信息修改成功！");
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
                    Intent intent = new Intent(UrgencyRelease.this, login.class);
                    startActivity(intent);
                    setContentNUll();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(UrgencyRelease.this, login.class);
                    startActivity(intent);
                    setContentNUll();
                }else if(status == 29){
                    error_hint("信息录入不完整！");
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

    //清空发布界面的录入信息
    private void setContentNUll() {
        //返回结果
        Intent intent = new Intent(UrgencyRelease.this, FarmlandManager.class);
        if(isEdit){
            intent = new Intent(UrgencyRelease.this, FarmerLandList.class);
            intent.putExtra("urgencyInfo", urgencyInfo);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    public DatePickerDialog selectDate() {
        //---选择日期---
        Calendar c = Calendar.getInstance();
        if (null == time) {
            time = (EditText) findViewById(R.id.disaster_time);
        }
        assert time != null;
        if(time.getText().toString().length() == 10){
            String[] temp = time.getText().toString().split("-");
            c.set(Integer.parseInt(temp[0]), Integer.parseInt(temp[1])-1, Integer.parseInt(temp[2]));
        }
        return new DatePickerDialog(this, mDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
    }

    private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            String yyyy = String.valueOf(year);
            String mm;
            String dd;

            mm = String.valueOf(monthOfYear + 1);
            if (mm.length() < 2)
                mm = "0" + mm;

            dd = String.valueOf(dayOfMonth);
            if (dd.length() < 2)
                dd = "0" + dd;

            if (null == time) {
                time = (EditText) findViewById(R.id.disaster_time);
            }
            if (null != time){
                time.setText(yyyy + "-" + mm + "-" + dd);
            }
        }
    };

    //监控信息是否有变化
    private void editTextIsNull(){
        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    releaseEditFinish.setEnabled(true);
                    releaseEditFinish.setClickable(true);
                }else {
                    releaseEditFinish.setClickable(false);
                    releaseEditFinish.setEnabled(false);
                }
            }
        });

        remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(isEdit){
                    if(s.toString().equals(urgencyInfo.getDisaster_remark())){
                        releaseEditFinish.setEnabled(false);
                        releaseEditFinish.setClickable(false);
                    }else {
                        releaseEditFinish.setClickable(true);
                        releaseEditFinish.setEnabled(true);
                    }
                }
            }
        });
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
