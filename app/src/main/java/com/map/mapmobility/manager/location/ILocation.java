package com.map.mapmobility.manager.location;

import com.map.mapmobility.manager.location.bean.MapLocation;

/**
 *  这是获取定位的管理类
 *
 * @author mingjiezuo
 */
public interface ILocation {

    /**
     * 开始定位
     */
    void startLocation();

    /**
     * 设置位置回调接口
     */
    void setLocationListener(ILocationListener listener);

    /**
     * 结束定位
     */
    void stopLocation();

    interface ILocationListener {
        /**
         * 当定位更新位置
         */
        void onLocationChanged(MapLocation location);
        /**
         * 当定位状态发生改变
         */
        void onStatusUpdate(String s, int i, String s1);
    }

}
