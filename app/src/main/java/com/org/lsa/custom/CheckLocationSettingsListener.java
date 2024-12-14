package com.org.lsa.custom;

import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public final class CheckLocationSettingsListener implements OnCompleteListener {
    public final Context context;

    public CheckLocationSettingsListener(Context context) {
        this.context = context;
    }

    public final void onComplete(Task task) {
        Utility.checkGPSEnabled(this.context, task);
    }
}
