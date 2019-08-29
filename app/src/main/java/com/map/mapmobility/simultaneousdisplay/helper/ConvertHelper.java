package com.map.mapmobility.simultaneousdisplay.helper;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.locussynchro.model.LatLng;
import com.tencent.map.locussynchro.model.SynchroLocation;
import com.tencent.map.locussynchro.model.SynchroRoute;
import com.tencent.map.navi.data.AttachedLocation;
import com.tencent.map.navi.data.RouteData;
import com.tencent.map.navi.data.TrafficItem;

import java.util.ArrayList;

public class ConvertHelper {

    private ConvertHelper() {}

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
     *  获得路线点串
     * @return
     */
    public static ArrayList<com.tencent.map.locussynchro.model.LatLng> getRoutePoints(RouteData routeData) {
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
    public static ArrayList<com.tencent.map.locussynchro.model.TrafficItem>
            getTrafficItems(ArrayList<Integer> indexList) {
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
            com.tencent.map.locussynchro.model.TrafficItem newTrafficItem =
                    new com.tencent.map.locussynchro.model.TrafficItem();

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
        synchroRoute.setRoutePoints(ConvertHelper.getRoutePoints(routeData));
        synchroRoute.setTrafficItems(ConvertHelper.getTrafficItems(routeData.getTrafficIndexList()));
        return synchroRoute;
    }

    /**
     *  将trafficItem转换成司乘需要的trafficItem
     * @param arrayList
     * @return
     */
    public static ArrayList<com.tencent.map.locussynchro.model.TrafficItem> converTrafficItems
            (ArrayList<TrafficItem> arrayList) {
        ArrayList<com.tencent.map.locussynchro.model.TrafficItem> result = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            TrafficItem tmpItem = arrayList.get(i);
            com.tencent.map.locussynchro.model.TrafficItem newTrafficItem =
                    new com.tencent.map.locussynchro.model.TrafficItem();
            newTrafficItem.setTraffic(tmpItem.getTraffic());
            newTrafficItem.setToIndex(tmpItem.getToIndex());
            newTrafficItem.setFromIndex(tmpItem.getFromIndex());
            result.add(newTrafficItem);
        }
        return  result;
    }
}
