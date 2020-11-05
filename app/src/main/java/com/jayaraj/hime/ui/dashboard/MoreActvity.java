package com.jayaraj.hime.ui.dashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jayaraj.hime.R;
import com.jayaraj.hime.model.StaticConfig;
import com.jayaraj.hime.model.UserProfile;
import com.jayaraj.hime.ui.quickblox.activities.BaseActivity;
import com.jayaraj.hime.ui.quickblox.services.LoginService;
import com.jayaraj.hime.ui.quickblox.utils.UsersUtils;
import com.jayaraj.hime.util.SharedPrefManager;
import com.quickblox.messages.services.SubscribeService;

import de.hdodenhof.circleimageview.CircleImageView;

public class MoreActvity extends BaseActivity {
  CircleImageView profileImage;
  TextView userName;
  TextView userNumber;
  TextView userAbout;

  LinearLayout editprofile;
  RelativeLayout appinfo;
  RelativeLayout settings;
  RelativeLayout notifications;
  RelativeLayout helpandSupport;
  RelativeLayout share;
  RelativeLayout signOut;

  String imageBase64;
  String name;
  String about;
  String phoneNumber;
  String userId;
  SharedPrefManager sharedPrefManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_more_actvity);

    profileImage = findViewById(R.id.profileImage);
    userName = findViewById(R.id.username);
    userNumber = findViewById(R.id.mobilenumber);
    userAbout = findViewById(R.id.aboutTextview);

    appinfo = findViewById(R.id.about_view);
    settings = findViewById(R.id.settings_view);
    notifications = findViewById(R.id.notification_view);
    helpandSupport = findViewById(R.id.help_view);
    share = findViewById(R.id.share_view);
    signOut = findViewById(R.id.logoutView);
    editprofile = findViewById(R.id.edit_profile);
    LinearLayout back = findViewById(R.id.backview);
    back.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            finish();
          }
        });

    sharedPrefManager = SharedPrefManager.getInstance(getApplicationContext());

    UserProfile userProfile = sharedPrefManager.getLoginUser();
    name = userProfile.name;
    about = userProfile.about;
    phoneNumber = userProfile.phonenumber;
    userId = userProfile.userId;
    imageBase64 = userProfile.profileImage;
    userNumber.setText(phoneNumber);
    userName.setText(name);
    userAbout.setText(about);
    if (imageBase64.equals(StaticConfig.STR_DEFAULT_BASE64)) {
      profileImage.setImageResource(R.drawable.ic_account_circle_black_24dp);
    } else {
      byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
      Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
      profileImage.setImageBitmap(src);
    }

    editprofile.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(MoreActvity.this, EditUserProfile.class);
            startActivity(intent);
          }
        });
    appinfo.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {}
        });
    settings.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {}
        });
    notifications.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {}
        });
    helpandSupport.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {}
        });
    share.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {}
        });
    signOut.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.d("TAG", "Removing User data, and Logout");
            SubscribeService.unSubscribeFromPushes(MoreActvity.this);
            LoginService.logout(MoreActvity.this);
            UsersUtils.removeUserData(getApplicationContext());
            requestExecutor.signOut();
            SharedPrefManager.getInstance(getApplicationContext()).logout();
            finish();
          }
        });
  }
}
