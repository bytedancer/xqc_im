package com.bonade.xxp.xqc_android_im.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bonade.xxp.xqc_android_im.App;

public class ViewUtil {

    private static Toast mToast;

    private ViewUtil() {
    }

    public static int getResId(
            @NonNull String resName,
            @NonNull String defType) {
        try {
            String packageName = App.getContext().getPackageName();
            Resources resources = App.getContext().getPackageManager().getResourcesForApplication(packageName);

            int resId = resources.getIdentifier(resName, defType, packageName);

            return resId;
        } catch (Exception e) {
        }
        return 0;
    }

    public static int getStringResId(@NonNull String resName) {
        return getResId(resName, "string");
    }

    public static int getDrawableResId(@NonNull String resName) {
        return getResId(resName, "drawable");
    }

    public static void setTextViewValue(Activity context,
                                        @IdRes int txtId,
                                        @NonNull String content) {
        if (context != null)
            ((TextView) context.findViewById(txtId)).setText(content);
    }

    public static void setTextViewValue(View container,
                                        @IdRes int txtId,
                                        @NonNull String content) {
        ((TextView) container.findViewById(txtId)).setText(content);
    }

    public static void setTextViewValue(Activity context,
                                        View container,
                                        @IdRes int txtId,
                                        @StringRes int contentId) {
        if (context != null)
            ((TextView) container.findViewById(txtId)).setText(context.getString(contentId));
    }

    public static void setImgResource(Activity context,
                                      @IdRes int imgId,
                                      @DrawableRes int sourceId) {
        if (context != null)
            ((ImageView) context.findViewById(imgId)).setImageResource(sourceId);
    }

    public static void setImgResource(Activity context,
                                      @IdRes int imgId,
                                      @NonNull Bitmap source) {
        if (context != null)
            ((ImageView) context.findViewById(imgId)).setImageBitmap(source);
    }

    public static void setImgResource(View container,
                                      @IdRes int imgId,
                                      @DrawableRes int sourceId) {
        ((ImageView) container.findViewById(imgId)).setImageResource(sourceId);
    }

    public static void setImgResource(View container,
                                      @IdRes int imgId,
                                      @NonNull Bitmap source) {
        ((ImageView) container.findViewById(imgId)).setImageBitmap(source);
    }

    public static void showMessage(@NonNull String message) {
        if (mToast == null) {
            mToast = Toast.makeText(App.getContext(), message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void showMessage(@StringRes int messageId) {
        if (mToast == null) {
            mToast = Toast.makeText(App.getContext(), messageId, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(messageId);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static MaterialDialog progressDialog;

    public static MaterialDialog createProgressDialog(Context context, String message) {
        dismissProgressDialog();

        progressDialog = new MaterialDialog.Builder(context)
                .content(message)
                .progress(true, 0)
                .cancelable(false)
                .show();

        return progressDialog;
    }

    public static void updateProgressDialog(String message) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setContent(message);
        }
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (IllegalArgumentException e) {
            }
            progressDialog = null;
        }
    }

    public static boolean isProgressDialogShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }
}
