package com.thingclips.sample.outdoor.device_control.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.thingclips.sample.outdoor.R;
import com.thingclips.sample.outdoor.utils.Constants;
import com.thingclips.sample.outdoor.utils.ToastUtils;
import com.thingclips.sdk.outdoor.ThingODSDK;
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.interior.device.bean.CommunicationEnum;
import com.thingclips.smart.outdoor.api.IODBtInductiveUnlock;
import com.thingclips.smart.outdoor.api.IODDevice;
import com.thingclips.smart.outdoor.api.IODHidInductiveUnlock;
import com.thingclips.smart.outdoor.api.IODInductiveUnlock;
import com.thingclips.smart.outdoor.api.IODInductiveUnlockManager;
import com.thingclips.smart.outdoor.enums.HidBindStatus;
import com.thingclips.smart.outdoor.enums.InductiveUnlockType;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.bean.DeviceBean;

/**
 * @author qinyun.miao
 */
public class DeviceRemoveActivity extends AppCompatActivity {
    private static final String TAG = "DeviceRemoveActivity";
    private ProgressDialog loadingDialog;
    private String deviceId;
    private DeviceBean deviceBean;
    private IODDevice mODDevice;
    private IODInductiveUnlockManager mInductiveUnlockManager;
    private IODInductiveUnlock mInductiveUnlock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_remove);
        initView();
        initData();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initData() {
        deviceId = getIntent().getStringExtra(Constants.INTENT_DEVICE_ID);
        deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(deviceId);
        if (deviceBean == null) {
            finish();
            return;
        }
        mODDevice = ThingODSDK.newODDeviceInstance(deviceId);
        mInductiveUnlockManager = ThingODSDK.getODInductiveUnlockManagerInstance();
        getInductiveUnlockType();
    }

    private void getInductiveUnlockType() {
        showLoading();
        mInductiveUnlockManager.getInductiveUnlockType(deviceId, new IThingResultCallback<InductiveUnlockType>() {
            @Override
            public void onSuccess(InductiveUnlockType result) {
                hideLoading();
                showInductiveUnlockType(result);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                L.d(TAG, "errorCode:" + errorCode + " errorMessage:" + errorMessage);
                hideLoading();
                ToastUtils.show(DeviceRemoveActivity.this, errorMessage);
                DeviceRemoveActivity.this.finish();
            }
        });
    }

    private void showInductiveUnlockType(InductiveUnlockType result) {
        if (result == InductiveUnlockType.BT) {
            mInductiveUnlock = mInductiveUnlockManager.newBtInductiveUnlockInstance(deviceId);
        } else if (result == InductiveUnlockType.BLE_HID) {
            mInductiveUnlock = mInductiveUnlockManager.newHidInductiveUnlockInstance(deviceId);
        }
        updateUI();
    }

    private void updateUI() {
        findViewById(R.id.root).setVisibility(View.VISIBLE);
        findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFactory();
            }
        });
        findViewById(R.id.btnRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDevice();
            }
        });
    }

    private void showLoading() {
        hideLoading();
        loadingDialog = ProgressDialog.show(
                this,
                "",
                "Loading",
                true
        );
    }

    private void hideLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private void resetFactory() {
        if (canRemoveDevice(deviceBean)) {
            showLoading();
            mODDevice.resetFactory(new IResultCallback() {
                @Override
                public void onError(String errorCode, String errorMsg) {
                    hideLoading();
                    ToastUtils.show(DeviceRemoveActivity.this, errorMsg);
                }

                @Override
                public void onSuccess() {
                    hideLoading();
                    finish();
                }
            });
        } else {
            showUnbindHidOrBtDeviceDialog();
        }
    }

    private void removeDevice() {
        if (canRemoveDevice(deviceBean)) {
            showLoading();
            mODDevice.removeDevice(new IResultCallback() {
                @Override
                public void onError(String errorCode, String errorMsg) {
                    hideLoading();
                    ToastUtils.show(DeviceRemoveActivity.this, errorMsg);
                }

                @Override
                public void onSuccess() {
                    hideLoading();
                    finish();
                }
            });
        } else {
            showUnbindHidOrBtDeviceDialog();
        }
    }

    private boolean canRemoveDevice(DeviceBean deviceBean) {
        //判断设备是否有配对
        boolean isBonded;
        if (mInductiveUnlock instanceof IODHidInductiveUnlock) {
            isBonded = ((IODHidInductiveUnlock) mInductiveUnlock).getHidBindStatus() == HidBindStatus.BIND;
        } else if (mInductiveUnlock instanceof IODBtInductiveUnlock) {
            isBonded = mInductiveUnlock.isInductiveUnlockTurnOn();
        } else {
            isBonded = false;
        }
        if (isBonded) {
            //返回在线状态
            return deviceBean.getCommunicationOnline(CommunicationEnum.BLE);
        }

        return true;
    }

    private void showUnbindHidOrBtDeviceDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_bonded_tip)
                .setMessage(R.string.remove_bonded_hint)
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
        if (mInductiveUnlockManager != null) {
            mInductiveUnlockManager.onDestroy();
        }
        if (mInductiveUnlock != null) {
            mInductiveUnlock.onDestroy();
        }
        if (mODDevice != null) {
            mODDevice.onDestroy();
        }
    }
}
