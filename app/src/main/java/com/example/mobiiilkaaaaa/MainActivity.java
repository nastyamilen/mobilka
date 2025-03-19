package com.example.mobiiilkaaaaa;

import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageAdapter adapter;
    private int selectedPosition = -1; // Позиция выбранного элемента (-1 означает, что ничего не выбрано)
    private int score = 0; // Счет игры
    private TextView scoreTextView; // TextView для отображения счета
    private boolean isProcessingMatches = false; // Флаг для отслеживания обработки совпадений

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridView = findViewById(R.id.gridView);
        scoreTextView = findViewById(R.id.scoreTextView);
        adapter = new ImageAdapter(this);
        gridView.setAdapter(adapter);

        // Обработка кликов на элементы GridView
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (isProcessingMatches) {
                // Если сейчас обрабатываются совпадения, игнорируем клик
                Toast.makeText(MainActivity.this, "Подождите, идет обработка...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedPosition == -1) {
                // Если ничего не выбрано, выбираем текущий элемент
                selectedPosition = position;
                Toast.makeText(MainActivity.this, "Выбран элемент: " + position, Toast.LENGTH_SHORT).show();
            } else {
                // Если уже выбран элемент, проверяем, допустим ли обмен
                if (isSwapValid(selectedPosition, position)) {
                    // Меняем элементы местами с анимацией
                    adapter.swapItemsWithAnimation(selectedPosition, position, () -> {
                        selectedPosition = -1; // Сбрасываем выбор
                        isProcessingMatches = true; // Начинаем обработку совпадений
                        checkMatches(); // Проверяем совпадения после обмена
                    });
                } else {
                    // Обмен недопустим, сбрасываем выбор
                    selectedPosition = -1;
                    Toast.makeText(MainActivity.this, "Недопустимый обмен", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Обновление счета
    private void updateScore(int points) {
        score += points;
        scoreTextView.setText("Счет: " + score);
    }

    // Проверка, допустим ли обмен
    private boolean isSwapValid(int position1, int position2) {
        // Временно меняем элементы местами
        adapter.swapItems(position1, position2);

        // Проверяем, есть ли совпадения после обмена
        boolean isValid = checkMatches();

        // Возвращаем элементы на место
        adapter.swapItems(position1, position2);

        return isValid;
    }

    // Метод для проверки совпадений
    private boolean checkMatches() {
        boolean hasMatches = false;

        // Проверяем строки
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 6; j++) { // Проверяем до 6-го элемента, чтобы не выйти за границы
                int position = i * 8 + j;
                if (checkRow(position)) {
                    // Найдено совпадение в строке
                    Log.d("Match", "Совпадение в строке: " + i);
                    hasMatches = true;
                    // Удаляем совпадающие элементы с анимацией
                    adapter.removeItemsWithAnimation(new int[]{position, position + 1, position + 2}, () -> {
                        updateScore(10); // Добавляем 10 очков за совпадение
                        fillEmptySpaces(); // Заполняем пустоты
                    });
                }
            }
        }

        // Проверяем столбцы
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 6; i++) { // Проверяем до 6-го элемента, чтобы не выйти за границы
                int position = i * 8 + j;
                if (checkColumn(position)) {
                    // Найдено совпадение в столбце
                    Log.d("Match", "Совпадение в столбце: " + j);
                    hasMatches = true;
                    // Удаляем совпадающие элементы с анимацией
                    adapter.removeItemsWithAnimation(new int[]{position, position + 8, position + 16}, () -> {
                        updateScore(10); // Добавляем 10 очков за совпадение
                        fillEmptySpaces(); // Заполняем пустоты
                    });
                }
            }
        }

        if (!hasMatches) {
            // Если совпадений нет, завершаем обработку
            isProcessingMatches = false;
        }

        return hasMatches;
    }

    // Метод для заполнения пустот
    private void fillEmptySpaces() {
        adapter.fillEmptySpaces(() -> {
            // После заполнения пустот завершаем обработку
            isProcessingMatches = false;
        });
    }

    // Проверка совпадений в строке (3 одинаковых элемента подряд)
    private boolean checkRow(int position) {
        return adapter.getItem(position) != null &&
                adapter.getItem(position).equals(adapter.getItem(position + 1)) &&
                adapter.getItem(position).equals(adapter.getItem(position + 2));
    }

    // Проверка совпадений в столбце (3 одинаковых элемента подряд)
    private boolean checkColumn(int position) {
        return adapter.getItem(position) != null &&
                adapter.getItem(position).equals(adapter.getItem(position + 8)) &&
                adapter.getItem(position).equals(adapter.getItem(position + 16));
    }
}