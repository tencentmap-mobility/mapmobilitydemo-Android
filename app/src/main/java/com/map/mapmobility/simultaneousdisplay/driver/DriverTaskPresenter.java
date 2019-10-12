package com.map.mapmobility.simultaneousdisplay.driver;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.map.mapmobility.R;
import com.map.mapmobility.simultaneousdisplay.helper.ConvertHelper;
import com.map.mapmobility.simultaneousdisplay.helper.SHelper;
import com.map.mapmobility.utils.CommentUtils;
import com.map.mapmobility.utils.ToastUtils;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.locussynchro.TencentLocusSynchro;
import com.tencent.map.locussynchro.model.DriverSynchroOptions;
import com.tencent.map.locussynchro.model.Order;
import com.tencent.map.locussynchro.model.RouteUploadError;
import com.tencent.map.locussynchro.model.SyncData;
import com.tencent.map.locussynchro.model.SynchroLocation;
import com.tencent.map.locussynchro.model.SynchroRoute;
import com.tencent.map.navi.GpsRestartManager;
import com.tencent.map.navi.INaviView;
import com.tencent.map.navi.TencentNaviCallback;
import com.tencent.map.navi.TencentRouteSearchCallback;
import com.tencent.map.navi.car.CarNaviView;
import com.tencent.map.navi.car.CarRouteSearchOptions;
import com.tencent.map.navi.car.TencentCarNaviManager;
import com.tencent.map.navi.data.AttachedLocation;
import com.tencent.map.navi.data.NaviPoi;
import com.tencent.map.navi.data.NaviTts;
import com.tencent.map.navi.data.NavigationData;
import com.tencent.map.navi.data.RouteData;
import com.tencent.map.navi.data.TrafficItem;
import com.tencent.map.navi.feedback.screen.percentor.UploadPercentor;
import com.tencent.map.navi.feedback.screen.report.OneKeyReportManager;
import com.tencent.map.screen.ScreenRecordManager;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.OverlayLevel;
import com.tencent.tencentmap.mapsdk.maps.model.Polyline;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;

import java.util.ArrayList;

public class DriverTaskPresenter implements DriverTaskContract.IPresenter {
    private static final String LOG_TAG = "navi";

    private DriverTaskContract.IView mView;

    /** 导航manager*/
    private TencentCarNaviManager tencentCarNaviManager;
    /** 算路的配置类*/
    private CarRouteSearchOptions carRouteSearchOptions;
    /** 司乘同显*/
    private TencentLocusSynchro tencentLocusSynchro;
    /** 定位*/
    private TencentLocationManager mLocationManager;
    /** 定位监听*/
    private TencentLocationListener locationListener;

    /** 路线规划的起点*/
    private NaviPoi mFrmoLatlng = new NaviPoi(22.779268,113.856332);
    /** 路线规划的终点*/
    private NaviPoi mToLatlng = new NaviPoi(22.600728,113.847116);
    /** 途经点*/
    private ArrayList<NaviPoi> wayPoints = new ArrayList<>();
    /** 乘客的marker*/
    private Marker passengerMarker;

    /** 当前是否已经进行算路*/
    private boolean isSearchRoute = false;
    /** 记录当前的路线polyline，不需要的时候擦除掉*/
    private ArrayList<Polyline> polylineList = new ArrayList<>();
    /** 记录当前添加的marker*/
    private ArrayList<Marker> markerList = new ArrayList<>();

    /** 订单id，注意：需要与乘客端保持一致*/
    private String orderId = "1010204403053717";
    /** 司机的id*/
    private String driverId = "1022001150863";
    /**
     *  订单状态
     *  STATUS_NO_ORDER 订单状态-无订单 订单已经结束开始空驶
     *  STATUS_ORDER_SENT 订单状态-已派单 司机开始接驾
     *  STATUS_CHARGING_STARTED 订单状态-开始计费 司机开始送驾
     */
    private int orderStatus = Order.STATUS_ORDER_SENT;
    /**
     *  司机服务状态
     *  DRIVER_STATUS_STOPPED 司机服务状态--停止接单 不接受派单
     *  DRIVER_STATUS_LISTENING 司机服务状态--听单中
     *  DRIVER_STATUS_SERVING 司机服务状态--服务中 有订单在身
     */
    private int driverStatus = Order.DRIVER_STATUS_SERVING;
    /** 当前路线信息*/
    private RouteData routeData;
    /** 路线id*/
    private String routeId = "";
    /** 订单信息*/
    private Order order = new Order(orderId, orderStatus);
    /**
     *  当前的cityCode
     *  因为在导航中，吐给用户的吸附点不包含cityCode，所以需要通过定位sdk获取不断更新cityCode
     */
    private String cityCode;

