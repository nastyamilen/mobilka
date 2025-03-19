package com.example.mobiiilkaaaaa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int GRID_SIZE = 8;
    private static final int MATCH_MIN = 3;
    private static final String PREFS_NAME = "GamePrefs";
    private static final String GAME_STATE_KEY = "gameState";
    private static final String SCORE_KEY = "score";
    
    private GridView gridView;
    private TextView scoreTextView;
    private GameAdapter gameAdapter;
    private int score = 0;
    private int selectedPosition = -1;
    private boolean isAnimating = false;
    private Handler handler = new Handler();
    private boolean continueGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = findViewById(R.id.gridView);
        scoreTextView = findViewById(R.id.scoreTextView);
        
        // Проверяем, нужно ли продолжить предыдущую игру
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            continueGame = extras.getBoolean("continueGame", false);
        }
        
        if (continueGame) {
            // Загружаем сохраненное состояние игры
            loadGameState();
        } else {
            // Начинаем новую игру
            updateScore(0);
            Log.d("MainActivity", "Creating GameAdapter with grid size: " + GRID_SIZE);
            gameAdapter = new GameAdapter(this, GRID_SIZE);
            gridView.setAdapter(gameAdapter);
            Log.d("MainActivity", "GridView adapter set");
        }

        // Настройка кнопки возврата в главное меню
        Button backToMenuButton = findViewById(R.id.backToMenuButton);
        backToMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сохраняем состояние игры перед выходом
                saveGameState();
                finish(); // Закрываем текущую активность и возвращаемся в предыдущую (главное меню)
            }
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("MainActivity", "Item clicked at position: " + position);
            if (isAnimating) {
                return;
            }

            if (selectedPosition == -1) {
                // First selection
                selectedPosition = position;
                view.setAlpha(0.6f);
            } else {
                // Second selection - try to swap
                View firstView = gridView.getChildAt(selectedPosition - gridView.getFirstVisiblePosition());
                if (firstView != null) {
                    firstView.setAlpha(1.0f);
                }

                if (isAdjacent(selectedPosition, position)) {
                    trySwap(selectedPosition, position);
                }
                selectedPosition = -1;
            }
        });
    }

    private boolean isAdjacent(int pos1, int pos2) {
        int row1 = pos1 / GRID_SIZE;
        int col1 = pos1 % GRID_SIZE;
        int row2 = pos2 / GRID_SIZE;
        int col2 = pos2 % GRID_SIZE;

        return (Math.abs(row1 - row2) == 1 && col1 == col2) ||
               (Math.abs(col1 - col2) == 1 && row1 == row2);
    }

    private void trySwap(int pos1, int pos2) {
        isAnimating = true;
        gameAdapter.swapItems(pos1, pos2);

        handler.postDelayed(() -> {
            if (checkForMatches()) {
                // Valid move
                processMatches();
            } else {
                // Invalid move - swap back
                gameAdapter.swapItems(pos1, pos2);
                isAnimating = false;
            }
        }, 300);
    }

    private boolean checkForMatches() {
        // Check rows
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE - 2; col++) {
                int pos = row * GRID_SIZE + col;
                if (checkMatch(pos, pos + 1, pos + 2)) {
                    return true;
                }
            }
        }

        // Check columns
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row < GRID_SIZE - 2; row++) {
                int pos = row * GRID_SIZE + col;
                if (checkMatch(pos, pos + GRID_SIZE, pos + GRID_SIZE * 2)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkMatch(int pos1, int pos2, int pos3) {
        Integer gem1 = gameAdapter.getItemId(pos1);
        Integer gem2 = gameAdapter.getItemId(pos2);
        Integer gem3 = gameAdapter.getItemId(pos3);
        return gem1 != null && gem1.equals(gem2) && gem1.equals(gem3);
    }

    private void processMatches() {
        boolean foundMatch = false;

        // Check and remove horizontal matches
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE - 2; col++) {
                int pos = row * GRID_SIZE + col;
                if (checkMatch(pos, pos + 1, pos + 2)) {
                    gameAdapter.removeGem(pos);
                    gameAdapter.removeGem(pos + 1);
                    gameAdapter.removeGem(pos + 2);
                    foundMatch = true;
                    updateScore(30);
                }
            }
        }

        // Check and remove vertical matches
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row < GRID_SIZE - 2; row++) {
                int pos = row * GRID_SIZE + col;
                if (checkMatch(pos, pos + GRID_SIZE, pos + GRID_SIZE * 2)) {
                    gameAdapter.removeGem(pos);
                    gameAdapter.removeGem(pos + GRID_SIZE);
                    gameAdapter.removeGem(pos + GRID_SIZE * 2);
                    foundMatch = true;
                    updateScore(30);
                }
            }
        }

        if (foundMatch) {
            handler.postDelayed(() -> {
                gameAdapter.dropGems();
                handler.postDelayed(() -> {
                    if (checkForMatches()) {
                        processMatches();
                    } else {
                        isAnimating = false;
                    }
                }, 300);
            }, 300);
        } else {
            isAnimating = false;
        }
    }

    private void updateScore(int points) {
        score += points;
        scoreTextView.setText("Счет: " + score);
    }

    private void saveGameState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SCORE_KEY, score);
        editor.putString(GAME_STATE_KEY, gameAdapter.saveGameState());
        editor.apply();
        Log.d("MainActivity", "Game state saved. Score: " + score);
    }

    private void loadGameState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        score = prefs.getInt(SCORE_KEY, 0);
        updateScore(0);
        
        Log.d("MainActivity", "Creating GameAdapter with grid size: " + GRID_SIZE);
        gameAdapter = new GameAdapter(this, GRID_SIZE);
        
        // Загружаем состояние камней
        String gameState = prefs.getString(GAME_STATE_KEY, "");
        if (!gameState.isEmpty()) {
            gameAdapter.loadGameState(gameState);
            Log.d("MainActivity", "Game state loaded. Score: " + score);
        }
        
        gridView.setAdapter(gameAdapter);
        Log.d("MainActivity", "GridView adapter set");
    }
}