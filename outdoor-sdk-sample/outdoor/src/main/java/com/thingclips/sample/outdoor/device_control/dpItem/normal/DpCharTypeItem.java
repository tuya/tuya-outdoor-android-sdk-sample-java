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

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.thingclips.sample.outdoor.R;
import com.thingclips.sample.outdoor.utils.OutdoorUtils;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data point(DP) Char Type type item
 *
 * @author chanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 * The current class is used to issue dp (Boolean) directives to a single device.
 */

public class DpCharTypeItem extends FrameLayout {
    private final String TAG = "MeshDpCharTypeItem";

    public DpCharTypeItem(Context context,
                          AttributeSet attrs,
                          int defStyle,
                          final SchemaBean schemaBean,
                          String value,
                          final IThingDevice device, final DeviceBean deviceBean) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_dp_char_type, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(OutdoorUtils.getI18nDpString(deviceBean, schemaBean));

        EditText etDp = findViewById(R.id.etDp);
        etDp.setText(value);

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            etDp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public final boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String charValue = etDp.getText().toString();
                        OutdoorUtils.publishDp(deviceBean.devId, schemaBean.code, charValue, device, new IResultCallback() {

                            @Override
                            public void onError(String code, String error) {

                            }

                            @Override
                            public void onSuccess() {

                            }
                        });
                        return true;

                    }
                    return true;
                }

            });
        } else {
            etDp.setVisibility(View.GONE);
            TextView tvDpValue = new TextView(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            layoutParams.rightMargin = 60;
            tvDpValue.setText(String.valueOf(value));
            addView(tvDpValue, layoutParams);
        }

    }

    // $FF: synthetic method
    public DpCharTypeItem(Context context, AttributeSet attrs, int defStyle, SchemaBean schemaBean, String value, IThingDevice device, int var) {


        this(context, attrs, defStyle, schemaBean, value, device, null);
    }

    public DpCharTypeItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull SchemaBean schemaBean, @NotNull String value, @NotNull IThingDevice device) {
        this(context, attrs, 0, schemaBean, value, device, 4);
    }

    public DpCharTypeItem(@NotNull Context context, @NotNull SchemaBean schemaBean, @NotNull String value, @NotNull IThingDevice device, @NotNull DeviceBean deviceBean) {
        this(context, null, 0, schemaBean, value, device, deviceBean);
    }
}