    public DriverTaskPresenter(DriverTaskFragment view) {
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        ToastUtils.INSTANCE().init(mView.getFragmentContext());
        initPassengerDriverSynchro();
        startLocation();
    }

    @Override
    public void destory() {
        isSearchRoute = false;
        ToastUtils.INSTANCE().destory();
        stopPassengerDriverSynchro();
        stopLocation();
    }

    /**
     * 初始化地图
     * @param carNaviView
     */
    @Override
    public void initNaviManager(CarNaviView carNaviView) {
        tencentCarNaviManager = new TencentCarNaviManager(mView.getFragmentContext());
        tencentCarNaviManager.addNaviView(carNaviView);
        // 添加自定义INaviView事件回调
        tencentCarNaviManager.addNaviView(new MyCarNaviView());
        // 添加导航事件回调
        tencentCarNaviManager.setNaviCallback(new MyTencentCallback());
        // 定位重启
        tencentCarNaviManager.setOnGpsManagerCallback(new GpsRestartManager.OnGpsManagerCallback() {
            @Override
            public void restart() {
                if(mLocationManager == null)
                    return;
                mLocationManager.reStartGpsLocationManager("GPS Error");
            }
        });
    }

    /**
     * 开始定位
     */
    @Override
    public void startLocation() {
        // 初始化定位
        mLocationManager = TencentLocationManager.getInstance(mView.getFragmentContext().getApplicationContext());
        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
        // 定位监听
        locationListener = new MyLocationListener();
        // 开始定位
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setInterval(5000);
        int error = mLocationManager.requestLocationUpdates(request, locationListener);
        Log.d(LOG_TAG, "request location error"+error);
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
     * 开始路线规划
     */
    @Override
    public void searchRoute() {
        // 算路的配置类，在这里可以进行相关需求的配置
        carRouteSearchOptions = CarRouteSearchOptions.create();
        try {
            tencentCarNaviManager.searchRoute(mFrmoLatlng
                    , mToLatlng
                    , wayPoints
                    , carRouteSearchOptions
                    , new MySearchRouteCallback());
        }catch (Exception e){
            Log.e(LOG_TAG, "search route error :" + e.getMessage());
        }
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

    /**
     * 开始模拟导航
     */
    @Override
    public void startSimulateNavi(int index) {
        if(isSearchRoute && tencentCarNaviManager != null){
            try{
                tencentCarNaviManager.startSimulateNavi(index);
                // 导航界面自带起点终点marker，所以需要将自己添加的marker清除掉
                removeMarkers();
            }catch (Exception e){
                Log.e(LOG_TAG, "start simulate navi error:"+e.getMessage());
            }
        }
    }

    /**
     * 开始普通导航
     */
    @Override
    public void startNavi(int index) {
        if(isSearchRoute && tencentCarNaviManager != null){
            try{
                tencentCarNaviManager.startNavi(index);
                // 导航界面自带起点终点marker，所以需要将自己添加的marker清除掉
                removeMarkers();
            }catch (Exception e){
                Log.e(LOG_TAG, "start navi error:"+e.getMessage());
            }
        }
    }

    /**
     * 司乘的初始化
     */
    @Override
    public void initPassengerDriverSynchro() {
        DriverSynchroOptions driverOptions = new DriverSynchroOptions();
        driverOptions.setAccountId(driverId);
        tencentLocusSynchro = new TencentLocusSynchro(mView.getFragmentContext(), driverOptions);
    }

    /**
     * 开始司乘同显
     */
    @Override
    public void startPassengerDriverSynchro() {
        try{
            tencentLocusSynchro.start(new MyDriverPassengerSynListener());
            ToastUtils.INSTANCE().Toast("开始司乘成功");
        }catch (Exception e){
            Log.e(LOG_TAG, "tencentLocusSynchro start error:"+e.getMessage());
        }
    }

    /**
     * 是否同步乘客端的数据
     * @param enable
     */
    @Override
    public void isSyncEnable(boolean enable) {
        if(tencentLocusSynchro != null){
            tencentLocusSynchro.setSyncEnabled(enable);
        }
    }

    /**
     * 结束司乘同显
     */
    @Override
    public void stopPassengerDriverSynchro() {
        if(tencentLocusSynchro != null){
            tencentLocusSynchro.stop();
        }
    }

    @Override
    public void startReport() {
        // 一键上报
        report(cityCode, orderId, driverId);
    }

    /**
     *  添加起点、终点marker
     *  路线规划成功后，需要自己添加起点终点图标
     */
    private void addFromAndToMarkers() {
        Marker fromMarker = addMarker(new LatLng(mFrmoLatlng.getLatitude(), mFrmoLatlng.getLongitude())
                , R.mipmap.navi_marker_start, 0, 0.5f, 1f);
        Marker toMarker = addMarker(new LatLng(mToLatlng.getLatitude(), mToLatlng.getLongitude())
                , R.mipmap.line_real_end_point, 0, 0.5f, 1f);
        markerList.add(fromMarker);
        markerList.add(toMarker);
    }

    /**
     * 清除起点、终点marker
     * 因为导航自带路线起点终点图标，所以开始导航时，需要将自己添加marker清除掉
     */
    private void removeMarkers() {
        if(markerList.size() > 0){
            for(Marker marker : markerList){
                marker.remove();
            }
            markerList.clear();
        }
    }

    /**
     *  开始一键上报
     */
    private void report(String adCode, String orderId, String userId) {
        UploadPercentor.setAdCode(adCode);
        // 设置apikey，地图的apikey
        UploadPercentor.setApiKey("");
        // 设置订单id
        UploadPercentor.setOrderId(orderId);
        // 设置用户id
        UploadPercentor.setUserId(userId);
        ScreenRecordManager.getInctance().setOnRecordListener(new ScreenRecordManager.OnRecordListener() {
            @Override
            public void onPrepare() {
                // 开始录音前调用，app如果有录音功能需要在这里结束录音，解决录音冲突
            }

            @Override
            public void onStart() {
                // 开始录音后调用
            }

            @Override
            public void onStop() {
                // 结束录音后调用，在这里回复app的录音
            }
        });
        OneKeyReportManager.getInstance().showOneKeyReportDialog(mView.getFragmentContext());
    }

    class MySearchRouteCallback implements TencentRouteSearchCallback {
        @Override
        public void onRouteSearchFailure(int i, String s) {
            Log.e(LOG_TAG, "search route fail error code:" + i + ",error:"+s);
            ToastUtils.INSTANCE().Toast("error:"+s);
        }

        @Override
        public void onRouteSearchSuccess(ArrayList<RouteData> arrayList) {
            if(arrayList == null){
                Log.e(LOG_TAG, "onRouteSearchSuccess : return list null");
                return;
            }
            ToastUtils.INSTANCE().Toast("算路成功");
            // 算路成功
            isSearchRoute = true;
            // 清除已经存在的路线
            if(polylineList.size() > 0){
                for(Polyline polyline : polylineList){
                    polyline.remove();
                }
                polylineList.clear();
            }
            // 清除已经添加的marker
            removeMarkers();
            // 添加起点终点marker
            addFromAndToMarkers();
            // 添加路线
            // 因为是demo，为了简单，只取第一个route date
            if(arrayList.size() > 0) {
                // 路线的宽度
                int width = (int)(10 * mView.getFragmentContext().getResources()
                        .getDisplayMetrics().density + 0.5);
                routeData = arrayList.get(0);
                // 没有显示路况，路况参考导航demo
                PolylineOptions options = new PolylineOptions()
                        .addAll(routeData.getRoutePoints())
                        .width(width)
                        .color(0xff6cbe89);
//                        .lineType(PolylineOptions.LineType.LINE_TYPE_DOTTEDLINE)// 圆点虚线方法
//                        .colorTexture(BitmapDescriptorFactory.fromResource(R.mipmap.bule_icon));
                // 设置虚线
//                ArrayList<Integer> l = new ArrayList<>();
//                // 虚线的实线部分长度
//                l.add(20);
//                // 虚线的空白部分长度
//                l.add(10);
//                options.pattern(l);
                Polyline polyline = mView.getTencentMap().addPolyline(options);
                polyline.setLevel(OverlayLevel.OverlayLevelAboveBuildings);// 层级在搂块之上
                polylineList.add(polyline);
                // 将路线显示在屏幕中央
                SHelper.fitsWithRoute(mView.getTencentMap()
                        , routeData.getRoutePoints()
                        , CommentUtils.dip2px(mView.getFragmentContext(), 32)
                        , CommentUtils.dip2px(mView.getFragmentContext(), 64)
                        , CommentUtils.dip2px(mView.getFragmentContext(), 32)
                        , CommentUtils.dip2px(mView.getFragmentContext(), 64));
                // 更新司乘的order
                routeId = routeData.getRouteId();
                order.setRouteId(routeId);
                order.setOrderStatus(orderStatus);
                order.setTime(routeData.getTime());
                // 上传路线
                SynchroRoute synchroRoute = new SynchroRoute();
                synchroRoute.setRouteId(routeId);
                synchroRoute.setRoutePoints(ConvertHelper.getRoutePoints(routeData));
                synchroRoute.setTrafficItems(ConvertHelper.getTrafficItems(routeData.getTrafficIndexList()));
                if(tencentLocusSynchro != null) {
                    tencentLocusSynchro.updateRoute(synchroRoute, order);
                }
            }
        }
    }

    class MyDriverPassengerSynListener implements TencentLocusSynchro.DataSyncListener {
        @Override
        public Order onOrderInfoSynchro() {
            Order order_temp = new Order();
            order_temp.setOrderId(orderId);
            order_temp.setOrderStatus(orderStatus);
            return order_temp;
        }

        @Override
        public void onSyncDataUpdated(SyncData syncData) {
            if(syncData == null){
                Log.e(LOG_TAG, "onSyncDataUpdated : syncData null");
                return;
            }
            // 注意：如果乘客没有上传定位点，则不需要实时展示乘客位置
            if (syncData.getLocations().size() > 0) {
                SynchroLocation last = syncData.getLocations().get(syncData.getLocations().size() - 1);
                LatLng position = new LatLng(last.getLatitude(), last.getLongitude());
                if (passengerMarker == null) {
                    passengerMarker = addMarker(position, R.mipmap.marker_green, last.getDirection());
                } else {
                    passengerMarker.setPosition(position);
                    passengerMarker.setRotation(last.getDirection());
                }
                passengerMarker.setTitle("distance:" + syncData.getOrder().getDistance()
                        + " time:" + syncData.getOrder().getTime());
            } else {
                Log.e(LOG_TAG, "onSyncDataUpdated : syncData size 0");
            }
        }

        @Override
        public void onRouteUploadFailed(RouteUploadError routeUploadError) {
            //路线上传失败。开发者需要重新上传路线。
            Log.e(LOG_TAG, "onRouteUploadFailed routeUploadError ; "+routeUploadError);
        }

        @Override
        public void onRouteUploadComplete() {
            // 路线上传成功。
            Log.e(LOG_TAG, "onRouteUploadComplete()");
        }

        @Override
        public void onLocationUploadFailed(RouteUploadError routeUploadError) {
            // 定位点上传失败
            Log.e(LOG_TAG, "onLocationUploadFailed routeUploadError ; "+routeUploadError);
        }

        @Override
        public void onLocationUploadComplete() {
            // 定位点上传成功
            Log.e(LOG_TAG, "onLocationUploadComplete()");
        }
    }

    class MyTencentCallback implements TencentNaviCallback {
        @Override
        public void onStartNavi() {

        }

        @Override
        public void onStopNavi() {
            // 结束导航
            if(tencentCarNaviManager != null){
                tencentCarNaviManager.stopNavi();
            }
        }

        @Override
        public void onOffRoute() {

        }

        @Override
        public void onRecalculateRouteSuccess(int i, ArrayList<RouteData> arrayList) {
            if(arrayList == null){
                Log.e(LOG_TAG, "onRecalculateRouteSuccess : return list null");
                return;
            }
            // 因为是demo，为了简单，只取第一个route date
            if(arrayList.size() > 0) {
                routeData = arrayList.get(0);
                // 更新司乘的order
                routeId = routeData.getRouteId();
                order.setRouteId(routeId);
                order.setOrderStatus(orderStatus);
                order.setDriverStatus(driverStatus);
                order.setTime(routeData.getTime());
                // 上传路线
                SynchroRoute synchroRoute = new SynchroRoute();
                synchroRoute.setRouteId(routeId);
                synchroRoute.setRoutePoints(ConvertHelper.getRoutePoints(routeData));
                synchroRoute.setTrafficItems(ConvertHelper.getTrafficItems(routeData.getTrafficIndexList()));
                if(tencentLocusSynchro != null) {
                    tencentLocusSynchro.updateRoute(synchroRoute, order);
                }
            }
        }

        /**
         *  这是版本5.1.2新增的接口
         * @param clickRouteId
         * @param arrayList 被选中的路线点串信息
         */
        @Override
        public void onFollowRouteClick(String clickRouteId, ArrayList<LatLng> arrayList) {
            /** 不需要在这里进行上传了，因为该回调后马上会进行onUpdateTraffic回调*/
            routeId = clickRouteId;
        }

        @Override
        public void onRecalculateRouteFailure(int i, int i1, String s) {

        }

        @Override
        public void onRecalculateRouteStarted(int i) {

        }

        @Override
        public void onRecalculateRouteCanceled() {

        }

        @Override
        public int onVoiceBroadcast(NaviTts naviTts) {
            return 1;
        }

        @Override
        public void onArrivedDestination() {

        }

        @Override
        public void onPassedWayPoint(int i) {

        }

        @Override
        public void onUpdateRoadType(int i) {

        }

        @Override
        public void onUpdateAttachedLocation(AttachedLocation attachedLocation) {
            // 上传吸附点
            // 在导航中，吐给用户的吸附点不包含cityCode，所以需要通过定位sdk获取不断更新cityCode
            SynchroLocation location = ConvertHelper.convertToSynchroLocation(attachedLocation);
            if(location == null){
                Log.e(LOG_TAG, "onUpdateAttachedLocation SynchroLocation null");
                return;
            }
            if(cityCode != null && !cityCode.isEmpty()){
                location.setCityCode(cityCode);
            }
            tencentLocusSynchro.updateLocation(location, order);
        }
    }

    class MyCarNaviView implements INaviView {
        @Override
        public void onGpsRssiChanged(int i) {

        }

        @Override
        public void onUpdateNavigationData(NavigationData navigationData) {
            order.setDriverStatus(driverStatus);
            order.setLeftDistance(navigationData.getLeftDistance());
            order.setLeftTime(navigationData.getLeftTime());
            Log.e(LOG_TAG, "里程:"+navigationData.getLeftDistance());
        }

        @Override
        public void onShowEnlargedIntersection(Bitmap bitmap) {

        }

        @Override
        public void onHideEnlargedIntersection() {

        }

        @Override
        public void onShowGuidedLane(Bitmap bitmap) {

        }

        @Override
        public void onHideGuidedLane() {

        }

        /**
         *  这是5.0.1以前的接口
         */
//        @Override
//        public void onUpdateTraffic(int i, int i1, ArrayList<TrafficItem> arrayList) {
//            // 上传路线
//            if(arrayList.size() != 0){
//                SynchroRoute synchroRoute = new SynchroRoute();
//                synchroRoute.setRouteId(routeId);
//                synchroRoute.setRoutePoints(ConvertHelper.getRoutePoints(routeData));
//                synchroRoute.setTrafficItems(ConvertHelper.converTrafficItems(arrayList));
//                tencentLocusSynchro.updateRoute(synchroRoute, order);
//            }
//        }

        /**
         *  这是5.1.2替换的接口
         */
        @Override
        public void onUpdateTraffic(String routeId
                , int totalDistance, int leftDistance
                , ArrayList<LatLng> ponits, ArrayList<TrafficItem> trafficItemArr
                , boolean isCurrent) {
            /** 注意：因为有伴随路线功能，所以onUpdateTraffic会调用多次，只需选择当前行驶的路线上传*/
            if (trafficItemArr == null || !isCurrent) {
                return;
            }
            Log.e(LOG_TAG, "routeId:"+routeId);
            if(trafficItemArr.size() != 0){
                SynchroRoute synchroRoute = new SynchroRoute();
                synchroRoute.setRouteId(routeId);
                synchroRoute.setRoutePoints(ConvertHelper.getRoutePoints(routeData));
                synchroRoute.setTrafficItems(ConvertHelper.converTrafficItems(trafficItemArr));
                tencentLocusSynchro.updateRoute(synchroRoute, order);
            }
        }

    }

    class MyLocationListener implements TencentLocationListener {
        @Override
        public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
            if(tencentLocation == null){
                Log.e(LOG_TAG, "driver onLocationChanged location null");
                return;
            }
            // 不断获取并更新cityCode
            cityCode = tencentLocation.getCityCode();

            // 导航sdk没有与定位sdk强绑定，导航sdk不具备定位的功能，需要定位sdk不断灌点才能移动
            if (tencentCarNaviManager != null) {
                tencentCarNaviManager.updateLocation(SHelper.convertToGpsLocation(tencentLocation), 0, "");
            }
        }

        @Override
        public void onStatusUpdate(String name, int status, String desc) {
            if (tencentCarNaviManager != null) {
                tencentCarNaviManager.updateGpsStatus(name, status, desc);
            }
        }
    }

}
