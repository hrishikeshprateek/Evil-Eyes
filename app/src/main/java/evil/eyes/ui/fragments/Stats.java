package evil.eyes.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import evil.eyes.R;
import evil.eyes.core.adapters.DeviceViwer;
import evil.eyes.core.models.Devices;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Stats} factory method to
 * create an instance of this fragment.
 */
public class Stats extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.deviceRecycler);

        FirebaseDatabase
                .getInstance()
                .getReference("DEVICES")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Devices> data = new ArrayList<>();
                        if (snapshot.exists()){
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                data.add(dataSnapshot.getValue(Devices.class));
                            }
                        }
                        recyclerView.setAdapter(new DeviceViwer(data));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(view.getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        return view;
    }
}