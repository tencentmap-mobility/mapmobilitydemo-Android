package com.map.mapmobility.manager.sensor;

/**
 *  这是传感器的协议接口
 *
 * @author mingjiezuo
 */
public interface ISensor {

    interface IOrientationSensor extends ISensor {
        /**
         * 开始传感器获取手机方向
         */
        void startOrientationSensor();
        /**
         * 注册角度回调监听
         */
        void registerDirectionListener(IDirectionListener listener);
        /**
         * 结束传感器
         */
        void stopOrientationSensor();
    }


    interface IDirectionListener {
        /**
         * 返回角度
         */
        void onDirection(float direction);
    }
}
