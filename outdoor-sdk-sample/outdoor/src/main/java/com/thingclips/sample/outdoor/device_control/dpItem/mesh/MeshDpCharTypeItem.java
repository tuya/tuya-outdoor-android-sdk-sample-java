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

package com.thingclips.sample.outdoor.device_control.dpItem.mesh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.sample.outdoor.R;
import com.thingclips.smart.android.blemesh.api.IThingBlueMeshDevice;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IResultCallback;

import java.util.HashMap;

/**
 * Data point(DP) Char Type type item
 *
 * @author chanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 * The current class is used to issue dp (Boolean) directives to a mesh device or group.
 * </p>
 */

public class MeshDpCharTypeItem extends FrameLayout {
    private final String TAG = "MeshDpCharTypeItem";

    public MeshDpCharTypeItem(Context context,
                              AttributeSet attrs,
                              int defStyle,
                              final SchemaBean schemaBean,
                              String value,
                              String meshId,
                              boolean isGroup,
                              String localOrNodeId,
                              String pcc) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_dp_char_type, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(schemaBean.name);

        EditText etDp = findViewById(R.id.etDp);
        etDp.setText(value);

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            IThingBlueMeshDevice mThingSigMeshDevice= ThingHomeSdk.newSigMeshDeviceInstance(meshId);
            etDp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public final boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        HashMap map = new HashMap();

                        map.put(schemaBean.id, etDp.getText().toString());
                        if (isGroup) {
                            mThingSigMeshDevice.multicastDps(localOrNodeId, pcc, JSONObject.toJSONString(map), new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    Log.d(TAG, "send dps error:" + error);
                                }

                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "send dps success");
                                }
                            });
                        }else{
                            mThingSigMeshDevice.publishDps(localOrNodeId, pcc, JSONObject.toJSONString(map), new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    Log.d(TAG, "send dps error:" + error);
                                }

                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "send dps success");
                                }
                            });
                        }

                        return true;

                    }
                    return true;
                }

            });
        }

    }
}
