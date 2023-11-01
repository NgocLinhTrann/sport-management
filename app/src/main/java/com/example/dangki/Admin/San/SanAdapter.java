package com.example.dangki.Admin.San;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Model.DoUong;
import com.example.dangki.Model.San;
import com.example.dangki.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SanAdapter extends RecyclerView.Adapter<SanAdapter.SanViewHolder> {
    List<San> sanList;
    private OnItemClickListener listener;
    public SanAdapter(List<San> sanList) {
        this.sanList = sanList;
    }


    public List<San> getSanList() {
        return sanList;
    }

    public void setSanList(List<San> sanList) {
        this.sanList = sanList;
        notifyDataSetChanged();
    }



    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_san_item, parent, false);
        return new SanAdapter.SanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SanViewHolder holder, int position) {
        San san = sanList.get(position);
        holder.bind(san);
    }

    @Override
    public int getItemCount() {
        return sanList.size();
    }
    public class SanViewHolder extends RecyclerView.ViewHolder{
        ImageView imv_san;
        TextView tv_tensan, tv_giasan;

        public SanViewHolder(@NonNull View itemView) {
            super(itemView);
            imv_san = itemView.findViewById(R.id.imv_admim_san_image);
            tv_tensan = itemView.findViewById(R.id.tv_admin_san_tensan);
            tv_giasan = itemView.findViewById(R.id.tv_admin_san_gia);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(sanList.get(position));
                    }
                }
            });
        }
        public void bind(San san){
            tv_tensan.setText(san.getName());
            tv_giasan.setText(san.getPrice()+"");

            Picasso.get().load(san.getImg_url()).into(imv_san);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(San san);
    }

}
