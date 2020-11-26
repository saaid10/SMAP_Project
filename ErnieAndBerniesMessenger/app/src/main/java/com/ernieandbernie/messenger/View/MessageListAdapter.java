package com.ernieandbernie.messenger.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ernieandbernie.messenger.Models.Message;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.R;

import java.util.List;


// MessengerListAdapter is a modification of https://sendbird.com/blog/android-chat-tutorial-building-a-messaging-ui
public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private final Context context;
    private Repository repository;
    private LayoutInflater layoutInflater;

    private List<Message> messages;

    public MessageListAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        repository = Repository.getInstance(context.getApplicationContext());
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageContent;

        public SentMessageViewHolder(@NonNull View view) {
            super(view);
            messageContent = view.findViewById(R.id.messageContent);
        }

        public void bind(Message message) {
            messageContent.setText(message.content);
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageContent;

        public ReceivedMessageViewHolder(@NonNull View view) {
            super(view);
            messageContent = view.findViewById(R.id.messageContent);
        }

        public void bind(Message message) {
            messageContent.setText(message.content);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        if (message.senderUid.equals(repository.getFirebaseUser().getUid())) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.messeage_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_RECEIVED) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (messages != null)
            return messages.size();
        else return 0;
    }
}
