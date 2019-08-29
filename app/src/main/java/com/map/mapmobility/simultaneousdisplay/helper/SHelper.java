package com.map.mapmobility.simultaneousdisplay.helper;

import android.os.Handler;

import com.tencent.map.locussynchro.model.SynchroLocation;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class SHelper {

    private SHelper(){}

    /**
     *  获取小车平滑需要的点串信息
     * @param locations
     */
    public static com.tencent.tencentmap.mapsdk.maps.model.LatLng[]
            getLatLngsBySynchroLocation(ArrayList<SynchroLocation> locations) {
        if(locations == null){
            return null;
        }
        for(int i=locations.size()-1; i>=0; i--){
            SynchroLocation l = locations.get(i);
            // 剔除掉吸附失败的点，会造成角度偏差
            if(l.getAttachedIndex() == -1){
                locations.remove(i);
            }
        }
        int size = locations.size();
        com.tencent.tencentmap.mapsdk.maps.model.LatLng[] latLngs =
                new com.tencent.tencentmap.mapsdk.maps.model.LatLng[size];
        for(int i=0; i<size; i++){
            latLngs[i] = new com.tencent.tencentmap.mapsdk.maps.model.LatLng
                    (locations.get(i).getAttachedLatitude()
                            , locations.get(i).getAttachedLongitude());
        }
        return latLngs;
    }

    public static com.tencent.tencentmap.mapsdk.maps.model.LatLng[]
            addLalng(com.tencent.tencentmap.mapsdk.maps.model.LatLng[] latLngs
                , com.tencent.tencentmap.mapsdk.maps.model.LatLng latLng) {
        if(latLngs == null)
            return null;
        int size = latLngs.length + 1;
        com.tencent.tencentmap.mapsdk.maps.model.LatLng[] ls = new com.tencent.tencentmap.mapsdk.maps.model.LatLng[size];
        for(int index = 0; index < size; index++){
            if(index == 0)
                ls[0] = latLng;
            else
                ls[index] = latLngs[index-1];
        }
        return ls;
    }

    /**
     * 缩放至整条路线都在可视区域内
     *
     * @param map 底图map
     * @param routePoints 当前待操作的路线的点串
     * @param leftMargin 左边距
     * @param topMargin 上边距
     * @param rightMargin 右边距
     * @param bottomMargin 下边距
     */
    public static void fitsWithRoute(final TencentMap map
            , final List<LatLng> routePoints
            , final int leftMargin
            , final int topMargin
            , final int rightMargin
            , final int bottomMargin) {
        if((routePoints == null)|| map == null){
            return;
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(routePoints);
                LatLngBounds bounds = builder.build();
                map.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(bounds,
                        leftMargin, rightMargin, topMargin, bottomMargin));
            }
        });
    }
}
