package com.njdp.njdp_farmer.adpter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.IControl;
import com.njdp.njdp_farmer.MainLink;
import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.PersonalSet;
import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.login;
import com.njdp.njdp_farmer.mainpages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.njdp.njdp_farmer.R.id.btn_submit;
import static com.njdp.njdp_farmer.R.id.et_pingyu;
import static com.njdp.njdp_farmer.R.id.fivestars;
import static com.njdp.njdp_farmer.R.id.et_pingyu;

/**
 * expandableListView适配器
 *
 */
public class FarmAdapter extends BaseExpandableListAdapter {
	private final String[][] cropsType = new String[][]{{"H","收割"}, {"C", "耕作"}, {"S", "播种"},
			{"WH", "小麦"}, {"CO", "玉米"}, {"RC", "水稻"}, {"GR", "谷物"}, {"OT", "其他"}, {"SS", "深松"}, {"HA", "平地"}};
	private Context context;
	private List<String> group;
	private List<List<FarmlandInfo>> child;

	private ProgressDialog pDialog;
	private static final String TAG = PersonalSet.class.getSimpleName();
	private int farmId;

	IControl control ;

	public void setControl(IControl control) {
		Log.i("cnonghu2","赋值");
		this.control = control;
	}

	public FarmAdapter(Context context, List<String> group,
					   List<List<FarmlandInfo>> child) {
		this.context = context;
		this.group = group;
		this.child = child;
	}

