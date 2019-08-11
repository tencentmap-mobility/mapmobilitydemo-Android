package com.map.mapmobility.simultaneousdisplay.passenger;

import android.content.Context;

import com.map.mapmobility.BasePresenter;
import com.map.mapmobility.BaseView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;

public class PassengerTaskContract {

    interface IPresenter extends BasePresenter {
        /**
         * 初始化地图
         */
        void initMap(TencentMap map);

        /**
         * 开始定位
         */
        void startLocation();

        /**
         * 结束定位
         */
        void stopLocation();

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
         * 调整视图中心点
         */
        void toOriginLocation(LatLng currentPosition, int currentZoomLevel);

        /**
         * 视图回到当前位置
         */
        void toOriginCurrentLocation();

    }

    interface IView extends BaseView<IPresenter> {
        /**
         * 获取当前上下文
         */
        Context getFragmentContext();

        /**
         * 获取地图
         */
        TencentMap getTencentMap();

        /**
         * 显示大头针图片
         */
        void showMapCenterMarker(boolean isShow);

        /**
         * 展示当前坐标测试数据
         */
        void showCurrentPoint(String point);

        /**
         * 展示地图中心定位数据
         */
        void showCurrentMapPoint(String point);
    }
}
