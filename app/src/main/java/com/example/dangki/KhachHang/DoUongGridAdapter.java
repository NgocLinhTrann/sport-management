package com.example.dangki.KhachHang;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dangki.Model.DoUong;
import com.example.dangki.Model.San;
import com.example.dangki.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DoUongGridAdapter extends BaseAdapter {
    Context context;
    List<DoUong> doUongList;
    String userID, rentalID;

    public DoUongGridAdapter(Context context, List<DoUong> doUongList, String rentalID,
                             String userID) {
        this.context = context;
        this.doUongList = doUongList;
        this.userID = userID;
        this.rentalID = rentalID;
    }


    public List<DoUong> getDoUongList() {
        return this.doUongList;
    }

    public void setDoUongList(List<DoUong> doUongList) {
        this.doUongList = doUongList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.doUongList.size();
    }

    @Override
    public Object getItem(int posistion) {
        return this.doUongList.get(posistion);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.khachhang_chondouong_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.imv_anhdouong = convertView.findViewById(R.id.imv_khachhang_chondouong_item);
            viewHolder.tv_tendouong = convertView.findViewById(R.id.tv_khachhang_chondouong_item_tendouong);
            viewHolder.tv_giadouong = convertView.findViewById(R.id.tv_khachhang_chondouong_item_gia);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DoUong doUong = doUongList.get(position);
        viewHolder.tv_tendouong.setText(doUong.getName());
        viewHolder.tv_giadouong.setText(doUong.getPrice()+"VND");
        Picasso.get().load(doUong.getImg_url()).into(viewHolder.imv_anhdouong);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chuyển sang màn hình chi tiết đồ uống với thông tin đồ uống tương ứng
                Intent intent = new Intent(context, ChiTietDoUong.class);
                intent.putExtra("idDoUong_intent", doUong.getId());
                intent.putExtra("img_url", doUong.getImg_url());
                intent.putExtra("name", doUong.getName());
                intent.putExtra("price", doUong.getPrice());
                intent.putExtra("remain", doUong.getRemain());

                intent.putExtra("userID", userID);
                intent.putExtra("rentalID", rentalID);
                context.startActivity(intent);
            }
        });
        return convertView;
    }
    private static class ViewHolder {
        ImageView imv_anhdouong;
        TextView tv_tendouong, tv_giadouong;
    }
}
