package com.map.mapmobility.driver;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.map.mapmobility.R;
import com.tencent.map.navi.car.CarNaviView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;

public class DriverTaskFragment extends Fragment
        implements DriverTaskContract.IView, View.OnClickListener {

    private DriverTaskContract.IPresenter mPresenter;

    private View mView;
    /** 地图*/
    private CarNaviView carNaviView;
    /** 开始路线规划*/
    private TextView tvSearchRoute;
    /** 开始模拟导航*/
    private TextView tvStartSimulateNavi;
    /** 开始导航*/
    private TextView tvStartNavi;
    /** 开始司乘*/
    private TextView tvDriverPassengerSyn;
    /** 抽屉控件*/
    private DrawerLayout drawerLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.driver_task_fragment, container,false);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        carNaviView = mView.findViewById(R.id.car_navi_view);
        // 初始化地图，记得生命周期的方法关联上地图
        mPresenter.initMap(carNaviView);

        tvSearchRoute = mView.findViewById(R.id.tv_start_search_route);
        tvSearchRoute.setOnClickListener(this);
        tvStartSimulateNavi = mView.findViewById(R.id.tv_start_simulate_navi);
        tvStartSimulateNavi.setOnClickListener(this);
        tvStartNavi = mView.findViewById(R.id.tv_start_navi);
        tvStartNavi.setOnClickListener(this);
        tvDriverPassengerSyn = mView.findViewById(R.id.tv_start_driver_passenger_syn);
        tvDriverPassengerSyn.setOnClickListener(this);
        drawerLayout = mView.findViewById(R.id.driver_task_fragment_drawer_layout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_start_search_route:
                // 开始路线规划
                mPresenter.searchRoute();
                break;
            case R.id.tv_start_simulate_navi:
                // 开始模拟导航，默认就选择第一条路线类
                mPresenter.startSimulateNavi(0);
                break;
            case R.id.tv_start_navi:
                // 开始导航，默认就选择第一条路线类
                mPresenter.startNavi(0);
                break;
            case R.id.tv_start_driver_passenger_syn:
                // 开始司乘同显
                mPresenter.startPassengerDriverSynchro();
                // 司乘开始同步
                mPresenter.isSyncEnable(true);
                break;
        }
    }

    @Override
    public Context getFragmentContext() {
        return getContext();
    }

    @Override
    public TencentMap getTencentMap() {
        return carNaviView.getMap();
    }

    @Override
    public void setPresenter(DriverTaskContract.IPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onStart() {
        if (carNaviView != null) {
            carNaviView.onStart();
        }
        super.onStart();
    }

    public void onRestart() {
        if (carNaviView != null) {
            carNaviView.onRestart();
        }
    }

    @Override
    public void onResume() {
        if (carNaviView != null) {
            carNaviView.onResume();
        }
        super.onResume();
        if(mPresenter != null){
            mPresenter.start();
        }
    }

    @Override
    public void onPause() {
        if (carNaviView != null) {
            carNaviView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (carNaviView != null) {
            carNaviView.onStop();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (carNaviView != null) {
            carNaviView.onDestroy();
        }
        super.onDestroy();
        if(mPresenter != null){
            mPresenter.destory();
        }
    }
}
