package com.example.dangki.KhachHang;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dangki.Model.San;
import com.example.dangki.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SanGridAdapter extends BaseAdapter {
    private Context context;
    private List<San> sanList;
    String userID;

    public SanGridAdapter(Context context, List<San> sanList, String userID) {
        this.context = context;
        this.sanList = sanList;
        this.userID = userID;
    }
    void setSanList(List<San> sanList){
        this.sanList = sanList;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return sanList.size();
    }

    @Override
    public Object getItem(int position) {
        return sanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.khachhang_chonsan_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.imv_anhsan = convertView.findViewById(R.id.imv_khachhang_chonsan_anhsan);
            viewHolder.tv_tensan = convertView.findViewById(R.id.tv_khachhang_chonsan_tensan);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        San san = sanList.get(position);
        viewHolder.tv_tensan.setText(san.getName());
        Picasso.get().load(san.getImg_url()).into(viewHolder.imv_anhsan);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chuyển sang màn hình chi tiết sân với thông tin sân tương ứng
                Intent intent = new Intent(context, ChiTietSan.class);
                intent.putExtra("userID", userID);
                intent.putExtra("idSan_intent", san.getId());
                intent.putExtra("img_url", san.getImg_url());
                intent.putExtra("name", san.getName());
                intent.putExtra("price", san.getPrice());
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        ImageView imv_anhsan;
        TextView tv_tensan;
    }
}
