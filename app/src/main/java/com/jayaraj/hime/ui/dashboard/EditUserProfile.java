package com.jayaraj.hime.ui.dashboard;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jayaraj.hime.R;
import com.jayaraj.hime.model.StaticConfig;
import com.jayaraj.hime.model.UserProfile;
import com.jayaraj.hime.ui.quickblox.utils.ToastUtils;
import com.jayaraj.hime.util.ImageUtils;
import com.jayaraj.hime.util.SharedPrefManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserProfile extends AppCompatActivity {

  LinearLayout nameLayout;
  LinearLayout aboutLayout;
  RelativeLayout profileImageLayout;
  TextView profileNumber;
  TextView profileName;
  TextView profileAbout;
  CircleImageView profileImage;
  Uri mCropImageUri;
  String imageBase64;
  String name;
  String about;
  String phoneNumber;
  String userId;
  FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
  private final FirebaseFirestore db = FirebaseFirestore.getInstance();
  SharedPrefManager sharedPrefManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_user_profile);

    nameLayout = findViewById(R.id.nameLayout);
    aboutLayout = findViewById(R.id.aboutLayout);
    profileImageLayout = findViewById(R.id.profileImageLayout);
    profileName = findViewById(R.id.nameText);
    profileNumber = findViewById(R.id.mobilenumber);
    profileAbout = findViewById(R.id.aboutText);
    profileImage = findViewById(R.id.profileImage);
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
    profileNumber.setText(phoneNumber);
    profileName.setText(name);
    profileAbout.setText(about);
    if (imageBase64.equals(StaticConfig.STR_DEFAULT_BASE64)) {
      profileImage.setImageResource(R.drawable.ic_account_circle_black_24dp);
    } else {
      byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
      Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
      profileImage.setImageBitmap(src);
    }
    profileImageLayout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            if (imageBase64.isEmpty()
                || imageBase64.equals(StaticConfig.STR_DEFAULT_BASE64)
                || imageBase64 == null) {
              requestPermission();

            } else {
              profilePicRemoveUpdate();
            }
          }
        });
    nameLayout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            ViewGroup viewGroup = findViewById(android.R.id.content);
            // then we will inflate the custom alert dialog xml that we created
            View dialogView =
                LayoutInflater.from(EditUserProfile.this)
                    .inflate(R.layout.my_dialog, viewGroup, false);

            // Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(EditUserProfile.this);
            // setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);

            // finally creating the alert dialog and displaying it
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
            TextView title = dialogView.findViewById(R.id.title);
            TextView no = dialogView.findViewById(R.id.no);
            TextView yes = dialogView.findViewById(R.id.yes);
            EditText editText = dialogView.findViewById(R.id.titleEdit);
            title.setText("Enter your name");
            editText.setText(name);
            no.setText("Cancel");
            yes.setText("Continue");

            no.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    alertDialog.dismiss();
                  }
                });
            yes.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    String namedit = editText.getText().toString();
                    if (namedit.isEmpty()) {
                      ToastUtils.shortToast("Name can't be empty");
                    } else {
                      DatabaseReference userDb =
                          FirebaseDatabase.getInstance()
                              .getReference()
                              .child("users")
                              .child(userId);
                      userDb.child("name").setValue(namedit);
                      alertDialog.dismiss();
                      profileName.setText(namedit);
                    }
                  }
                });
          }
        });

    aboutLayout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            ViewGroup viewGroup = findViewById(android.R.id.content);
            // then we will inflate the custom alert dialog xml that we created
            View dialogView =
                LayoutInflater.from(EditUserProfile.this)
                    .inflate(R.layout.my_dialog, viewGroup, false);

            // Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(EditUserProfile.this);
            // setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);

            // finally creating the alert dialog and displaying it
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
            TextView title = dialogView.findViewById(R.id.title);
            TextView no = dialogView.findViewById(R.id.no);
            TextView yes = dialogView.findViewById(R.id.yes);
            EditText editText = dialogView.findViewById(R.id.titleEdit);
            title.setText("Add about");
            editText.setText(about);
            no.setText("Cancel");
            yes.setText("Continue");

            no.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    alertDialog.dismiss();
                  }
                });
            yes.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    String abtedit = editText.getText().toString();
                    if (abtedit.isEmpty()) {
                      ToastUtils.shortToast("About can't be empty");
                    } else {
                      DatabaseReference userDb =
                          FirebaseDatabase.getInstance()
                              .getReference()
                              .child("users")
                              .child(userId);
                      userDb.child("about").setValue(abtedit);
                      profileAbout.setText(abtedit);
                      alertDialog.dismiss();
                    }
                  }
                });
          }
        });
  }

  private void profilePicRemoveUpdate() {
    View view = getLayoutInflater().inflate(R.layout.custom_dialog_profile_options_menu, null);
    LinearLayout remove = view.findViewById(R.id.layoutRemove);
    LinearLayout edit = view.findViewById(R.id.layoutUpdate);

    final Dialog mBottomSheetDialog = new Dialog(EditUserProfile.this, R.style.MaterialDialogSheet);
    mBottomSheetDialog.setContentView(view);
    mBottomSheetDialog.setCancelable(true);
    mBottomSheetDialog
        .getWindow()
        .setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
    mBottomSheetDialog.show();

    remove.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mBottomSheetDialog.dismiss();
            imageBase64 = StaticConfig.STR_DEFAULT_BASE64;
            DatabaseReference userDb =
                FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            userDb.child("profileImage").setValue(imageBase64);
            if (imageBase64.equals(StaticConfig.STR_DEFAULT_BASE64)) {
              profileImage.setImageResource(R.drawable.ic_account_circle_black_24dp);
            } else {
              byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
              Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
              profileImage.setImageBitmap(src);
            }
          }
        });

    edit.setOnClickListener(
        new View.OnClickListener() {
          @RequiresApi(api = Build.VERSION_CODES.M)
          @Override
          public void onClick(View v) {
            mBottomSheetDialog.dismiss();
            requestPermission();
          }
        });
  }

  private void requestPermission() {
    Dexter.withActivity(EditUserProfile.this)
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
    AlertDialog.Builder builder = new AlertDialog.Builder(EditUserProfile.this);
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
          DatabaseReference userDb =
              FirebaseDatabase.getInstance().getReference().child("users").child(userId);
          userDb.child("profileImage").setValue(imageBase64);

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
}
