package com.map.mapmobility.carpreview;

import com.map.mapmobility.manager.location.bean.MapLocation;

public interface ICarPreView {

    /**
     * 定位改变的监听
     */
    void onLocationChange(MapLocation location);

}
