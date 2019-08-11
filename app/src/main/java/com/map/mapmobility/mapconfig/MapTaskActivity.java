package com.map.mapmobility.mapconfig;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import com.map.mapmobility.BaseActivity;
import com.map.mapmobility.R;

/**
 *  这是一个包含操作地图的方法配置类
 *
 * @author mingjiezuo
 */
public class MapTaskActivity extends BaseActivity {

    MapTaskFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_task_activity);

        FragmentManager manager = getSupportFragmentManager();
        fragment = (MapTaskFragment)manager.findFragmentById(R.id.any_task_frame_layout);
        if(fragment == null){
            fragment = new MapTaskFragment();
            manager.beginTransaction()
                    .add(R.id.any_task_frame_layout, fragment)
                    .commit();
        }
        new MapTaskPresenter(fragment);
    }

    @Override
    protected void onRestart() {
        if(fragment != null){
            fragment.onRestart();
        }
        super.onRestart();
    }
}

