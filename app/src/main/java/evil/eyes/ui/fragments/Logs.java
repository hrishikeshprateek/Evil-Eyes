package evil.eyes.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import evil.eyes.R;
import evil.eyes.core.DeviceConfig;
import evil.eyes.core.adapters.LogsDisplayAdapter;
import evil.eyes.core.models.Devices;
import evil.eyes.ui.Home;

public class Logs extends Fragment {

    private DeviceConfig deviceConfig;
    private LinearLayout no_device;
    private Devices device = null;
    private ChildEventListener childEventListener;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logs, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.logsDisplay);
        AppCompatButton setDevice = view.findViewById(R.id.setDevice);
        no_device = view.findViewById(R.id.no_found);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe);

        deviceConfig = DeviceConfig.getInstance(view.getContext()).initializeStorage();

        if (deviceConfig.getSelectedDevice().getUUID() == null) {
            no_device.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            setDevice.setOnClickListener(t -> {
                if (Home.viewPager != null) Home.viewPager.setCurrentItem(4);
                else
                    Toast.makeText(view.getContext(), "Internal Error !!, try setting default device manually !!", Toast.LENGTH_SHORT).show();
            });

        } else {
            recyclerView.setVisibility(View.VISIBLE);
            no_device.setVisibility(View.GONE);

            List<evil.eyes.core.models.Logs> logsList = new ArrayList<>();
            LogsDisplayAdapter adapter = new LogsDisplayAdapter(logsList);
            recyclerView.setAdapter(adapter);

            addListener(logsList,adapter);

            swipeRefreshLayout.setOnRefreshListener(() -> {
                FirebaseDatabase
                        .getInstance()
                        .getReference("Logs")
                        .child(device.getUUID())
                        .removeEventListener(childEventListener);
                logsList.clear();
                adapter.notifyDataSetChanged();
                addListener(logsList,adapter);
                swipeRefreshLayout.setRefreshing(false);
            });

        }


        return view;
    }

    private void addListener(List<evil.eyes.core.models.Logs> logsList, LogsDisplayAdapter adapter){
        device = deviceConfig.getSelectedDevice();
        childEventListener = FirebaseDatabase
                .getInstance()
                .getReference("Logs")
                .child(device.getUUID())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            logsList.add(new evil.eyes.core.models.Logs(snapshot.getKey(), snapshot.getValue(String.class)));
                            int position = logsList.size() - 1; // Index of the last item
                            adapter.notifyItemInserted(position);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (device != null) {
            FirebaseDatabase
                    .getInstance()
                    .getReference("Logs")
                    .child(device.getUUID())
                    .removeEventListener(childEventListener);
        }
    }
}