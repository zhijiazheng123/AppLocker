package org.twinone.locker.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.twinone.locker.R;

import org.twinone.locker.util.PrefUtils;


class Dialogs {

    public static AlertDialog getRecoveryCodeDialog(final Context c) {
        PrefUtils prefs = new PrefUtils(c);
        String code = prefs.getString(R.string.pref_key_recovery_code);
        if (code != null) {
            return null;
        }
        // Code = null
        code = PrefUtils.generateRecoveryCode(c);
        // save it directly to avoid it to change
        prefs.put(R.string.pref_key_recovery_code, code).apply();
        final String finalcode = code;
        AlertDialog.Builder ab = new AlertDialog.Builder(c);
        ab.setCancelable(false);
        ab.setNeutralButton(R.string.recovery_code_send_button,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(
                                android.content.Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_TEXT, c.getString(
                                R.string.recovery_intent_message, finalcode));
                        c.startActivity(Intent.createChooser(i,
                                c.getString(R.string.recovery_intent_tit)));
                    }
                });
        ab.setPositiveButton(android.R.string.ok, null);
        ab.setTitle(R.string.recovery_tit);
        ab.setMessage(String.format(c.getString(R.string.recovery_dlgmsg),
                finalcode));
        return ab.create();
    }

    /**
     * Get the dialog to share the app
     */
    public static AlertDialog getShareEditDialog(final Context c,
                                                 boolean addNeverButton) {
        String promoText = c.getString(R.string.share_promo_text);
        final AlertDialog.Builder ab = new AlertDialog.Builder(c);

        LayoutInflater inflater = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.share_dialog, null);
        ab.setView(v);
        final EditText et = (EditText) v
                .findViewById(R.id.share_dialog_et_content);
        et.setText(promoText);

        ab.setCancelable(false);
        ab.setTitle(R.string.lib_share_dlg_tit);
        ab.setMessage(R.string.lib_share_dlg_msg);
        ab.setPositiveButton(android.R.string.ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                final String text = et.getText().toString();
                intent.putExtra(Intent.EXTRA_TEXT, text);
                Intent sender = Intent.createChooser(intent,
                        c.getString(R.string.lib_share_dlg_tit));
                c.startActivity(sender);
                // At this point, we can assume the user will share the app.
                // So never show the dialog again, he can manually open it from
                // the navigation
            }
        });
        ab.setNeutralButton(R.string.share_dlg_later, null);
        if (addNeverButton) {
            ab.setNegativeButton(R.string.share_dlg_never,
                    new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
        return ab.create();
    }
}
