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
         * 显示罗盘与否
         */
        void enableCompass(boolean isEnableCompass);

        /**
         * 关于location Source
         */
        void enableAddMarkerWithLS(TencentMap map, boolean isEnableLocationSorce);
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
