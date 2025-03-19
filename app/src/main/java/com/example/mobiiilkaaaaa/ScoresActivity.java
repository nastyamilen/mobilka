package com.example.mobiiilkaaaaa;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ScoresActivity extends AppCompatActivity {

    private ListView scoresListView;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        scoresListView = findViewById(R.id.scoresListView);
        backButton = findViewById(R.id.backButton);

        // Загружаем результаты из базы данных
        loadScores();

        // Настраиваем кнопку "Назад"
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadScores() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        List<DatabaseHelper.ScoreRecord> scoreRecords = dbHelper.getAllScores();
        
        // Преобразуем записи в строки для отображения
        ArrayList<String> scoreStrings = new ArrayList<>();
        for (DatabaseHelper.ScoreRecord record : scoreRecords) {
            String scoreString = record.getScore() + " очков - " + record.getDate();
            scoreStrings.add(scoreString);
        }
        
        // Если нет результатов, добавляем сообщение
        if (scoreStrings.isEmpty()) {
            scoreStrings.add("Нет сохраненных результатов");
        }
        
        // Создаем адаптер и устанавливаем его для ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_list_item_1, scoreStrings);
        scoresListView.setAdapter(adapter);
    }
}
