package com.map.mapmobility.mapconfig;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.map.mapmobility.R;
import com.tencent.map.navi.car.CarNaviView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;

public class MapTaskFragment extends Fragment implements MapTaskContract.IView {

    private MapTaskContract.IPresenter mPresenter;

    private View mView;
    /** 地图*/
    private CarNaviView carNaviView;
    /** 功能列表*/
    private ExpandableListView exView;
    /** 抽屉*/
    private DrawerLayout drawerLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.map_task_fragment, container,false);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        carNaviView = mView.findViewById(R.id.car_navi_view);
        exView = mView.findViewById(R.id.map_task_ex_view);
        drawerLayout = mView.findViewById(R.id.map_task_fragment_drawer_layout);
        // 初始化地图，记得生命周期的方法关联上地图
        if(mPresenter != null){
            mPresenter.start();
        }
        // 初始化recycler列表
        initExView();
    }

    @Override
    public Context getFragmentContext() {
        return mView.getContext();
    }

    @Override
    public TencentMap getTencentMap() {
        return carNaviView.getMap();
    }

    @Override
    public void setPresenter(MapTaskContract.IPresenter presenter) {
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

    private void initExView() {
        exView.setAdapter(new MapTaskExpandableAdapter());
        exView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                drawerLayout.closeDrawers();
                String tag = groupPosition+""+childPosition;
                if(tag == null){
                    Log.e("navi","map task onChildClick() tag is null");
                    return false;
                }
                //罗盘显示
                if("00".equals(tag))
                    mPresenter.enableCompass(true);
                //罗盘隐藏
                if("01".equals(tag))
                    mPresenter.enableCompass(false);
                //展示location source效果
                if("10".equals(tag))
                    mPresenter.enableAddMarkerWithLS(getTencentMap(), true);
                //异常location source效果
                if("11".equals(tag))
                    mPresenter.enableAddMarkerWithLS(getTencentMap(), false);
                return false;
            }
        });
    }
}
