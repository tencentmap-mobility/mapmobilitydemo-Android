package com.map.mapmobility.passenger;

import android.util.Log;

import com.map.mapmobility.R;
import com.map.mapmobility.heaper.Heaper;
import com.map.mapmobility.utils.CommentUtil;
import com.map.mapmobility.utils.ToastUtils;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.locussynchro.TencentLocusSynchro;
import com.tencent.map.locussynchro.model.Order;
import com.tencent.map.locussynchro.model.PassengerSynchroOptions;
import com.tencent.map.locussynchro.model.RouteUploadError;
import com.tencent.map.locussynchro.model.SyncData;
import com.tencent.map.locussynchro.model.SynchroLocation;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.Polyline;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;
import com.tencent.tencentmap.mapsdk.vector.utils.animation.MarkerTranslateAnimator;

import java.util.ArrayList;

public class PassengerTaskPresenter implements PassengerTaskContract.IPresenter {
    private static final String LOG_TAG = "navi";

    private PassengerTaskContract.IView mView;
    /** 定位*/
    private TencentLocationManager mLocationManager;
    /** 定位监听*/
    private TencentLocationListener locationListener;
    /** 移动地图监听*/
    private TencentMap.OnCameraChangeListener cameraChangeListener;
    /** 司乘同显*/
    private TencentLocusSynchro tencentLocusSynchro;
    /** 小车平滑移动*/
    private MarkerTranslateAnimator mTranslateAnimator;

    /** 是否进行路线规划。规划了路线后则不能拖动地图选择上车点*/
    private boolean isHasRoute = false;
    /** 当前坐标*/
    private LatLng currentPosition;
    /** 当前缩放比例*/
    private int currentZoomLevel = 17;
    /** 当前位置marker*/
    private Marker currentLocationMarker;
    /** 司乘同显时，司机小车的marker*/
    private Marker driverMarker;
    /** 当前展示的路线*/
    private Polyline polyline;

    /** 订单id*/
    private String orderId = "xc_10001";
    /** 乘客id*/
    private String passengerId = "OU_xc_10001_1";
    /**
     * 订单状态
     * STATUS_NO_ORDER 订单状态-无订单
     * STATUS_ORDER_SENT 订单状态-已派单
     * STATUS_CHARGING_STARTED 订单状态-开始计费
     */
    private int orderStatus = Order.STATUS_CHARGING_STARTED;
    /** 最新的路线id*/
    private String lastRouteId;
    /** 是否进行司乘同步*/
    private boolean syncEnable = false;

    public PassengerTaskPresenter(PassengerTaskContract.IView view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        ToastUtils.INSTANCE().init(mView.getFragmentContext());
    }

    @Override
    public void destory() {
        stopLocation();
        stopPassengerDriverSynchro();
        mLocationManager = null;
        locationListener = null;
        ToastUtils.INSTANCE().destory();
    }

    /**
     *  添加地图移动监听
     */
    @Override
    public void initMap(TencentMap map) {
        // 移动地图监听
        cameraChangeListener = new MyCameraChangeListener();
        map.setOnCameraChangeListener(cameraChangeListener);
    }

    /**
     *  开始定位
     */
    @Override
    public void startLocation() {
        // 初始化定位
        mLocationManager = TencentLocationManager.getInstance(mView.getFragmentContext());
        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
        // 定位监听
        locationListener = new MyLocationListener();
        // 开始定位
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setInterval(5000);
        switch (mLocationManager.requestLocationUpdates(request, locationListener)){
            case 0:
                Log.d(LOG_TAG, "request location success");
                break;
        }
    }

    /**
     * 结束定位
     */
    @Override
    public void stopLocation() {
        if(mLocationManager != null){
            mLocationManager.removeUpdates(locationListener);
        }
    }

    /**
     * 开始司乘同显
     */
    @Override
    public void startPassengerDriverSynchro() {
        // 司乘的乘客端初始化
        PassengerSynchroOptions passengerOptions = new PassengerSynchroOptions();
        passengerOptions.setAccountId(passengerId);
        tencentLocusSynchro = new TencentLocusSynchro
                (mView.getFragmentContext(), passengerOptions);
        // 开启司乘
        try{
            tencentLocusSynchro.start(new MyPDSynListener());
        }catch (Exception e){
            Log.e(LOG_TAG, "passenger driver synchro fail while starting! message:"+e.getMessage());
        }
    }

    /**
     * 是否进行信息同步
     * @param enable
     */
    @Override
    public void isSyncEnable(boolean enable) {
        if(tencentLocusSynchro != null){
            tencentLocusSynchro.setSyncEnabled(enable);
            this.syncEnable = enable;
        }
    }

