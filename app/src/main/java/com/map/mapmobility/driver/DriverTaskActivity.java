package com.map.mapmobility.driver;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentManager;

import com.map.mapmobility.BaseActivity;
import com.map.mapmobility.R;

/**
 *  这是司机端demo
 *  @author mingjiezuo
 */
public class DriverTaskActivity extends BaseActivity {

    DriverTaskFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_task_activity);

        FragmentManager manager = getSupportFragmentManager();
        fragment = (DriverTaskFragment)manager.findFragmentById(R.id.any_task_frame_layout);
        if(fragment == null){
            fragment = new DriverTaskFragment();
            manager.beginTransaction()
                    .add(R.id.any_task_frame_layout, fragment)
                    .commit();
        }
        new DriverTaskPresenter(fragment);
    }

    @Override
    protected void onRestart() {
        if(fragment != null){
            fragment.onRestart();
        }
        super.onRestart();
    }
}
