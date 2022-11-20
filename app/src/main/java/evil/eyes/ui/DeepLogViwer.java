package evil.eyes.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import evil.eyes.R;
import evil.eyes.core.RecentModel;
import evil.eyes.core.adapters.RecentAdapter;

public class DeepLogViwer extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_log_viwer);

        if (getIntent().getStringExtra("key")==null) finish();

        List<RecentModel> recentModels = new ArrayList<>();
        String key = getIntent().getStringExtra("key");
        toolbar = findViewById(R.id.toolbar);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        toolbar.setNavigationOnClickListener(p->finish());
        toolbar.setTitle(key);
        FirebaseDatabase
                .getInstance()
                .getReference("RECORDS")
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                recentModels.add(new RecentModel(dataSnapshot.getValue(String.class),snapshot.getKey(),dataSnapshot.getKey(),snapshot.getKey()));
                            }

                            recyclerView.setAdapter(new RecentAdapter(recentModels,true));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DeepLogViwer.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}