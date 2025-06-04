package com.example.jingziqi;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btn_start);
        Button btnRule = findViewById(R.id.btn_rule);
        Button btnExit = findViewById(R.id.btn_exit);

        btnStart.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, JingziqiActivity.class));
        });

        btnRule.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RuleActivity.class));
        });

        btnExit.setOnClickListener(v -> finish());
    }
}