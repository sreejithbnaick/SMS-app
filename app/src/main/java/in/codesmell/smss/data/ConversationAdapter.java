package in.codesmell.smss.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.ViewGroup;

import java.util.ArrayList;

import in.codesmell.smss.ui.ConversationViewHolder;

/**
 * Created by Sreejith on 16/6/16.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationViewHolder> {
    private final static String TAG = ConversationAdapter.class.getSimpleName();
    private final static boolean DEBUG = true;
    private final Context context;

    private String address;
    private ArrayList<SmsData> datas;

    public ConversationAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ConversationViewHolder.create(context, viewType,parent);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        SmsData data = datas.get(position);
        holder.message.setText(data.body);
        holder.date.setText(DateUtils.formatDateTime(context, data.date, DateUtils.FORMAT_ABBREV_RELATIVE));
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    public void setData(String address, ArrayList<SmsData> datas) {
        this.address = address;
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return datas.get(position).type;
    }
}

