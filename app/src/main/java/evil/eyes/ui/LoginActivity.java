package evil.eyes.ui;

import android.content.Intent;
import android.hardware.biometrics.BiometricManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.standalones.axismarkers.Text;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import evil.eyes.R;

public class LoginActivity extends AppCompatActivity {

    private TextView email, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        findViewById(R.id.sendotp).setOnClickListener(t -> {
            if (email.getText().toString().isEmpty()){
                email.setError("Email cannot be empty !!");
                email.requestFocus();
            } else if (password.getText().toString().isEmpty()) {
                password.setError("Password cannot be empty !!");
                password.requestFocus();
            }else {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setMessage("Waiting for payload response")
                        .setCancelable(false)
                        .create();
                alertDialog.show();
                FirebaseAuth
                        .getInstance()
                        .signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnCompleteListener(task -> {
                            alertDialog.dismiss();
                            if (task.isSuccessful()){
                                startActivity(new Intent(LoginActivity.this, Home.class));
                                finish();
                            }else {
                                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, Home.class));
            finish();
        }
    }
}