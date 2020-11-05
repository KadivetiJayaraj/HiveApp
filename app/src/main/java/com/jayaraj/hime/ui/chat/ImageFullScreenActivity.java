package com.jayaraj.hime.ui.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jayaraj.hime.BuildConfig;
import com.jayaraj.hime.R;
import com.jayaraj.hime.notifications.OreoNotification;
import com.jayaraj.hime.util.GlideLoader;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.mateware.snacky.Snacky;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class ImageFullScreenActivity extends AppCompatActivity {

  ImageView back;
  String fileUrl;
  PhotoView mImageView;
  TextView chatName;
  TextView sharedTime;
  String chatFriendId;
  String userName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_full_screen);
    back = findViewById(R.id.back);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    mImageView = findViewById(R.id.imageView);
    chatName = findViewById(R.id.sharedName);
    sharedTime = findViewById(R.id.sharedTime);

    back.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            finish();
          }
        });

    fileUrl = getIntent().getStringExtra("urlPhotoClick");
    chatFriendId = getIntent().getStringExtra("chatId");
    String filesharedDate = getIntent().getStringExtra("sharedDate");
    userName = getIntent().getStringExtra("sharedName");

    if (fileUrl != null) {
      processUrl(fileUrl);
    }
    chatName.setText(userName);
    sharedTime.setText(filesharedDate);
  }

  private void processUrl(String url) {

    GlideLoader.loadImage(
        mImageView, getApplicationContext(), R.drawable.procedure_placeholder, url);
  }

  private void requestPermission() {
    Dexter.withActivity(ImageFullScreenActivity.this)
        .withPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        .withListener(
            new MultiplePermissionsListener() {
              @RequiresApi(api = Build.VERSION_CODES.KITKAT)
              @Override
              public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                  // do you work now
                  downloadDocument();
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

  private void openSettings() {
    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    Uri uri = Uri.fromParts("package", getPackageName(), null);
    intent.setData(uri);
    startActivityForResult(intent, 101);
  }

  private void showSettingsDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(ImageFullScreenActivity.this);
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

  private void downloadDocument() {
    final StorageReference mstorageRefference =
        FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
    try {
      String root = Environment.getExternalStorageDirectory().toString();
      File myDir = new File(root + "/HiMe/Media/HiMe Images");

      if (!myDir.exists()) {
        myDir.mkdirs();
      }

      @SuppressLint("SimpleDateFormat")
      String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());

      final String name = "IMG-" + timeStamp + ".jpg";
      final File downloadFile = new File(myDir, name);
      if (downloadFile.exists()) downloadFile.delete();
      mstorageRefference
          .getFile(downloadFile)
          .addOnSuccessListener(
              new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                  MediaScannerConnection.scanFile(
                      getApplicationContext(),
                      new String[] {downloadFile.getAbsolutePath()},
                      null,
                      new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                          Snacky.builder()
                              .setView(findViewById(android.R.id.content))
                              .setText("Downloaded Successfully")
                              .setDuration(Snacky.LENGTH_LONG)
                              .build()
                              .show();
                          showNotification(name, downloadFile);
                        }
                      });
                }
              })
          .addOnFailureListener(
              new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  Log.e("Exce", e.toString());
                }
              });
    } catch (Exception e) {
      // some action
      Log.e("exception", e.toString());
    }
  }

  private void showNotification(String name, File file) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    Uri fileUri =
        FileProvider.getUriForFile(
            ImageFullScreenActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);

    intent.setDataAndType(fileUri, "image/*");

    List<ResolveInfo> resInfoList =
        getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    for (ResolveInfo info : resInfoList) {
      grantUriPermission(
          info.activityInfo.packageName,
          fileUri,
          FLAG_GRANT_WRITE_URI_PERMISSION | FLAG_GRANT_READ_URI_PERMISSION);
    }

    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            ImageFullScreenActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      sendOreonotify(name, pendingIntent);
    } else {
      sendnotify(name, pendingIntent);
    }
  }

  @TargetApi(Build.VERSION_CODES.O)
  private void sendOreonotify(String name, PendingIntent pendingIntent) {
    Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    OreoNotification oreoNotification = new OreoNotification(this);
    Notification.Builder builder =
        oreoNotification.getOreoNotification(name, "Downloaded", pendingIntent, defaultSound);

    int i = 126;
    oreoNotification.getManager().notify(i, builder.build());
  }

  private void sendnotify(String name, PendingIntent pendingIntent) {
    NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(name)
            .setContentIntent(pendingIntent)
            .setContentText("Downloaded")
            .setVibrate(new long[] {100, 100, 100, 100})
            .setSound(defaultSound);
    int i = 126;
    assert noti != null;
    noti.notify(i, builder.build());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.file_res, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {

    if (item.getItemId() == R.id.download) {
      requestPermission();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
