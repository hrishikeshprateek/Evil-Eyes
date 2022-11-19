package evil.eyes.core.helpers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import evil.eyes.core.interfaces.PayloadConnectionListner;

public class PayloadConnection {

    public static PayloadConnection initializePayloadConnection(){
        return new PayloadConnection();
    }

    public void checkPayloadConnection(PayloadConnectionListner payloadConnectionListner) {
        FirebaseDatabase
                .getInstance()
                .getReference("test")
                .child("command")
                .setValue(System.currentTimeMillis() % 10)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            FirebaseDatabase
                                    .getInstance()
                                    .getReference("test")
                                    .child("command")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                try {
                                                    snapshot.getValue(Boolean.class);
                                                    payloadConnectionListner.onConnectionSuccessful();
                                                    snapshot.getRef().removeEventListener(this);

                                                } catch (DatabaseException databaseException) {
                                                    databaseException.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            payloadConnectionListner.onPayloadError("Failed to connect to the server : "+error.getMessage());
                                        }
                                    });
                        } else {
                            payloadConnectionListner.onPayloadError("Failed to connect to the server.");
                        }
                    }
                });
    }

}
