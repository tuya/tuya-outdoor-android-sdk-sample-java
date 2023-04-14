package com.thingclips.sample.outdoor.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.thingclips.sample.outdoor.OutdoorPanelActivity;
import com.thingclips.sdk.outdoor.ThingODSDK;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.android.device.bean.ValueSchemaBean;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.home.sdk.utils.SchemaMapper;
import com.thingclips.smart.outdoor.bean.DeviceHardwareInfo;
import com.thingclips.smart.outdoor.bean.ProductMap;
import com.thingclips.smart.outdoor.bean.req.ReportLocationReq;
import com.thingclips.smart.outdoor.bean.req.StoreGetReq;
import com.thingclips.smart.outdoor.bean.req.StoreSearchReq;
import com.thingclips.smart.outdoor.bean.req.TrackSegmentReq;
import com.thingclips.smart.outdoor.bean.req.TrackStatisticReq;
import com.thingclips.smart.outdoor.bean.resp.StoreInfo;
import com.thingclips.smart.outdoor.bean.resp.TrackSegmentInfo;
import com.thingclips.smart.outdoor.bean.resp.TrackStatisticInfo;
import com.thingclips.smart.panel.i18n.api.model.I18nRequestParam;
import com.thingclips.smart.panel.i18n.sdk.ThingPanelI18nEntrance;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * TODO feature
 *
 * @author Nickey <a href="mailto:developer@tuya.com"/>
 * @since 2022/1/7 14:29
 */

public class OutdoorUtils {

