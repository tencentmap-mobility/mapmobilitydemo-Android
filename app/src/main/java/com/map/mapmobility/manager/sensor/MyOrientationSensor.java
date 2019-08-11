package com.map.mapmobility.manager.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 *  通过陀螺仪传感器获取手机的方向
 *
 * @author 算法copy:http://plaw.info/articles/sensorfusion/
 */
public class MyOrientationSensor implements ISensor.IOrientationSensor {
    public static final String LOG_TAG = "navi";

    public static final float EPSILON = 0.000000001f;

    /** 陀螺仪角速度*/
    private float[] gyro = new float[3];
    /** 陀螺仪数据的旋转矩阵*/
    private float[] gyroMatrix = new float[9];
    /** 陀螺矩阵的方向角*/
    private float[] gyroOrientation = new float[3];
    /** 地磁场旋转矩阵*/
    private float[] magnet = new float[3];
    /** 加速度旋转矩阵*/
    private float[] accel = new float[3];
    /** 基于加速度与地磁场的旋转矩阵*/
    private float[] rotationMatrix = new float[9];

    /** 通过加速度与地磁场获得的夹角*/
    private float[] accMagOrientation = new float[3];
    /** 融合后的最终方向角*/
    private float[] fusedOrientation = new float[3];

    /** 传感器管理类*/
    private SensorManager mSensorManager = null;
    /** 加速度传感器*/
    private Sensor accelerometer;
    /** 初始化地磁场传感器*/
    private Sensor magnetic;
    /** 陀螺仪传感器*/
    private Sensor gyroscope;

    /** 传感器监听*/
    private MySensorEventListener mSensorEventListener;
    /** 用户的角度监听*/
    private ISensor.IDirectionListener mDirectionListener;

    private static final float NS2S = 1.0f / 1000000000.0f;
    /** 设备在最后一次和当前陀螺仪测量之间的旋转间隔*/
    private float timestamp;
    /** 陀螺旋转矩阵是否已经初始化*/
    private boolean initState = true;

    /** 定时时间和采样频率*/
    public static final int TIME_CONSTANT = 60;
    public static final float FILTER_COEFFICIENT = 0.99f;
    private Timer fuseTimer = new Timer();

    /** 当前上下文*/
    private Context mContext;

    public MyOrientationSensor(Context context) {
        mContext = context;
    }

    /**
     * 开始传感器监听
     */
    @Override
    public void startOrientationSensor() {
        // 初始化数据
        init();
        // 初始化传感器
        initSensor();
        // 添加传感器监听
        addListener();
        // 定时采样，通过互补滤波器，过滤陀螺仪漂移
        fuseTimer.scheduleAtFixedRate
                (new calculateFusedOrientationTask()
                        , 1000
                        , TIME_CONSTANT);
    }

    /**
     * 外部添加方向监听
     * @param listener 当前手机的方向
     */
    @Override
    public void registerDirectionListener(IDirectionListener listener) {
        mDirectionListener = listener;
    }

    /**
     * 结束传感器
     */
    @Override
    public void stopOrientationSensor() {
        fuseTimer.cancel();
        if(mSensorManager != null){
            mSensorManager.unregisterListener(mSensorEventListener);
            mSensorEventListener = null;
            mSensorManager = null;
        }
    }

    /**
     * 数据的初始化
     */
    private void init() {
        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;
        // 用单位矩阵初始化回旋矩阵
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;
    }

    /**
     * 初始化传感器
     */
    private void initSensor() {
        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
        // 是否支持
        if(mSensorManager != null){
            // 初始化加速度传感器
            accelerometer = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // 初始化地磁场传感器
            magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            // 初始化陀螺仪传感器
            gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }else{
            Log.e(LOG_TAG, "Sensors are not supported");
        }
        //如果手机有方向传感器，精度可以自己去设置
        if(accelerometer == null || magnetic == null){
            Log.e(LOG_TAG, "accelerometer or magnetic Sensors are not supported");
            return;
        }
    }

    /**
     * 添加内部传感器监听
     */
    private void addListener() {
        // 监听类
        mSensorEventListener = new MySensorEventListener();
        // 加速度传感器
        mSensorManager.registerListener(mSensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        // 陀螺仪传感器
        mSensorManager.registerListener(mSensorEventListener
                , gyroscope
                , SensorManager.SENSOR_DELAY_FASTEST);
        // 地磁场传感器
        mSensorManager.registerListener(mSensorEventListener,
                magnetic,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * 通过加速度、磁场传感器旋转矩阵计算手机的方向
     */
    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch(event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    // copy new accelerometer data into accel array
                    // then calculate new orientation
                    System.arraycopy(event.values, 0, accel, 0, 3);
                    calculateAccMagOrientation();
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    // process gyro data
                    gyroFunction(event);
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    // copy new magnetometer data into magnet array
                    System.arraycopy(event.values, 0, magnet, 0, 3);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
            fusedOrientation[0] =
                    FILTER_COEFFICIENT * gyroOrientation[0]
                            + oneMinusCoeff * accMagOrientation[0];

            fusedOrientation[1] =
                    FILTER_COEFFICIENT * gyroOrientation[1]
                            + oneMinusCoeff * accMagOrientation[1];

            fusedOrientation[2] =
                    FILTER_COEFFICIENT * gyroOrientation[2]
                            + oneMinusCoeff * accMagOrientation[2];

            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);

            // 给用户吐角度数据
            if(mDirectionListener != null){
                float direction = (float) Math.toDegrees(gyroOrientation[0]);
                mDirectionListener.onDirection(direction);
            }
        }
    }

    /**
     *  从陀螺仪中获得旋转矢量
     * @param gyroValues
     * @param deltaRotationVector
     * @param timeFactor
     */
    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor) {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    /**
     * 用于陀螺仪传感器数据处理。这是将陀螺仪旋转间隔添加到基于绝对陀螺仪的方向的位置
     * @param event
     */
    private void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    /**
     * 将方向角转换为旋转矩阵
     * @param o
     * @return
     */
    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    /**
     * 矩阵乘法的函数
     * @param A
     * @param B
     * @return
     */
    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

}
