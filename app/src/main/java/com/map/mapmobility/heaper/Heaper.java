package com.map.mapmobility.heaper;

import android.os.Handler;
import android.util.Log;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.locussynchro.model.LatLng;
import com.tencent.map.locussynchro.model.SynchroLocation;
import com.tencent.map.locussynchro.model.SynchroRoute;
import com.tencent.map.navi.data.AttachedLocation;
import com.tencent.map.navi.data.RouteData;
import com.tencent.map.navi.data.TrafficItem;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class Heaper {

    private Heaper() {}

    /**
     *  乘客端更新位置的时候，需要转换
     * @param location 定位返回的location
     * @return 司乘需要的location
     */
    public static SynchroLocation convertToSynchroLocation(TencentLocation location) {
        if (location == null) {
            return null;
        }
        SynchroLocation synchro = new SynchroLocation();
        synchro.setAccuracy(location.getAccuracy());
        synchro.setAltitude(location.getAltitude());
        synchro.setDirection(location.getBearing());
        synchro.setLatitude(location.getLatitude());
        synchro.setLongitude(location.getLongitude());
        synchro.setProvider(location.getProvider());
        synchro.setVelocity(location.getSpeed());
        synchro.setTime(location.getTime());
        // 注意：记得添加城市code
        synchro.setCityCode(location.getCityCode());
        return synchro;
    }

    /**
     *  司机端更新吸附点后，需要转换
     */
    public static SynchroLocation convertToSynchroLocation(AttachedLocation attachedLocation) {
        if (attachedLocation == null) {
            return null;
        }
        SynchroLocation location = new SynchroLocation();
        location.setTime(attachedLocation.getTime());
        location.setLatitude(attachedLocation.getLatitude());
        location.setLongitude(attachedLocation.getLongitude());
        location.setAttachedLatitude(attachedLocation.getAttachedLatitude());
        location.setAttachedLongitude(attachedLocation.getAttachedLongitude());

        location.setAttachedIndex(attachedLocation.getAttachedIndex());

        location.setAccuracy(attachedLocation.getAccuracy());
        location.setDirection(attachedLocation.getRoadDirection());
        location.setVelocity(attachedLocation.getVelocity());
        location.setProvider(attachedLocation.getProvider());
        return location;
    }

    /**
     *  获取小车平滑需要的点串信息
     * @param locations
     */
    public static com.tencent.tencentmap.mapsdk.maps.model.LatLng[]
            getLatLngsBySynchroLocation(ArrayList<SynchroLocation> locations) {
        if(locations == null){
            return null;
        }
        int size = locations.size();
        com.tencent.tencentmap.mapsdk.maps.model.LatLng[] latLngs =
                new com.tencent.tencentmap.mapsdk.maps.model.LatLng[size];
        for(int i=0; i<size; i++){
            if(locations.get(i).getAttachedIndex() != -1){
                latLngs[i] = new com.tencent.tencentmap.mapsdk.maps.model.LatLng
                        (locations.get(i).getAttachedLatitude(), locations.get(i).getAttachedLongitude());
            }else{
                latLngs[i] = new com.tencent.tencentmap.mapsdk.maps.model.LatLng
                        (locations.get(i).getAltitude(), locations.get(i).getLongitude());
            }
        }
        return latLngs;
    }

    /**
     *  两个latlng的转换
     * @param list
     */
    public static ArrayList<com.tencent.tencentmap.mapsdk.maps.model.LatLng>
            transformLatLngs(ArrayList<LatLng> list) {
        if(list == null){
            return null;
        }
        ArrayList<com.tencent.tencentmap.mapsdk.maps.model.LatLng> latLngs = new ArrayList<>();
        for(LatLng lstlng : list){
            latLngs.add(new com.tencent.tencentmap.mapsdk.maps.model.LatLng
                    (lstlng.getLatitude(), lstlng.getLongitude()));
        }
        return latLngs;
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
            , final List<com.tencent.tencentmap.mapsdk.maps.model.LatLng> routePoints
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

    /**
     *  获得路线点串
     * @return
     */
    public static ArrayList<LatLng> getRoutePoints(RouteData routeData) {
        ArrayList<com.tencent.map.locussynchro.model.LatLng> routePoints = new ArrayList<>();
        for (int i=0; i < routeData.getRoutePoints().size(); i++) {
            routePoints.add(new com.tencent.map.locussynchro.model.LatLng
                    (routeData.getRoutePoints().get(i).latitude
                            , routeData.getRoutePoints().get(i).longitude));
        }
        return routePoints;
    }

    /**
     *  获取路况items
     * @return
     */
    public static ArrayList<com.tencent.map.locussynchro.model.TrafficItem> getTrafficItems(ArrayList<Integer> indexList) {
        ArrayList<TrafficItem> trafficItems = new ArrayList<>();
        for (int i=0; i<indexList.size(); i=i+3) {
            TrafficItem item = new TrafficItem();
            item.setTraffic(indexList.get(i));
            item.setFromIndex(indexList.get(i+1));
            item.setToIndex(indexList.get(i+2));
            trafficItems.add(item);
        }

        ArrayList<com.tencent.map.locussynchro.model.TrafficItem> result = new ArrayList<>();
        for (int i = 0; i < trafficItems.size(); i++) {
            TrafficItem tmpItem = trafficItems.get(i);
            com.tencent.map.locussynchro.model.TrafficItem newTrafficItem = new com.tencent.map.locussynchro.model.TrafficItem();

            newTrafficItem.setTraffic(tmpItem.getTraffic());
            newTrafficItem.setToIndex(tmpItem.getToIndex());
            newTrafficItem.setFromIndex(tmpItem.getFromIndex());

            result.add(newTrafficItem);
        }
        return result;
    }

    /**
     *  获取同步路线
     */
    public static SynchroRoute getSynRoute(RouteData routeData) {
        SynchroRoute synchroRoute = new SynchroRoute();
        synchroRoute.setRouteId(routeData.getRouteId());
        synchroRoute.setRoutePoints(Heaper.getRoutePoints(routeData));
        synchroRoute.setTrafficItems(Heaper.getTrafficItems(routeData.getTrafficIndexList()));
        return synchroRoute;
    }
}
