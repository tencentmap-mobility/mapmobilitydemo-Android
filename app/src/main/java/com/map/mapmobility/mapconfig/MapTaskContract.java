package com.map.mapmobility.mapconfig;

import android.content.Context;

import com.map.mapmobility.BasePresenter;
import com.map.mapmobility.BaseView;
import com.tencent.map.navi.car.CarNaviView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;

public class MapTaskContract {

    interface IPresenter extends BasePresenter {

        /**
         * 初始化地图
         */
        void initMap(CarNaviView map);

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

    }

    interface IView extends BaseView<MapTaskContract.IPresenter> {
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
