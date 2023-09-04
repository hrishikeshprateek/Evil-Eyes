package evil.eyes.core.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import evil.eyes.R;
import evil.eyes.ui.IndivisualAnalysis;
import thundersharp.sensivisionhealth.loganalyzer.constants.JSONConstants;

public class CallAnalysisResultAdapter extends RecyclerView.Adapter<CallAnalysisResultAdapter.ViewHolder> {

    private JSONArray AnalysisData;
    private String timeStampSharedPref;

    public CallAnalysisResultAdapter(JSONArray analysisData, String timeStamp) {
        AnalysisData = analysisData;
        timeStampSharedPref = timeStamp;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.aa,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //resetViews(holder);
        try {
            JSONObject jsonObject = AnalysisData.getJSONObject(position);
            long incoming = jsonObject.getLong(JSONConstants.INCOMING_COUNT);
            long outGoing = jsonObject.getLong(JSONConstants.OUTGOING_COUNT);
            long missed = jsonObject.getLong(JSONConstants.MISSED_COUNT);

            holder.name.setText(jsonObject.getString(JSONConstants.NAME).isEmpty() ? "Name n/a" :
                    jsonObject.getString(JSONConstants.NAME));
            holder.phone.setText("Ph: "+jsonObject.getString(JSONConstants.NUMBER));
            holder.incoming.setText("In: "+incoming);
            holder.outgoing.setText("Out: "+outGoing);
            holder.missedCount.setText("Miss: "+missed);
            holder.totlTalk.setText("Talk Time: "+jsonObject.get(JSONConstants.DURATION));
            holder.count.setText("Called "+jsonObject.getLong(JSONConstants.CALL_COUNT)+" times");

            if (incoming != outGoing)
                holder.defMessage.setTextColor(incoming < outGoing ? Color.parseColor("#367AA5") :
                        Color.parseColor("#CA5040"));
            holder.defMessage.setText(incoming == outGoing ?
                    "Congratulations no deficit found between incoming and outgoing calls !" :
                    "Your "+ (incoming > outGoing ? "Outgoing" : "Incoming") +
                    " calls have a deficit compared to "+(incoming > outGoing ? "Incoming" : "Outgoing")+
                    ", with a deficit percent of "+
                    calculateDeficitPercent(incoming,outGoing)+
                    "%");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return AnalysisData.length();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView name,phone, incoming,outgoing,defMessage,missedCount,totlTalk,count;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            phone = itemView.findViewById(R.id.phone);
            incoming = itemView.findViewById(R.id.incomingCount);
            outgoing = itemView.findViewById(R.id.outCount);
            defMessage = itemView.findViewById(R.id.defMessage);
            missedCount = itemView.findViewById(R.id.missCount);
            totlTalk = itemView.findViewById(R.id.tt);
            count = itemView.findViewById(R.id.count);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            try {
                view.getContext().startActivity(new Intent(view.getContext(), IndivisualAnalysis.class)
                        .putExtra("data",timeStampSharedPref)
                        .putExtra("phone",AnalysisData.getJSONObject(getAdapterPosition()).getString(JSONConstants.NUMBER)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String calculateDeficitPercent(long incoming, long outGoing){
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(((double)Math.abs(incoming-outGoing))/(incoming+outGoing) * 100);
    }

    private void resetViews(ViewHolder holder){
        holder.name.setText("");
        holder.phone.setText("");
        holder.incoming.setText("");
        holder.defMessage.setText("");
        holder.name.setTextColor(Color.BLACK);
        holder.outgoing.setText("");
    }
}
