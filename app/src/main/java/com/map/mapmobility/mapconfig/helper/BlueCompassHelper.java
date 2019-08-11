package com.map.mapmobility.mapconfig.helper;

import android.content.Context;

import com.map.mapmobility.manager.sensor.ISensor;
import com.map.mapmobility.manager.sensor.MyOrientationSensor;

/**
 *  这是使用陀螺仪传感器的helper类
 *
 * @author mingjiezuo
 */
public class BlueCompassHelper {

    /** 这是传感器manager*/
    ISensor sensorManager;
    /** 传感器方向的监听类*/
    ISensor.IDirectionListener listener;

    /**上下文*/
    Context mContext;

    public BlueCompassHelper(Context context, ISensor.IDirectionListener listener) {
        mContext = context;
        this.listener = listener;
    }

    public void startSensor(){
        sensorManager = new MyOrientationSensor(mContext.getApplicationContext());
        ((MyOrientationSensor) sensorManager).registerDirectionListener(listener);
        ((MyOrientationSensor) sensorManager).startOrientationSensor();
    }

    public void stopSensor() {
        if(sensorManager != null){
            ((MyOrientationSensor) sensorManager).stopOrientationSensor();
        }
    }
}