    /**
     * 停止司乘同显
     */
    @Override
    public void stopPassengerDriverSynchro() {
        if(tencentLocusSynchro != null) {
            tencentLocusSynchro.stop();
        }
    }

    /**
     * 添加marker
     * @param latLng 经纬度
     * @param markerId id
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

    /**
     * 调整视图中心点
     */
    @Override
    public void toOriginLocation(LatLng latlng, int currentZoomLevel) {
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

    /**
     * 视图中心为当前位置
     */
    @Override
    public void toOriginCurrentLocation() {
        toOriginLocation(currentPosition, currentZoomLevel);
    }

    /**
     * 添加所在位置marker
     */
    private void addCurrentLocationMarker(TencentLocation tencentLocation) {
        if(currentLocationMarker == null){
            currentLocationMarker = addMarker
                    (new LatLng(tencentLocation.getLatitude(), tencentLocation.getLongitude())
                            , R.mipmap.main_ic_location
                            , tencentLocation.getBearing());
        }else{
            currentLocationMarker.setPosition(new LatLng(tencentLocation.getLatitude(), tencentLocation.getLongitude()));
            currentLocationMarker.setRotation(tencentLocation.getBearing());
        }
    }

    class MyLocationListener implements TencentLocationListener {
        @Override
        public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
            if(tencentLocation == null){
                Log.e(LOG_TAG, "passenger onLocationChanged location null");
                return;
            }
            // 添加当前定位marker
            addCurrentLocationMarker(tencentLocation);
            // 乘客端上传location，司机端实时展示乘客位置
            // 如果不需要此功能，乘客端无需上传定位点
            if (tencentLocusSynchro != null) {
                tencentLocusSynchro.updateLocation
                        (Heaper.convertToSynchroLocation(tencentLocation)
                                , new Order(orderId, orderStatus));
            }
            // 记录当前坐标，这是test展示
            currentPosition = new LatLng(tencentLocation.getLatitude(), tencentLocation.getLongitude());
            // 将当前定位点展示出来，test展示
            mView.showCurrentPoint(tencentLocation.getLatitude()+","+tencentLocation.getLongitude());
        }

        @Override
        public void onStatusUpdate(String s, int i, String s1) {

        }
    }

    class MyCameraChangeListener implements TencentMap.OnCameraChangeListener {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            // 地图拖动的时候
            if(!isHasRoute){

            }
        }

