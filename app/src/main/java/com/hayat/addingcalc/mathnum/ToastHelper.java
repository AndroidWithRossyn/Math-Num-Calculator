package com.hayat.addingcalc.mathnum;



import android.content.Context;
import android.widget.Toast;

public class ToastHelper {

    public static void showToastLong(final String text, Context context) {
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static void showToastShort(final String text, Context context) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
































