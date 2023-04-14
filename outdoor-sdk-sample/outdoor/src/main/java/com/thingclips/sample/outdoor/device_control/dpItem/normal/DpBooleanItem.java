/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NO
 */

package com.thingclips.sample.outdoor.device_control.dpItem.normal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.thingclips.sample.outdoor.R;
import com.thingclips.sample.outdoor.utils.OutdoorUtils;
import com.thingclips.sdk.ble.ThingBlePlugin;
import com.thingclips.sdk.core.PluginManager;
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.android.device.utils.ThingBleUtil;
import com.thingclips.smart.interior.api.IThingBlePlugin;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data point(DP) Boolean type item
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 *
 * <p>
 * The current class is used to issue dp (Boolean) directives to a single device.
 * Different constructors correspond to different types of action objects.
 * </p>
 */

@SuppressLint("ViewConstructor")
public class DpBooleanItem extends FrameLayout {

    private final String TAG = "MeshDpBooleanItem";

    private final String DP_CODE_AUTO_UNLOCK = "auto_unlock";

    /**
     * 当操作的是
     *
     * @param context
     * @param attrs
     * @param defStyle
     * @param schemaBean
     * @param value
     * @param device
     */
    @SuppressLint({"UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility"})
    public DpBooleanItem(Context context,
                         AttributeSet attrs,
                         int defStyle,
                         final SchemaBean schemaBean,
                         boolean value, final IThingDevice device, final DeviceBean deviceBean) {
        super(context, attrs, defStyle);

        inflate(context, R.layout.device_mgt_item_dp_boolean, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(OutdoorUtils.getI18nDpString(deviceBean, schemaBean));

        Switch swDp = findViewById(R.id.swDp);
        swDp.setChecked(value);

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            swDp.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    boolean booleanValue = !swDp.isChecked();
                    OutdoorUtils.publishDp(deviceBean.devId, schemaBean.code, booleanValue, device, new IResultCallback() {

                        @Override
                        public void onError(String code, String error) {

                        }

                        @Override
                        public void onSuccess() {
                            swDp.setChecked(booleanValue);
                        }
                    });
                }
                return true;
            });

            //hid bind status listener
            if (TextUtils.equals(schemaBean.code, DP_CODE_AUTO_UNLOCK)) {
                ThingBlePlugin thingBlePlugin = (ThingBlePlugin) PluginManager.service(IThingBlePlugin.class);
                thingBlePlugin.getThingBleOperator().addConnectHidListener(ThingBleUtil.convertMac(deviceBean.getMac()), new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        L.d(TAG, "addConnectHidListener onError");
                        //Failed to connect HID.User needs to manually enter the system settings for operation.
                    }

                    @Override
                    public void onSuccess() {
                        L.d(TAG, "addConnectHidListener onSuccess");
                        //Must unregister listener when not in use
                        thingBlePlugin.getThingBleOperator().unregisterBleConnectStatus(ThingBleUtil.convertMac(deviceBean.getMac()));
                    }
                });
            }
        } else {
            swDp.setVisibility(View.GONE);
            TextView tvDpValue = new TextView(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            layoutParams.rightMargin = 60;
            tvDpValue.setText(OutdoorUtils.getI18nDpUnit(deviceBean, schemaBean, value ? "_on" : "_off"));
            addView(tvDpValue, layoutParams);
        }
    }

    // $FF: synthetic method
    public DpBooleanItem(Context context,
                         AttributeSet attrs,
                         int defStyle,
                         SchemaBean schemaBean,
                         boolean value,
                         IThingDevice device,
                         int var7) {
        this(context, attrs, defStyle, schemaBean, value, device, null);
    }


    public DpBooleanItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull SchemaBean schemaBean, boolean value, @NotNull IThingDevice device) {
        this(context, attrs, 0, schemaBean, value, device, 4);
    }

    /**
     * This constructor is used when the operating device is a single point Bluetooth device
     *
     * @param context
     * @param schemaBean
     * @param value
     * @param device
     * @param deviceBean
     */
    public DpBooleanItem(Context context,
                         SchemaBean schemaBean,
                         boolean value,
                         IThingDevice device, DeviceBean deviceBean) {
        this(context, null, 0, schemaBean, value, device, deviceBean);
    }
}
