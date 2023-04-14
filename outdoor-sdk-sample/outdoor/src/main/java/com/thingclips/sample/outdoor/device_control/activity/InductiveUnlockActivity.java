package com.thingclips.sample.outdoor.device_control.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.thingclips.sample.outdoor.R;
import com.thingclips.sample.outdoor.utils.Constants;
import com.thingclips.sample.outdoor.utils.ToastUtils;
import com.thingclips.sdk.outdoor.ThingODSDK;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.interior.device.bean.CommunicationEnum;
import com.thingclips.smart.outdoor.api.IODBtInductiveUnlock;
import com.thingclips.smart.outdoor.api.IODHidInductiveUnlock;
import com.thingclips.smart.outdoor.api.IODInductiveUnlock;
import com.thingclips.smart.outdoor.api.IODInductiveUnlockManager;
import com.thingclips.smart.outdoor.api.callback.InductiveUnlockCallback;
import com.thingclips.smart.outdoor.enums.HidBindStatus;
import com.thingclips.smart.outdoor.enums.InductiveUnlockType;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.bean.DeviceBean;

/**
 * @author qinyun.miao
 */
public class InductiveUnlockActivity extends AppCompatActivity {
    private static final String TAG = "InductiveUnlockActivity";

    private ProgressDialog loadingDialog;

    private IODInductiveUnlockManager inductiveUnlockManager;
    private IODBtInductiveUnlock btInductiveUnlock;
    private IODHidInductiveUnlock hidInductiveUnlock;

