package com.jayaraj.hime.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jayaraj.hime.model.UserProfile;
import com.jayaraj.hime.ui.MainActivity;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefManager {

  // the constants
  private static final String SHARED_PREF_NAME = "hiveappsharedpref";
  private static final String KEY_FULLNAME = "name";
  public static final String KEY_ID = "id";
  public static final String KEY_PHONE = "phonenumber";
  public static final String KEY_IMAGE = "image";
  public static final String KEY_ABOUT = "about";
  private static final String KEY_UID = "uid";

  private static SharedPrefManager mInstance;
  private Context mCtx;

  private SharedPrefManager(Context context) {
    mCtx = context;
  }

  public static synchronized SharedPrefManager getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new SharedPrefManager(context);
    }
    return mInstance;
  }

  // method to let the user login
  // this method will store the user data in shared preferences
  public void userLogin(UserProfile user) {
    SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(KEY_ID, user.getUserId());
    editor.putString(KEY_FULLNAME, user.getName());
    editor.putString(KEY_PHONE, user.getPhonenumber());
    editor.putString(KEY_UID, user.getUserUid());
    editor.putString(KEY_IMAGE, user.getProfileImage());
    editor.putString(KEY_ABOUT, user.getAbout());

    editor.apply();
  }

  // this method will checker whether user is already logged in or not
  public boolean isLoggedIn() {
    SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    return sharedPreferences.getString(KEY_PHONE, null) != null;
  }

  // this method will give the logged in user
  public UserProfile getLoginUser() {
    SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    return new UserProfile(
        sharedPreferences.getString(KEY_PHONE, null),
        sharedPreferences.getString(KEY_FULLNAME, null),
        sharedPreferences.getString(KEY_ABOUT, null),
        sharedPreferences.getString(KEY_IMAGE, null),
        sharedPreferences.getString(KEY_ID, null),
        sharedPreferences.getString(KEY_UID, null));
  }

  // this method will logout the user
  public void logout() {
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    UserProfile user2 = SharedPrefManager.getInstance(mCtx).getLoginUser();

    db.collection("users_table")
        .document(user2.userId.toLowerCase())
        .delete()
        .addOnSuccessListener(
            new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
                // Do Something
                FirebaseAuth.getInstance().signOut();
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                // Do Something
                FirebaseAuth.getInstance().signOut();
              }
            });

    FileUtil.deleteDir(mCtx.getExternalCacheDir());
    SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.clear();
    editor.commit();
    editor.apply();
    Intent intent = new Intent(mCtx, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    mCtx.startActivity(intent);
  }
}
