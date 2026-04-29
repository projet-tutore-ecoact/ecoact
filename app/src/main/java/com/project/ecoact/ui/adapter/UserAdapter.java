package com.project.ecoact.ui.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.ecoact.R;
import com.project.ecoact.data.entity.User;

import java.util.List;
import java.util.Objects;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> users;

    public void setData(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User p = users.get(position);
        holder.tvNom.setText(p.getFirstName());
        holder.tvPrenom.setText(p.getLastName());
    }

    @Override
    public int getItemCount() {
        return Objects.isNull(users) ? 0 : users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom, tvPrenom;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tvNom);
            tvPrenom = itemView.findViewById(R.id.tvPrenom);
        }
    }
}