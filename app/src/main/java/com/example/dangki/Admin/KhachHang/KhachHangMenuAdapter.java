package com.example.dangki.Admin.KhachHang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Model.DoUong;
import com.example.dangki.Model.User;
import com.example.dangki.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class KhachHangMenuAdapter extends RecyclerView.Adapter<KhachHangMenuAdapter.ViewHolder> {
    List<User> userList;
    OnItemClickListener listener;

    public KhachHangMenuAdapter(List<User> userList) {
        this.userList = userList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_khachhang_item, parent, false);
        return new ViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imv_user;
        TextView tv_ten, tv_email;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imv_user = itemView.findViewById(R.id.imv_admin_khachhang_anh);
            tv_ten = itemView.findViewById(R.id.tv_admin_khachhang_fullname);
            tv_email = itemView.findViewById(R.id.tv_admin_khachhang_email);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(userList.get(position));
                    }
                }
            });

        }
        public void bind(User user){
            if(!user.getImg_url().isEmpty()){
                Picasso.get().load(user.getImg_url()).into(imv_user);
            }else{
                Picasso.get().load(R.drawable.profile).into(imv_user);
            }

            tv_ten.setText(user.getName());
            tv_email.setText(user.getEmail());
        }

    }
    public interface OnItemClickListener {
        void onItemClick(User user);
    }
}
