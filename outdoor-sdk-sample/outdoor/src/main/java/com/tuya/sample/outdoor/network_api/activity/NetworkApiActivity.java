package com.tuya.sample.outdoor.network_api.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.sample.outdoor.R;
import com.tuya.sample.outdoor.network_api.adapter.NetworkApiAdapter;
import com.tuya.sample.outdoor.utils.Constants;
import com.tuya.sample.outdoor.utils.OutdoorUtils;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.outdoor.bean.DeviceHardwareInfo;
import com.tuya.smart.outdoor.bean.ProductMap;
import com.tuya.smart.outdoor.bean.StoreInfoConstants;
import com.tuya.smart.outdoor.bean.req.ReportLocationReq;
import com.tuya.smart.outdoor.bean.req.StoreGetReq;
import com.tuya.smart.outdoor.bean.req.StoreSearchReq;
import com.tuya.smart.outdoor.bean.req.TrackSegmentReq;
import com.tuya.smart.outdoor.bean.req.TrackStatisticReq;
import com.tuya.smart.outdoor.bean.resp.StoreInfo;
import com.tuya.smart.outdoor.bean.resp.TrackSegmentInfo;
import com.tuya.smart.outdoor.bean.resp.TrackStatisticInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * network interface debug
 *
 * @author Nickey <a href="mailto:developer@tuya.com"/>
 * @since 2022/1/10 17:13
 */

public class NetworkApiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_api);
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView rvApi = findViewById(R.id.rvApi);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        String deviceId = getIntent().getStringExtra(Constants.INTENT_DEVICE_ID);
        String productId = Objects.requireNonNull(TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId)).productId;
        NetworkApiAdapter adapter = new NetworkApiAdapter();
        adapter.setApiSendClickListener(apiName -> {
            switch (apiName) {
                case "getDeviceHardwareInfo":
                    getDeviceHardwareInfo(deviceId);
                    break;
                case "getDeviceIcon":
                    getDeviceIcon(deviceId);
                    break;
                case "getTrackSegment":
                    getTrackSegment(deviceId);
                    break;
                case "getTrackStatistic":
                    getTrackStatistic(deviceId);
                    break;
                case "reportLocation":
                    reportLocation(deviceId, productId);
                    break;
                case "getStoreList":
                    getStoreList();
                    break;
                case "searchStoreList":
                    searchStoreList();
                    break;
                default:
                    break;
            }
        });
        rvApi.setLayoutManager(new LinearLayoutManager(this));
        rvApi.setAdapter(adapter);
        List<String> apiList = new ArrayList<>();
        apiList.add("getDeviceHardwareInfo");
        apiList.add("getDeviceIcon");
        apiList.add("getTrackSegment");
        apiList.add("getTrackStatistic");
        apiList.add("reportLocation");
        apiList.add("getStoreList");
        apiList.add("searchStoreList");
        adapter.updateData(apiList);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void getDeviceHardwareInfo(String deviceId) {
        OutdoorUtils.getDeviceHardwareInfo(deviceId, new ITuyaResultCallback<DeviceHardwareInfo>() {
            @Override
            public void onSuccess(DeviceHardwareInfo result) {
                showToast("success");
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void getDeviceIcon(String deviceId) {
        OutdoorUtils.getDeviceIcon(deviceId, new ITuyaResultCallback<ProductMap>() {
            @Override
            public void onSuccess(ProductMap result) {
                showToast("success");
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void getTrackSegment(String deviceId) {
        TrackSegmentReq trackSegmentReq = new TrackSegmentReq().setDeviceId(deviceId).setLastId(-1).setLessMileage(1).setPageSize(10);
        OutdoorUtils.getTrackSegment(trackSegmentReq, new ITuyaResultCallback<TrackSegmentInfo>() {
            @Override
            public void onSuccess(TrackSegmentInfo result) {
                showToast("success");
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void getTrackStatistic(String deviceId) {
        long startTime = System.currentTimeMillis() - 30 * 1000;
        long endTime = System.currentTimeMillis() - 1000;
        TrackStatisticReq trackStatisticReq = new TrackStatisticReq().setDeviceId(deviceId).setStartTime(startTime).setEndTime(endTime);
        OutdoorUtils.getTrackStatistic(trackStatisticReq, new ITuyaResultCallback<TrackStatisticInfo>() {
            @Override
            public void onSuccess(TrackStatisticInfo result) {
                showToast("success");
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void reportLocation(String deviceId, String productId) {
        double longitude = 121.355359;
        double latitude = 31.217979;
        ReportLocationReq.PayLoad payLoad = new ReportLocationReq.PayLoad().setLon(longitude).setLat(latitude).setBattery(50).setMileage(10).setSpeed(20).setStart(true);
        ReportLocationReq reportLocationReq = new ReportLocationReq().setDeviceId(deviceId).setProductId(productId).setPayLoad(payLoad);
        OutdoorUtils.reportLocation(reportLocationReq, new ITuyaResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                showToast("success");
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void getStoreList() {
        double longitude = 121.355359;
        double latitude = 31.217979;
        StoreGetReq storeGetReq = new StoreGetReq().setLon(longitude).setLat(latitude).setRadius(5).setSource(StoreInfoConstants.Source.TUYA.value).setType(StoreInfoConstants.STORE_TYPE.ELECTRONIC.value);
        OutdoorUtils.getStoreList(storeGetReq, new ITuyaResultCallback<List<StoreInfo>>() {
            @Override
            public void onSuccess(List<StoreInfo> result) {
                showToast("success");
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void searchStoreList() {
        double longitude = 121.355359;
        double latitude = 31.217979;
        StoreSearchReq storeSearchReq = new StoreSearchReq().setLon(longitude).setLat(latitude).setRadius(5).setSource(StoreInfoConstants.Source.TUYA.value).setType(StoreInfoConstants.STORE_TYPE.ELECTRONIC.value).setKeyword("car");
        OutdoorUtils.searchStoreList(storeSearchReq, new ITuyaResultCallback<List<StoreInfo>>() {
            @Override
            public void onSuccess(List<StoreInfo> result) {
                showToast("success");
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showToast(errorMessage);
            }
        });
    }
}
