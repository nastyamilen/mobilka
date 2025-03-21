package com.example.mobiiilkaaaaa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    
    private static final String SETTINGS_PREFS = "GameSettings";
    private static final String DIFFICULTY_KEY = "difficulty";
    
    // Константы для сложности
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;
    
    // Константа для размера сетки
    public static final int GRID_SIZE = 8;
    
    // Время для каждой сложности (в миллисекундах)
    public static final long TIME_EASY = 90000; // 90 секунд
    public static final long TIME_MEDIUM = 60000; // 60 секунд
    public static final long TIME_HARD = 30000; // 30 секунд
    
    private RadioGroup difficultyRadioGroup;
    private Button saveSettingsButton;
    private Button resetSettingsButton;
    private Button backToMenuButton;
    
    private SharedPreferences settings;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        // Инициализация настроек
        settings = requireActivity().getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        
        // Инициализация UI элементов
        difficultyRadioGroup = view.findViewById(R.id.difficultyRadioGroup);
        saveSettingsButton = view.findViewById(R.id.saveSettingsButton);
        resetSettingsButton = view.findViewById(R.id.resetSettingsButton);
        backToMenuButton = view.findViewById(R.id.backToMenuFromSettingsButton);
        
        // Загрузка текущих настроек
        loadSettings();
        
        // Настройка обработчиков событий
        saveSettingsButton.setOnClickListener(v -> saveSettings());
        resetSettingsButton.setOnClickListener(v -> resetSettings());
        backToMenuButton.setOnClickListener(v -> {
            // Возвращаемся в главное меню
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        return view;
    }
    
    private void loadSettings() {
        // Загрузка сложности
        int difficulty = settings.getInt(DIFFICULTY_KEY, DIFFICULTY_MEDIUM);
        switch (difficulty) {
            case DIFFICULTY_EASY:
                ((RadioButton) difficultyRadioGroup.findViewById(R.id.easyRadioButton)).setChecked(true);
                break;
            case DIFFICULTY_MEDIUM:
                ((RadioButton) difficultyRadioGroup.findViewById(R.id.mediumRadioButton)).setChecked(true);
                break;
            case DIFFICULTY_HARD:
                ((RadioButton) difficultyRadioGroup.findViewById(R.id.hardRadioButton)).setChecked(true);
                break;
        }
    }
    
    private void saveSettings() {
        SharedPreferences.Editor editor = settings.edit();
        
        // Сохранение сложности
        int difficulty;
        int checkedDifficultyId = difficultyRadioGroup.getCheckedRadioButtonId();
        if (checkedDifficultyId == R.id.easyRadioButton) {
            difficulty = DIFFICULTY_EASY;
        } else if (checkedDifficultyId == R.id.hardRadioButton) {
            difficulty = DIFFICULTY_HARD;
        } else {
            difficulty = DIFFICULTY_MEDIUM;
        }
        editor.putInt(DIFFICULTY_KEY, difficulty);
        
        editor.apply();
        
        Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show();
    }
    
    private void resetSettings() {
        // Сброс к настройкам по умолчанию
        ((RadioButton) difficultyRadioGroup.findViewById(R.id.mediumRadioButton)).setChecked(true);
        
        // Сохранение настроек по умолчанию
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(DIFFICULTY_KEY, DIFFICULTY_MEDIUM);
        editor.apply();
        
        Toast.makeText(requireContext(), "Настройки сброшены", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Получение начального времени игры в зависимости от сложности
     * @param context Контекст приложения
     * @return Время в миллисекундах
     */
    public static long getInitialTime(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        int difficulty = settings.getInt(DIFFICULTY_KEY, DIFFICULTY_MEDIUM);
        
        switch (difficulty) {
            case DIFFICULTY_EASY:
                return TIME_EASY;
            case DIFFICULTY_HARD:
                return TIME_HARD;
            default:
                return TIME_MEDIUM;
        }
    }
    
    /**
     * Получение размера игровой сетки
     * @param context Контекст приложения
     * @return Размер сетки (8x8)
     */
    public static int getGridSize(Context context) {
        return GRID_SIZE;
    }
}
