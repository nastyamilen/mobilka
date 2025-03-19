package com.example.mobiiilkaaaaa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "GamePrefs";
    private static final String GAME_STARTED_KEY = "gameStarted";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button startButton = findViewById(R.id.startButton);
        Button continueButton = findViewById(R.id.continueButton);
        
        // Проверяем, была ли начата игра ранее
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean gameStarted = prefs.getBoolean(GAME_STARTED_KEY, false);
        
        // Если игра не была начата, скрываем кнопку "Продолжить"
        if (!gameStarted) {
            continueButton.setVisibility(View.GONE);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сохраняем информацию о том, что игра была начата
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(GAME_STARTED_KEY, true);
                editor.apply();
                
                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
                intent.putExtra("continueGame", true);
                startActivity(intent);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
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
}
