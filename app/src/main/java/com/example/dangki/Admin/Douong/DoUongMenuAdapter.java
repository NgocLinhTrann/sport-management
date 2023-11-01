package com.example.dangki.Admin.Douong;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Model.DoUong;
import com.example.dangki.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DoUongMenuAdapter extends RecyclerView.Adapter<DoUongMenuAdapter.DoUongMenuViewHolder> {
    List<DoUong> doUongList;
    private OnItemClickListener listener;


    public DoUongMenuAdapter(List<DoUong> doUongList){
        this.doUongList = doUongList;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public DoUongMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_douong_item, parent, false);
        return new DoUongMenuViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull DoUongMenuViewHolder holder, int position) {
        DoUong doUong = doUongList.get(position);
        holder.bind(doUong);
    }

    @Override
    public int getItemCount() {
        return doUongList.size();
    }

    public void setDoUongList(List<DoUong> doUongList){
        this.doUongList =doUongList;
        notifyDataSetChanged();
    }

    public class DoUongMenuViewHolder extends RecyclerView.ViewHolder {
        ImageView imv_doUong;
        TextView tv_tenDouong,tv_gia, tv_sl;
        public DoUongMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            imv_doUong = itemView.findViewById(R.id.imv_admim_douong_image);
            tv_tenDouong = itemView.findViewById(R.id.tv_admin_douong_tendouong);
            tv_gia = itemView.findViewById(R.id.tv_admin_douong_gia);
            tv_sl = itemView.findViewById(R.id.tv_admin_douong_sl);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(doUongList.get(position));
                    }
                }
            });
        }

        public void bind(DoUong doUong){
            tv_tenDouong.setText(doUong.getName());
            double gia = doUong.getPrice();
            tv_gia.setText(doUong.getPrice()+"");
            tv_sl.setText(doUong.getRemain()+"");
            Picasso.get().load(doUong.getImg_url()).into(imv_doUong);
        }
    }
    public List<DoUong> getDoUongList() {
        return doUongList;
    }
    public interface OnItemClickListener {
        void onItemClick(DoUong doUong);
    }
}
