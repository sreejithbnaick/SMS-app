package in.codesmell.smss.data;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import in.codesmell.smss.MainActivity;
import in.codesmell.smss.ui.SmsViewHolder;

/**
 * Created by Sreejith on 16/6/16.
 */
public class SmsAdapter extends RecyclerView.Adapter<SmsViewHolder> {
    private final static String TAG = SmsAdapter.class.getSimpleName();
    private final static boolean DEBUG = true;
    private final Context context;

    SmsResult smsResult;
    View.OnClickListener onClickListener;

    public SmsAdapter(Context context, View.OnClickListener onClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @Override
    public SmsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return SmsViewHolder.create(context);
    }

    @Override
    public void onBindViewHolder(SmsViewHolder holder, int position) {
        SmsData data = smsResult.groupData.get(position);
        holder.itemView.setTag(data);
        holder.itemView.setOnClickListener(onClickListener);
        String contact = data.address;
        ArrayList<ContactData> contactList = ((MainActivity)context).contactResult.contactMap.get(data.reverseAddress);
        if (contactList!=null && contactList.size() > 0) {
            String name = contactList.get(0).name;
            if (!TextUtils.isEmpty(name))
                contact = name;
        }
        holder.number.setText(contact);
        holder.message.setText(data.body);
        holder.date.setText(DateUtils.formatDateTime(context, data.date, DateUtils.FORMAT_ABBREV_RELATIVE));
        if (data.read) {
            holder.number.setTypeface(null, Typeface.NORMAL);
            holder.message.setTypeface(null, Typeface.NORMAL);
        } else {
            holder.number.setTypeface(null, Typeface.BOLD);
            holder.message.setTypeface(null, Typeface.BOLD);
        }
    }

    @Override
    public int getItemCount() {
        return smsResult == null ? 0 : smsResult.groupData.size();
    }

    public void setData(SmsResult smsResult) {
        this.smsResult = smsResult;
        notifyDataSetChanged();
    }
}

