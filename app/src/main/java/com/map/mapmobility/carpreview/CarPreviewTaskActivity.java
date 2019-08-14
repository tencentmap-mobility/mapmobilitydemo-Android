package com.map.mapmobility.carpreview;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.map.mapmobility.BaseActivity;
import com.map.mapmobility.R;
import com.map.mapmobility.carpreview.helper.Helper;
import com.map.mapmobility.manager.location.bean.MapLocation;
import com.tencent.map.carpreview.PreviewMapManager;
import com.tencent.map.carpreview.ui.TencentCarsMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *  这是周边运力模块的controller
 *
 * @author mingjiezuo
 */
public class CarPreviewTaskActivity extends BaseActivity implements ICarPreView{
    static final String LOG_TAG = "navi";

    /** 同时呼叫，全部车型*/
    public static final int MORE_CAR = 0;
    /** 出租车*/
    public static final int TAXI_TYPE = 1;
    /** 新能源*/
    public static final int ENERGY_TYPE = 2;
    /** 舒适型*/
    public static final int COMFORTABLE_TYPE = 3;
    /** 豪华型*/
    public static final int LUXURY_TYPE = 4;
    /** 商务型*/
    public static final int BUSINESS_TYPE = 5;
    /** 经济型*/
    public static final int ECONOMIC_TYPE = 6;

    private IModel model;

    private PreviewMapManager previewMapManager;

    private TencentCarsMap mTencentCarsMap;

    /** 最近一次的定位点*/
    LatLng lastLatLng;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_preview_task_activity);
        mTencentCarsMap = findViewById(R.id.cars_map);
        recyclerView = findViewById(R.id.car_preview_recycler_view);

        initRecyclerView();

        model = new CarPreviewModel(this);
        model.register(this);

        PreviewMapManager.init(this);

        previewMapManager = new PreviewMapManager();
        /**
         * 导航、司乘、运力的key相同
         */
        previewMapManager.setKey("xxx");
        /**
         * 默认请求车数，默认10个
         */
        previewMapManager.setCarsCount(10);
        /**
         * 请求半径，默认5000米
         */
        previewMapManager.setRadius(100);
        /**
         * 城市代码
         */
        previewMapManager.setCity(110000);
        /**
         * types 车辆类型 1.出租车;2.新能源;3.舒适型;4.豪华型;5.商务型;6.经济型
         */
        ArrayList types = new ArrayList();
        types.add("1");
        types.add("2");
        types.add("3");
        types.add("4");
        types.add("5");
        types.add("6");
        previewMapManager.setCarsType(types);
        // 注意：需要在setCarsTypeResMap()与setCurrentLatLng()方法之前添加
        previewMapManager.attachCarsMap(mTencentCarsMap);
        /**
         * 车型对应的图片资源，只需要赋值即可
         */
        HashMap<String, Integer> typeResMap = new HashMap<>();
        typeResMap.put("1", R.mipmap.car1);
        typeResMap.put("2", R.mipmap.car2);
        typeResMap.put("3", R.mipmap.car3);
        typeResMap.put("4", R.mipmap.car4);
        typeResMap.put("5", R.mipmap.car5);
        try {
            previewMapManager.setCarsTypeResMap(typeResMap);
        } catch (Exception e) {
            Log.e(LOG_TAG, "previewMapManager setCarsTypeResMap() error:"+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *  清除当前所有运力小车图标
     * @param view
     */
    public void clear(View view) {
        mTencentCarsMap.clearCarsMarkers();
    }

    /**
     *  获取当前定位
     * @param view
     */
    public void location(View view) {
        model.postChangeLocationEvent();
    }

    @Override
    public void onLocationChange(MapLocation location) {
        lastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        try {
            previewMapManager.setCurrentLatLng(lastLatLng);
        } catch (Exception e) {
            Log.e(LOG_TAG, "preview manager setCurrentLatLng() error:"+e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkTencentMap())
            mTencentCarsMap.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkTencentMap())
            mTencentCarsMap.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (checkTencentMap())
            mTencentCarsMap.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (checkTencentMap())
            mTencentCarsMap.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(model != null)
            model.unregister();
    }

    private boolean checkTencentMap() {
        return mTencentCarsMap != null;
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new CarPreviewRecycler(new CarPreviewRecycler.IClickListener() {
            @Override
            public void onClick(int itemPosition) {
                click(itemPosition);
            }
        }));
    }

    private void click(int position) {
        previewMapManager.setCarsType(Helper.carTypeFactory(position));
        if(lastLatLng != null){
            previewMapManager.getNearbyCars(lastLatLng);
        }else{
            model.postChangeLocationEvent();
        }
    }

}