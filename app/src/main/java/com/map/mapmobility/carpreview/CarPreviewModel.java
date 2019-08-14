package com.map.mapmobility.carpreview;

import android.content.Context;

import com.map.mapmobility.manager.location.ILocation;
import com.map.mapmobility.manager.location.TencentLocaiton;
import com.map.mapmobility.manager.location.bean.MapLocation;

import java.util.ArrayList;

/**
 *  这是周边运力模块的数据model
 *
 * @author mingjiezuo
 */
public class CarPreviewModel implements IModel{

    ArrayList<ICarPreView> views = new ArrayList<>();

    /** 定位的管理类*/
    private ILocation locationManager;
    /** 定位监听*/
    private ILocation.ILocationListener locationListener;
    /** 定位定位结果bean*/
    private MapLocation mapLocation;

    Context mContext;

    public CarPreviewModel(Context context) {
        mContext = context;
    }

    @Override
    public void register(ICarPreView view) {
        int index = views.indexOf(view);
        if(index == -1){
            views.add(view);
        }
    }

    @Override
    public void unregister() {
        views.clear();
    }

    @Override
    public void postChangeLocationEvent() {
        if(locationManager == null){
            initLocation();
        }
        startLocation();
    }

    @Override
    public void onLocationChangeEvent() {
        for(ICarPreView carPreView: views){
            carPreView.onLocationChange(mapLocation);
        }
    }

    class MyLocationListener implements ILocation.ILocationListener {
        @Override
        public void onLocationChanged(MapLocation location) {
            // 定位更新后，通知view
            mapLocation = location;
            onLocationChangeEvent();
            // 不需要实时定位
            stopLocation();
        }

        @Override
        public void onStatusUpdate(String s, int i, String s1) {

        }
    }

    private void initLocation() {
        // 定位的管理类
        locationManager = new TencentLocaiton(mContext.getApplicationContext());
        // 注册定位监听
        locationListener = new MyLocationListener();
        locationManager.setLocationListener(locationListener);
    }

    private void startLocation() {
        // 开始定位
        locationManager.startLocation();
    }

    private void stopLocation() {
        // 结束定位
        locationManager.stopLocation();
        locationManager = null;
    }
}
