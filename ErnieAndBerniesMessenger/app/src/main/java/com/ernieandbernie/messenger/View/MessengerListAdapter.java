package com.ernieandbernie.messenger.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ernieandbernie.messenger.R;

public class MessengerListAdapter extends RecyclerView.Adapter<MessengerListAdapter.MessengerViewHolder> {

    public static class MessengerViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName;
        private ImageView profilePicture;

        public MessengerViewHolder(@NonNull View view) {
            super(view);
            txtName = view.findViewById(R.id.txtName);
            profilePicture = view.findViewById(R.id.profilePicture);
        }

    }

    private LayoutInflater layoutInflater;

    @NonNull
    @Override
    public MessengerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.activity_friendlist, parent, false);
        return new MessengerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessengerViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
