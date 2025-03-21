package com.example.mobiiilkaaaaa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MatchThreePrefs";
    private static final String GAME_STATE_KEY = "gameState";
    private static final String SCORE_KEY = "score";
    private static final String TIMER_KEY = "timer";
    private static final long TIME_BONUS = 30000; // 30 секунд в миллисекундах
    private static final int SCORE_FOR_BONUS = 500; // Очки для бонуса времени
    
    private int GRID_SIZE; // Будет установлено из настроек
    private long INITIAL_TIME; // Будет установлено из настроек
    
    private GridView gridView;
    private TextView scoreTextView;
    private TextView timerTextView;
    private GameAdapter gameAdapter;
    private int score = 0;
    private int selectedPosition = -1;
    private boolean isAnimating = false;
    private Handler handler = new Handler();
    private boolean continueGame = false;
    
    // Переменные для таймера
    private CountDownTimer gameTimer;
    private long timeLeftInMillis;
    private boolean timerRunning = false;
    private int lastBonusThreshold = 0; // Последний порог для бонуса времени

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Загружаем настройки
        GRID_SIZE = SettingsFragment.getGridSize(this);
        INITIAL_TIME = SettingsFragment.getInitialTime(this);
        
        gridView = findViewById(R.id.gridView);
        scoreTextView = findViewById(R.id.scoreTextView);
        timerTextView = findViewById(R.id.timerTextView);
        
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
            timeLeftInMillis = INITIAL_TIME;
            updateTimerText();
            gridView.setAdapter(gameAdapter);
            Log.d("MainActivity", "GridView adapter set");
        }

        // Настройка кнопки возврата в главное меню
        Button backToMenuButton = findViewById(R.id.backToMenuButton);
        backToMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сохраняем состояние игры перед выходом
                pauseTimer();
                saveGameState();
                finish(); // Закрываем текущую активность и возвращаемся в предыдущую (главное меню)
            }
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("MainActivity", "Item clicked at position: " + position);
            if (isAnimating || !timerRunning) {
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
        
        // Запускаем таймер
        startTimer();
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
        // Дополнительная проверка на валидность позиций
        if (pos1 < 0 || pos1 >= GRID_SIZE * GRID_SIZE || pos2 < 0 || pos2 >= GRID_SIZE * GRID_SIZE) {
            Log.e("MainActivity", "Invalid positions: " + pos1 + ", " + pos2);
            return;
        }
        
        isAnimating = true;
        gameAdapter.swapItems(pos1, pos2);
        
        Log.d("MainActivity", "Swapping items at positions: " + pos1 + ", " + pos2);

        handler.postDelayed(() -> {
            if (checkForMatches()) {
                // Valid move
                Log.d("MainActivity", "Valid move - matches found");
                processMatches();
            } else {
                // Invalid move - swap back
                Log.d("MainActivity", "Invalid move - no matches found, swapping back");
                gameAdapter.swapItems(pos1, pos2);
                isAnimating = false;
            }
        }, 300);
    }

    private boolean checkForMatches() {
        // Проверяем все позиции в сетке
        for (int position = 0; position < GRID_SIZE * GRID_SIZE; position++) {
            if (gameAdapter.isPartOfMatch(position)) {
                return true;
            }
        }
        return false;
    }

    private void processMatches() {
        boolean foundMatch = false;
        Set<Integer> processedPositions = new HashSet<>();
        
        Log.d("MainActivity", "Processing matches...");

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
                int scoreToAdd = horizontalMatches.size() * 10;
                updateScore(scoreToAdd);
                foundMatch = true;
                
                Log.d("MainActivity", "Horizontal match found with " + horizontalMatches.size() + 
                                     " gems starting at position " + position + ". Added " + scoreToAdd + " points.");
            }
            
            List<Integer> verticalMatches = gameAdapter.getVerticalMatchPositions(position);
            if (!verticalMatches.isEmpty()) {
                processedPositions.addAll(verticalMatches);
                
                // Удаляем все совпадающие элементы
                for (int pos : verticalMatches) {
                    gameAdapter.removeGem(pos);
                }
                
                // Обновляем счет (10 очков за каждый элемент)
                int scoreToAdd = verticalMatches.size() * 10;
                updateScore(scoreToAdd);
                foundMatch = true;
                
                Log.d("MainActivity", "Vertical match found with " + verticalMatches.size() + 
                                     " gems starting at position " + position + ". Added " + scoreToAdd + " points.");
            }
        }

        if (foundMatch) {
            Log.d("MainActivity", "Matches found. Dropping gems...");
            handler.postDelayed(() -> {
                gameAdapter.dropGems();
                handler.postDelayed(() -> {
                    if (checkForMatches()) {
                        Log.d("MainActivity", "Matches found after dropping gems. Processing again...");
                        processMatches();
                    } else {
                        Log.d("MainActivity", "No more matches found. Ending turn.");
                        isAnimating = false;
                    }
                }, 300);
            }, 300);
        } else {
            Log.d("MainActivity", "No matches found. Ending turn.");
            isAnimating = false;
        }
    }

    private void updateScore(int points) {
        score += points;
        scoreTextView.setText("Счет: " + score);
        
        // Проверяем, достигнут ли порог для добавления времени
        int bonusThreshold = score / SCORE_FOR_BONUS;
        if (bonusThreshold > lastBonusThreshold) {
            int bonusesToAdd = bonusThreshold - lastBonusThreshold;
            long bonusTime = TIME_BONUS * bonusesToAdd;
            
            // Обновляем оставшееся время
            timeLeftInMillis += bonusTime;
            lastBonusThreshold = bonusThreshold;
            
            // Если таймер активен - перезапускаем с новым временем
            if (timerRunning) {
                gameTimer.cancel();
                startTimer();
            }
            
            // Обновляем отображение
            updateTimerText();
            Toast.makeText(this, "Бонус! +" + (bonusTime/1000) + " секунд!", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Time bonus added: " + bonusTime + "ms. Total time: " + timeLeftInMillis);
        }
    }

    private void saveGameState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SCORE_KEY, score);
        editor.putString(GAME_STATE_KEY, gameAdapter.saveGameState());
        editor.putLong(TIMER_KEY, timeLeftInMillis);
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
        
        timeLeftInMillis = prefs.getLong(TIMER_KEY, INITIAL_TIME);
        updateTimerText();
        
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

    private void startTimer() {
        gameTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                showResultDialog();
            }
        }.start();
        
        timerRunning = true;
    }

    private void pauseTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        timerRunning = false;
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeText = String.format("Время: %02d:%02d", minutes, seconds);
        timerTextView.setText(timeText);
    }

    private void showResultDialog() {
        // Получаем текущий рекорд для сравнения
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        int bestScore = dbHelper.getBestScore();
        boolean isNewRecord = score > bestScore;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isNewRecord ? "Новый рекорд!" : "Результат");
        
        String message = "Ваш результат: " + score + " очков";
        if (isNewRecord) {
            message += "\nПоздравляем! Это новый рекорд!";
        } else if (bestScore > 0) {
            message += "\nТекущий рекорд: " + bestScore + " очков";
        }
        
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Сохраняем результат в базу данных
            dbHelper.addScore(score);
            Log.d("MainActivity", "Score saved to database: " + score + ", is new record: " + isNewRecord);
            finish(); // Закрываем текущую активность и возвращаемся в предыдущую (главное меню)
        });
        builder.setCancelable(false); // Предотвращаем закрытие диалога нажатием "назад"
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Возобновляем таймер, если он был приостановлен
        if (!timerRunning && timeLeftInMillis > 0) {
            startTimer();
            Log.d("MainActivity", "Timer resumed on onResume. Time left: " + timeLeftInMillis);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Останавливаем таймер и сохраняем состояние игры
        if (timerRunning) {
            pauseTimer();
            Log.d("MainActivity", "Timer paused on onPause. Time left: " + timeLeftInMillis);
        }
        saveGameState();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Если таймер все еще работает при уничтожении активности, останавливаем его
        if (timerRunning) {
            pauseTimer();
        }
        
        // Сохраняем результат в базу данных только если игра закончилась естественным путем (таймер истек)
        // и результат не был уже сохранен через диалоговое окно
        if (score > 0 && timeLeftInMillis <= 0) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            int bestScore = dbHelper.getBestScore();
            boolean isNewRecord = score > bestScore;
            
            dbHelper.addScore(score);
            Log.d("MainActivity", "Score saved to database on onDestroy: " + score + ", is new record: " + isNewRecord);
        }
    }
}