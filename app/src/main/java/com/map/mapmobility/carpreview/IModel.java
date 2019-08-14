package com.map.mapmobility.carpreview;

/**
 *  model 对外提供的接口协议
 *
 * @author mingjiezuo
 */
public interface IModel {

    /**
     *  注册监听
     */
    void register(ICarPreView view);

    /**
     *  取消监听
     */
    void unregister();

    /**
     *  获取定位
     */
    void postChangeLocationEvent();

    /**
     *  当定位改变后的通知
     */
    void onLocationChangeEvent();
}
