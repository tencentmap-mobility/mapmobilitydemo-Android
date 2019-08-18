package com.map.mapmobility.mapconfig;

import android.location.Location;

import com.map.mapmobility.R;
import com.map.mapmobility.manager.location.ILocation;
import com.map.mapmobility.manager.location.TencentLocaiton;
import com.map.mapmobility.manager.location.bean.MapLocation;
import com.map.mapmobility.manager.sensor.ISensor;
import com.map.mapmobility.mapconfig.helper.BlueCompassHelper;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;

public class MapTaskPresenter implements MapTaskContract.IPresenter {
    static final String LOG_TAG = "navi";

    /** 定位的管理类*/
    private ILocation locationManager;
    /** 定位监听*/
    private ILocation.ILocationListener locationListener;

    /** location Source*/
    private LocationSource.OnLocationChangedListener ls;
    /** 当前位置*/
    private Location lo;

    /** 陀螺仪罗盘helper*/
    private BlueCompassHelper mBlueCompassHelper;

    /** 罗盘marker*/
    private Marker compassMarker;
    /** 是否展示旋转罗盘*/
    private boolean enableShowCompass = false;
    /** 当前手机的角度*/
    private float mDirection = 0;
    /** 当前地图缩放层级*/
    private int currentZoomLevel = 16;

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
        if(locationManager != null)
            locationManager.stopLocation();
        // 结束陀螺仪
        if(mBlueCompassHelper != null)
            mBlueCompassHelper.stopSensor();
    }

    /**
     *  是否显示罗盘marker
     * @param isEnableCompass
     */
    @Override
    public void enableCompass(boolean isEnableCompass) {
        enableShowCompass = isEnableCompass;
    }

    /**
     *  是否展示location Source 效果
     * @param map
     * @param isEnableLocationSorce
     */
    @Override
    public void enableAddMarkerWithLS(TencentMap map, boolean isEnableLocationSorce) {
        if(isEnableLocationSorce)
            showMarkerByLocationSource(map);
        else
            removeMarkerByLocationSource(map);
    }

    private void showMarkerByLocationSource(TencentMap map) {
        // location source init
        map.setLocationSource(new MyLocationSource());
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        // add style
        MyLocationStyle myLocationStyle = new MyLocationStyle()
                . anchor(0.5f, 0.5f)
                . icon(BitmapDescriptorFactory.fromResource(R.mipmap.main_ic_location))
                . myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        map.setMyLocationStyle(myLocationStyle);
    }

    private void removeMarkerByLocationSource(TencentMap map) {
        if(map.isMyLocationEnabled()){
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    protected Marker addMarker(LatLng latLng, int markerId, float rotation) {
        return addMarker(latLng, markerId, rotation, 0.5f, 0.5f);
    }

    protected Marker addMarker(LatLng latLng, int markerId, float rotation, float anchorX, float anchorY) {
        return mView.getTencentMap().addMarker
                (new MarkerOptions(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(markerId))
                        .rotation(rotation)
                        .anchor(anchorX, anchorY));
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

    class MyLocationListener implements ILocation.ILocationListener {
        @Override
        public void onLocationChanged(MapLocation location) {
            // 定位更新后，添加罗盘marker
            if(enableShowCompass){
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                // 并调整视图位置
                if(addCompassMaker(latLng, location.getBearing(), R.mipmap.main_ic_location))
                    moveMapToPosition(latLng, currentZoomLevel);
            }else{
                // 移除罗盘marker
                if(compassMarker != null)
                    compassMarker.remove();
            }
            // location source
            if(ls != null){
                lo = new Location(location.getProvider());
                lo.setLatitude(location.getLatitude());
                lo.setLongitude(location.getLongitude());
                lo.setBearing(mDirection);
                ls.onLocationChanged(lo);
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
            mDirection = direction;
            if(enableShowCompass && compassMarker != null){
                compassMarker.setRotation(direction);
            }
            // 更新location source
            if(ls != null && lo != null){
                lo.setLatitude(lo.getLatitude());
                lo.setLongitude(lo.getLongitude());
                lo.setBearing(mDirection);
                ls.onLocationChanged(lo);
            }
        }
    }

    class MyLocationSource implements LocationSource {
        @Override
        public void activate(OnLocationChangedListener onlocationchangedlistener) {
            ls = onlocationchangedlistener;
        }

        @Override
        public void deactivate() {
            ls = null;
            lo = null;
        }
    }

}
