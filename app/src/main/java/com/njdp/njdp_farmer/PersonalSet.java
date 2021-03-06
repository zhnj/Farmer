package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.njdp.njdp_farmer.address.AddressSelect;
import com.njdp.njdp_farmer.MyClass.Farmer;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SQLiteHandler;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.njdp.njdp_farmer.R.id.radioGroup;

public class PersonalSet extends AppCompatActivity implements View.OnClickListener{
    private static final int PHONEEDIT = 1;
    private static final int ADDRESSEDIT = 2;
    private static final int IMAGEEDIT = 3;
    private static final String TAG = PersonalSet.class.getSimpleName();
    private AwesomeValidation mValidation=new AwesomeValidation(ValidationStyle.BASIC);
    private ProgressDialog pDialog;
    Farmer farmer = new Farmer();
    EditText setPhoneNum;
    EditText setUserPic;
    EditText setAddress;
    ImageButton getback;
    Button editFinish;
    TextView tv_phone;
    TextView tv_address;
    EditText et_name;
    EditText et_QQ;
    EditText et_weixin;

    EditText et_personsfzh;
    EditText et_populationnum;
    EditText et_farmlandarea;

    private SQLiteHandler db;
    private boolean isRegister;
    private boolean uploadUserName;
    RadioButton malebutton;





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
        setContentView(R.layout.activity_personal_set);


        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        farmer = (Farmer)getIntent().getSerializableExtra("user");
        isRegister = getIntent().getBooleanExtra("register", false);
        if(farmer == null){
            error_hint("参数传输错误！");
            finish();
        }
        if(isRegister){
            uploadUserName = true;
            checkEdit(farmer);
        }
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        setPhoneNum = (EditText)super.findViewById(R.id.phonenum);
        setAddress = (EditText)super.findViewById(R.id.address);
        setUserPic = (EditText)super.findViewById(R.id.set_user_image);
        getback = (ImageButton)super.findViewById(R.id.getback);
        editFinish = (Button)super.findViewById(R.id.btn_editFinish);
        tv_phone = (TextView)super.findViewById(R.id.phonenum);
        malebutton = (RadioButton)super.findViewById(R.id.btnMan);
        RadioButton btnWoman=(RadioButton)super.findViewById(R.id.btnWoman);
        if(farmer.getSex().equals("男"))
            malebutton.setChecked(true);
        else
            btnWoman.setChecked(true);
        assert tv_phone != null;
        tv_phone.setText(farmer.getTelephone());
        tv_address = (TextView)super.findViewById(R.id.address);
        assert tv_address != null;
        tv_address.setText(farmer.getAddress());
        et_name = (EditText)super.findViewById(R.id.user_name);
        assert et_name != null;
        et_name.setText(farmer.getName());
        et_QQ = (EditText)super.findViewById(R.id.qq);
        assert et_QQ != null;
        if(farmer.getQQ().equals("未设置")){
            et_QQ.setText("");
        }else {
            et_QQ.setText(farmer.getQQ());
        }
        et_weixin = (EditText)super.findViewById(R.id.weixin);
        assert et_weixin != null;
        et_weixin.setText(farmer.getWeixin());
        et_personsfzh = (EditText)super.findViewById(R.id.personsfzh);
        assert et_personsfzh !=null;
        et_personsfzh.setText(farmer.getPersonsfzh());
        et_populationnum = (EditText)super.findViewById(R.id.populationnum);
        assert et_populationnum != null;
        et_populationnum.setText(farmer.getPopulationnum());
        et_farmlandarea = (EditText)super.findViewById(R.id.farmlandarea);
        assert et_farmlandarea !=null;
        et_farmlandarea.setText(farmer.getFarmlandarea());


        listenerEvent();

