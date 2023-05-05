package evil.eyes.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

import evil.eyes.R;
import evil.eyes.ui.ScheduleCreator;

public class Schedule extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_schedule, container, false);
        MaterialButton newSchedule = view.findViewById(R.id.schedule_new);
        newSchedule.setOnClickListener(o -> {
            startActivityForResult(new Intent(getActivity(), ScheduleCreator.class),1005);
        });

        
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}