package com.example.jingziqi;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class JingziqiActivity extends AppCompatActivity {
    private JingziqiView gameView;
    private TextView statusText;
    private TextView undoCountX;
    private TextView undoCountO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jingziqi);

        gameView = findViewById(R.id.gameView);
        statusText = findViewById(R.id.statusText);
        undoCountX = findViewById(R.id.undoCountX);
        undoCountO = findViewById(R.id.undoCountO);
        Button btnReset = findViewById(R.id.btnReset);
        Button btnUndo = findViewById(R.id.btnUndo);
        Button btnBack = findViewById(R.id.btnBack);

        // 设置游戏状态监听
        gameView.setOnGameStatusListener(message -> {
            statusText.setText(message);
            // 更新双方剩余悔棋次数显示
            undoCountX.setText("X 剩余悔棋: " + gameView.getRemainingUndoCount(1));
            undoCountO.setText("O 剩余悔棋: " + gameView.getRemainingUndoCount(2));
            // 胜利或平局时显示 Toast
            if (message.contains("获胜") || message.contains("平局")) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // 重置游戏
        btnReset.setOnClickListener(v -> gameView.resetGame());

        // 悔棋
        btnUndo.setOnClickListener(v -> gameView.undoLastMove());

        // 返回主菜单
        btnBack.setOnClickListener(v -> finish());

        // 初始化显示
        statusText.setText("玩家 X 回合");
        undoCountX.setText("X 剩余悔棋: 1");
        undoCountO.setText("O 剩余悔棋: 1");
    }
}