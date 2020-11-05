package com.jayaraj.hime.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class ImageCompressor {

  private ImageCompressor() {}

  public static byte[] compressImage(Context context, Uri imageUri) throws IOException {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    File actualImage = FileUtil.from(context, imageUri);
    long fileSize = actualImage.length();
    long fileSizeinKb = fileSize / 1024;
    long filesizinMb = fileSizeinKb / 1024;
    Log.e("Before compression", getReadableFileSize(fileSize));

    Bitmap compressedImage =
        MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
    if (filesizinMb > 20) {
      compressedImage.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
      Log.e("20Mb compression", getReadableFileSize(byteArrayOutputStream.toByteArray().length));

    } else if (filesizinMb > 15) {
      compressedImage.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
      Log.e("15Mb compression", getReadableFileSize(byteArrayOutputStream.toByteArray().length));

    } else if (filesizinMb > 10) {
      compressedImage.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
      Log.e("10Mb compression", getReadableFileSize(byteArrayOutputStream.toByteArray().length));

    } else if (filesizinMb > 5) {
      compressedImage.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
      Log.e("5Mb compression", getReadableFileSize(byteArrayOutputStream.toByteArray().length));

    } else {
      compressedImage.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
      Log.e("0Mb compression", getReadableFileSize(byteArrayOutputStream.toByteArray().length));
    }
    return byteArrayOutputStream.toByteArray();
  }

  public static String getReadableFileSize(long size) {
    if (size <= 0) {
      return "0";
    }
    final String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
        + " "
        + units[digitGroups];
  }
}
