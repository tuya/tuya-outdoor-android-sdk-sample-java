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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.thingclips.sample.outdoor.R;
import com.thingclips.sample.outdoor.utils.OutdoorUtils;
import com.thingclips.smart.android.device.bean.EnumSchemaBean;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.home.sdk.utils.SchemaMapper;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Data point(DP) Enum type item
 *
 * @author chaunfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 * <p>
 * The current class is used to issue dp (Boolean) directives to a single device .
 * </p>
 */
public class DpEnumItem extends FrameLayout {

    private final String TAG = "MeshDpBooleanItem";


    public DpEnumItem(Context context,
                      AttributeSet attrs,
                      int defStyle,
                      SchemaBean schemaBean,
                      String value,
                      IThingDevice device,
                      DeviceBean deviceBean) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_dp_enum, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(OutdoorUtils.getI18nDpString(deviceBean, schemaBean));

        Button btnDp = findViewById(R.id.btnDp);
        btnDp.setText(value);

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            ListPopupWindow listPopupWindow = new ListPopupWindow(context, null, R.attr.listPopupWindowStyle);
            listPopupWindow.setAnchorView(btnDp);

            EnumSchemaBean enumSchemaBean = SchemaMapper.toEnumSchema(schemaBean.property);
            Set set = enumSchemaBean.range;
            List items = Arrays.asList(set.toArray());
            ArrayAdapter adapter = new ArrayAdapter(context, R.layout.device_mgt_item_dp_enum_popup_item, items);
            listPopupWindow.setAdapter(adapter);
            listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
                Object enumDpData = items.get(position);
                OutdoorUtils.publishDp(deviceBean.devId, schemaBean.code, enumDpData, device, new IResultCallback() {

                    @Override
                    public void onError(String code, String error) {

                    }

                    @Override
                    public void onSuccess() {
                        btnDp.setText((CharSequence) items.get(position));
                    }
                });
                listPopupWindow.dismiss();


            });
            btnDp.setOnClickListener(v -> {
                listPopupWindow.show();
            });
        } else {
            btnDp.setVisibility(View.GONE);
            TextView tvDpValue = new TextView(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            layoutParams.rightMargin = 60;
            tvDpValue.setText(String.valueOf(value));
            addView(tvDpValue, layoutParams);
        }
    }

    // $FF: synthetic method
    public DpEnumItem(Context context, AttributeSet attrs, int defStyle, SchemaBean schemaBean, String value, IThingDevice device, int var7) {

        this(context, attrs, defStyle, schemaBean, value, device, null);
    }


    public DpEnumItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull SchemaBean schemaBean, @NotNull String value, @NotNull IThingDevice device) {
        this(context, attrs, 0, schemaBean, value, device, 4);
    }


    public DpEnumItem(@NotNull Context context, @NotNull SchemaBean schemaBean, @NotNull String value, @NotNull IThingDevice device, @NotNull DeviceBean deviceBean) {
        this(context, null, 0, schemaBean, value, device, deviceBean);
    }
}
