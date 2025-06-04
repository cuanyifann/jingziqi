package com.example.jingziqi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.Stack;

public class JingziqiView extends View {
    private static final int BOARD_SIZE = 3;
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE]; // 0=空, 1=X, 2=O
    private int cellSize; // 每个格子的大小
    private Paint gridPaint, xPaint, oPaint, winLinePaint;
    private int currentPlayer = 1; // 1=X, 2=O
    private boolean gameOver = false;
    private int winner = 0; // 0=未结束, 1=X赢, 2=O赢, 3=平局
    private int[] winLine = new int[4]; // 胜利线的起止坐标 [x1, y1, x2, y2]
    private Stack<MoveRecord> moveHistory = new Stack<>(); // 悔棋记录栈
    private int[] undoCounts = new int[3]; // 悔棋次数 [0]=未使用, [1]=玩家X, [2]=玩家O
    private static final int MAX_UNDO = 1; // 每位玩家最大悔棋次数
    private OnGameStatusListener statusListener;

    public JingziqiView(Context context) {
        super(context);
        init();
    }

    public JingziqiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 初始化画笔
        gridPaint = new Paint();
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStrokeWidth(5f);
        gridPaint.setAntiAlias(true);

        xPaint = new Paint();
        xPaint.setColor(Color.RED);
        xPaint.setStrokeWidth(10f);
        xPaint.setStyle(Paint.Style.STROKE);
        xPaint.setAntiAlias(true);

        oPaint = new Paint();
        oPaint.setColor(Color.BLUE);
        oPaint.setStrokeWidth(10f);
        oPaint.setStyle(Paint.Style.STROKE);
        oPaint.setAntiAlias(true);

        winLinePaint = new Paint();
        winLinePaint.setColor(Color.GREEN);
        winLinePaint.setStrokeWidth(15f);
        winLinePaint.setStyle(Paint.Style.STROKE);
        winLinePaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        cellSize = size / BOARD_SIZE;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
        drawPieces(canvas);
        drawWinLine(canvas);
    }

    private void drawGrid(Canvas canvas) {
        // 绘制横线
        for (int i = 1; i < BOARD_SIZE; i++) {
            canvas.drawLine(0, i * cellSize, getWidth(), i * cellSize, gridPaint);
        }
        // 绘制竖线
        for (int i = 1; i < BOARD_SIZE; i++) {
            canvas.drawLine(i * cellSize, 0, i * cellSize, getHeight(), gridPaint);
        }
    }

    private void drawPieces(Canvas canvas) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 1) {
                    drawX(canvas, i, j);
                } else if (board[i][j] == 2) {
                    drawO(canvas, i, j);
                }
            }
        }
    }

    private void drawX(Canvas canvas, int row, int col) {
        float padding = cellSize * 0.2f;
        float x = col * cellSize + cellSize / 2f;
        float y = row * cellSize + cellSize / 2f;
        canvas.drawLine(x - cellSize / 2f + padding, y - cellSize / 2f + padding,
                x + cellSize / 2f - padding, y + cellSize / 2f - padding, xPaint);
        canvas.drawLine(x + cellSize / 2f - padding, y - cellSize / 2f + padding,
                x - cellSize / 2f + padding, y + cellSize / 2f - padding, xPaint);
    }

    private void drawO(Canvas canvas, int row, int col) {
        float padding = cellSize * 0.2f;
        float x = col * cellSize + cellSize / 2f;
        float y = row * cellSize + cellSize / 2f;
        canvas.drawCircle(x, y, cellSize / 2f - padding, oPaint);
    }

    private void drawWinLine(Canvas canvas) {
        if (winner != 0 && winner != 3) { // 有获胜方
            canvas.drawLine(winLine[0], winLine[1], winLine[2], winLine[3], winLinePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !gameOver) {
            float x = event.getX();
            float y = event.getY();
            int col = (int) (x / cellSize);
            int row = (int) (y / cellSize);

            if (isValidMove(row, col)) {
                // 记录历史
                moveHistory.push(new MoveRecord(row, col, currentPlayer));

                // 落子
                board[row][col] = currentPlayer;
                invalidate();

                // 检查胜负
                if (checkWin(row, col)) {
                    gameOver = true;
                    winner = currentPlayer;
                    notifyStatus("玩家 " + (currentPlayer == 1 ? "X" : "O") + " 获胜！");
                } else if (isBoardFull()) {
                    gameOver = true;
                    winner = 3; // 平局
                    notifyStatus("游戏平局！");
                } else {
                    // 切换玩家
                    currentPlayer = currentPlayer == 1 ? 2 : 1;
                    notifyStatus("玩家 " + (currentPlayer == 1 ? "X" : "O") + " 回合");
                }
            }
        }
        return true;
    }

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && board[row][col] == 0;
    }

    private boolean checkWin(int row, int col) {
        int player = board[row][col];
        int width = getWidth();
        int height = getHeight();

        // 检查行
        boolean win = true;
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (board[row][i] != player) {
                win = false;
                break;
            }
        }
        if (win) {
            winLine[0] = 0;
            winLine[1] = (int) (row * cellSize + cellSize / 2f);
            winLine[2] = width;
            winLine[3] = (int) (row * cellSize + cellSize / 2f);
            return true;
        }

        // 检查列
        win = true;
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (board[i][col] != player) {
                win = false;
                break;
            }
        }
        if (win) {
            winLine[0] = (int) (col * cellSize + cellSize / 2f);
            winLine[1] = 0;
            winLine[2] = (int) (col * cellSize + cellSize / 2f);
            winLine[3] = height;
            return true;
        }

        // 检查对角线
        if (row == col) {
            win = true;
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (board[i][i] != player) {
                    win = false;
                    break;
                }
            }
            if (win) {
                winLine[0] = 0;
                winLine[1] = 0;
                winLine[2] = width;
                winLine[3] = height;
                return true;
            }
        }

        // 检查反对角线
        if (row + col == BOARD_SIZE - 1) {
            win = true;
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (board[i][BOARD_SIZE - 1 - i] != player) {
                    win = false;
                    break;
                }
            }
            if (win) {
                winLine[0] = width;
                winLine[1] = 0;
                winLine[2] = 0;
                winLine[3] = height;
                return true;
            }
        }

        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public void resetGame() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = 0;
            }
        }
        currentPlayer = 1;
        gameOver = false;
        winner = 0;
        moveHistory.clear();
        undoCounts[1] = 0; // 重置玩家X悔棋次数
        undoCounts[2] = 0; // 重置玩家O悔棋次数
        invalidate();
        notifyStatus("玩家 X 回合");
    }

    public void undoLastMove() {
        if (moveHistory.isEmpty()) {
            if (statusListener != null) {
                statusListener.onStatusUpdate("没有可悔棋的步骤");
            }
            return;
        }

        // 获取当前玩家的上一步操作
        MoveRecord lastMove = moveHistory.peek();
        int player = lastMove.player;

        // 检查当前玩家悔棋次数
        if (undoCounts[player] >= MAX_UNDO) {
            if (statusListener != null) {
                statusListener.onStatusUpdate("玩家 " + (player == 1 ? "X" : "O") + " 已用完悔棋次数");
            }
            return;
        }

        // 执行悔棋
        moveHistory.pop();
        board[lastMove.row][lastMove.col] = 0;

        // 切换回上一个玩家
        currentPlayer = player;

        // 重置游戏状态
        if (gameOver) {
            gameOver = false;
            winner = 0;
        }

        // 增加当前玩家悔棋计数
        undoCounts[player]++;

        invalidate();
        notifyStatus("玩家 " + (currentPlayer == 1 ? "X" : "O") + " 回合");
    }

    // 获取指定玩家的剩余悔棋次数
    public int getRemainingUndoCount(int player) {
        return MAX_UNDO - undoCounts[player];
    }

    public void setOnGameStatusListener(OnGameStatusListener listener) {
        this.statusListener = listener;
    }

    private void notifyStatus(String message) {
        if (statusListener != null) {
            statusListener.onStatusUpdate(message);
        }
    }

    public interface OnGameStatusListener {
        void onStatusUpdate(String message);
    }

    // 落子记录类
    private static class MoveRecord {
        int row;
        int col;
        int player; // 1=X, 2=O

        public MoveRecord(int row, int col, int player) {
            this.row = row;
            this.col = col;
            this.player = player;
        }
    }
}