        @Override
        public void onCameraChangeFinished(CameraPosition cameraPosition) {
            if(isHasRoute){
                // 路线规划之后，就不处理滑动地图监听了
                return;
            }
            // 展示地图中心坐标，test专用
            mView.showCurrentMapPoint(cameraPosition.target.latitude
                    + "," +cameraPosition.target.longitude);
        }
    }

    class MyPDSynListener implements TencentLocusSynchro.DataSyncListener {
        @Override
        public Order onOrderInfoSynchro() {
            // 订单信息同步
            // 最新的订单信息。为保证司乘同显功能正常，请开发者务必返回最新的OrderInfo！
            Order order = new Order();
            order.setOrderId(orderId);
            order.setOrderStatus(orderStatus);
            return order;
        }

        @Override
        public void onSyncDataUpdated(SyncData syncData) {
            if(syncData == null){
                Log.e(LOG_TAG, "onSyncDataUpdated : syncData null");
                return;
            }
            // 获取司机端的位置点串，注意：每次数量不定
            ArrayList<SynchroLocation> locations = syncData.getLocations();
            // 当前司机端最新的latlng
            SynchroLocation firstLocation = null;
            LatLng fromPosition = null;
            if (locations.size() > 0) {
                // 司机的位置，相对于乘客来说，已经走过的点串，last是当前小车的位置（无动画时）
                // 在接驾场景中
                firstLocation = locations.get(0);
                if(firstLocation == null){
                    Log.i(LOG_TAG, "syncData.getLocations() first location null");
                    return;
                }
                // 定位点吸附成功
                if(firstLocation.getAttachedIndex() != -1) {
                    fromPosition = new LatLng(firstLocation.getAttachedLatitude()
                            , firstLocation.getAttachedLongitude());
                }else{
                    // 注意：因为没吸附成功，所以会飘到路外
                    fromPosition = new LatLng(firstLocation.getLatitude()
                            , firstLocation.getLongitude());
                }
                // 展示小车图标
                if (driverMarker == null) {
                    driverMarker = mView.getTencentMap().addMarker(
                            new MarkerOptions(fromPosition)
                                    .anchor(0.5f, 0.5f)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_taxi))
                                    //设置此属性 marker 会跟随地图旋转
                                    .flat(false)
                                    //marker 逆时针方向旋转
                                    .clockwise(true));
                } else {
                    driverMarker.setPosition(fromPosition);
                    driverMarker.setRotation(firstLocation.getDirection());
                }
                driverMarker.setTitle("leftDistance:" + syncData.getOrder().getLeftDistance()
                                + " leftTime:" + syncData.getOrder().getLeftTime());

            } else {
                Log.e(LOG_TAG, "onSyncDataUpdated : syncData.getLocations() size 0");
            }
            // 小车平滑移动
            if(locations != null && locations.size() != 0){
                /**
                 *  MarkerTranslateAnimator
                 *  Marker: 待平滑移动的marker
                 *  int: 动画时间 ms
                 *  ArrayList<Latlng>: 平移动画点串
                 *  boolean: marker 是否会根据传入的点串计算并执行旋转动画, marker 方向将与移动方向保持一致
                 */
                LatLng[] latlngs = Heaper.getLatLngsBySynchroLocation(locations);
                // 展示小车运动轨迹
//                mView.getTencentMap().addPolyline(new PolylineOptions().add(latlngs));
                mTranslateAnimator = new MarkerTranslateAnimator(driverMarker
                        , 6 * 1000
                        , latlngs
                        , true);
                mTranslateAnimator.startAnimation();
            }
            // 获取司机路线
            ArrayList<LatLng> points = new ArrayList<>();
            int size = syncData.getRoute().getRoutePoints().size();
            LatLng from = null;
            LatLng to = null;
            for (int i=0; i<size; i++) {
                LatLng position = new LatLng(syncData.getRoute().getRoutePoints().get(i).getLatitude()
                        , syncData.getRoute().getRoutePoints().get(i).getLongitude());
                points.add(position);
                if(i == 0){
                    from = position;
                }
                if(i == size - 1){
                    to = position;
                }
            }
            // 添加起点、终点的marker
            // 只需要在路线展示的时候添加即可
            if(polyline == null){
                addMarker(from, R.mipmap.navi_marker_start, 0, 0.5f, 1f);
                addMarker(to, R.mipmap.line_real_end_point, 0,0.5f, 1f);
                // 调整视图，使中心点为起点终点的中点
                Heaper.fitsWithRoute(mView.getTencentMap()
                        , Heaper.transformLatLngs(syncData.getRoute().getRoutePoints())
                        , CommentUtil.dip2px(mView.getFragmentContext(), 32)
                        , CommentUtil.dip2px(mView.getFragmentContext(), 64)
                        , CommentUtil.dip2px(mView.getFragmentContext(), 32)
                        , CommentUtil.dip2px(mView.getFragmentContext(), 64));
            }
            // 绘制路线
            if (polyline == null) {
                polyline = mView.getTencentMap().addPolyline
                        (new PolylineOptions()
                                .latLngs(points)
                                .color(0xff6cbe89)
                                .arrow(true));
            } else {
                if(syncData.getRoute() != null
                        && syncData.getRoute().getRouteId() != null
                        && !syncData.getRoute().getRouteId().equals(lastRouteId)){
                    polyline.setPoints(points);
                }
            }
            // 更新routedId
            if(syncData.getRoute() != null){
                lastRouteId = syncData.getRoute().getRouteId();
            }
            // 处理已经走过的路线
            // 0: 置灰已走路线 1: 擦除已走路线
            int eraseableType = 0;
            polyline.setEraseable(eraseableType == 1);
            SynchroLocation last = locations.get(locations.size()-1);
            if(fromPosition != null && last.getAttachedIndex() != -1) {
                polyline.eraseTo(last.getAttachedIndex(), fromPosition);
            }
            // 绘制路线后，则不能拖动地图选择位置了
            isHasRoute = true;
            mView.showMapCenterMarker(false);
        }

        @Override
        public void onRouteUploadFailed(RouteUploadError routeUploadError) {
            // 路线上传失败。开发者需要重新上传路线。
            Log.e(LOG_TAG, "onRouteUploadFailed routeUploadError ; "+routeUploadError);
        }

        @Override
        public void onRouteUploadComplete() {
            // 路线上传成功。
            Log.d(LOG_TAG, "onRouteUploadComplete()");
        }

        @Override
        public void onLocationUploadFailed(RouteUploadError routeUploadError) {
            // 定位点上传失败
            Log.e(LOG_TAG, "onLocationUploadFailed routeUploadError ; "+routeUploadError);
        }

        @Override
        public void onLocationUploadComplete() {
            // 定位点上传成功
            Log.d(LOG_TAG, "onLocationUploadComplete()");
        }
    }

}
