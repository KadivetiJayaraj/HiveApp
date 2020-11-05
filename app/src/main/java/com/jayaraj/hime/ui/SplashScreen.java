package com.jayaraj.hime.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jayaraj.hime.R;
import com.jayaraj.hime.model.UserProfile;
import com.jayaraj.hime.ui.chat.MainChatScreen;
import com.jayaraj.hime.ui.chat.MainTabScreen;
import com.jayaraj.hime.ui.quickblox.activities.BaseActivity;
import com.jayaraj.hime.ui.quickblox.services.LoginService;
import com.jayaraj.hime.ui.quickblox.utils.SharedPrefsHelper;
import com.jayaraj.hime.util.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

public class SplashScreen extends BaseActivity {
  Handler handler;

  private static final int SPLASH_DELAY = 1500;
  private static final String TAG = SplashScreen.class.getSimpleName();
  private SharedPrefsHelper sharedPrefsHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash_screen);
    FirebaseApp.initializeApp(getApplicationContext());
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
    FirebaseCrashlytics.getInstance().sendUnsentReports();
    sharedPrefsHelper = SharedPrefsHelper.getInstance();

    handler = new Handler();
    handler.postDelayed(
        () -> {
          if (SharedPrefManager.getInstance(SplashScreen.this).isLoggedIn()
              && sharedPrefsHelper.hasQbUser()) {
            LoginService.start(SplashScreen.this, sharedPrefsHelper.getQbUser());
            final FirebaseFirestore db = FirebaseFirestore.getInstance();

            FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(
                    new OnCompleteListener<InstanceIdResult>() {
                      @Override
                      public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                          return;
                        }

                        String token = task.getResult().getToken();

                        Map<String, Object> user1 = new HashMap<>();
                        user1.put("fcmToken", token);

                        UserProfile user2 =
                            SharedPrefManager.getInstance(getApplicationContext()).getLoginUser();
                        // Add a new document with a generated ID
                        db.collection("users_table")
                            .document(user2.userId.toLowerCase())
                            .set(user1, SetOptions.merge());
                        // Get new Instance ID token
                      }
                    });
            startActivity(new Intent(SplashScreen.this, MainTabScreen.class));

            finish();
          } else {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
          }
        },
        SPLASH_DELAY);
  }
}
