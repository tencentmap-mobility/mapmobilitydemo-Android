package com.map.mapmobility.simultaneousdisplay.driver;

import android.content.Context;

import com.map.mapmobility.BasePresenter;
import com.map.mapmobility.BaseView;
import com.tencent.map.navi.car.CarNaviView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;

public class DriverTaskContract {

    interface IPresenter extends BasePresenter {

        /**
         * 初始化导航管理类
         */
        void initNaviManager(CarNaviView map);

        /**
         * 开始定位
         */
        void startLocation();

        /**
         * 结束定位
         */
        void stopLocation();

        /**
         * 开始路线规划
         */
        void searchRoute();

        /**
         * 开始模拟导航
         */
        void startSimulateNavi(int index);

        /**
         * 开始普通导航
         */
        void startNavi(int index);

        /**
         * 添加marker
         */
        Marker addMarker(LatLng latLng, int markerId, float rotation);

        /**
         * 添加marker
         * anchorX:锚点x
         * anchorY:锚点y
         */
        Marker addMarker(LatLng latLng, int markerId, float rotation, float anchorX, float anchorY);

        /**
         * 司乘初始化
         */
        void initPassengerDriverSynchro();

        /**
         * 开始司乘同显
         */
        void startPassengerDriverSynchro();

        /**
         * 是否进行信息同步
         */
        void isSyncEnable(boolean enable);

        /**
         * 结束司乘同显
         */
        void stopPassengerDriverSynchro();

    }

    interface IView extends BaseView<DriverTaskContract.IPresenter> {
        /**
         * 获取当前上下文
         */
        Context getFragmentContext();

        /**
         * 获取地图
         */
        TencentMap getTencentMap();
    }
}
