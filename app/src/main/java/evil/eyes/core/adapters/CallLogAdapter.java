package evil.eyes.core.adapters;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
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

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> implements Filterable {

    JSONArray data;

    public CallLogAdapter(JSONArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_call_logs,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject jsonObject = data.getJSONObject(data.length() - (position +1));

            Glide.with(holder.itemView).load(jsonObject.getString("CALL_TYPE").equals("INCOMING") ? R.drawable.incoming_call : R.drawable.outgoing_call).into(holder.call_type);
            holder.duration.setText("Duration : "+jsonObject.getString("DURATION")+" Seconds");
            holder.date.setText("Date :"+jsonObject.getString("CALL_DATE"));
            holder.phoneNo.setText(jsonObject.getString("NUMBER")+" ("+(jsonObject.getString("NAME").isEmpty()?"Name N/A":jsonObject.getString("NAME"))+")");

            holder.call.setOnClickListener(k->{
                Intent intent = new Intent(Intent.ACTION_CALL);

                try {
                    intent.setData(Uri.parse("tel:" + jsonObject.getString("NUMBER")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                holder.itemView.getContext().startActivity(intent);
            });

        } catch (JSONException e) {
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

        ImageView call,call_type;
        TextView date,phoneNo,duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            call = itemView.findViewById(R.id.dial);
            call_type = itemView.findViewById(R.id.call_type);
            date = itemView.findViewById(R.id.date);
            duration = itemView.findViewById(R.id.diration);
            phoneNo = itemView.findViewById(R.id.phoneNO);
        }
    }
}
