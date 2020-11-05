package com.jayaraj.hime.util;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.jayaraj.hime.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

public class FileUtil {
  private static final int EOF = -1;
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  private FileUtil() {}

  public static File from(Context context, Uri uri) throws IOException {

    String fileName = getFileName(context, uri);
    String[] splitName = splitFileName(fileName);
    File tempFile = File.createTempFile(splitName[0], splitName[1]);
    tempFile = rename(tempFile, fileName);
    tempFile.deleteOnExit();
    try {
      InputStream inputStream = context.getContentResolver().openInputStream(uri);

      FileOutputStream out = new FileOutputStream(tempFile);

      copy(inputStream, out);

    } catch (FileNotFoundException e) {
      Log.e("Excep", e.getMessage());
    } catch (Exception e) {
      Log.e("Exc", e.getMessage());
    }

    return tempFile;
  }

  private static String[] splitFileName(String fileName) {
    String name = fileName;
    String extension = "";
    int i = fileName.lastIndexOf('.');
    if (i != -1) {
      name = fileName.substring(0, i);
      extension = fileName.substring(i);
    }

    return new String[] {name, extension};
  }

  public static String getFileName(Context context, Uri uri) {

    String result = null;
    String path = uri.toString();
    Log.e("FilePath", path);
    File myFile = new File(path);
    if (uri.getScheme().equals("content")) {
      try {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
          result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
      } catch (Exception e) {
        Log.e("Excep", e.getMessage());
      }
    } else if (path.startsWith("file://")) {
      result = myFile.getName();

      Log.d("msg>>>>  ", result);
    }
    if (result == null) {
      result = uri.getPath();
      int cut = result.lastIndexOf(File.separator);
      if (cut != -1) {
        result = result.substring(cut + 1);
      }
    }
    return result;
  }

  private static File rename(File file, String newName) {
    File newFile = new File(file.getParent(), newName);
    if (!newFile.equals(file)) {
      if (newFile.exists() && newFile.delete()) {
        Log.d("FileUtil", "Delete old " + newName + " file");
      }
      if (file.renameTo(newFile)) {
        Log.d("FileUtil", "Rename file to " + newName);
      }
    }
    return newFile;
  }

  private static long copy(InputStream input, OutputStream output) throws IOException {
    long count = 0;
    int n;
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    while (EOF != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  public byte[] getBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];

    int len;
    while ((len = inputStream.read(buffer)) != -1) {
      byteBuffer.write(buffer, 0, len);
    }
    return byteBuffer.toByteArray();
  }

  public static Uri createCapturedFile(Context context) {
    File file = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
    return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
  }

  public static boolean deleteDir(File dir) {
    if (dir != null && dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
      return dir.delete();
    } else if (dir != null && dir.isFile()) {
      return dir.delete();
    } else {
      return false;
    }
  }

  @SuppressLint("NewApi")
  public static String getPath(Context context, Uri uri) throws URISyntaxException {
    String selection = null;
    String[] selectionArgs = null;

    if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
      if (isExternalStorageDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        return Environment.getExternalStorageDirectory() + "/" + split[1];
      } else if (isDownloadsDocument(uri)) {
        final String id = DocumentsContract.getDocumentId(uri);
        uri =
            ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
      } else if (isMediaDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];
        if ("image".equals(type)) {
          uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
          uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
          uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        selection = "_id=?";
        selectionArgs = new String[] {split[1]};
      }
    }
    if ("content".equalsIgnoreCase(uri.getScheme())) {
      String[] projection = {MediaStore.Images.Media.DATA};
      Cursor cursor = null;
      try {
        cursor =
            context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        if (cursor.moveToFirst()) {
          Log.d("File Path", cursor.getString(column_index));
          return cursor.getString(column_index);
        }
      } catch (Exception e) {
        Log.e("String", e.toString());
      }
    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
      return uri.getPath();
    }
    return null;
  }

  public static boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  public static boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  public static boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }
}
