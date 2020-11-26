package com.ernieandbernie.messenger.View;

import android.content.Context;
import android.text.format.DateUtils;
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
        public TextView txtTime;

        public SentMessageViewHolder(@NonNull View view) {
            super(view);
            messageContent = view.findViewById(R.id.messageContent);
            txtTime = view.findViewById(R.id.txtTime);
        }

        public void bind(Context context, Message message) {
            messageContent.setText(message.content);
            txtTime.setText(DateUtils.formatDateTime(context, Long.parseLong(message.timestamp) * 1000, DateUtils.FORMAT_SHOW_TIME));
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageContent;
        public TextView txtTime;

        public ReceivedMessageViewHolder(@NonNull View view) {
            super(view);
            messageContent = view.findViewById(R.id.messageContent);
            txtTime = view.findViewById(R.id.txtTime);
        }

        public void bind(Context context, Message message) {
            messageContent.setText(message.content);
            txtTime.setText(DateUtils.formatDateTime(context, Long.parseLong(message.timestamp) * 1000, DateUtils.FORMAT_SHOW_TIME));
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
            view = layoutInflater.inflate(R.layout.messeage_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = layoutInflater.inflate(R.layout.message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            ((SentMessageViewHolder) holder).messageContent.setText(message.content);
            ((SentMessageViewHolder) holder).txtTime.setText(DateUtils.formatDateTime(context, Long.parseLong(message.timestamp) * 1000, DateUtils.FORMAT_SHOW_TIME));
        } else if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_RECEIVED) {
            ((ReceivedMessageViewHolder) holder).messageContent.setText(message.content);
            ((ReceivedMessageViewHolder) holder).txtTime.setText(DateUtils.formatDateTime(context, Long.parseLong(message.timestamp) * 1000, DateUtils.FORMAT_SHOW_TIME));
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
