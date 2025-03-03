package com.skillix.merchant.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skillix.merchant.R;

public class ToastUtils {

    // Method to show success toast
    public static void showSuccessToast(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_success, R.color.green);
    }

    // Method to show warning toast
    public static void showWarningToast(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_warning, R.color.yellow);
    }

    // Method to show error toast
    public static void showErrorToast(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_error, R.color.red);
    }

    // Private method to show the custom toast with icon and background color
    private static void showCustomToast(Context context, String message, int iconResId, int colorResId) {
        // Inflate the custom layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        // Set the message and icon
        TextView textView = layout.findViewById(R.id.toast_message);
        textView.setText(message);

        ImageView imageView = layout.findViewById(R.id.toast_icon);
        imageView.setImageResource(iconResId);

        // Set background color based on the toast type
        layout.setBackgroundColor(context.getResources().getColor(colorResId));

        // Create and show the Toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
