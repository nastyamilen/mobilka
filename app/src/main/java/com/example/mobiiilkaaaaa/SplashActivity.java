package com.example.mobiiilkaaaaa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    
    private static final int SPLASH_DURATION = 2500; // 2.5 секунды
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Находим элементы для анимации
        ImageView logoImageView = findViewById(R.id.splashLogoImageView);
        TextView titleTextView = findViewById(R.id.splashTitleTextView);
        
        // Загружаем анимации
        Animation scaleUpAnim = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        
        // Применяем анимации
        logoImageView.startAnimation(scaleUpAnim);
        titleTextView.startAnimation(fadeInAnim);
        
        // Запускаем таймер для перехода на главный экран
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);
            startActivity(intent);
            
            // Применяем анимацию перехода
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            
            // Закрываем сплэш-экран
            finish();
        }, SPLASH_DURATION);
    }
}
