package com.map.mapmobility.ui.animation;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.map.mapmobility.simultaneousdisplay.helper.SHelper;
import com.tencent.map.locussynchro.model.SynchroLocation;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.Polyline;
import com.tencent.tencentmap.mapsdk.vector.utils.animation.MarkerTranslateAnimator;

import java.util.ArrayList;

/**
 *  小车平滑移动的封装类
 *
 * @author mingjiezuo
 */
public class CarSmoothMovement {

    static final String LOG_TAG = "navi";

    private static final byte[] eraseLock = new byte[0];
    static final int ERASE_MSG = 0;

    /** 将未走过的点串信息缓存下来*/
    private ArrayList<SynchroLocation> pointCache = new ArrayList<>();

    /**
     * 开始一个线程轮询不断擦除小车运动轨迹
     */
    private HandlerThread eraseThread;
    private MyEraseHandler eraseHandler;
    /**
     * 几秒轮询一次
     */
    private int MAX_ERASE_TIME = 1 * 1000;

    /** 动画的参数配置类*/
    SmoothMovementOption mOption;

    /** 这是是小车平滑组件类*/
    MarkerTranslateAnimator markerAnim;

    /** 操作的地图类*/
    TencentMap map;
    /** 当前小车的图标*/
    Marker carMarker;

    public CarSmoothMovement(SmoothMovementOption option, TencentMap map) {
        this.mOption = option;
        this.map = map;

        if(mOption.getEraseLineType() != -1){
            // 初始化轮询线程
            eraseThread = new HandlerThread("car_erase_line");
            eraseThread.start();
            eraseHandler = new MyEraseHandler(eraseThread.getLooper());
        }
    }

    public void updatePoints(ArrayList<SynchroLocation> locations) {
        // 更新点串信息
        this.mOption.locations(locations);
    }

    /**
     *  开始动画，执行完会自动结束
     */
    public void startAnim(){
        LatLng[] latLngs = mOption.getLatLngs();
        if(latLngs == null || latLngs.length == 0){
            Log.e(LOG_TAG,"smooth movement has lat and lng null or size 0");
            return;
        }
        // 添加小车图标
        SynchroLocation location = mOption.getLocations().get(0);
        addCarMarker(latLngs[0], location.getRoadDirection() == -1
                ? location.getDirection():location.getRoadDirection());
        // 使用平滑移动sdk
        markerAnim = new MarkerTranslateAnimator(carMarker
                , mOption.getDuration()
                , latLngs
                , true);
        markerAnim.startAnimation();
        // 擦除已经走过的轨迹
        if(mOption.getEraseLineType() != -1) {
            // 动画持续时间
            int duration = mOption.getDuration();
            if(latLngs.length >1){
                MAX_ERASE_TIME = duration/(latLngs.length-1);
            }else{
                MAX_ERASE_TIME = 0;
            }
            if(pointCache.size() > 0){
                synchronized (eraseLock){
                    // 如果当前擦除还未结束，又有新串来，则直接擦除到最后
                    location = pointCache.get(pointCache.size()-1);
                    LatLng latLng = new LatLng(location.getAltitude(), location.getLongitude());
                    mOption.getPolyline().eraseTo
                            (pointCache.get(0).getAttachedIndex(), latLng);
                    pointCache.clear();
                }
            }else{
                pointCache.clear();
            }
            // 将点缓存下来
            for(SynchroLocation l : mOption.getLocations()){
                pointCache.add(l);
            }
            synchronized (eraseLock){
                eraseHandler.sendEmptyMessageDelayed(ERASE_MSG, 0);
            }
        }
    }

    /**
     *  结束擦除路线
     */
    public void stopErase() {
        if(eraseHandler != null){
            eraseHandler.removeMessages(ERASE_MSG);
        }
    }

    public void destory() {
        if(eraseHandler != null){
            eraseHandler = null;
            eraseThread = null;
            mOption =  null;
            markerAnim = null;
        }
    }

    class MyEraseHandler extends Handler {

        public MyEraseHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try{
                switch (msg.what){
                    case ERASE_MSG:
                        if(pointCache.size() != 0 || pointCache.size() != 1){
                            // 擦除路线
                            SynchroLocation location = pointCache.get(1);
                            LatLng latLng = new LatLng(location.getAltitude(), location.getLongitude());
                            mOption.getPolyline().eraseTo
                                    (pointCache.get(0).getAttachedIndex(), latLng);
                            synchronized (eraseLock){
                                pointCache.remove(0);
                            }
                            eraseHandler.removeMessages(ERASE_MSG);
                            eraseHandler.sendEmptyMessageDelayed(ERASE_MSG, MAX_ERASE_TIME);
                        }else{
                            if(pointCache.size()>0){
                                SynchroLocation last = pointCache.get(pointCache.size()-1);
                                addCarMarker(null, last.getRoadDirection() == -1
                                        ? last.getDirection() : last.getRoadDirection());
                            }
                            stopErase();
                        }
                        break;
                }
            }catch (Exception e){
                Log.e(LOG_TAG, "erase handler handle message error:"+e.getMessage());
            }
        }
    }

    private void addCarMarker(LatLng latLng, float direction) {
        // 展示小车图标
        if (carMarker == null) {
            carMarker = map.addMarker(mOption.getCarMarkerOptions());
        } else {
            if(latLng != null){
                carMarker.setPosition(latLng);
            }
            carMarker.setRotation(direction);
        }
    }

    public static class SmoothMovementOption {
        /**
         *  小车图标marker
         */
        MarkerOptions carMarkerOptions;

        /**
         *  移动的经纬度点串
         */
        LatLng[] latLngs;

        /**
         *  带吸附点和index的location
         */
        ArrayList<SynchroLocation> locations;

        /**
         *  动画持续时间 ms
         */
        int duration;

        /**
         *  当前轨迹
         */
        Polyline polyline;

        /**
         *  -1:不做处理；0：置灰；1：擦除
         */
        int eraseLineType;

        public SmoothMovementOption maker(MarkerOptions carMarkerOptions){
            this.carMarkerOptions = carMarkerOptions;
            return this;
        }

        public SmoothMovementOption latlngs(LatLng[] latLngs){
            this.latLngs = latLngs;
            return this;
        }

        public SmoothMovementOption duration(int duration){
            this.duration = duration;
            return this;
        }

        public SmoothMovementOption isEraseLine(int eraseLineType){
            this.eraseLineType = eraseLineType;
            return this;
        }

        public SmoothMovementOption polyline(Polyline polyline){
            this.polyline = polyline;
            return this;
        }

        public SmoothMovementOption locations(ArrayList<SynchroLocation> locations){
            this.locations = locations;
            this.latLngs = SHelper.getLatLngsBySynchroLocation(locations);
            return this;
        }

        public MarkerOptions getCarMarkerOptions() {
            return carMarkerOptions;
        }

        public LatLng[] getLatLngs() {
            return latLngs;
        }

        public int getDuration() {
            return duration;
        }

        public int getEraseLineType() {
            return eraseLineType;
        }

        public Polyline getPolyline() {
            return polyline;
        }

        public ArrayList<SynchroLocation> getLocations() {
            return locations;
        }
    }
}
