/*
 * Copyright (C) 2019-2020 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Smart Flasher, which is a simple app aimed to make flashing
 * recovery zip files much easier. Significant amount of code for this app has been from
 * Kernel Adiutor by Willi Ye <williye97@gmail.com>.
 *
 * Smart Flasher is a free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Smart Flasher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Smart Flasher. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.smartpack.smartflasher.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.root.RootUtils;
import com.smartpack.smartflasher.views.dialog.Dialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashSet;
import java.util.Set;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class ViewUtils {

    public static int getTextSecondaryColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, value, true);
        return value.data;
    }

    public static Drawable getSelectableBackground(Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});
        Drawable drawable = typedArray.getDrawable(0);
        typedArray.recycle();
        return drawable;
    }

    public static void showDialog(FragmentManager manager, DialogFragment fragment) {
        FragmentTransaction ft = manager.beginTransaction();
        Fragment prev = manager.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        fragment.show(ft, "dialog");
    }

    public static void dismissDialog(FragmentManager manager) {
        FragmentTransaction ft = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag("dialog");
        if (fragment != null) {
            ft.remove(fragment).commit();
        }
    }

    public static int getColorPrimaryColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
        return value.data;
    }

    public static int getThemeAccentColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    public static Dialog dialogBuilder(CharSequence message, DialogInterface.OnClickListener negativeListener,
                                       DialogInterface.OnClickListener positiveListener,
                                       DialogInterface.OnDismissListener dismissListener, Context context) {
        Dialog dialog = new Dialog(context).setMessage(message);
        if (negativeListener != null) {
            dialog.setNegativeButton(context.getString(R.string.cancel), negativeListener);
        }
        if (positiveListener != null) {
            dialog.setPositiveButton(context.getString(R.string.ok), positiveListener);
        }
        if (dismissListener != null) {
            dialog.setOnDismissListener(dismissListener);
        }
        return dialog;
    }

    private static final Set<CustomTarget> mProtectedFromGarbageCollectorTargets = new HashSet<>();

    private static class CustomTarget implements Target {
        private ImageView mImageView;
        private int mMaxWidth;
        private int mMaxHeight;

        private CustomTarget(ImageView imageView, int maxWidth, int maxHeight) {
            mImageView = imageView;
            mMaxWidth = maxWidth;
            mMaxHeight = maxHeight;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mImageView.setImageBitmap(scaleDownBitmap(bitmap, mMaxWidth, mMaxHeight));
            mProtectedFromGarbageCollectorTargets.remove(this);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mProtectedFromGarbageCollectorTargets.remove(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

    public static Bitmap scaleDownBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int newWidth = width;
        int newHeight = height;

        if (maxWidth != 0 && newWidth > maxWidth) {
            newHeight = Math.round((float) maxWidth / newWidth * newHeight);
            newWidth = maxWidth;
        }

        if (maxHeight != 0 && newHeight > maxHeight) {
            newWidth = Math.round((float) maxHeight / newHeight * newWidth);
            newHeight = maxHeight;
        }

        return width != newWidth || height != newHeight ? resizeBitmap(bitmap, newWidth, newHeight) : bitmap;
    }

    private static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

    public interface OnDialogEditTextListener {
        void onClick(String text);
    }

    public interface onDialogEditTextsListener {
        void onClick(String text, String text2);
    }

    public static Dialog dialogEditTexts(String text, String text2, String hint, String hint2,
                                         final DialogInterface.OnClickListener negativeListener,
                                         final onDialogEditTextsListener onDialogEditTextListener,
                                         Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) context.getResources().getDimension(R.dimen.dialog_padding);
        layout.setPadding(padding, padding, padding, padding);

        final AppCompatEditText editText = new AppCompatEditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (text != null) {
            editText.append(text);
        }
        if (hint != null) {
            editText.setHint(hint);
        }
        editText.setSingleLine(true);

        final AppCompatEditText editText2 = new AppCompatEditText(context);
        editText2.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        if (text2 != null) {
            editText2.setText(text2);
        }
        if (hint2 != null) {
            editText2.setHint(hint2);
        }
        editText2.setSingleLine(true);

        layout.addView(editText);
        layout.addView(editText2);

        Dialog dialog = new Dialog(context).setView(layout);
        if (negativeListener != null) {
            dialog.setNegativeButton(context.getString(R.string.cancel), negativeListener);
        }
        if (onDialogEditTextListener != null) {
            dialog
                    .setPositiveButton(context.getString(R.string.ok), (dialog1, which)
                            -> onDialogEditTextListener.onClick(
                            editText.getText().toString(), editText2.getText().toString()))
                    .setOnDismissListener(dialog1 -> {
                        if (negativeListener != null) {
                            negativeListener.onClick(dialog1, 0);
                        }
                    });
        }
        return dialog;
    }

    public static Dialog dialogEditText(String text, final DialogInterface.OnClickListener negativeListener,
                                        final OnDialogEditTextListener onDialogEditTextListener,
                                        Context context) {
        return dialogEditText(text, negativeListener, onDialogEditTextListener, -1, context);
    }

    public static Dialog dialogEditText(String text, final DialogInterface.OnClickListener negativeListener,
                                        final OnDialogEditTextListener onDialogEditTextListener, int inputType,
                                        Context context) {
        LinearLayout layout = new LinearLayout(context);
        int padding = (int) context.getResources().getDimension(R.dimen.dialog_padding);
        layout.setPadding(padding, padding, padding, padding);

        final AppCompatEditText editText = new AppCompatEditText(context);
        editText.setGravity(Gravity.CENTER);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (text != null) {
            editText.append(text);
        }
        editText.setSingleLine(true);
        if (inputType >= 0) {
            editText.setInputType(inputType);
        }

        layout.addView(editText);

        Dialog dialog = new Dialog(context).setView(layout);
        if (negativeListener != null) {
            dialog.setNegativeButton(context.getString(R.string.cancel), negativeListener);
        }
        if (onDialogEditTextListener != null) {
            dialog.setPositiveButton(context.getString(R.string.ok), (dialog1, which)
                    -> onDialogEditTextListener.onClick(editText.getText().toString()))
                    .setOnDismissListener(dialog1 -> {
                        if (negativeListener != null) {
                            negativeListener.onClick(dialog1, 0);
                        }
                    });
        }
        return dialog;
    }

    public static void rebootDialog(Context context) {
        Dialog rebootDialog = new Dialog(context);
        rebootDialog.setMessage(context.getString(R.string.reboot_dialog));
        rebootDialog.setCancelable(false);
        rebootDialog.setNegativeButton(context.getString(R.string.cancel), (dialog1, id1) -> {
        });
        rebootDialog.setPositiveButton(context.getString(R.string.reboot), (dialog1, id1) -> {
            new AsyncTask<Void, Void, Void>() {
                private ProgressDialog mProgressDialog;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mProgressDialog = new ProgressDialog(context);
                    mProgressDialog.setMessage(context.getString(R.string.rebooting) + ("..."));
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                }
                @Override
                protected Void doInBackground(Void... voids) {
                    RootUtils.runCommand(Utils.prepareReboot());
                    return null;
                }
                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    try {
                        mProgressDialog.dismiss();
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }.execute();
        });
        rebootDialog.show();
    }

}
