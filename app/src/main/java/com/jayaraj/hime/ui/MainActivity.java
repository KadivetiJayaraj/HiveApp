package com.jayaraj.hime.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jayaraj.hime.R;
import com.jayaraj.hime.util.NoInternet;

public class MainActivity extends AppCompatActivity {
  EditText phone_number;
  Button submit_number;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    phone_number = findViewById(R.id.phoneNumber);
    submit_number = findViewById(R.id.signButton);
    submit_number.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            if (NoInternet.isNetworkAvailable(getApplicationContext())) {

              final AlertDialog.Builder redirectalert = new AlertDialog.Builder(MainActivity.this);
              redirectalert.setTitle("Verifying Phone Number");
              redirectalert.setMessage(
                  "+91 "
                      + phone_number.getText().toString()
                      + ", is this OK, or would you like to edit the number?");

              redirectalert.setPositiveButton(
                  "Ok",
                  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                      Intent verify = new Intent(getApplicationContext(), VerifyActivity.class);
                      verify.putExtra("Number", phone_number.getText().toString());
                      startActivity(verify);
                      finish();
                    }
                  });
              redirectalert.setNegativeButton(
                  "Edit",
                  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                      // User cancelled the dialog
                    }
                  });
              redirectalert.create().show();
            } else {
              Toast.makeText(MainActivity.this, "Please Check your internet", Toast.LENGTH_SHORT)
                  .show();
            }
          }
        });
  }
}
