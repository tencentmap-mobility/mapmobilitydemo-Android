package com.map.mapmobility.homepage;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.map.mapmobility.BaseActivity;
import com.map.mapmobility.R;
import com.map.mapmobility.driver.DriverTaskActivity;
import com.map.mapmobility.passenger.PassengerTaskActivity;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 *  这是司乘同显demno的首页
 *  @author mingjiezuo
 */
public class HomePageTaskActivity extends BaseActivity
        implements EasyPermissions.PermissionCallbacks {

    /** 所要申请的权限*/
    String[] perms = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_task_activity);
        click();

        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this
                    , "必要的权限"
                    , 0
                    , perms);
        }
    }

    private void click(){
        /** 跳转乘客端界面*/
        findViewById(R.id.tv_intent_to_passenger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTaskActivity.this
                        , PassengerTaskActivity.class);
                startActivity(intent);
            }
        });
        /** 跳转司机端界面*/
        findViewById(R.id.tv_intent_to_driver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTaskActivity.this
                        , DriverTaskActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
