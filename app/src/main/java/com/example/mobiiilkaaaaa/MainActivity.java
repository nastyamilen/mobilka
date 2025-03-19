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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        
        // Устанавливаем размеры ячеек GridView
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int availableWidth = screenWidth - 48; // 48 = padding (16*2) + border padding (8*2)
        int cellSize = availableWidth / GRID_SIZE;
        
        Log.d("MainActivity", "Screen width: " + screenWidth + ", Cell size: " + cellSize);
        
        // Проверяем, нужно ли продолжить предыдущую игру
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            continueGame = extras.getBoolean("continueGame", false);
        }
        
        Log.d("MainActivity", "Creating GameAdapter with grid size: " + GRID_SIZE);
        gameAdapter = new GameAdapter(this, GRID_SIZE);
        gameAdapter.setCellSize(cellSize);
        
        if (continueGame) {
            // Загружаем сохраненное состояние игры
            loadGameState();
        } else {
            // Начинаем новую игру
            updateScore(0);
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
        return gameAdapter.isPartOfMatch(0) || 
               gameAdapter.isPartOfMatch(1) || 
               gameAdapter.isPartOfMatch(2) || 
               gameAdapter.isPartOfMatch(3) || 
               gameAdapter.isPartOfMatch(4) || 
               gameAdapter.isPartOfMatch(5) || 
               gameAdapter.isPartOfMatch(6) || 
               gameAdapter.isPartOfMatch(7) || 
               gameAdapter.isPartOfMatch(8) || 
               gameAdapter.isPartOfMatch(9) || 
               gameAdapter.isPartOfMatch(10) || 
               gameAdapter.isPartOfMatch(11) || 
               gameAdapter.isPartOfMatch(12) || 
               gameAdapter.isPartOfMatch(13) || 
               gameAdapter.isPartOfMatch(14) || 
               gameAdapter.isPartOfMatch(15);
    }

    private void processMatches() {
        boolean foundMatch = false;
        Set<Integer> processedPositions = new HashSet<>();

        // Проверяем все возможные начальные позиции
        for (int position = 0; position < GRID_SIZE * GRID_SIZE; position++) {
            if (processedPositions.contains(position)) continue;
            
            List<Integer> horizontalMatches = gameAdapter.getHorizontalMatchPositions(position);
            if (!horizontalMatches.isEmpty()) {
                processedPositions.addAll(horizontalMatches);
                
                // Удаляем все совпадающие элементы
                for (int pos : horizontalMatches) {
                    gameAdapter.removeGem(pos);
                }
                
                // Обновляем счет (10 очков за каждый элемент)
                updateScore(horizontalMatches.size() * 10);
                foundMatch = true;
                
                Log.d("MainActivity", "Horizontal match found with " + horizontalMatches.size() + " gems");
            }
            
            List<Integer> verticalMatches = gameAdapter.getVerticalMatchPositions(position);
            if (!verticalMatches.isEmpty()) {
                processedPositions.addAll(verticalMatches);
                
                // Удаляем все совпадающие элементы
                for (int pos : verticalMatches) {
                    gameAdapter.removeGem(pos);
                }
                
                // Обновляем счет (10 очков за каждый элемент)
                updateScore(verticalMatches.size() * 10);
                foundMatch = true;
                
                Log.d("MainActivity", "Vertical match found with " + verticalMatches.size() + " gems");
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
        
        // Загружаем состояние камней
        String gameState = prefs.getString(GAME_STATE_KEY, "");
        if (!gameState.isEmpty()) {
            gameAdapter.loadGameState(gameState);
            Log.d("MainActivity", "Game state loaded. Score: " + score);
        }
        
        gridView.setAdapter(gameAdapter);
        Log.d("MainActivity", "GridView adapter set");
        
        // Принудительно обновляем GridView
        gridView.post(new Runnable() {
            @Override
            public void run() {
                gameAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Сохраняем состояние игры
        saveGameState();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Сохраняем результат в базу данных
        if (score > 0) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            dbHelper.addScore(score);
            Log.d("MainActivity", "Score saved to database: " + score);
        }
    }
}