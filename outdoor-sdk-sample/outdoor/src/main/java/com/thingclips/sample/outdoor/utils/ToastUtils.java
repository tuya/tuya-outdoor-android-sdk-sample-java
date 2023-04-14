package com.thingclips.sample.outdoor.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @author qinyun.miao
 */
public class ToastUtils {
    private static Toast toast;

    public static void show(Context context, String text) {
        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

}