        editFinish.setEnabled(false);
        editFinish.setClickable(false);
        editTextIsChange();
    }






    //监听按钮点击事件
    private void listenerEvent()
    {
        setPhoneNum.setOnClickListener(this);
        setUserPic.setOnClickListener(this);
        setAddress.setOnClickListener(this);
        getback.setOnClickListener(this);
        editFinish.setOnClickListener(this);


    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {



            case R.id.phonenum:
                Toast toast = Toast.makeText(getApplicationContext(), "此项信息不允许修改！", Toast.LENGTH_SHORT);
                toast.show();
                //Intent intent = new Intent(this, SetPhoneNum.class);
                //intent.putExtra("token", farmer.getFm_token());
                //startActivityForResult(intent, PHONEEDIT);
                break;
            case R.id.set_user_image:
                Intent intent2 = new Intent(this, register_image.class);
                intent2.putExtra("token", farmer.getFm_token());
                intent2.putExtra("IsSetImage", true);
                intent2.putExtra("telephone", farmer.getTelephone());
                startActivityForResult(intent2, IMAGEEDIT);

                break;
            case R.id.address:
                Intent intent3 = new Intent(this, AddressSelect.class);
                intent3.putExtra("address", tv_address.getText().toString());
                startActivityForResult(intent3, ADDRESSEDIT);
                break;
            case R.id.getback:
                if(isRegister){
                    this.finish();
                    Intent intent = new Intent(PersonalSet.this, MainLink.class);
                    intent.putExtra("TOKEN", farmer.getFm_token());
                    startActivity(intent);
                    return;
                }
                finish();
                break;
            case R.id.btn_editFinish:
                mValidation.addValidation(PersonalSet.this, R.id.user_name, "^[\\u4e00-\\u9fa5]+$", R.string.err_name);
                if(mValidation.validate()){
                    if(et_QQ.getText().length() > 5 || TextUtils.isEmpty(et_QQ.getText())){
                        farmer.setName(et_name.getText().toString());
                        farmer.setQQ(et_QQ.getText().toString());
                        farmer.setWeixin(et_weixin.getText().toString());
                        if(malebutton.isChecked()==true)
                            farmer.setSex("男");
                        else
                            farmer.setSex("女");

                        farmer.setPersonsfzh(et_personsfzh.getText().toString());
                        farmer.setPopulationnum(et_populationnum.getText().toString());
                        farmer.setFarmlandarea(et_farmlandarea.getText().toString());


                        checkEdit(farmer);
                    }else{
                        error_hint("请输入正确的QQ号！");
                    }
                }
                break;

            case 0:

                break;
        }
    }

    //这是跳转到另一个布局页面返回来的操作
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){
            return;
        }
        switch (requestCode) {
            case PHONEEDIT:
                //User user = (User) data.getSerializableExtra("user");
                //Log.i("main", "注册信息是："+user);
                //Toast.makeText(this,"注册信息是："+user, 50000).show();
                farmer.setTelephone(data.getStringExtra("telephone"));
                tv_phone.setText(farmer.getTelephone());
                break;
            case ADDRESSEDIT:
                tv_address.setText(data.getStringExtra("address"));
                break;
            case IMAGEEDIT:
                setUserPic.setText("头像修改成功");
                editFinish.setClickable(true);
                editFinish.setEnabled(true);
                break;
        }
    }

    //提交修改
    public void checkEdit(final Farmer farmer) {

        String tag_string_req = "req_user_edit";

        pDialog.setMessage("正在提交 ...");
        showDialog();

        if (!NetUtil.checkNet(PersonalSet.this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {

            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_USERINFO_EDIT, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<>();
                    params.put("token", farmer.getFm_token());
                    params.put("person_name", farmer.getName());
                    params.put("person_qq", farmer.getQQ());
                    params.put("person_weixin", farmer.getWeixin());
                    params.put("person_address", farmer.getAddress());
                    params.put("person_Sex",farmer.getSex());
                    params.put("person_sfzh",farmer.getPersonsfzh());
                    params.put("population_num",farmer.getPopulationnum());
                    params.put("farmland_area",farmer.getFarmlandarea());
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
            Log.d(TAG, "EditUser Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");

                // Check for error node in json
                if (status == 0) {
                    //更新Session信息
                    SessionManager session = new SessionManager(getApplicationContext());
                    session.setUserInfo(farmer.getName(),farmer.getTelephone(),farmer.getQQ(),farmer.getWeixin(),farmer.getAddress(),farmer.getSex(),farmer.getPersonsfzh(),farmer.getPopulationnum(),farmer.getFarmlandarea());
                    // Inserting row in users table
                    db.editUser(farmer.getId(), farmer.getName(), farmer.getTelephone(), farmer.getPassword(), farmer.getImageUrl());
                    //为了保存注册时的用户名
                    if(uploadUserName){
                        uploadUserName = false;
                        return;
                    }
                    //服务器返回修改成功
                    error_hint("修改成功！");

                    //Launch main activity
                    if(isRegister){
                        Intent intent = new Intent(PersonalSet.this, MainLink.class);
                        intent.putExtra("TOKEN", farmer.getFm_token());
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(PersonalSet.this, mainpages.class);
                        intent.putExtra("user", farmer);
                        setResult(RESULT_OK, intent);
                    }
                    finish();
                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(PersonalSet.this, login.class);
                    startActivity(intent);
                    finish();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(PersonalSet.this, login.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    error_hint("其他未知错误！");
                }
            } catch (JSONException e) {
                error_hint(getResources().getString(R.string.connect_error));
                // JSON error
                e.printStackTrace();
                Log.e(TAG, "Json error：response错误！" + e.getMessage());
            }
        }
    };

    //响应服务器失败
    private Response.ErrorListener mErrorListener= new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "UpdateError: " + error.toString());
            error_hint(error.toString());
            hideDialog();
        }
    };

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //ProgressDialog显示与隐藏
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //输入是否改变，判断是否禁用按钮
    private void editTextIsChange(){
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && (!s.toString().equals(farmer.getName())
                        || !et_personsfzh.getText().toString().equals(farmer.getPersonsfzh())
                        ||!malebutton.getText().toString().equals(farmer.getSex())
                        ||!et_populationnum.getText().toString().equals(farmer.getPopulationnum())
                        ||!et_farmlandarea.getText().toString().equals(farmer.getFarmlandarea())
                        ||!et_QQ.getText().toString().equals(farmer.getQQ())
                        || !et_weixin.getText().toString().equals(farmer.getWeixin())
                        || !tv_address.getText().toString().equals(farmer.getAddress()))) {
                    editFinish.setClickable(true);
                    editFinish.setEnabled(true);
                } else {
                    editFinish.setEnabled(false);
                    editFinish.setClickable(false);
                }
            }
        });

        et_QQ.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(farmer.getQQ()) || !et_name.getText().toString().equals(farmer.getName())
                        || !et_weixin.getText().toString().equals(farmer.getWeixin()) || !tv_address.getText().toString().equals(farmer.getAddress())) {
                    editFinish.setClickable(true);
                    editFinish.setEnabled(true);
                } else {
                    editFinish.setEnabled(false);
                    editFinish.setClickable(false);
                }
            }
        });

        et_weixin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(farmer.getWeixin()) || !et_QQ.getText().toString().equals(farmer.getQQ())
                        || !et_name.getText().toString().equals(farmer.getName()) || !tv_address.getText().toString().equals(farmer.getAddress())) {
                    editFinish.setClickable(true);
                    editFinish.setEnabled(true);
                } else {
                    editFinish.setEnabled(false);
                    editFinish.setClickable(false);
                }
            }
        });

        tv_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(farmer.getAddress()) || !et_QQ.getText().toString().equals(farmer.getQQ())
                        || !et_name.getText().toString().equals(farmer.getName()) || !et_weixin.getText().toString().equals(farmer.getWeixin())) {
                    farmer.setAddress(s.toString());
                    editFinish.setClickable(true);
                    editFinish.setEnabled(true);
                } else {
                    editFinish.setEnabled(false);
                    editFinish.setClickable(false);
                }
            }
        });
        et_farmlandarea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(farmer.getFarmlandarea()) || !et_QQ.getText().toString().equals(farmer.getQQ())
                        || !et_name.getText().toString().equals(farmer.getName()) || !et_weixin.getText().toString().equals(farmer.getWeixin())) {
                    farmer.setAddress(s.toString());
                    editFinish.setClickable(true);
                    editFinish.setEnabled(true);
                } else {
                    editFinish.setEnabled(false);
                    editFinish.setClickable(false);
                }
            }
        });
    }

    /**
     * 监听返回按钮
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(isRegister){
                this.finish();
                Intent intent = new Intent(PersonalSet.this, MainLink.class);
                intent.putExtra("TOKEN", farmer.getFm_token());
                startActivity(intent);
                return true;
            }
        }

        return super.onKeyDown(keyCode,event);
    }

    //不跟随系统变化字体大小
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;
    }
}
