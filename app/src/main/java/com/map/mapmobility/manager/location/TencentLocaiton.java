package com.map.mapmobility.manager.location;

import android.content.Context;
import android.util.Log;

import com.map.mapmobility.manager.location.bean.MapLocation;
import com.map.mapmobility.manager.location.bean.TencentParseLocation;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

/**
 *  使用腾讯定位sdk进行定位
 *
 * @author mingjiezuo
 */
public class TencentLocaiton implements ILocation {
    public static final String LOG_TAG = "navi";

    /** 定位*/
    private TencentLocationManager mLocationManager;
    /** 定位监听*/
    private TencentLocationListener locationListener;

    /** 用户添加的定位监听*/
    private ILocationListener listener;

    /** 上下文*/
    private Context mContext;

    public TencentLocaiton(Context context) {
        mContext = context;
    }

    @Override
    public void startLocation() {
        // 初始化定位
        mLocationManager = TencentLocationManager.getInstance(mContext.getApplicationContext());
        if(mLocationManager == null){
            Log.e(LOG_TAG,"tencent location sdk mLocationManger null");
            return;
        }
        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
        // 定位监听
        locationListener = new TencentLocaiton.MyLocationListener();
        // 开始定位
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setInterval(5000);
        int error = mLocationManager.requestLocationUpdates(request, locationListener);
        Log.d(LOG_TAG, "tencent location sdk request:" + error);
    }

    @Override
    public void setLocationListener(ILocationListener listener) {
        this.listener = listener;
    }

    @Override
    public void stopLocation() {
        if(mLocationManager != null){
            mLocationManager.removeUpdates(locationListener);
        }
    }

    class MyLocationListener implements TencentLocationListener {
        @Override
        public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
            if(tencentLocation == null){
                Log.e(LOG_TAG, "tencent location sdk: location null while onLocationChanged");
                return;
            }
            if(listener != null){
                MapLocation location = new TencentParseLocation().paseLocation(tencentLocation);
                listener.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusUpdate(String s, int i, String s1) {
            if(listener != null){
                listener.onStatusUpdate(s, i, s1);
            }
        }
    }
}
