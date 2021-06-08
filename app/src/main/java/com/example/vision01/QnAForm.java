package com.example.vision01;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class QnAForm extends AppCompatActivity {

    ImageButton buttonClose;
    TextView textViewEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qna_form);

        textViewEmail.findViewById(R.id.textViewQnA_Email);
        buttonClose.findViewById(R.id.imageButtonClose);

        buttonClose.setOnClickListener(view -> {
            finish();
        });

    }
}
