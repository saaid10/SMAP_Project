package com.ernieandbernie.messenger.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ernieandbernie.messenger.Models.Friend;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessengerListAdapter extends RecyclerView.Adapter<MessengerListAdapter.MessengerViewHolder> {
    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
    }

    private final LayoutInflater layoutInflater;
    private final OnFriendClickListener clickListener;
    private List<Friend> friends;
    private Context context;

    private Repository repository;

    public MessengerListAdapter(Context context, OnFriendClickListener clickListener) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.clickListener = clickListener;
        repository = Repository.getInstance(context.getApplicationContext());
    }

    public static class MessengerViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName;
        private ImageView profilePicture;

        public MessengerViewHolder(@NonNull View view) {
            super(view);
            txtName = view.findViewById(R.id.txtName);
            profilePicture = view.findViewById(R.id.profilePicture);
        }

    }

    @NonNull
    @Override
    public MessengerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.activity_list_item, parent, false);
        return new MessengerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessengerViewHolder holder, int position) {
        if (getItemCount() > 0) {
            Friend current = friends.get(position);

            repository.getProfileUrlByUid(current.uuid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Glide.with(holder.profilePicture.getContext())
                            .load(snapshot.getValue(String.class))
                            .placeholder(R.drawable.ic_default_user)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.profilePicture);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.txtName.setText(current.getDisplayName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onFriendClick(current);
                }
            });
        }
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (friends != null)
            return friends.size();
        else return 0;
    }
}
