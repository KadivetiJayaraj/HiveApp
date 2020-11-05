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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jayaraj.hime.BuildConfig;
import com.jayaraj.hime.R;
import com.jayaraj.hime.notifications.OreoNotification;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

import de.mateware.snacky.Snacky;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class FileDownloadActivity extends AppCompatActivity {
  TextView chatName;
  TextView sharedTime;
  TextView fileName;
  TextView filesize;
  Button openDoc;
  ImageView fileImage;
  String mimeType;
  String fileUrl;
  String nameOfFile;
  String sharedName;
  private static final int NOTIFICATION_ID = 125;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_file_download);
    chatName = findViewById(R.id.sharedName);
    sharedTime = findViewById(R.id.sharedTime);
    fileName = findViewById(R.id.filename);
    filesize = findViewById(R.id.fileSize);
    openDoc = findViewById(R.id.openDoc);
    fileImage = findViewById(R.id.filePreview);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ImageView fileBack = findViewById(R.id.back);

    fileBack.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            finish();
          }
        });

    String chatFriendId = getIntent().getStringExtra("chatId");
    fileUrl = getIntent().getStringExtra("fileUrl");
    String filesharedDate = getIntent().getStringExtra("sharedDate");
    String fileExtension = getIntent().getStringExtra("fileExtension");
    String size = getIntent().getStringExtra("fileSize");
    nameOfFile = getIntent().getStringExtra("fileName");
    sharedName = getIntent().getStringExtra("sharedName");
    fileName.setText(nameOfFile);

    chatName.setText(sharedName);

    openDoc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            openDocument(fileUrl, nameOfFile);
          }
        });

    sharedTime.setText(filesharedDate);
    if (fileExtension.equals(".doc") || fileExtension.equals(".docx")) {

      fileImage.setImageResource(R.drawable.doc);
      mimeType = "application/msword";
      filesize.setText(size + " Word Document");

    } else if (fileExtension.equals(".pdf")) {

      fileImage.setImageResource(R.drawable.pdf);
      mimeType = "application/pdf";
      filesize.setText(size + " PDF");

    } else if (fileExtension.equals(".odt")) {

      fileImage.setImageResource(R.drawable.odt_icon_white);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      filesize.setText(size + " Open Document Text");

    } else if (fileExtension.equals(".ppt") || fileExtension.equals(".pptx")) {

      fileImage.setImageResource(R.drawable.ppt);
      mimeType = "application/vnd.ms-powerpoint";
      filesize.setText(size + " PowerPoint Presentation");

    } else if (fileExtension.equals(".xls") || fileExtension.equals(".xlsx")) {

      fileImage.setImageResource(R.drawable.xls);
      mimeType = "application/vnd.ms-excel";
      filesize.setText(size + " Excel Spreadsheet");

    } else if (fileExtension.equals(".txt")) {

      fileImage.setImageResource(R.drawable.txt);
      mimeType = "text/html";
      filesize.setText(size + " Text");

    } else if (fileExtension.equals(".zip")) {

      fileImage.setImageResource(R.drawable.zip);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      filesize.setText(size + " ZIP");

    } else if (fileExtension.equals(".jpg") || fileExtension.equals(".jpeg")) {

      fileImage.setImageResource(R.drawable.jpg);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      filesize.setText(size + " JPG");

    } else if (fileExtension.equals(".html")) {

      fileImage.setImageResource(R.drawable.html);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      filesize.setText(size + " HTML");

    } else if (fileExtension.equals(".png")) {

      fileImage.setImageResource(R.drawable.png);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      filesize.setText(size + " PNG");

    } else if (fileExtension.equals(".exe")) {

      fileImage.setImageResource(R.drawable.exe);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      filesize.setText(size + " Executable file");

    } else if (fileExtension.equals(".apk")) {

      fileImage.setImageResource(R.drawable.apk);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      filesize.setText(size + " APK");

    } else if (fileExtension.equals(".mp3")) {

      fileImage.setImageResource(R.drawable.mp3);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      filesize.setText(size + " MP3");

    } else if (fileExtension.equals(".mp4")) {

      fileImage.setImageResource(R.drawable.mp4);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      filesize.setText(size + " MP4");

    } else {

      fileImage.setImageResource(R.drawable.file);
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
    }
  }

  public void openDocument(String fileUrl, String name) {

    final StorageReference storageReference =
        FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
    final File file = new File(getCacheDir(), name);

    storageReference
        .getFile(file)
        .addOnSuccessListener(
            new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Uri uriFile =
                    FileProvider.getUriForFile(
                        FileDownloadActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);

                Intent viewIntent = new Intent();
                viewIntent.setAction(Intent.ACTION_VIEW);
                viewIntent.setDataAndType(uriFile, mimeType);
                viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(viewIntent);
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.e("Exc", e.toString());
              }
            });
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  private void requestPermission() {
    Dexter.withActivity(FileDownloadActivity.this)
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
                  Log.d(
                      "dennial permision res",
                      report.getDeniedPermissionResponses().get(i).getPermissionName());
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
    AlertDialog.Builder builder = new AlertDialog.Builder(FileDownloadActivity.this);
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
      File myDir = new File(root + "/HiMe/Media/HiMe Documents");

      if (!myDir.exists()) {
        myDir.mkdirs();
      }

      final String name = nameOfFile;
      final File downloadFile = new File(myDir, name);
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
            FileDownloadActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);

    intent.setDataAndType(fileUri, mimeType);

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
            FileDownloadActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
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

    switch (item.getItemId()) {
      case R.id.download:
        requestPermission();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
