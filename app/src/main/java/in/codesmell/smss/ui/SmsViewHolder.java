package in.codesmell.smss.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import in.codesmell.smss.R;

/**
 * Created by Sreejith on 16/6/16.
 */
public class SmsViewHolder extends RecyclerView.ViewHolder {
    public TextView number, message, date;
    public View seperator;

    public static SmsViewHolder create(Context context) {
        return new SmsViewHolder(View.inflate(context, R.layout.sms_item, null));
    }

    public SmsViewHolder(View itemView) {
        super(itemView);
        number = (TextView) itemView.findViewById(R.id.number);
        message = (TextView) itemView.findViewById(R.id.message);
        date = (TextView) itemView.findViewById(R.id.date);
        seperator = itemView.findViewById(R.id.seperator);
    }
}
