package evil.eyes.core.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import evil.eyes.R;
import evil.eyes.core.models.Logs;
import evil.eyes.core.utils.TimeUtils;

public class LogsDisplayAdapter extends RecyclerView.Adapter<LogsDisplayAdapter.ViewHolder> implements Filterable {

    List<Logs> data;

    public LogsDisplayAdapter(List<Logs> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_server_logd, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Logs log = data.get(position);
        String color;

        if (log.getData().contains("[ERROR]")){
            color = "#FF0000";
        }else if (log.getData().contains("[COMMAND]")){
            color = "#3F51B5";
        } else if (log.getData().contains("[DATA]")) {
            color = "#009688";
        } else if (log.getData().contains("WARNING")) {
            color = "#FF5722";
        }else color = "#000000";

        holder.textView.setTextColor(Color.parseColor(color));
        holder.textView.setText(String.format("%s  ::::  %s", TimeUtils.formatDateInLongFormat(log.getTimestamp()), log.getData()));
    }

    @Override
    public int getItemCount() {
        if (data != null) return data.size(); else return 0;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textHolder);
        }
    }
}
