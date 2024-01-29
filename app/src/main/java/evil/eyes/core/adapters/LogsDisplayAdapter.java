package evil.eyes.core.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import evil.eyes.R;
import evil.eyes.ui.fragments.Logs;

public class LogsDisplayAdapter extends RecyclerView.Adapter<LogsDisplayAdapter.ViewHolder> implements Filterable {

    List<Logs> data;

    public LogsDisplayAdapter(List<Logs> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_call_logs, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        if (data != null) return data.size(); else return 0;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }
}