	@Override
	public int getGroupCount() {
		return group.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return child.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return group.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return child.get(childPosition).get(childPosition);

	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	/**
	 * 显示：group
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_farmgroup, null);
			holder = new ViewHolder();
			holder.textView = (TextView) convertView.findViewById(R.id.textView);
			holder.releaseTime = (TextView) convertView.findViewById(R.id.release_time);
			holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
			convertView.setTag(holder);
			convertView.setTag(R.id.flag, groupPosition);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String[] temp = group.get(groupPosition).split("-");
		if(temp.length != 4) //传进来的参数不正确
			return null;
		holder.textView.setText(temp[0]);
		holder.textView.setTextSize(18);
		holder.textView.setPadding(36, 10, 0, 6);

		holder.textView1.setText(temp[1] + "-" + temp[2] + "-" + temp[3]);
		holder.textView1.setPadding(36, 0, 0, 6);

		if(child.get(groupPosition).get(0).getCreatetime().indexOf(".") > 0)
			child.get(groupPosition).get(0).setCreatetime(child.get(groupPosition).get(0).getCreatetime()
					.substring(0,child.get(groupPosition).get(0).getCreatetime().indexOf(".")));
		holder.releaseTime.setText("发布时间：" + child.get(groupPosition).get(0).getCreatetime());
		holder.releaseTime.setPadding(36, 0, 0, 10);

		holder.textView.getPaint().setFakeBoldText(true); //加粗
		return convertView;
	}





	/**
	 * 显示：child
	 */
	@Override
	public View getChildView(int groupPosition, int childPosition,
							 boolean isLastChild, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_farmchild, null);
			viewHolder = new ViewHolder();
			viewHolder.cropkind = (TextView) convertView.findViewById(R.id.crop_kind);
			viewHolder.status = (TextView)convertView.findViewById(R.id.status);
			viewHolder.area = (TextView)convertView.findViewById(R.id.area);
			viewHolder.price = (TextView)convertView.findViewById(R.id.price);
			viewHolder.blocktype = (TextView)convertView.findViewById(R.id.block_type);
			viewHolder.address = (TextView)convertView.findViewById(R.id.address);
			viewHolder.starttime = (TextView)convertView.findViewById(R.id.start_time);
			viewHolder.endtime = (TextView)convertView.findViewById(R.id.end_time);
			viewHolder.remark = (TextView)convertView.findViewById(R.id.remark);
			viewHolder.pingjia=(TextView)convertView.findViewById(R.id.pingjia);
			viewHolder.fivestars=(RatingBar)convertView.findViewById(fivestars);
			viewHolder.etpingyu=(EditText) convertView.findViewById(R.id.et_pingyu);
			viewHolder.btn_submit=(Button) convertView.findViewById(R.id.btn_submit);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.cropkind.setText("作业类型：" + ConvertToCHS(child.get(groupPosition).get(childPosition).getCrops_kind()));
		viewHolder.status.setText("作业状态："+ (child.get(groupPosition).get(childPosition).getStatus().equals("0")?"未完成":"已完成"));
		viewHolder.area.setText("面积 (亩)："+child.get(groupPosition).get(childPosition).getArea());
		viewHolder.price.setText("单价 (元)："+child.get(groupPosition).get(childPosition).getUnit_price());
		viewHolder.blocktype.setText("地块类型："+child.get(groupPosition).get(childPosition).getBlock_type());
		viewHolder.address.setText(child.get(groupPosition).get(childPosition).getProvince() + child.get(groupPosition).get(childPosition).getCity() +
				child.get(groupPosition).get(childPosition).getCounty() + child.get(groupPosition).get(childPosition).getTown() +
				child.get(groupPosition).get(childPosition).getVillage());
		viewHolder.starttime.setText("开始时间："+child.get(groupPosition).get(childPosition).getStart_time_String());
		viewHolder.endtime.setText("结束时间："+child.get(groupPosition).get(childPosition).getEnd_time_String());
		viewHolder.remark.setText("补充说明："+child.get(groupPosition).get(childPosition).getRemark());


		if(!child.get(groupPosition).get(childPosition).getStartCount().equals("null"))
			viewHolder.etpingyu.setText(child.get(groupPosition).get(childPosition).getPingJia());


		if(child.get(groupPosition).get(childPosition).getStartCount()!=null && !child.get(groupPosition).get(childPosition).getStartCount().equals("null"))
			viewHolder.fivestars.setRating(Float.parseFloat(child.get(groupPosition).get(childPosition).getStartCount()));

		farmId=child.get(groupPosition).get(childPosition).getId();

		viewHolder.groupPosition=groupPosition;
		viewHolder.childPosition=childPosition;



		if(child.get(groupPosition).get(childPosition).getStatus().equals("0")){
			viewHolder.pingjia.setVisibility(View.GONE);
			viewHolder.fivestars.setVisibility(View.GONE);
			viewHolder.etpingyu.setVisibility(View.GONE);
			viewHolder.btn_submit.setVisibility(View.GONE);
		}else if(child.get(groupPosition).get(childPosition).getStatus().equals("1")){

			//未评价之前不允许提交
			viewHolder.pingjia.setVisibility(View.VISIBLE);
			viewHolder.fivestars.setVisibility(View.VISIBLE);
			viewHolder.etpingyu.setVisibility(View.VISIBLE);
			viewHolder.btn_submit.setEnabled(false);
			viewHolder.btn_submit.setClickable(false);
			//输入是否改变，判断是否禁用按钮

			viewHolder.etpingyu.addTextChangedListener( new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					if ((s.length() > 0) || (!"".equals(viewHolder.etpingyu.getText().toString()))){
						viewHolder.btn_submit.setClickable(true);
						viewHolder.btn_submit.setEnabled(true);
					} else {
						viewHolder.btn_submit.setEnabled(false);
						viewHolder.btn_submit.setClickable(false);
					}
				}
			});

		}
		viewHolder.btn_submit.setOnClickListener(new View.OnClickListener() {
			String tag_string_req = "req_user_edit";
			//当用户提交数据，将评分和评价保存集合
			// child.get(groupPosition).get(childPosition).getPingJia();
			@Override
			public void onClick(View view) {

				child.get(viewHolder.groupPosition).get(viewHolder.childPosition).setStartCount(String.valueOf(viewHolder.fivestars.getRating()));
				child.get(viewHolder.groupPosition).get(viewHolder.childPosition).setPingJia(viewHolder.etpingyu.getText().toString());

				Log.i("cnonghu",AppConfig.URL_FARMLAND_PINGJIA);
				//viewHolder.setPingyu(viewHolder.etpingyu.getText().toString());
				StringRequest strReq = new StringRequest(Request.Method.POST,
						AppConfig.URL_FARMLAND_PINGJIA, mSuccessListener, mErrorListener) {
					@Override
					protected Map<String, String> getParams() {
						// Posting parameters to url
						Map<String, String> params = new HashMap<>();
						params.put("FarmLand_id", String.valueOf(farmId));
						params.put("PingJia", viewHolder.etpingyu.getText().toString());
						params.put("StartCount", String.valueOf(viewHolder.fivestars.getRating()));
						return params;
					}
				};
				strReq.setRetryPolicy(new DefaultRetryPolicy(2000,1,1.0f)); //请求超时时间2S，重复1次
				// Adding request to request queue
				AppController.getInstance().addToRequestQueue(strReq,tag_string_req);
				view.setEnabled(false);
			}

		});
		return convertView;
	}

	private Response.Listener<String> mSuccessListener = new Response.Listener<String>() {

		@Override
		public void onResponse(String response) {
			Log.i("tagconvertstr", "[" + response + "]");
			Log.d(TAG, "EditUser Response: " + response);


			try {
				JSONObject jObj = new JSONObject(response);
				int status = jObj.getInt("status");

				// Check for error node in json
				if (status == 0) {
					//服务器返回修改成功
					//Toast toast = Toast.makeText(, "修改成功！", Toast.LENGTH_LONG);
					//toast.show();

					control.btnClick("评价成功！");



				} else if(status == 1){
					//密匙失效
					//Toast toast = Toast.makeText(null, "数据库错误！", Toast.LENGTH_LONG);
					//toast.show();
					//control.btnClick("数据库错误！");
					control.btnClick("数据库错误！");

					//SessionManager session=new SessionManager(getApplicationContext());
					//session.setLogin(false, false, "");
					//Intent intent = new Intent(.this, login.class);
					//startActivity(intent);
					//finish();
				} else{
					//Toast toast = Toast.makeText(null, "其他未知错误！", Toast.LENGTH_LONG);
					//toast.show();
					control.btnClick("其他未知错误！");
					//error_hint("其他未知错误！");
				}
			} catch (JSONException e) {
				//error_hint("连接服务器错误");
				control.btnClick("连接服务器错误！");
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
			control.btnClick(error.toString());
		}
	};


	//错误信息提示
	private void error_hint(String str) {
		Toast toast = Toast.makeText(null, str, Toast.LENGTH_LONG);
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

	class ViewHolder {
		//子节点使用
		LinearLayout layout;
		TextView cropkind, status, area, price, blocktype, address, starttime, endtime, remark,pingjia;
		RatingBar fivestars;

		//public String getPingyu() {return pingyu;}

		//public void setPingyu(String pingyu) {	this.pingyu = pingyu;	}

		private String pingyu;
		EditText etpingyu;
		Button btn_submit;
		//父节点使用
		TextView textView, textView1, releaseTime;

		//记录位置
		int groupPosition;
		int childPosition;



	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	//类型转换为中文
	private String ConvertToCHS(String s){
		String operation = "", crop = "";

		if(s.length() == 3){
			for(int i = 0; i < cropsType.length; i++){
				if(cropsType[i][0].equals(s.substring(0,1))){
					operation = cropsType[i][1];
				}
				if(cropsType[i][0].equals(s.substring(1,3))){
					crop = cropsType[i][1];
				}
			}
			if(operation.isEmpty() || crop.isEmpty()){
				operation = "植保";
				crop = "所有";
			}
		}else {
			operation = "未知";
		}
		return operation + crop;
	}

}
