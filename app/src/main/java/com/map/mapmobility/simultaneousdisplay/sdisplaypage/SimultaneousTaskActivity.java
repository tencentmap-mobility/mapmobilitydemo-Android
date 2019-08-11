package com.map.mapmobility.simultaneousdisplay.sdisplaypage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.map.mapmobility.BaseActivity;
import com.map.mapmobility.R;
import com.map.mapmobility.simultaneousdisplay.driver.DriverTaskActivity;
import com.map.mapmobility.simultaneousdisplay.passenger.PassengerTaskActivity;

/**
 *  这是司乘同显功能
 *
 * @author mingjiezuo
 */
public class SimultaneousTaskActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simultaneous_task_activity);
        click();
    }

    private void click(){
        /** 跳转乘客端界面*/
        findViewById(R.id.tv_intent_to_passenger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SimultaneousTaskActivity.this
                        , PassengerTaskActivity.class);
                startActivity(intent);
            }
        });
        /** 跳转司机端界面*/
        findViewById(R.id.tv_intent_to_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SimultaneousTaskActivity.this
                        , DriverTaskActivity.class);
                startActivity(intent);
            }
        });
    }
}
