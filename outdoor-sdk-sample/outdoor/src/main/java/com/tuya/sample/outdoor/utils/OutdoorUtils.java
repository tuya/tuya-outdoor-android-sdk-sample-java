package com.tuya.sample.outdoor.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tuya.sample.outdoor.OutdoorPanelActivity;
import com.tuya.sdk.outdoor.TuyaODSDK;
import com.tuya.smart.android.device.bean.SchemaBean;
import com.tuya.smart.android.device.bean.ValueSchemaBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.home.sdk.utils.SchemaMapper;
import com.tuya.smart.outdoor.bean.DeviceHardwareInfo;
import com.tuya.smart.outdoor.bean.ProductMap;
import com.tuya.smart.outdoor.bean.req.ReportLocationReq;
import com.tuya.smart.outdoor.bean.req.StoreGetReq;
import com.tuya.smart.outdoor.bean.req.StoreSearchReq;
import com.tuya.smart.outdoor.bean.req.TrackSegmentReq;
import com.tuya.smart.outdoor.bean.req.TrackStatisticReq;
import com.tuya.smart.outdoor.bean.resp.StoreInfo;
import com.tuya.smart.outdoor.bean.resp.TrackSegmentInfo;
import com.tuya.smart.outdoor.bean.resp.TrackStatisticInfo;
import com.tuya.smart.panel.i18n.api.model.I18nRequestParam;
import com.tuya.smart.panel.i18n.sdk.TuyaPanelI18nEntrance;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;

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
            DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId);
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

    private static String getI18n(DeviceBean deviceBean, SchemaBean schemaBean, String defaultText, String dpUnitText) {
        final String[] dpString = {defaultText};
        Map<String, Map<String, Object>> i18nMap = TuyaPanelI18nEntrance.getInstance().getI18nMap(new I18nRequestParam(deviceBean.productId, deviceBean.i18nTime));
        if (i18nMap.isEmpty()) {
            TuyaPanelI18nEntrance.getInstance().updateI18n(new I18nRequestParam(deviceBean.productId, deviceBean.i18nTime), new ITuyaResultCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                    Map<String, Map<String, Object>> i18nMap1 = TuyaPanelI18nEntrance.getInstance().getI18nMap(new I18nRequestParam(deviceBean.productId, 0L));
                    String language = Locale.getDefault().getLanguage();
                    if (!i18nMap.containsKey(language)) {
                        language = "en";
                    }
                    Map<String, Object> dpLanguageMap = i18nMap1.get(language);
                    String sb = "dp_" + schemaBean.code + dpUnitText;
                    Object object = dpLanguageMap.get(sb);
                    if (object instanceof String) {
                        if (!TextUtils.isEmpty((String) object)) {
                            dpString[0] = (String) object;
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
            Object object = dpLanguageMap.get(sb);
            if (object instanceof String) {
                dpString[0] = (String) object;
            }
        }
        return dpString[0];
    }

    public static <T> T getDpValueByDpCode(String deviceId, String dpCode, Class<T> t) {
        return TuyaODSDK.getIODDeviceManagerInstance().getValueByDPCode(deviceId, dpCode, t);
    }

    public static void publishDp(String deviceId, String dpCode, Object value, ITuyaDevice device, IResultCallback callback) {
        TuyaODSDK.getIODDeviceManagerInstance().publishDP(deviceId, dpCode, value, device, callback);
    }

    public static boolean isDpCodeSupport(String deviceId, String dpCode) {
        return TuyaODSDK.getIODDeviceManagerInstance().isDPCodeSupport(deviceId, dpCode);
    }

    public static SchemaBean getSchemaByDpCode(String deviceId, String dpCode) {
        return TuyaODSDK.getIODDeviceManagerInstance().getSchemaByDPCode(deviceId, dpCode);
    }

    public static void getDeviceHardwareInfo(String deviceId, ITuyaResultCallback<DeviceHardwareInfo> callback) {
        TuyaODSDK.getIODDeviceInfoInstance().getDeviceHardwareInfo(deviceId, callback);
    }

    public static void getDeviceIcon(String deviceId, ITuyaResultCallback<ProductMap> callback) {
        TuyaODSDK.getIODDeviceInfoInstance().getDeviceIcon(deviceId, callback);
    }

    public static void getTrackSegment(TrackSegmentReq trackSegmentReq, ITuyaResultCallback<TrackSegmentInfo> callback) {
        TuyaODSDK.getIODTrackInstance().getTrackSegment(trackSegmentReq, callback);
    }

    public static void getTrackStatistic(TrackStatisticReq trackStatisticReq, ITuyaResultCallback<TrackStatisticInfo> callback) {
        TuyaODSDK.getIODTrackInstance().getTrackStatistic(trackStatisticReq, callback);
    }

    public static void reportLocation(ReportLocationReq reportLocationReq, ITuyaResultCallback<Boolean> callback) {
        TuyaODSDK.getIODTrackInstance().reportLocation(reportLocationReq, callback);
    }

    public static void getStoreList(StoreGetReq storeGetReq, ITuyaResultCallback<List<StoreInfo>> callback) {
        TuyaODSDK.getIODStoreInstance().getStoreList(storeGetReq, callback);
    }

    public static void searchStoreList(StoreSearchReq storeSearchReq, ITuyaResultCallback<List<StoreInfo>> callback) {
        TuyaODSDK.getIODStoreInstance().searchStoreList(storeSearchReq, callback);
    }
}
