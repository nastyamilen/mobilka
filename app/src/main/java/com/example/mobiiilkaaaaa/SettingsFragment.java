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
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    
    private static final String SETTINGS_PREFS = "GameSettings";
    private static final String DIFFICULTY_KEY = "difficulty";
    private static final String GRID_SIZE_KEY = "gridSize";
    private static final String SOUND_EFFECTS_KEY = "soundEffects";
    private static final String MUSIC_KEY = "music";
    
    // Константы для сложности
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;
    
    // Константы для размера сетки
    public static final int GRID_SIZE_SMALL = 6;
    public static final int GRID_SIZE_MEDIUM = 8;
    public static final int GRID_SIZE_LARGE = 10;
    
    // Время для каждой сложности (в миллисекундах)
    public static final long TIME_EASY = 90000; // 90 секунд
    public static final long TIME_MEDIUM = 60000; // 60 секунд
    public static final long TIME_HARD = 30000; // 30 секунд
    
    private RadioGroup difficultyRadioGroup;
    private RadioGroup gridSizeRadioGroup;
    private Switch soundEffectsSwitch;
    private Switch musicSwitch;
    private Button saveSettingsButton;
    private Button resetSettingsButton;
    
    private SharedPreferences settings;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        // Инициализация настроек
        settings = requireActivity().getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        
        // Инициализация UI элементов
        difficultyRadioGroup = view.findViewById(R.id.difficultyRadioGroup);
        gridSizeRadioGroup = view.findViewById(R.id.gridSizeRadioGroup);
        soundEffectsSwitch = view.findViewById(R.id.soundEffectsSwitch);
        musicSwitch = view.findViewById(R.id.musicSwitch);
        saveSettingsButton = view.findViewById(R.id.saveSettingsButton);
        resetSettingsButton = view.findViewById(R.id.resetSettingsButton);
        
        // Загрузка текущих настроек
        loadSettings();
        
        // Настройка обработчиков событий
        saveSettingsButton.setOnClickListener(v -> saveSettings());
        resetSettingsButton.setOnClickListener(v -> resetSettings());
        
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
        
        // Загрузка размера сетки
        int gridSize = settings.getInt(GRID_SIZE_KEY, GRID_SIZE_MEDIUM);
        switch (gridSize) {
            case GRID_SIZE_SMALL:
                ((RadioButton) gridSizeRadioGroup.findViewById(R.id.smallGridRadioButton)).setChecked(true);
                break;
            case GRID_SIZE_MEDIUM:
                ((RadioButton) gridSizeRadioGroup.findViewById(R.id.mediumGridRadioButton)).setChecked(true);
                break;
            case GRID_SIZE_LARGE:
                ((RadioButton) gridSizeRadioGroup.findViewById(R.id.largeGridRadioButton)).setChecked(true);
                break;
        }
        
        // Загрузка настроек звука
        boolean soundEffects = settings.getBoolean(SOUND_EFFECTS_KEY, true);
        boolean music = settings.getBoolean(MUSIC_KEY, true);
        
        soundEffectsSwitch.setChecked(soundEffects);
        musicSwitch.setChecked(music);
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
        
        // Сохранение размера сетки
        int gridSize;
        int checkedGridSizeId = gridSizeRadioGroup.getCheckedRadioButtonId();
        if (checkedGridSizeId == R.id.smallGridRadioButton) {
            gridSize = GRID_SIZE_SMALL;
        } else if (checkedGridSizeId == R.id.largeGridRadioButton) {
            gridSize = GRID_SIZE_LARGE;
        } else {
            gridSize = GRID_SIZE_MEDIUM;
        }
        editor.putInt(GRID_SIZE_KEY, gridSize);
        
        // Сохранение настроек звука
        editor.putBoolean(SOUND_EFFECTS_KEY, soundEffectsSwitch.isChecked());
        editor.putBoolean(MUSIC_KEY, musicSwitch.isChecked());
        
        editor.apply();
        
        Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show();
    }
    
    private void resetSettings() {
        // Сброс к настройкам по умолчанию
        ((RadioButton) difficultyRadioGroup.findViewById(R.id.mediumRadioButton)).setChecked(true);
        ((RadioButton) gridSizeRadioGroup.findViewById(R.id.mediumGridRadioButton)).setChecked(true);
        soundEffectsSwitch.setChecked(true);
        musicSwitch.setChecked(true);
        
        // Сохранение настроек по умолчанию
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(DIFFICULTY_KEY, DIFFICULTY_MEDIUM);
        editor.putInt(GRID_SIZE_KEY, GRID_SIZE_MEDIUM);
        editor.putBoolean(SOUND_EFFECTS_KEY, true);
        editor.putBoolean(MUSIC_KEY, true);
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
     * @return Размер сетки (6, 8 или 10)
     */
    public static int getGridSize(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        return settings.getInt(GRID_SIZE_KEY, GRID_SIZE_MEDIUM);
    }
    
    /**
     * Проверка, включены ли звуковые эффекты
     * @param context Контекст приложения
     * @return true, если звуковые эффекты включены
     */
    public static boolean isSoundEffectsEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        return settings.getBoolean(SOUND_EFFECTS_KEY, true);
    }
    
    /**
     * Проверка, включена ли фоновая музыка
     * @param context Контекст приложения
     * @return true, если фоновая музыка включена
     */
    public static boolean isMusicEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        return settings.getBoolean(MUSIC_KEY, true);
    }
}
