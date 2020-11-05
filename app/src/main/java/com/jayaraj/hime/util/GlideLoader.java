package com.jayaraj.hime.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class GlideLoader {

  private GlideLoader() {}

  public static void loadImage(
      final ImageView image, Context context, int placeholder, String imageUrl) {

    Glide.with(context)
        .asBitmap()
        .load(imageUrl)
        .override(1080, 1080)
        .thumbnail(0.5f)
        .placeholder(placeholder)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .dontAnimate()
        .dontTransform()
        .fitCenter()
        .into(
            new CustomTarget<Bitmap>() {

              @Override
              public void onLoadStarted(@Nullable Drawable placeholder) {
                super.onLoadStarted(placeholder);
                image.setImageDrawable(placeholder);
              }

              @Override
              public void onResourceReady(
                  @NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                image.setImageBitmap(resource);
              }

              @Override
              public void onLoadCleared(@Nullable Drawable placeholder) {
                image.setImageDrawable(placeholder);
              }
            });
  }

  public static void loadnewsImage(
      final ImageView image,
      final ImageView blurredImage,
      final Context context,
      int placeholder,
      String imageUrl) {

    Glide.with(context)
        .asBitmap()
        .load(imageUrl)
        .override(720, 720)
        .thumbnail(0.5f)
        .placeholder(placeholder)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .dontAnimate()
        .dontTransform()
        .fitCenter()
        .into(
            new CustomTarget<Bitmap>() {

              @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
              @Override
              public void onLoadStarted(@Nullable Drawable placeholder) {
                super.onLoadStarted(placeholder);
                image.setImageDrawable(placeholder);

                Bitmap bitmap = ((BitmapDrawable) placeholder).getBitmap();
                Bitmap blurredBitmap = BlurBuilder.blur(context, bitmap);
                blurredImage.setImageBitmap(blurredBitmap);
                blurredImage.setColorFilter(0x76AAAAAA, PorterDuff.Mode.MULTIPLY);
              }

              @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
              @Override
              public void onResourceReady(
                  @NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                image.setImageBitmap(resource);

                Bitmap blurredBitmap = BlurBuilder.blur(context, resource);
                blurredImage.setImageBitmap(blurredBitmap);
                blurredImage.setColorFilter(0x76AAAAAA, PorterDuff.Mode.MULTIPLY);
              }

              @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
              @Override
              public void onLoadCleared(@Nullable Drawable placeholder) {
                image.setImageDrawable(placeholder);

                Bitmap bitmap = ((BitmapDrawable) placeholder).getBitmap();
                Bitmap blurredBitmap = BlurBuilder.blur(context, bitmap);
                blurredImage.setImageBitmap(blurredBitmap);
                blurredImage.setColorFilter(0x76AAAAAA, PorterDuff.Mode.MULTIPLY);
              }
            });
  }
}
