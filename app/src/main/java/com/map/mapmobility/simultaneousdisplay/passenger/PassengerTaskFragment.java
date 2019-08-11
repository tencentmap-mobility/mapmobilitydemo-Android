package com.map.mapmobility.simultaneousdisplay.passenger;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.map.mapmobility.R;
import com.map.mapmobility.utils.StringUtils;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;

public class PassengerTaskFragment extends Fragment
        implements PassengerTaskContract.IView, View.OnClickListener {

    private PassengerTaskContract.IPresenter mPresenter;

    private View mView;
    /** 地图*/
    private MapView mapView;
    /** 当前位置*/
    private ImageView imgCurrentPosition;
    /** 当前定位经纬度*/
    private TextView tvCurrentPoint;
    /** 当前地图中心点经纬度*/
    private TextView tvCurrentMapPoint;
    /** 地图中央显示的大头针*/
    private LinearLayout liMapCenterLayout;

    @Override
    public void setPresenter(PassengerTaskContract.IPresenter presenter) {
        mPresenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.passenger_task_fragment, container,false);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapView = mView.findViewById(R.id.passenger_task_fragment_map);
        mPresenter.initMap(mapView.getMap());
        // 当前位置
        imgCurrentPosition = mView.findViewById(R.id.img_location_to_origin);
        imgCurrentPosition.setOnClickListener(this);
        // 展示当前位置经纬度
        tvCurrentPoint = mView.findViewById(R.id.tv_current_point_content);
        // 展示地图中心位置经纬度
        tvCurrentMapPoint = mView.findViewById(R.id.tv_current_map_point_content);
        // 大头针布局
        liMapCenterLayout = mView.findViewById(R.id.li_map_center_marker);

        mPresenter.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mPresenter.destory();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_location_to_origin:
                // 当前位置
                mPresenter.toOriginCurrentLocation();
                break;
        }
    }

    @Override
    public Context getFragmentContext() {
        return getContext();
    }

    @Override
    public TencentMap getTencentMap() {
        return mapView.getMap();
    }

    @Override
    public void showMapCenterMarker(boolean isShow) {
        if(isShow){
            liMapCenterLayout.setVisibility(View.VISIBLE);
        }else{
            liMapCenterLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showCurrentPoint(String point) {
        if(StringUtils.isJudgeEmpty(point)){
            tvCurrentPoint.setText(point);
        }
    }

    @Override
    public void showCurrentMapPoint(String point) {
        if(StringUtils.isJudgeEmpty(point)){
            tvCurrentMapPoint.setText(point);
        }
    }
}
