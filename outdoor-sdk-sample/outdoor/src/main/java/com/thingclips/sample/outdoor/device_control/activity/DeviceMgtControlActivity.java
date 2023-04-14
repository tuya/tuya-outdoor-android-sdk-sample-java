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

package com.thingclips.sample.outdoor.device_control.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.thingclips.sample.outdoor.R;
import com.thingclips.sample.outdoor.device_control.dpItem.DpFaultItem;
import com.thingclips.sample.outdoor.device_control.dpItem.mesh.MeshDpBooleanItem;
import com.thingclips.sample.outdoor.device_control.dpItem.mesh.MeshDpCharTypeItem;
import com.thingclips.sample.outdoor.device_control.dpItem.mesh.MeshDpEnumItem;
import com.thingclips.sample.outdoor.device_control.dpItem.mesh.MeshDpIntegerItem;
import com.thingclips.sample.outdoor.device_control.dpItem.normal.DpBooleanItem;
import com.thingclips.sample.outdoor.device_control.dpItem.normal.DpCharTypeItem;
import com.thingclips.sample.outdoor.device_control.dpItem.normal.DpEnumItem;
import com.thingclips.sample.outdoor.device_control.dpItem.normal.DpIntegerItem;
import com.thingclips.sample.outdoor.device_control.dpItem.normal.DpRawTypeItem;
import com.thingclips.sample.outdoor.utils.Constants;
import com.thingclips.sample.outdoor.utils.OutdoorUtils;
import com.thingclips.smart.android.device.bean.BitmapSchemaBean;
import com.thingclips.smart.android.device.bean.BoolSchemaBean;
import com.thingclips.smart.android.device.bean.EnumSchemaBean;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.android.device.bean.StringSchemaBean;
import com.thingclips.smart.android.device.bean.ValueSchemaBean;
import com.thingclips.smart.android.device.enums.DataTypeEnum;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;

import java.util.Collection;
import java.util.Map;

/**
 * Device control sample
 *
 * @author chanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/20 10:30 AM
 */

public class DeviceMgtControlActivity extends AppCompatActivity {
    private final String TAG = "DeviceMgtControlActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        LinearLayout llDp = findViewById(R.id.llDp);
        String deviceId = getIntent().getStringExtra(Constants.INTENT_DEVICE_ID);

        IThingDevice mDevice = ThingHomeSdk.newDeviceInstance(deviceId);
        DeviceBean deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(deviceId);

        TextView tvDeviceName = findViewById(R.id.tvDeviceName);
        tvDeviceName.setText(deviceBean.getName());


        Map<String, SchemaBean> map = ThingHomeSdk.getDataInstance().getSchema(deviceId);
        Collection<SchemaBean> schemaBeans = map.values();

        for (SchemaBean bean : schemaBeans) {

            Object value = deviceBean.getDps().get(bean.getId());

            if (bean.type.equals(DataTypeEnum.OBJ.getType())) {
                // obj
                switch (bean.getSchemaType()) {
                    case BoolSchemaBean.type:
                        boolean booleanValue = OutdoorUtils.getDpValueByDpCode(deviceId, bean.code, Boolean.class);
//                        boolean booleanValue = (boolean) value;
                        if (deviceBean.isSigMesh()) {
                            MeshDpBooleanItem dpBooleanItem = new MeshDpBooleanItem(
                                    this, null, 0, bean, booleanValue,
                                    deviceBean.getMeshId(),
                                    false,
                                    deviceBean.getNodeId(),
                                    deviceBean.getCategory());
                            llDp.addView(dpBooleanItem);
                        } else {
                            DpBooleanItem dpBooleanItem = new DpBooleanItem(
                                    this,
                                    bean,
                                    booleanValue,
                                    mDevice,
                                    deviceBean);
                            llDp.addView(dpBooleanItem);
                        }
                        break;

                    case EnumSchemaBean.type:
                        String enumValue = OutdoorUtils.getDpValueByDpCode(deviceId, bean.code, String.class);
//                        String enumValue = value.toString();
                        if (deviceBean.isSigMesh()) {
                            MeshDpEnumItem dpEnumItem = new MeshDpEnumItem(
                                    this, null, 0, bean, enumValue,
                                    deviceBean.getMeshId(),
                                    false,
                                    deviceBean.getNodeId(),
                                    deviceBean.getCategory());
                            llDp.addView(dpEnumItem);
                        } else {
                            DpEnumItem dpEnumItem = new DpEnumItem(
                                    this,
                                    bean,
                                    enumValue,
                                    mDevice,
                                    deviceBean);
                            llDp.addView(dpEnumItem);
                        }
                        break;

                    case StringSchemaBean.type:
                        String charValue = OutdoorUtils.getDpValueByDpCode(deviceId, bean.code, String.class);
//                        String charValue = value.toString();
                        if (deviceBean.isSigMesh()) {
                            MeshDpCharTypeItem dpCharTypeItem = new MeshDpCharTypeItem(
                                    this, null, 0, bean, charValue,
                                    deviceBean.getMeshId(),
                                    false,
                                    deviceBean.getNodeId(),
                                    deviceBean.getCategory());
                            llDp.addView(dpCharTypeItem);
                        } else {
                            DpCharTypeItem dpCharTypeItem = new DpCharTypeItem(
                                    this,
                                    bean,
                                    charValue,
                                    mDevice,
                                    deviceBean);
                            llDp.addView(dpCharTypeItem);
                        }
                        break;

                    case ValueSchemaBean.type:
                        int integerValue = OutdoorUtils.getDpValueByDpCode(deviceId, bean.code, Integer.class);
//                        int integerValue = (int) value;
                        if (deviceBean.isSigMesh()) {
                            MeshDpIntegerItem dpIntegerItem = new MeshDpIntegerItem(
                                    this, null, 0, bean, integerValue,
                                    deviceBean.getMeshId(),
                                    false,
                                    deviceBean.getNodeId(),
                                    deviceBean.getCategory());
                            llDp.addView(dpIntegerItem);
                        } else {
                            DpIntegerItem dpIntegerItem = new DpIntegerItem(
                                    this,
                                    bean,
                                    integerValue,
                                    mDevice,
                                    deviceBean);
                            llDp.addView(dpIntegerItem);
                        }

                        break;

                    case BitmapSchemaBean.type:
                        DpFaultItem dpFaultItem = new DpFaultItem(
                                this,
                                bean,
                                value.toString(),
                                deviceBean);
                        llDp.addView(dpFaultItem);
                        break;
                    default:
                        break;
                }

            } else if (bean.type.equals(DataTypeEnum.RAW.getType())) {
                // raw | file
                if (value == null) {
                    value = "null";
                }
                DpRawTypeItem dpRawTypeItem = new DpRawTypeItem(
                        this,
                        bean,
                        value.toString(),
                        mDevice,
                        deviceBean);
                llDp.addView(dpRawTypeItem);

            }
        }
    }
}

