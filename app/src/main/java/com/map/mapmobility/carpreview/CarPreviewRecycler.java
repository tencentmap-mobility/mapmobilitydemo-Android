package com.map.mapmobility.carpreview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.map.mapmobility.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CarPreviewRecycler extends RecyclerView.Adapter<CarPreviewRecycler.ViewHolder>{
    public static final String LOG_TAG = "navi";

    private ArrayList<String> carPreviewDate = new ArrayList<>();

    /** 外部的点击监听*/
    private CarPreviewRecycler.IClickListener clickListener;

    public CarPreviewRecycler(CarPreviewRecycler.IClickListener listener) {
        this.clickListener = listener;

        if(carPreviewDate.size() != 0)
            carPreviewDate.clear();
        carPreviewDate.add("同时呼叫");
        carPreviewDate.add("出租车");
        carPreviewDate.add("新能源");
        carPreviewDate.add("舒适型");
        carPreviewDate.add("豪华型");
        carPreviewDate.add("商务型");
        carPreviewDate.add("经济型");
    }

    @NonNull
    @Override
    public CarPreviewRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.car_preview_task_recycler_item, parent, false);
        return new CarPreviewRecycler.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarPreviewRecycler.ViewHolder holder, int position) {
        String content = carPreviewDate.get(position);
        holder.tvContent.setText(content);

        holder.listener.position = position;
        holder.listener.setViewHolder(holder);
        holder.tvContent.setOnClickListener(holder.listener);
    }

    @Override
    public int getItemCount() {
        return carPreviewDate.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CarPreviewRecycler.MyClickListener listener = new CarPreviewRecycler.MyClickListener();

        TextView tvContent;

        public ViewHolder(View view) {
            super(view);
            tvContent = view.findViewById(R.id.tv_car_preview_recycler_view_item_content);
        }
    }

    class MyClickListener implements View.OnClickListener {

        public WeakReference<CarPreviewRecycler.ViewHolder> wrf;
        public int position;

        public void setViewHolder(CarPreviewRecycler.ViewHolder viewHolder) {
            wrf = new WeakReference<>(viewHolder);
        }

        @Override
        public void onClick(View v) {
            if(wrf == null || wrf.get() == null){
                Log.e(LOG_TAG, "view holder or wrf null");
                return;
            }
            if(clickListener != null){
                clickListener.onClick(position);
            }
        }
    }

    interface IClickListener {
        void onClick(int itemPosition);
    }
}
