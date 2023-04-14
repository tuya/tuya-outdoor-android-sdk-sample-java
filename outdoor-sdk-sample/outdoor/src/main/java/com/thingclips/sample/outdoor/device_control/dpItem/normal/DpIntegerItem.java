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
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.slider.Slider;
import com.thingclips.sample.outdoor.R;
import com.thingclips.sample.outdoor.utils.OutdoorUtils;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.android.device.bean.ValueSchemaBean;
import com.thingclips.smart.home.sdk.utils.SchemaMapper;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data point(DP) Integer type item
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 *
 * <p>
 * The current class is used to issue dp (Boolean) directives to a single device.
 * </p>
 */
@SuppressLint("ViewConstructor")
public class DpIntegerItem extends FrameLayout {
    private final String TAG = "MeshDpIntegerItem";


    public DpIntegerItem(Context context,
                         AttributeSet attrs,
                         int defStyle,
                         final SchemaBean schemaBean,
                         int value,
                         final IThingDevice device,
                         final DeviceBean deviceBean) {
        super(context, attrs, defStyle);

        inflate(context, R.layout.device_mgt_item_dp_integer, this);

        Slider slDp = findViewById(R.id.slDp);

        ValueSchemaBean valueSchemaBean = SchemaMapper.toValueSchema(schemaBean.property);
        double offset = 0;
        if (valueSchemaBean.min < 0) {
            offset = -valueSchemaBean.min;
        }
        double scale = Math.pow(10.0, valueSchemaBean.getScale());
        if (value > valueSchemaBean.max) {
            value = valueSchemaBean.max;
        }
        double curValue = (value + offset) / scale;
        double min = (valueSchemaBean.min + offset) / scale;
        double max = (valueSchemaBean.max + offset) / scale;
        slDp.setValue((float) curValue);

        slDp.setStepSize((float) ((double) valueSchemaBean.step / scale));
        slDp.setValueFrom((float) min);
        slDp.setValueTo((float) max);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(OutdoorUtils.getI18nDpString(deviceBean, schemaBean));

        Log.i("zongwu.lin", ">>>>>>"
                + "\nvalueSchemaBean.getScale():" + valueSchemaBean.getScale()
                + "\nvalueSchemaBean.step:" + valueSchemaBean.step
                + "\nvalueSchemaBean.min:" + valueSchemaBean.min
                + "\nvalueSchemaBean.max:" + valueSchemaBean.max
                + "\nvalue:" + value
                + "\ncurValue:" + curValue
                + "\nmin:" + min
                + "\nmax:" + max
                + "\nscale:" + scale
        );
        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            double finalOffset = offset;
            slDp.addOnChangeListener((slider, sValue, fromUser) -> {
                int integerValue = (int) (((sValue * scale) - finalOffset) / valueSchemaBean.step);
                OutdoorUtils.publishDp(deviceBean.devId, schemaBean.code, integerValue, device, new IResultCallback() {

                    @Override
                    public void onError(String code, String error) {

                    }

                    @Override
                    public void onSuccess() {

                    }
                });
            });
        } else {
            slDp.setVisibility(View.GONE);
            TextView tvDpValue = new TextView(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            layoutParams.rightMargin = 60;
            String text = OutdoorUtils.getScaledIntegerDpValue(deviceBean, schemaBean) + OutdoorUtils.getI18nDpUnit(deviceBean, schemaBean, "_unit");
            tvDpValue.setText(text);
            addView(tvDpValue, layoutParams);
        }
    }

    // $FF: synthetic method
    public DpIntegerItem(Context context, AttributeSet attrs, int defStyle, SchemaBean schemaBean, int value, IThingDevice device, int var7) {

        this(context, attrs, defStyle, schemaBean, value, device, null);
    }

    public DpIntegerItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull SchemaBean schemaBean, int value, @NotNull IThingDevice device) {
        this(context, attrs, 0, schemaBean, value, device, 4);
    }

    public DpIntegerItem(@NotNull Context context, @NotNull SchemaBean schemaBean, int value, @NotNull IThingDevice device, @NotNull DeviceBean deviceBean) {
        this(context, null, 0, schemaBean, value, device, deviceBean);
    }

}