    private String deviceId;
    private InductiveUnlockType inductiveUnlockType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inductive_unlock);
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
        if (!checkBLEOnline()) {
            showToast(getString(R.string.bluetooth_offline));
            finish();
            return;
        }
        inductiveUnlockManager = ThingODSDK.getODInductiveUnlockManagerInstance();
        getInductiveUnlockType();
    }

    private boolean checkBLEOnline() {
        DeviceBean deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(deviceId);
        if (deviceBean != null) {
            return deviceBean.getCommunicationOnline(CommunicationEnum.BLE);
        }
        return false;
    }

    private void getInductiveUnlockType() {
        inductiveUnlockManager.getInductiveUnlockType(deviceId, new IThingResultCallback<InductiveUnlockType>() {
            @Override
            public void onSuccess(InductiveUnlockType result) {
                showInductiveUnlockType(result);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showError(errorCode, errorMessage);
                finish();
            }
        });
    }

    private void showInductiveUnlockType(InductiveUnlockType type) {
        if (type == InductiveUnlockType.NONE) {
            showToast(getString(R.string.not_support_inductive_unlock));
            finish();
            return;
        }
        inductiveUnlockType = type;
        ViewGroup root = findViewById(R.id.root);
        if (inductiveUnlockType == InductiveUnlockType.BT) {
            btInductiveUnlock = inductiveUnlockManager.newBtInductiveUnlockInstance(deviceId);

            getLayoutInflater().inflate(R.layout.layout_inductive_unlock_bt, root, true);
            SeekBar seekBarBtDistance = findViewById(R.id.seekbar_bt_distance);
            seekBarBtDistance.setMax(4);
            seekBarBtDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    setBTInductiveUnlockDistance(seekBar, seekBar.getProgress() + 1);
                }
            });
        } else if (inductiveUnlockType == InductiveUnlockType.BLE_HID) {
            hidInductiveUnlock = inductiveUnlockManager.newHidInductiveUnlockInstance(deviceId);

            getLayoutInflater().inflate(R.layout.layout_inductive_unlock_hid, root, true);
            findViewById(R.id.btn_record_fortify_distance).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recordFortifyDistance();
                }
            });
            findViewById(R.id.btn_record_disarm_distance).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recordDisarmDistance();
                }
            });
        }
        updateStatus();
    }

    private void updateStatus() {
        TextView tvInductiveUnlockType = findViewById(R.id.tv_inductive_unlock_type);
        tvInductiveUnlockType.setText(inductiveUnlockType.toString());
        if (inductiveUnlockType == InductiveUnlockType.BLE_HID) {
            HidBindStatus hidBind = hidInductiveUnlock.getHidBindStatus();
            TextView tvHidBind = findViewById(R.id.tv_hid_bind);
            tvHidBind.setText(String.valueOf(hidBind));
        }

        boolean isTurnOn = getInductiveUnlock().isInductiveUnlockTurnOn();
        SwitchCompat inductiveUnlockSwitch = findViewById(R.id.switch_inductive_unlock);
        inductiveUnlockSwitch.setChecked(isTurnOn);
        inductiveUnlockSwitch.setEnabled(true);
        inductiveUnlockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!checkBLEOnline()) {
                    showToast(getString(R.string.bluetooth_offline));
                    return;
                }
                if (isChecked) {
                    turnOnInductiveUnlock();
                } else {
                    turnOffInductiveUnlock();
                }
            }
        });

        if (btInductiveUnlock != null) {
            Integer inductiveUnlockDistance = btInductiveUnlock.getInductiveUnlockDistance();
            View layoutBtDistance = findViewById(R.id.layout_bt_distance);
            if (inductiveUnlockDistance != null) {
                layoutBtDistance.setVisibility(View.VISIBLE);
                SeekBar seekBarBtDistance = findViewById(R.id.seekbar_bt_distance);
                seekBarBtDistance.setProgress(inductiveUnlockDistance - 1);
            } else {
                layoutBtDistance.setVisibility(View.GONE);
            }
        }
    }

    private IODInductiveUnlock getInductiveUnlock() {
        return btInductiveUnlock != null ? btInductiveUnlock : hidInductiveUnlock;
    }

    private void turnOnInductiveUnlock() {
        showLoading();
        getInductiveUnlock().turnOnInductiveUnlock(new InductiveUnlockCallback() {
            @Override
            public boolean interceptBluetoothPairing(String bluetoothName) {
                boolean smartisan = "smartisan".equalsIgnoreCase(Build.BRAND);
                if (smartisan) {
                    manualPairing(bluetoothName);
                    return true;
                }
                return super.interceptBluetoothPairing(bluetoothName);
            }

            @Override
            public void onError(String code, String error) {
                hideLoading();
                showError(code, error);
                updateStatus();
            }

            @Override
            public void onSuccess() {
                hideLoading();
                updateStatus();
            }
        });
    }

    private void manualPairing(String bluetoothName) {
        new AlertDialog.Builder(this)
                .setMessage(String.format(getString(R.string.bt_unlock_manual_pair), bluetoothName))
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                    }
                })
                .show();
    }

    private void turnOffInductiveUnlock() {
        showLoading();
        getInductiveUnlock().turnOffInductiveUnlock(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                hideLoading();
                showError(code, error);
                updateStatus();
            }

            @Override
            public void onSuccess() {
                hideLoading();
                updateStatus();
            }
        });
    }

    private void showToast(String message) {
        ToastUtils.show(this, message);
    }

    private void showError(String errorCode, String errorMessage) {
        Log.d(TAG, "errorCode:" + errorCode + " errorMessage:" + errorMessage);
        ToastUtils.show(this, errorMessage);
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

    private void setBTInductiveUnlockDistance(SeekBar seekBar, int newValue) {
        Integer oldValue = btInductiveUnlock.getInductiveUnlockDistance();
        if (oldValue == null) {
            return;
        }
        btInductiveUnlock.setInductiveUnlockDistance(newValue, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                showError(code, error);
                seekBar.setProgress(oldValue - 1);
            }

            @Override
            public void onSuccess() {
                seekBar.setProgress(newValue - 1);
            }
        });
    }

    private void recordFortifyDistance() {
        hidInductiveUnlock.recordFortifyDistance(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                showError(code, error);
            }

            @Override
            public void onSuccess() {
                showToast(getString(R.string.publish_record_fortify_distance_success));
            }
        });
    }

    private void recordDisarmDistance() {
        hidInductiveUnlock.recordDisarmDistance(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                showError(code, error);
            }

            @Override
            public void onSuccess() {
                showToast(getString(R.string.publish_record_disarm_distance_success));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
        if (inductiveUnlockManager != null) {
            inductiveUnlockManager.onDestroy();
        }
        if (btInductiveUnlock != null) {
            btInductiveUnlock.onDestroy();
        }
        if (hidInductiveUnlock != null) {
            hidInductiveUnlock.onDestroy();
        }
    }
}
