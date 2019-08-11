package com.map.mapmobility.mobilitypage;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.map.mapmobility.BaseActivity;
import com.map.mapmobility.R;
import com.map.mapmobility.mapconfig.MapTaskActivity;
import com.map.mapmobility.simultaneousdisplay.sdisplaypage.SimultaneousTaskActivity;
import com.map.mapmobility.utils.ToastUtils;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 *  这是腾讯出行sdk demo的首页
 *
 *  @author mingjiezuo
 */
public class MobilityTaskActivity extends BaseActivity
        implements EasyPermissions.PermissionCallbacks {

    /** 地图helper*/
    static final int MAP_HELPER = 0;
    /** 司乘同显*/
    static final int S_DISPLAY = 1;
    /** 周边车辆展示*/
    static final int CAR_PREVIEW = 2;
    /** 推荐上车点*/
    static final int CAR_POI = 3;

    /** 所要申请的权限*/
    String[] perms = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobility_page_task_activity);

        init();

        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this
                    , "必要的权限"
                    , 0
                    , perms);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ToastUtils.INSTANCE().init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastUtils.INSTANCE().destory();
    }

    private void init() {
        recyclerView = findViewById(R.id.mobility_recycler_view);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new MobilityPageRecyclerView(new MobilityPageRecyclerView.IClickListener() {
            @Override
            public void onClick(int itemPosition) {
                // 点击的item
                click(itemPosition);
            }
        }));

    }

    private void click(int position) {
        switch (position){
            case MAP_HELPER:
                // 地图helper
                toIntent(MapTaskActivity.class);
                break;
            case S_DISPLAY:
                // 司乘同显
                toIntent(SimultaneousTaskActivity.class);
                break;
            case CAR_PREVIEW:
                // 周边车辆
                ToastUtils.INSTANCE().Toast("功能待添加");
                break;
            case CAR_POI:
                // 推荐上车点
                ToastUtils.INSTANCE().Toast("功能待添加");
                break;
        }
    }

    private void toIntent(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
