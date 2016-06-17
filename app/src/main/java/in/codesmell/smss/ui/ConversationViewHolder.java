package in.codesmell.smss.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import in.codesmell.smss.R;

/**
 * Created by Sreejith on 16/6/16.
 */
public class ConversationViewHolder extends RecyclerView.ViewHolder {
    public static final int TYPE_SENT = 0;
    public static final int TYPE_INBOX = 1;

    public TextView message, date;

    public static ConversationViewHolder create(Context context, int type, ViewGroup parent) {
        ConversationViewHolder holder;
        if (type == TYPE_INBOX)
            holder = new ConversationViewHolder(LayoutInflater.from(context).inflate(R.layout.sms_item_inbox, parent, false));
        else
            holder = new ConversationViewHolder(LayoutInflater.from(context).inflate(R.layout.sms_item_send, parent, false));
        return holder;
    }

    public ConversationViewHolder(View itemView) {
        super(itemView);
        message = (TextView) itemView.findViewById(R.id.message);
        date = (TextView) itemView.findViewById(R.id.date);
    }
}
