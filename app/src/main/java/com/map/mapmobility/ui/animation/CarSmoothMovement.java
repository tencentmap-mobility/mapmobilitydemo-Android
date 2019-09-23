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
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
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
    static final int EEASE_MOVE_MSG = 1;

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
     * 动画几秒轮询一次
     */
    private static int MAX_ERASE_TIME = 1 * 1000;
    /**
     * 小车平滑的动画时长
     */
    private static int CAR_ANIMATION_DURATION = MAX_ERASE_TIME;

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

    /** 最大级别，不使用动画直接跳跃*/
    private float maxZoom = 17.0f;

    public CarSmoothMovement(SmoothMovementOption option, TencentMap map) {
        this.mOption = option;
        this.map = map;
        map.setOnCameraChangeListener(mOnCameraChangeListener);
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
        addCarMarker(latLngs[0], -1);
        // 将最后一个吸附点缓存下来
        lastLatlng = latLngs[latLngs.length-1];

        // 缩放级别正常时，使用平滑移动sdk
        if(markerAnim != null)
            markerAnim = null;
        // 两个gps动画持续时间
        int duration = mOption.getDuration();
        if(latLngs.length > 1){
            CAR_ANIMATION_DURATION = duration * (latLngs.length - 1);
        }
        // 如果缩放级别过大，直接跳到下个点，不做动画效果
        if(mOption.getZoomLevel() <= maxZoom){
            markerAnim = new MarkerTranslateAnimator(carMarker
                    , CAR_ANIMATION_DURATION
                    , latLngs
                    , true);
            // 设置开始时候的角度
            SynchroLocation location = mOption.getLocations().get(0);
            float direction = location.getRoadDirection();
            mListener.setStartDirection(360-direction);
            // 设置最终点的角度
            int size = latLngs.length;
            // 最终点的角度取路线的方向
            if(mOption.getLocations() != null && mOption.getLocations().size() > 0){
                float d = mOption.getLocations().get(mOption.getLocations().size() - 1).getRoadDirection();
                if(d == -1 && size > 1){
                    float realDirection = CarPreviewUtils
                            .getDirection(latLngs[size - 2],latLngs[size - 1]);
                    if(realDirection < 0)
                        realDirection = realDirection + 360;
                    mListener.setDirection(realDirection);
                }
                else
                    mListener.setDirection(d);
            }
            // 添加小车滑动接听
            markerAnim.addAnimatorListener(mListener);
            // 开始小车平滑
            markerAnim.startAnimation();
        }

        // 擦除已经走过的轨迹
        if(mOption.getEraseLineType() != -1) {
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
                if(mOption.getZoomLevel() <= maxZoom)
                    eraseHandler.sendEmptyMessageDelayed(ERASE_MSG, 0);
                else{
                    eraseHandler.sendEmptyMessageDelayed(EEASE_MOVE_MSG, 0);
                }
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
        if(map != null){
            mOnCameraChangeListener = null;
            map = null;
        }
        if(eraseHandler != null){
            eraseHandler = null;
            eraseThread = null;
            mOption =  null;
            markerAnim = null;
            carMarker = null;
        }
    }

    class MyAnimListener implements Animator.AnimatorListener {
        /** 结束时的校验角度*/
        private float mDirection = -1;
        /** 开始时候的校验角度*/
        private float mStartDirection = -1;

        public void setDirection(float direction) {
            mDirection = direction;
        }

        public void setStartDirection(float startDirection){
            mStartDirection = startDirection;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            // 当动画开始的时候，做次方向校验
            if(mStartDirection != -1)
                addCarMarker(null, mStartDirection);
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
                            eraseHandler.sendEmptyMessageDelayed(ERASE_MSG, CAR_ANIMATION_DURATION);
                        }else{
                            if(pointCache.size()>0){
                                SynchroLocation last = pointCache.get(pointCache.size()-1);
                                addCarMarker(null, last.getDirection() != -1
                                        ? last.getDirection() : last.getRoadDirection());
                            }

                            stopErase();
                        }
                        break;
                    case EEASE_MOVE_MSG:
                        if(pointCache.size() != 0 || pointCache.size() != 1){
                            // 擦除路线
                            LatLng latLng = converLatlng(pointCache.get(1));
                            mOption.getPolyline().eraseTo
                                    (pointCache.get(0).getAttachedIndex(), latLng);
                            addCarMarker(latLng, pointCache.get(1).getDirection() != -1
                                    ? pointCache.get(1).getDirection() : pointCache.get(1).getRoadDirection());
                            synchronized (eraseLock){
                                pointCache.remove(0);
                            }
                            eraseHandler.removeMessages(EEASE_MOVE_MSG);
                            eraseHandler.sendEmptyMessageDelayed(EEASE_MOVE_MSG, MAX_ERASE_TIME);
                        }else{
                            if(pointCache.size()>0){
                                SynchroLocation last = pointCache.get(pointCache.size()-1);
                                addCarMarker(null, last.getRoadDirection() != -1
                                        ? last.getRoadDirection() : last.getDirection());
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

    /**
     * 地图视野中心变化监听
     */
    private TencentMap.OnCameraChangeListener mOnCameraChangeListener = new TencentMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            if(mOption != null)
                mOption.setZoomLevel(cameraPosition.zoom);
        }

        @Override
        public void onCameraChangeFinished(CameraPosition cameraPosition) {

        }
    };

    private void addCarMarker(LatLng latLng, float direction) {
        // 展示小车图标
        if (carMarker == null) {
            carMarker = map.addMarker(mOption.getCarMarkerOptions().rotation(direction));
        } else {
            if(latLng != null){
                carMarker.setPosition(latLng);
            }
            carMarker.setRotation(direction);
        }
    }

    private LatLng converLatlng(SynchroLocation location) {
        LatLng latLng;
        if(location.getAttachedIndex() == -1)
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
         *  两个gps点动画持续时间 ms
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

        /**
         *  当前地图的缩放级别
         */
        float zoomLevel = 17;

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
            this.locations = SHelper.getLocationsWithOutAttachFail(locations);
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

        public float getZoomLevel() {
            return zoomLevel;
        }

        public SmoothMovementOption setZoomLevel(float zoomLevel) {
            this.zoomLevel = zoomLevel;
            return this;
        }
    }
}

