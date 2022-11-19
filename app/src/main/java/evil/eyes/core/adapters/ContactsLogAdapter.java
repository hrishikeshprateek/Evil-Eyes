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

public class ContactsLogAdapter extends RecyclerView.Adapter<ContactsLogAdapter.ViewHolder> implements Filterable {

    JSONArray data;

    public ContactsLogAdapter(JSONArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_contacts_logs,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject jsonObject = data.getJSONObject(position);
            holder.call_type.setText(jsonObject.getString("NAME").substring(0,1));
            holder.duration.setText(jsonObject.getString("NUMBER"));
            holder.date.setText("Called : "+jsonObject.getString("TIMES_CALLED")+" Times");
            holder.phoneNo.setText(jsonObject.getString("NAME"));

            holder.call.setOnClickListener(k->{
                Intent intent = new Intent(Intent.ACTION_CALL);

                try {
                    intent.setData(Uri.parse("tel:" + jsonObject.getString("NUMBER")));
                    holder.itemView.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

        ImageView call;
        TextView call_type;
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