    public static boolean outdoorProcess(Context context, String deviceId) {
        String outdoorProductType = "phc,ddc,ddzxc,ddly,hbc,znddc";
        String[] types = outdoorProductType.split(",");
        if (!TextUtils.isEmpty(deviceId)) {
            DeviceBean deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(deviceId);
            if (deviceBean != null) {
                String category = deviceBean.getProductBean().getCategory();
                if (!TextUtils.isEmpty(category)) {
                    for (String type : types) {
                        if (type.equals(category)) {
                            Intent intent = new Intent(context, OutdoorPanelActivity.class);
                            intent.putExtra(Constants.INTENT_DEVICE_ID, deviceId);
                            context.startActivity(intent);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String getI18nDpString(DeviceBean deviceBean, SchemaBean schemaBean) {
        String defaultText = "";
        if (!TextUtils.isEmpty(schemaBean.name)) {
            defaultText = schemaBean.name;
        }
        return getI18n(deviceBean, schemaBean, defaultText, "");
    }

    public static String getI18nDpUnit(DeviceBean deviceBean, SchemaBean schemaBean, String dpUnitText) {
        String defaultText = "";
        if (!TextUtils.isEmpty(schemaBean.property)) {
            ValueSchemaBean valueSchemaBean = SchemaMapper.toValueSchema(schemaBean.property);
            if (valueSchemaBean != null && !TextUtils.isEmpty(valueSchemaBean.getUnit())) {
                defaultText = valueSchemaBean.getUnit();
            }
        }
        return getI18n(deviceBean, schemaBean, defaultText, dpUnitText);
    }

    public static String getScaledIntegerDpValue(DeviceBean deviceBean, SchemaBean schemaBean) {
        ValueSchemaBean valueSchemaBean = SchemaMapper.toValueSchema(schemaBean.property);
        int dpValue = getDpValueByDpCode(deviceBean.devId, schemaBean.code, Integer.class);
        if (dpValue != 0) {
            return new BigDecimal(dpValue / Math.pow(10, valueSchemaBean.scale)).setScale(1, RoundingMode.HALF_DOWN).toString();
        }
        return String.valueOf(dpValue);
    }


    public static String DP_CODE_LANGUAGE = "1.0.0";

    private static String getI18n(DeviceBean deviceBean, SchemaBean schemaBean, String defaultText, String dpUnitText) {
        final String[] dpString = {defaultText};


        Map<String, Map<String, Object>> i18nMap = ThingPanelI18nEntrance.getInstance().getI18nMap(new I18nRequestParam(deviceBean.productId,DP_CODE_LANGUAGE, deviceBean.i18nTime));
        if (i18nMap.isEmpty()) {
            ThingPanelI18nEntrance.getInstance().updateI18n(new I18nRequestParam(deviceBean.productId, DP_CODE_LANGUAGE, deviceBean.i18nTime), new IThingResultCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                    Map<String, Map<String, Object>> i18nMapNew = ThingPanelI18nEntrance.getInstance().getI18nMap(new I18nRequestParam(deviceBean.productId, DP_CODE_LANGUAGE,0));
                    String language = Locale.getDefault().getLanguage();
                    if (!i18nMapNew.containsKey(language)) {
                        language = "en";
                    }
                    Map<String, Object> dpLanguageMap = i18nMapNew.get(language);
                    String sb = "dp_" + schemaBean.code + dpUnitText;
                    if (dpLanguageMap != null) {
                        Object object = dpLanguageMap.get(sb);
                        if (object instanceof String) {
                            String dpText = (String) object;
                            if (!TextUtils.isEmpty(dpText)) {
                                dpString[0] = dpText;
                            }
                        }
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                }
            });
        } else {
            String language = Locale.getDefault().getLanguage();
            if (!i18nMap.containsKey(language)) {
                language = "en";
            }
            Map<String, Object> dpLanguageMap = i18nMap.get(language);
            String sb = "dp_" + schemaBean.code + dpUnitText;
            if (dpLanguageMap != null) {
                Object object = dpLanguageMap.get(sb);
                if (object instanceof String) {
                    String dpText = (String) object;
                    if (!TextUtils.isEmpty(dpText)) {
                        dpString[0] = dpText;
                    }
                }
            }
        }
        return dpString[0];
    }

    public static <T> T getDpValueByDpCode(String deviceId, String dpCode, Class<T> t) {
        return ThingODSDK.getIODDeviceManagerInstance().getValueByDPCode(deviceId, dpCode, t);
    }

    public static void publishDp(String deviceId, String dpCode, Object value, IThingDevice device, IResultCallback callback) {
        ThingODSDK.getIODDeviceManagerInstance().publishDP(deviceId, dpCode, value, device, callback);
    }

    public static boolean isDpCodeSupport(String deviceId, String dpCode) {
        return ThingODSDK.getIODDeviceManagerInstance().isDPCodeSupport(deviceId, dpCode);
    }

    public static SchemaBean getSchemaByDpCode(String deviceId, String dpCode) {
        return ThingODSDK.getIODDeviceManagerInstance().getSchemaByDPCode(deviceId, dpCode);
    }

    public static void getDeviceHardwareInfo(String deviceId, IThingResultCallback<DeviceHardwareInfo> callback) {
        ThingODSDK.getIODDeviceInfoInstance().getDeviceHardwareInfo(deviceId, callback);
    }

    public static void getDeviceIcon(String deviceId, IThingResultCallback<ProductMap> callback) {
        ThingODSDK.getIODDeviceInfoInstance().getDeviceIcon(deviceId, callback);
    }

    public static void getTrackSegment(TrackSegmentReq trackSegmentReq, IThingResultCallback<TrackSegmentInfo> callback) {
        ThingODSDK.getIODTrackInstance().getTrackSegment(trackSegmentReq, callback);
    }

    public static void getTrackStatistic(TrackStatisticReq trackStatisticReq, IThingResultCallback<TrackStatisticInfo> callback) {
        ThingODSDK.getIODTrackInstance().getTrackStatistic(trackStatisticReq, callback);
    }

    public static void reportLocation(ReportLocationReq reportLocationReq, IThingResultCallback<Boolean> callback) {
        ThingODSDK.getIODTrackInstance().reportLocation(reportLocationReq, callback);
    }

    public static void getStoreList(StoreGetReq storeGetReq, IThingResultCallback<List<StoreInfo>> callback) {
        ThingODSDK.getIODStoreInstance().getStoreList(storeGetReq, callback);
    }

    public static void searchStoreList(StoreSearchReq storeSearchReq, IThingResultCallback<List<StoreInfo>> callback) {
        ThingODSDK.getIODStoreInstance().searchStoreList(storeSearchReq, callback);
    }
}
