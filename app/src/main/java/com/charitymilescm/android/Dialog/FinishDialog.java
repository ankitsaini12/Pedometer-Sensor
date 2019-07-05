package com.charitymilescm.android.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.charitymilescm.android.R;

public class FinishDialog extends Dialog implements View.OnClickListener {
    private Dialog dialog;

    private Button btnContinue, btnFinish;
    private View.OnClickListener listener;

    public FinishDialog(@NonNull Context context, View.OnClickListener listener) {
        super(context);
        dialog = this;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_finish);
        init();
        setListener();
    }

    private void setListener() {
        btnContinue.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
    }

    private void init() {
        btnContinue = findViewById(R.id.btnContinue);
        btnFinish = findViewById(R.id.btnFinish);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnContinue: {
                dialog.dismiss();
                break;
            }

            case R.id.btnFinish: {
                dialog.dismiss();
                listener.onClick(view);
                break;
            }
        }
    }
}
