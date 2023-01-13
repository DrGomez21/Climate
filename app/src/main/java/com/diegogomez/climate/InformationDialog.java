package com.diegogomez.climate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

public class InformationDialog {
    Activity activity;
    AlertDialog dialog;

    InformationDialog(Activity myActivity) {
        activity = myActivity;
    }

    void loadingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }

    void dissmisDialog() {
        dialog.dismiss();
    }

}
