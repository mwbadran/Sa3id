package com.example.sa3id.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.sa3id.R;

public abstract class EmailInputDialog extends Dialog {
    private EditText etEmailReset;
    private Button btnCancel, btnSubmit;
    private CustomAlertDialog customAlertDialog;

    public EmailInputDialog(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_email_input, null);
        setContentView(view);
        customAlertDialog = new CustomAlertDialog(context);
        etEmailReset = view.findViewById(R.id.etEmailReset);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnCancel.setOnClickListener(v -> dismiss());
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void validateAndSubmit() {
        String email = etEmailReset.getText().toString().trim();
        if (email.isEmpty()) {
            customAlertDialog.show("الرجاء إدخال البريد الإلكتروني", R.drawable.baseline_error_24);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            customAlertDialog.show("الرجاء إدخال بريد إلكتروني صالح", R.drawable.baseline_error_24);
            return;
        }
        onEmailSubmit(email);
        dismiss();
    }

    protected abstract void onEmailSubmit(String email);
}
