package com.ernieandbernie.messenger.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ernieandbernie.messenger.Models.Friend;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {
    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
    }

    private final LayoutInflater layoutInflater;
    private OnFriendClickListener clickListener;
    private OnFriendClickListener longClickListener;
    private List<Friend> friends;
    private Context context;

    private Repository repository;

    public FriendListAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        repository = Repository.getInstance(context.getApplicationContext());
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName;
        private ImageView profilePicture;

        public FriendViewHolder(@NonNull View view) {
            super(view);
            txtName = view.findViewById(R.id.txtName);
            profilePicture = view.findViewById(R.id.profilePicture);
        }

    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.activity_list_item, parent, false);
        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        if (getItemCount() > 0) {
            Friend current = friends.get(position);

            repository.getProfileUrlByUid(current.uuid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Glide.with(holder.profilePicture.getContext())
                            .load(snapshot.getValue(String.class))
                            .placeholder(R.drawable.ic_default_user)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.profilePicture);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.txtName.setText(current.getDisplayName());

            if (clickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onFriendClick(current);
                    }
                });
            }

            if (this.longClickListener != null) {
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onFriendClick(current);
                        return true;
                    }
                });
            }
        }
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

    public FriendListAdapter setOnClickListener(OnFriendClickListener listener) {
        this.clickListener = listener;
        return this;
    }

    public FriendListAdapter setOnLongClickListener(OnFriendClickListener listener) {
        this.longClickListener = listener;
        return this;
    }

    @Override
    public int getItemCount() {
        if (friends != null)
            return friends.size();
        else return 0;
    }
}
