package com.map.mapmobility.mapconfig;

import com.map.mapmobility.R;
import com.map.mapmobility.manager.location.ILocation;
import com.map.mapmobility.manager.location.TencentLocaiton;
import com.map.mapmobility.manager.location.bean.MapLocation;
import com.map.mapmobility.manager.sensor.ISensor;
import com.map.mapmobility.mapconfig.helper.BlueCompassHelper;
import com.tencent.map.navi.car.CarNaviView;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

public class MapTaskPresenter implements MapTaskContract.IPresenter {

    /** 移动地图监听*/
    private TencentMap.OnCameraChangeListener cameraChangeListener;

    /** 定位的管理类*/
    private ILocation locationManager;
    /** 定位监听*/
    private ILocation.ILocationListener locationListener;

    /** 陀螺仪罗盘helper*/
    private BlueCompassHelper mBlueCompassHelper;
    /** 罗盘marker*/
    private Marker compassMarker;

    /** 当前地图缩放层级*/
    private int currentZoomLevel = 17;

    private MapTaskContract.IView mView;

    public MapTaskPresenter(MapTaskContract.IView view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        // 定位的管理类
        locationManager = new TencentLocaiton(mView.getFragmentContext());
        // 注册定位监听
        locationListener = new MyLocationListener();
        locationManager.setLocationListener(locationListener);
        // 开始定位
        locationManager.startLocation();

        // 陀螺仪helper
        mBlueCompassHelper = new BlueCompassHelper
                (mView.getFragmentContext(), new MySensorListener());
        mBlueCompassHelper.startSensor();
    }

    @Override
    public void destory() {
        // 结束定位
        if(locationManager != null) {
            locationManager.stopLocation();
        }
        // 结束陀螺仪
        if(mBlueCompassHelper != null){
            mBlueCompassHelper.stopSensor();
        }
    }

    @Override
    public void initMap(CarNaviView carNaviView) {
        // 移动地图监听
        cameraChangeListener = new MyCameraChangeListener();
        carNaviView.getMap().setOnCameraChangeListener(cameraChangeListener);
    }

    /**
     *  添加marker
     * @param latLng marker所在的经纬度
     * @param markerId marker图片id
     * @param rotation marker的角度
     * @return
     */
    @Override
    public Marker addMarker(LatLng latLng, int markerId, float rotation) {
        return addMarker(latLng, markerId, rotation, 0.5f, 0.5f);
    }

    /**
     * 添加marker，可以设置锚点
     */
    @Override
    public Marker addMarker(LatLng latLng, int markerId, float rotation, float anchorX, float anchorY) {
        return mView.getTencentMap().addMarker
                (new MarkerOptions(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(markerId))
                        .rotation(rotation)
                        .anchor(anchorX, anchorY));
    }

    class MyCameraChangeListener implements TencentMap.OnCameraChangeListener {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            // 地图拖动的时候

        }

        @Override
        public void onCameraChangeFinished(CameraPosition cameraPosition) {

        }
    }

    class MyLocationListener implements ILocation.ILocationListener {
        @Override
        public void onLocationChanged(MapLocation location) {
            // 定位更新后，添加罗盘marker
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if(addCompassMaker(latLng, location.getBearing(), R.mipmap.main_ic_location)) {
                // 并调整视图位置
                moveMapToPosition(latLng, currentZoomLevel);
            }
        }

        @Override
        public void onStatusUpdate(String s, int i, String s1) {

        }
    }

    class MySensorListener implements ISensor.IDirectionListener {
        @Override
        public void onDirection(float direction) {
            // 更新罗盘marker的角度
            if(compassMarker != null){
                compassMarker.setRotation(direction);
            }
        }
    }

    /**
     * 添加罗盘marker
     *
     * @return 是否是第一次添加
     */
    private boolean addCompassMaker(LatLng latLng, float bearing, int drawId) {
        if(compassMarker == null){
            compassMarker = addMarker(latLng, drawId, bearing);
            return true;
        }else{
            compassMarker.setPosition(latLng);
            compassMarker.setRotation(bearing);
            return false;
        }
    }

    /**
     * 移动地图到指定位置
     */
    private void moveMapToPosition(LatLng latlng, int currentZoomLevel) {
        // 如果第一次定位还未结束，则不能回到初始位置
        if(latlng != null){
            CameraUpdate cameraSigma = CameraUpdateFactory.newCameraPosition
                    (new CameraPosition(latlng
                            , currentZoomLevel
                            , 0
                            , 0));
            if(mView.getTencentMap() != null){
                mView.getTencentMap().moveCamera(cameraSigma);
                mView.getTencentMap().animateCamera(cameraSigma);
            }
        }
    }

}
