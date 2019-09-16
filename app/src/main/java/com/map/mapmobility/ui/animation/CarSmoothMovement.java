package com.map.mapmobility.ui.animation;

import android.animation.Animator;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.map.mapmobility.simultaneousdisplay.helper.SHelper;
import com.tencent.map.carpreview.utils.CarPreviewUtils;
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
    /** 每次缓存最后一个点，要和下一次的点做角度计算*/
    private LatLng lastLatlng;

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
    /** 当前小车动画的监听*/
    MyAnimListener mListener;

    /** 这是是小车平滑组件类*/
    MarkerTranslateAnimator markerAnim;

    /** 操作的地图类*/
    TencentMap map;
    /** 当前小车的图标*/
    static Marker carMarker;

    public CarSmoothMovement(SmoothMovementOption option, TencentMap map) {
        this.mOption = option;
        this.map = map;
        mListener = new MyAnimListener();

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
        // 剔除掉重复数据
        latLngs = SHelper.removeRepeat(latLngs);
        if(lastLatlng != null){
            // 如果司机静止，如果传的点一样，则过滤掉
            if(latLngs.length == 1 && lastLatlng.equals(latLngs[0]))
                return;
            latLngs = SHelper.addLalng(latLngs, lastLatlng);
        }
        // 添加小车图标
        SynchroLocation location = mOption.getLocations().get(0);
        float direction = location.getDirection() != -1
                ? location.getDirection():location.getRoadDirection();
        if(lastLatlng != null)
            direction = CarPreviewUtils.getDirection(lastLatlng,latLngs[0]);
        addCarMarker(latLngs[0], direction);
        // 将最后一个吸附点缓存下来
        lastLatlng = latLngs[latLngs.length-1];
        // 使用平滑移动sdk
        markerAnim = new MarkerTranslateAnimator(carMarker
                , mOption.getDuration()
                , latLngs
                , true);
        markerAnim.startAnimation();
        // 设置最终点的角度
        int size = latLngs.length;
        if(size > 1)
            mListener.setDirection(CarPreviewUtils
                    .getDirection(latLngs[size - 2],latLngs[size - 1]));
        else
            mListener.setDirection(-1);
        markerAnim.addAnimatorListener(mListener);
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
                    LatLng latLng = converLatlng(pointCache.get(pointCache.size()-1));
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
            carMarker = null;
        }
    }

    class MyAnimListener implements Animator.AnimatorListener {
        private float mDirection = -1;

        public void setDirection(float direction) {
            mDirection = direction;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // 当动画停止的时候，做次方向校验
            if(mDirection != -1)
                addCarMarker(null, mDirection);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

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
                            LatLng latLng = converLatlng(pointCache.get(1));
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
                                addCarMarker(null, last.getDirection() != -1
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

    private LatLng converLatlng(SynchroLocation location) {
        LatLng latLng;
        if(location.getAttachedLatitude() == 0 || location.getAttachedLongitude() == 0)
            latLng= new LatLng(location.getAltitude(), location.getLongitude());
        else
            latLng= new LatLng(location.getAttachedLatitude(), location.getAttachedLongitude());
        return latLng;
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

        public Marker getMarker() {
            if(carMarkerOptions == null)
                return null;
            return carMarker;
        }
    }
}
