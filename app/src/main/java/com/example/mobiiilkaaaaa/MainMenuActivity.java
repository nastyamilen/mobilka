package com.example.mobiiilkaaaaa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class MainMenuActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "GamePrefs";
    private static final String GAME_STARTED_KEY = "gameStarted";
    private TextView bestScoreTextView;
    private View mainMenuContent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        bestScoreTextView = findViewById(R.id.bestScoreTextView);
        mainMenuContent = findViewById(R.id.mainMenuContent);
        
        Button startButton = findViewById(R.id.startButton);
        Button continueButton = findViewById(R.id.continueButton);
        Button showScoresButton = findViewById(R.id.showScoresButton);
        Button settingsButton = findViewById(R.id.settingsButton);
        Button helpButton = findViewById(R.id.helpButton);
        
        // Загружаем и отображаем лучший результат
        updateBestScore();
        
        // Проверяем, была ли начата игра ранее
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean gameStarted = prefs.getBoolean(GAME_STARTED_KEY, false);
        
        // Если игра не была начата, скрываем кнопку "Продолжить"
        if (!gameStarted) {
            continueButton.setVisibility(View.GONE);
        }

        // Загружаем анимации
        Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        
        // Применяем анимацию к заголовку
        bestScoreTextView.startAnimation(fadeInAnim);
        
        // Применяем анимацию к кнопкам с задержкой
        new Handler().postDelayed(() -> startButton.startAnimation(slideUpAnim), 100);
        new Handler().postDelayed(() -> continueButton.startAnimation(slideUpAnim), 200);
        new Handler().postDelayed(() -> showScoresButton.startAnimation(slideUpAnim), 300);
        new Handler().postDelayed(() -> settingsButton.startAnimation(slideUpAnim), 400);
        new Handler().postDelayed(() -> helpButton.startAnimation(slideUpAnim), 500);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сохраняем информацию о том, что игра была начата
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(GAME_STARTED_KEY, true);
                editor.apply();
                
                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
                intent.putExtra("continueGame", true);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        
        showScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ScoresActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем фрагмент настроек
                SettingsFragment settingsFragment = new SettingsFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                    R.anim.slide_up, R.anim.slide_down,
                    R.anim.slide_up, R.anim.slide_down
                );
                transaction.replace(R.id.fragmentContainer, settingsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                
                // Скрываем основное содержимое меню
                mainMenuContent.setVisibility(View.GONE);
            }
        });
        
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем фрагмент помощи
                HelpFragment helpFragment = new HelpFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                    R.anim.slide_up, R.anim.slide_down,
                    R.anim.slide_up, R.anim.slide_down
                );
                transaction.replace(R.id.fragmentContainer, helpFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                
                // Скрываем основное содержимое меню
                mainMenuContent.setVisibility(View.GONE);
            }
        });
        
        // Настраиваем слушатель для обработки возврата из фрагментов
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                // Если стек фрагментов пуст, показываем основное содержимое меню
                mainMenuContent.setVisibility(View.VISIBLE);
                
                // Применяем анимацию к элементам меню при возврате
                bestScoreTextView.startAnimation(fadeInAnim);
                new Handler().postDelayed(() -> startButton.startAnimation(slideUpAnim), 100);
                new Handler().postDelayed(() -> continueButton.startAnimation(slideUpAnim), 200);
                new Handler().postDelayed(() -> showScoresButton.startAnimation(slideUpAnim), 300);
                new Handler().postDelayed(() -> settingsButton.startAnimation(slideUpAnim), 400);
                new Handler().postDelayed(() -> helpButton.startAnimation(slideUpAnim), 500);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Обновляем лучший результат при возвращении в меню
        updateBestScore();
        
        // Проверяем, была ли начата игра
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean gameStarted = prefs.getBoolean(GAME_STARTED_KEY, false);
        
        // Показываем или скрываем кнопку "Продолжить" в зависимости от того, была ли начата игра
        Button continueButton = findViewById(R.id.continueButton);
        if (gameStarted) {
            continueButton.setVisibility(View.VISIBLE);
        } else {
            continueButton.setVisibility(View.GONE);
        }
    }
    
    private void updateBestScore() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        int bestScore = dbHelper.getBestScore();
        bestScoreTextView.setText("Лучший результат: " + bestScore);
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
