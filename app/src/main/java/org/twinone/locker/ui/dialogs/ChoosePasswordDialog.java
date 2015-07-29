package org.twinone.locker.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.twinone.locker.R;

import org.twinone.locker.lock.LockPreferences;
import org.twinone.locker.lock.LockService;

/**
 * Created by twinone on 7/29/15.
 */
public class ChoosePasswordDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle(R.string.old_main_choose_lock_type);
        ab.setItems(R.array.lock_type_names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int type = which == 0 ? LockPreferences.TYPE_PASSWORD
                        : LockPreferences.TYPE_PATTERN;
                LockService.showCreate(getActivity(), type);
            }
        });
        return ab.create();
    }
}
