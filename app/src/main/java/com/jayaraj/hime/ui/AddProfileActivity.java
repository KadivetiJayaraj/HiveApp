package com.jayaraj.hime.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jayaraj.hime.R;
import com.jayaraj.hime.model.StaticConfig;
import com.jayaraj.hime.model.UserProfile;
import com.jayaraj.hime.ui.chat.MainChatScreen;
import com.jayaraj.hime.ui.chat.MainTabScreen;
import com.jayaraj.hime.ui.quickblox.App;
import com.jayaraj.hime.ui.quickblox.activities.BaseActivity;
import com.jayaraj.hime.ui.quickblox.services.LoginService;
import com.jayaraj.hime.ui.quickblox.utils.Consts;
import com.jayaraj.hime.ui.quickblox.utils.QBEntityCallbackImpl;
import com.jayaraj.hime.ui.quickblox.utils.SharedPrefsHelper;
import com.jayaraj.hime.ui.quickblox.utils.ToastUtils;
import com.jayaraj.hime.util.ImageUtils;
import com.jayaraj.hime.util.SharedPrefManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddProfileActivity extends BaseActivity {

  CircleImageView profileImage;
  EditText nameEdit;
  EditText aboutEdit;
  Button done;
  Uri mCropImageUri;
  String imageBase64;
  String name;
  String about;
  FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
  private final FirebaseFirestore db = FirebaseFirestore.getInstance();
  String token;
  String Id;
  String number;
  private QBUser userForSave;
  private String TAG = AddProfileActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_profile);
    profileImage = findViewById(R.id.profileImage);
    nameEdit = findViewById(R.id.profileName);
    aboutEdit = findViewById(R.id.about);
    done = findViewById(R.id.done);

    done.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            name = nameEdit.getText().toString();
            about = aboutEdit.getText().toString();
            if (imageBase64 == null || imageBase64.isEmpty()) {
              imageBase64 = "default";
            }
            if (about == null || about.isEmpty()) {
              about = "Hi, I am using HiveApp";
            }
            if (name == null || name.isEmpty()) {
              Toast.makeText(
                      AddProfileActivity.this, "Please enter profile name", Toast.LENGTH_SHORT)
                  .show();
            } else {
              UserProfileChangeRequest profileUpdates =
                  new UserProfileChangeRequest.Builder().setDisplayName(name).build();
              user.updateProfile(profileUpdates);
              uploadToFirebase();
            }
          }
        });

    number = user.getPhoneNumber().substring(3);
    Id = "hive" + number;

    FirebaseDatabase.getInstance()
        .getReference()
        .child("users")
        .child(Id)
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                  HashMap userinfo = (HashMap) dataSnapshot.getValue();
                  imageBase64 = (String) userinfo.get("profileImage");
                  name = (String) userinfo.get("name");
                  about = (String) userinfo.get("about");
                  nameEdit.setText(name);
                  aboutEdit.setText(about);
                  if (imageBase64.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                    profileImage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                  } else {
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    Bitmap src =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profileImage.setImageBitmap(src);
                  }
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {}
            });

    profileImage.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            requestPermission();
          }
        });
  }

  private void requestPermission() {
    Dexter.withActivity(AddProfileActivity.this)
        .withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        .withListener(
            new MultiplePermissionsListener() {
              @RequiresApi(api = Build.VERSION_CODES.M)
              @Override
              public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                  // do you work now
                  selectImage();
                }
                for (int i = 0; i < report.getDeniedPermissionResponses().size(); i++) {
                  // check for permanent denial of any permission

                }
                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied()) {
                  // permission is denied permenantly, navigate user to app settings
                  showSettingsDialog();
                }
              }

              @Override
              public void onPermissionRationaleShouldBeShown(
                  List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
              }
            })
        .check();
  }

  private void showSettingsDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(AddProfileActivity.this);
    builder.setTitle("Need Permissions");
    builder.setMessage(
        "This app needs permission to use this feature. You can grant them in app settings.");
    builder.setPositiveButton(
        "GOTO SETTINGS",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            openSettings();
          }
        });
    builder.setNegativeButton(
        "Cancel",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
    builder.show();
  }

  private void openSettings() {
    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    Uri uri = Uri.fromParts("package", getPackageName(), null);
    intent.setData(uri);
    startActivityForResult(intent, 101);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void selectImage() {

    if (CropImage.isExplicitCameraPermissionRequired(this)) {
      requestPermissions(
          new String[] {Manifest.permission.CAMERA},
          CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
    } else {
      CropImage.startPickImageActivity(this);
    }
  }

  private void cropImage(Uri uri) {
    CropImage.activity(uri)
        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
        .setAspectRatio(1, 1)
        .setScaleType(CropImageView.ScaleType.FIT_CENTER)
        .start(this);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {
      boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
      String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

      if (isLoginSuccess) {
        saveUserData(userForSave);
        signInCreatedUser(userForSave);
      } else {
        ToastUtils.longToast(getString(R.string.login_chat_login_error) + errorMessage);
      }
    }

    if (resultCode == Activity.RESULT_OK && data != null) {
      if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        final Uri resultUri = result.getUri();

        InputStream inputStream = null;
        try {
          Bitmap src =
              MediaStore.Images.Media.getBitmap(
                  getApplicationContext().getContentResolver(), resultUri);
          profileImage.setImageBitmap(src);

          inputStream = getContentResolver().openInputStream(resultUri);

          Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
          imgBitmap = ImageUtils.cropToSquare(imgBitmap);
          InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
          final Bitmap liteImage =
              ImageUtils.makeImageLite(
                  is,
                  imgBitmap.getWidth(),
                  imgBitmap.getHeight(),
                  ImageUtils.AVATAR_WIDTH,
                  ImageUtils.AVATAR_HEIGHT);

          imageBase64 = ImageUtils.encodeBase64(liteImage);

        } catch (FileNotFoundException e) {
          Log.e("Exception", e.toString());
        } catch (IOException e) {
          Log.e("Exception", e.toString());
        }
      }
    }
    if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
        && resultCode == AppCompatActivity.RESULT_OK) {
      Uri imageUri = CropImage.getPickImageResultUri(this, data);
      // For API >= 23 we need to check specifically that we have permissions to read external
      // storage,
      // but we don't know if we need to for the URI so the simplest is to try open the stream and
      // see if we get error.
      if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {

        // request permissions and handle the result in onRequestPermissionsResult()
        mCropImageUri = imageUri;
        requestPermissions(
            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
            CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
      } else {

        cropImage(imageUri);
      }
    }
  }

  private void uploadToFirebase() {

    @SuppressLint("HardwareIds")
    String serviceNo = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

    String modelNo =
        Build.MANUFACTURER
            + " "
            + Build.MODEL
            + " "
            + Build.VERSION.RELEASE
            + " "
            + Build.VERSION_CODES.class.getFields()[Build.VERSION.SDK_INT].getName();

    UserProfile userProfile = new UserProfile();
    userProfile.setPhonenumber(user.getPhoneNumber());
    userProfile.setName(name);
    userProfile.setAbout(about);
    userProfile.setUserId(Id);
    userProfile.setProfileImage(imageBase64);
    userProfile.setUserUid(user.getUid());
    userProfile.modelNo = modelNo;
    userProfile.serviceNo = serviceNo;
    SharedPrefManager.getInstance(getApplicationContext()).userLogin(userProfile);

    DatabaseReference userDb =
        FirebaseDatabase.getInstance().getReference().child("users").child(Id);
    userDb
        .setValue(userProfile)
        .addOnCompleteListener(
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                userForSave = createUserWithEnteredData();
                startSignUpNewUser(userForSave);

                FirebaseInstanceId.getInstance()
                    .getInstanceId()
                    .addOnCompleteListener(
                        new OnCompleteListener<InstanceIdResult>() {
                          @Override
                          public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                              return;
                            }

                            token = task.getResult().getToken();

                            Map<String, Object> user1 = new HashMap<>();
                            user1.put("fcmToken", token);

                            db.collection("users_table")
                                .document(Id.toLowerCase())
                                .set(user1)
                                .addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void aVoid) {

                                        Intent intent =
                                            new Intent(
                                                AddProfileActivity.this, MainTabScreen.class);
                                        startActivity(intent);
                                        finish();
                                      }
                                    })
                                .addOnFailureListener(
                                    new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(
                                            AddProfileActivity.this,
                                            e.toString(),
                                            Toast.LENGTH_SHORT);
                                      }
                                    }); // Get new Instance ID token
                          }
                        });
              }
            });
  }

  private void startSignUpNewUser(final QBUser newUser) {
    Log.d(TAG, "SignUp New User");
    requestExecutor.signUpNewUser(
        newUser,
        new QBEntityCallback<QBUser>() {
          @Override
          public void onSuccess(QBUser result, Bundle params) {
            Log.d(TAG, "SignUp Successful");
            saveUserData(newUser);
            loginToChat(result);
          }

          @Override
          public void onError(QBResponseException e) {
            Log.d(TAG, "Error SignUp" + e.getMessage());
            if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
              signInCreatedUser(newUser);
            } else {
              ToastUtils.longToast(R.string.sign_up_error);
            }
          }
        });
  }

  private void loginToChat(final QBUser qbUser) {
    qbUser.setPassword(App.USER_DEFAULT_PASSWORD);
    userForSave = qbUser;
    startLoginService(qbUser);
  }

  private void saveUserData(QBUser qbUser) {
    SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
    sharedPrefsHelper.saveQbUser(qbUser);
  }

  private QBUser createUserWithEnteredData() {
    return createQBUserWithCurrentData(user.getPhoneNumber(), String.valueOf(name));
  }

  private QBUser createQBUserWithCurrentData(String userLogin, String userFullName) {
    QBUser qbUser = null;
    if (!TextUtils.isEmpty(userLogin) && !TextUtils.isEmpty(userFullName)) {
      qbUser = new QBUser();
      qbUser.setLogin(userLogin);
      qbUser.setFullName(userFullName);
      qbUser.setPassword(App.USER_DEFAULT_PASSWORD);
    }
    return qbUser;
  }

  private void signInCreatedUser(final QBUser qbUser) {
    Log.d(TAG, "SignIn Started");
    requestExecutor.signInUser(
        qbUser,
        new QBEntityCallbackImpl<QBUser>() {
          @Override
          public void onSuccess(QBUser user, Bundle params) {
            Log.d(TAG, "SignIn Successful");
            sharedPrefsHelper.saveQbUser(userForSave);
            updateUserOnServer(qbUser);
          }

          @Override
          public void onError(QBResponseException responseException) {
            Log.d(TAG, "Error SignIn" + responseException.getMessage());
            ToastUtils.longToast(R.string.sign_in_error);
          }
        });
  }

  private void startLoginService(QBUser qbUser) {
    Intent tempIntent = new Intent(this, LoginService.class);
    PendingIntent pendingIntent =
        createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
    LoginService.start(this, qbUser, pendingIntent);
  }

  private void updateUserOnServer(QBUser user) {
    user.setPassword(null);
    QBUsers.updateUser(user)
        .performAsync(
            new QBEntityCallback<QBUser>() {
              @Override
              public void onSuccess(QBUser qbUser, Bundle bundle) {
                /// Do Something
              }

              @Override
              public void onError(QBResponseException e) {
                ToastUtils.longToast(R.string.update_user_error);
              }
            });
  }
}
