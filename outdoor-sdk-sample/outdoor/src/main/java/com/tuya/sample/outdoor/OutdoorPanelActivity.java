package com.tuya.sample.outdoor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.sample.outdoor.device_control.activity.DeviceMgtControlActivity;
import com.tuya.sample.outdoor.network_api.activity.NetworkApiActivity;
import com.tuya.sample.outdoor.utils.Constants;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.bean.DeviceBean;

/**
 * outdoor entrance
 *
 * @author Nickey <a href="mailto:developer@tuya.com"/>
 * @since 2022/1/7 10:58
 */

public class OutdoorPanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdoor_panel);
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        String deviceId = getIntent().getStringExtra(Constants.INTENT_DEVICE_ID);
        TextView tvDeviceControl = findViewById(R.id.tvDeviceControl);
        TextView tvNetworkApi = findViewById(R.id.tvNetworkApi);
        tvDeviceControl.setOnClickListener(view -> goToActivity(deviceId, DeviceMgtControlActivity.class));
        tvNetworkApi.setOnClickListener(view -> goToActivity(deviceId, NetworkApiActivity.class));
    }

    private void goToActivity(String deviceId, Class<? extends Activity> activityClass) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId);
        if (deviceBean != null) {
            Intent intent = new Intent(this, activityClass);
            intent.putExtra(Constants.INTENT_DEVICE_ID, deviceId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "device not exist", Toast.LENGTH_SHORT).show();
        }
    }
}
