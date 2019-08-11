package com.map.mapmobility.simultaneousdisplay.passenger;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import com.map.mapmobility.BaseActivity;
import com.map.mapmobility.R;

/**
 *  这是乘客端
 * @author mingjiezuo
 */
public class PassengerTaskActivity extends BaseActivity {

    PassengerTaskFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_task_activity);

        FragmentManager manager = getSupportFragmentManager();
        fragment = (PassengerTaskFragment)manager.findFragmentById(R.id.any_task_frame_layout);
        if(fragment == null){
            fragment = new PassengerTaskFragment();
            manager.beginTransaction()
                    .add(R.id.any_task_frame_layout, fragment)
                    .commit();
        }
        new PassengerTaskPresenter(fragment);
    }
}
