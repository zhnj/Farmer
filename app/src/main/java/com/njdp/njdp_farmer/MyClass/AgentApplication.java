package com.njdp.njdp_farmer.MyClass;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/26.
 * 公用数据缓存类
 */
public class AgentApplication extends Application {
    private static List<Activity> activities = new ArrayList<>();      //页面缓存
    public static ArrayList<FarmlandInfo> farmlandInfos = new ArrayList<>();   //农田数据缓存
    public static List<MachineInfo> machinesToShow = new ArrayList<>();        //需要显示的农机
    public static ArrayList<UrgencyInfo> urgencyInfos = new ArrayList<>();     //紧急灾情数据
    public static ArrayList<FarmlandInfoUrgency> farmlandInfoUrgencies = new ArrayList<>(); //紧急调配农田信息
    public static boolean permission_WRITE_EXTERNAL_STORAGE = false;    //写入扩展存储卡权限
    public static boolean permission_READ_EXTERNAL_STORAGE = false;     //读取扩展存储卡权限
    public static boolean permission_MOUNT_UNMOUNT_FILESYSTEMS = false; //SD卡读取权限
    public static boolean permission_CAMERA = false;                    //调用相机权限
    public static boolean permission_ACCESS_COARSE_LOCATION = false;    //网络定位权限
    public  static boolean permission_ACCESS_FINE_LOCATION = false;     //GPS定位权限
    public static boolean permission_READ_PHONE_STATE = false;          //读取手机当前的状态权限
    public static boolean permission_CALL_PHONE = false;                //拨打电话的权限
    private static int gcCount = 0;    //手动释放内存计数

    public static void addActivity(Activity activity) {
        if(activities.indexOf(activity) < 0)
            activities.add(activity);
    }

    public static void cleanActivity(){
        for (Activity activity : activities) {
            activity.finish();
        }
        activities.clear();
    }

    public static void removeActivity(Activity activity){
        activities.remove(activity);
        activity.finish();
        if(gcCount > 3) {
            System.gc();
            System.runFinalization();
            gcCount = 0;
        }
        gcCount++;
    }

    //退出应用，销毁加载的页面
    public static void ExitApp() {

        for (Activity activity : activities) {
            activity.finish();
        }

        System.exit(0);
    }
}
