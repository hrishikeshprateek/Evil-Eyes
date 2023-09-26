package evil.eyes.core.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import evil.eyes.R;
import evil.eyes.core.utils.TimeUtils;
import thundersharp.aigs.expandablecardview.ExpandableCardView;
import thundersharp.sensivisionhealth.loganalyzer.constants.JSONConstants;
import thundersharp.sensivisionhealth.loganalyzer.utils.TimeUtil;

public class IndivisualDayCallLogAdapter extends RecyclerView.Adapter<IndivisualDayCallLogAdapter.ViewHolder> implements Filterable {

    JSONArray data;

    public IndivisualDayCallLogAdapter(JSONArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_indivisual_day_record,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject jsonObject = data.getJSONObject(data.length() - (position +1));
            holder.expandableCardView.setDate(TimeUtils.formatDateWithSuffix(jsonObject.getString(JSONConstants.CALL_DATE)));
            holder.expandableCardView.setDuration(TimeUtil.convertSecondsToFormat(jsonObject.getLong(JSONConstants.DURATION)));
            holder.expandableCardView.setCallCount((int) jsonObject.getLong(JSONConstants.CALL_COUNT));
            holder.expandableCardView.setIncomingCallCount((int) jsonObject.getLong(JSONConstants.INCOMING_COUNT_OUT));
            holder.expandableCardView.setOutgoingCallsCount((int) jsonObject.getLong(JSONConstants.OUTGOING_COUNT_OUT));
            holder.expandableCardView.setMissedCallCount((Integer) jsonObject.get(JSONConstants.MISSED_COUNT_OUT));
            holder.expandableCardView.initializeProgress();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (data != null) return data.length(); else return 0;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ExpandableCardView expandableCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            expandableCardView = itemView.findViewById(R.id.expand);

        }
    }
}
