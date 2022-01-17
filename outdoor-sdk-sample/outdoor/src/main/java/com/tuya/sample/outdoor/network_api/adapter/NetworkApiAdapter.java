package com.tuya.sample.outdoor.network_api.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.sample.outdoor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nickey <a href="mailto:developer@tuya.com"/>
 * @since 2022/1/10 17:11
 */

public class NetworkApiAdapter extends RecyclerView.Adapter<NetworkApiAdapter.NetworkApiViewHolder> {

    private final List<String> apiList = new ArrayList<>();
    private ApiSendClickListener apiSendClickListener;

    public void setApiSendClickListener(ApiSendClickListener apiSendClickListener) {
        this.apiSendClickListener = apiSendClickListener;
    }

    @NonNull
    @Override
    public NetworkApiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NetworkApiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recyclerview_item_network_api, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NetworkApiViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tvApiName.setText(apiList.get(position));
        holder.tvApiSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (apiSendClickListener != null) {
                    apiSendClickListener.onApiSend(apiList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return apiList.size();
    }

    public void updateData(List<String> list) {
        apiList.clear();
        apiList.addAll(list);
        notifyDataSetChanged();
    }

    static class NetworkApiViewHolder extends RecyclerView.ViewHolder {

        public TextView tvApiName;
        public TextView tvApiSend;

        public NetworkApiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvApiName = itemView.findViewById(R.id.tvApiName);
            tvApiSend = itemView.findViewById(R.id.tvApiSend);
        }
    }

    public interface ApiSendClickListener {
        void onApiSend(String apiName);
    }
}
