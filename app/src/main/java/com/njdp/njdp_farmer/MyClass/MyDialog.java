package com.njdp.njdp_farmer.MyClass;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.njdp.njdp_farmer.FarmerRelease;
import com.njdp.njdp_farmer.UrgencyRelease;

/**
 * Created by Administrator on 2016/6/22.
 * 自建Dialog
 */
public class MyDialog extends DialogFragment {

    public static MyDialog newInstance(String title, String type) {
        MyDialog fragment = new MyDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //String title = getArguments().getString("title");
        String type = getArguments().getString("type");
        if (type != null) {
            if(type.equals("农田发布")) {
                return ((FarmerRelease) getActivity()).selectDate();
            }
            if(type.equals("紧急灾情描述")){
                return ((UrgencyRelease) getActivity()).selectDate();
            }
        }
        return null; //Android 5.0以前的版本返回null可能会报空指针的错误
    }
}
