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

package com.smartpack.smartflasher.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Flasher;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.utils.root.RootUtils;
import com.smartpack.smartflasher.views.recyclerview.CardView;
import com.smartpack.smartflasher.views.recyclerview.DescriptionView;
import com.smartpack.smartflasher.views.dialog.Dialog;
import com.smartpack.smartflasher.views.recyclerview.RecyclerViewItem;

import java.io.File;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public class FlasherFragment extends RecyclerViewFragment {

    private boolean mPermissionDenied;

    private String mPath;
    @Override
    protected boolean showTopFab() {
        return true;
    }

    @Override
    protected Drawable getTopFabDrawable() {
        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_flash));
        DrawableCompat.setTint(drawable, Color.WHITE);
        return drawable;
    }

    @Override
    protected void init() {
        super.init();

        addViewPagerFragment(DescriptionFragment.newInstance(getString(R.string.flasher),
                getString(R.string.flasher_summary)));
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        SmartPackInit(items);
        requestPermission(0, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void postInit() {
        super.postInit();
    }

    private void SmartPackInit(List<RecyclerViewItem> items) {

        CardView flasherCard = new CardView(getActivity());
        flasherCard.setTitle(getString(R.string.flasher_options));

        DescriptionView kernelinfo = new DescriptionView();
        kernelinfo.setTitle(getString(R.string.kernel) + (" ") + getString(R.string.version));
        if (RootUtils.rootAccess()) {
            kernelinfo.setSummary(RootUtils.runCommand("uname -r"));
        } else {
            kernelinfo.setSummary(getString(R.string.unavailable));
        }

        flasherCard.addItem(kernelinfo);

        // Show wipe (Cache/Data) functions only if we recognize recovery...
        if (Flasher.hasRecovery()) {
            DescriptionView wipe_cache = new DescriptionView();
            wipe_cache.setTitle(getString(R.string.wipe_cache));
            wipe_cache.setSummary(getString(R.string.wipe_cache_summary));
            wipe_cache.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                @Override
                public void onClick(RecyclerViewItem item) {
                    Dialog wipecache = new Dialog(getActivity());
                    wipecache.setIcon(R.mipmap.ic_launcher);
                    wipecache.setTitle(getString(R.string.sure_question));
                    wipecache.setMessage(getString(R.string.wipe_cache_message));
                    wipecache.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    wipecache.setPositiveButton(getString(R.string.wipe_cache), (dialog1, id1) -> {
                        new Execute().execute("echo --wipe_cache > /cache/recovery/command");
                        new Execute().execute(Utils.prepareReboot() + " recovery");
                    });
                    wipecache.show();
                }
            });
            flasherCard.addItem(wipe_cache);

            DescriptionView wipe_data = new DescriptionView();
            wipe_data.setTitle(getString(R.string.wipe_data));
            wipe_data.setSummary(getString(R.string.wipe_data_summary));
            wipe_data.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                @Override
                public void onClick(RecyclerViewItem item) {
                    Dialog wipedata = new Dialog(getActivity());
                    wipedata.setIcon(R.mipmap.ic_launcher);
                    wipedata.setTitle(getString(R.string.sure_question));
                    wipedata.setMessage(getString(R.string.wipe_data_message));
                    wipedata.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    wipedata.setPositiveButton(getString(R.string.wipe_data), (dialog1, id1) -> {
                        new Execute().execute("echo --wipe_data > /cache/recovery/command");
                        new Execute().execute(Utils.prepareReboot() + " recovery");
                    });
                    wipedata.show();
                }
            });
            flasherCard.addItem(wipe_data);
        }

        DescriptionView turnoff = new DescriptionView();
        turnoff.setTitle(getString(R.string.turn_off));
        turnoff.setSummary(getString(R.string.turn_off_summary));
        turnoff.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (RootUtils.rootAccess()) {
                    Dialog turnoff = new Dialog(getActivity());
                    turnoff.setIcon(R.mipmap.ic_launcher);
                    turnoff.setTitle(getString(R.string.sure_question));
                    turnoff.setMessage(getString(R.string.turn_off_message));
                    turnoff.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    turnoff.setPositiveButton(getString(R.string.turn_off), (dialog1, id1) -> {
                        new Execute().execute(Utils.prepareReboot() + " -p");
                    });
                    turnoff.show();
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
                }
            }
        });
        flasherCard.addItem(turnoff);

        DescriptionView reboot = new DescriptionView();
        reboot.setTitle(getString(R.string.reboot));
        reboot.setSummary(getString(R.string.reboot_summary));
        reboot.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (RootUtils.rootAccess()) {
                    Dialog reboot = new Dialog(getActivity());
                    reboot.setIcon(R.mipmap.ic_launcher);
                    reboot.setTitle(getString(R.string.sure_question));
                    reboot.setMessage(getString(R.string.normal_reboot_message));
                    reboot.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    reboot.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                        new Execute().execute(Utils.prepareReboot());
                    });
                    reboot.show();
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
                }
            }
        });
        flasherCard.addItem(reboot);

        DescriptionView recoveryreboot = new DescriptionView();
        recoveryreboot.setTitle(getString(R.string.reboot_recovery));
        recoveryreboot.setSummary(getString(R.string.reboot_recovery_summary));
        recoveryreboot.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (RootUtils.rootAccess()) {
                    Dialog recoveryreboot = new Dialog(getActivity());
                    recoveryreboot.setIcon(R.mipmap.ic_launcher);
                    recoveryreboot.setTitle(getString(R.string.sure_question));
                    recoveryreboot.setMessage(getString(R.string.reboot_recovery_message));
                    recoveryreboot.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    recoveryreboot.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                        new Execute().execute(Utils.prepareReboot() + " recovery");
                    });
                    recoveryreboot.show();
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
                }
            }
        });
        flasherCard.addItem(recoveryreboot);

        DescriptionView bootloaderreboot = new DescriptionView();
        bootloaderreboot.setTitle(getString(R.string.reboot_bootloader));
        bootloaderreboot.setSummary(getString(R.string.reboot_bootloader_summary));
        bootloaderreboot.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (RootUtils.rootAccess()) {
                    Dialog bootloaderreboot = new Dialog(getActivity());
                    bootloaderreboot.setIcon(R.mipmap.ic_launcher);
                    bootloaderreboot.setTitle(getString(R.string.sure_question));
                    bootloaderreboot.setMessage(getString(R.string.reboot_bootloader_message));
                    bootloaderreboot.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    bootloaderreboot.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                        new Execute().execute(Utils.prepareReboot() + " bootloader");
                    });
                    bootloaderreboot.show();
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
                }
            }
        });
        flasherCard.addItem(bootloaderreboot);

        if (flasherCard.size() > 0) {
            items.add(flasherCard);
        }
    }

    @Override
    protected void onTopFabClick() {
        super.onTopFabClick();
        if (!RootUtils.rootAccess()) {
            Utils.toast(R.string.no_root_access, getActivity());
            return;
        }
        if (mPermissionDenied) {
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
            return;
        }
        Intent manualflash = new Intent(Intent.ACTION_GET_CONTENT);
        manualflash.setType("application/zip");
        startActivityForResult(manualflash, 0);
    }

    private void flash_zip_file(final File file) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage(getString(R.string.flashing) + (" ") + file.getName());
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Flasher.manualFlash(file);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                Dialog rebootDialog = new Dialog(getActivity());
                rebootDialog.setMessage(getString(R.string.reboot_dialog));
                rebootDialog.setCancelable(false);
                if (Flasher.isPathLog() && Flasher.isFlashLog()) {
                    rebootDialog.setNeutralButton(getString(R.string.last_flash), (dialog1, id1) -> {
                        Dialog flashLog = new Dialog(getActivity());
                        flashLog.setIcon(R.mipmap.ic_launcher);
                        flashLog.setTitle(getString(R.string.last_flash));
                        flashLog.setMessage(Utils.readFile(Utils.getInternalDataStorage() + "/flasher_log.txt"));
                        flashLog.setCancelable(false);
                        flashLog.setNeutralButton(getString(R.string.cancel), (dialog2, id2) -> {
                        });
                        flashLog.setPositiveButton(getString(R.string.reboot), (dialog2, id2) -> {
                            new Execute().execute(Utils.prepareReboot());
                        });
                        flashLog.show();

                    });
                }
                rebootDialog.setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> {
                });
                rebootDialog.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                    new Execute().execute(Utils.prepareReboot());
                });
                rebootDialog.show();
            }
        }.execute();
    }

    @Override
    public void onPermissionDenied(int request) {
        super.onPermissionDenied(request);
        if (request == 0) {
            mPermissionDenied = true;
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
        }
    }

    private class Execute extends AsyncTask<String, Void, Void> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.executing) + ("..."));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            RootUtils.runCommand(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            File file = new File(uri.getPath());
            mPath = Utils.getPath(file);
            if (Utils.isDocumentsUI(uri)) {
                Dialog dialogueDocumentsUI = new Dialog(getActivity());
                dialogueDocumentsUI.setMessage(getString(R.string.documentsui_message));
                dialogueDocumentsUI.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                });
                dialogueDocumentsUI.show();
                return;
            }
            if (requestCode == 0) {
                Flasher.cleanLogs();
                RootUtils.runCommand("echo '" + mPath + "' > " + Utils.getInternalDataStorage() + "/last_flash.txt");
                if (!file.getName().endsWith(".zip")) {
                    Utils.toast(getString(R.string.file_selection_error), getActivity());
                    return;
                }
                if (Flasher.fileSize(new File(mPath)) <= 100000000) {
                    Dialog flashzip = new Dialog(getActivity());
                    flashzip.setIcon(R.mipmap.ic_launcher);
                    flashzip.setTitle(getString(R.string.flasher));
                    flashzip.setMessage(getString(R.string.sure_message, file.getName()) +
                            getString(R.string.flasher_warning));
                    flashzip.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    flashzip.setPositiveButton(getString(R.string.flash), (dialogInterface, i) -> {
                        flash_zip_file(new File(mPath));
                    });
                    flashzip.show();
                } else {
                    Dialog flashSizeError = new Dialog(getActivity());
                    flashSizeError.setIcon(R.mipmap.ic_launcher);
                    flashSizeError.setTitle(getString(R.string.flasher));
                    flashSizeError.setMessage(getString(R.string.file_size_limit, file.getName()));
                    flashSizeError.setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    flashSizeError.show();
                }
            }
        }
    }
    
}