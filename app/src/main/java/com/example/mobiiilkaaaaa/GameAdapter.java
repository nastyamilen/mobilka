package com.example.mobiiilkaaaaa;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

public class GameAdapter extends BaseAdapter {
    private Context context;
    private int gridSize;
    private Integer[] gems;
    private Random random;
    
    private static final Integer[] GEM_RESOURCES = {
        R.drawable.gem_red,
        R.drawable.gem_blue,
        R.drawable.gem_green,
        R.drawable.gem_yellow,
        R.drawable.gem_purple
    };

    private int cellSize = 40; // Размер ячейки по умолчанию
    
    public GameAdapter(Context context, int gridSize) {
        this.context = context;
        this.gridSize = gridSize;
        this.gems = new Integer[gridSize * gridSize];
        this.random = new Random();
        Log.d("GameAdapter", "Constructor called with gridSize: " + gridSize + ", total elements: " + (gridSize * gridSize));
        initializeBoard();
        
        // Log the first few elements after initialization
        for (int i = 0; i < Math.min(10, gems.length); i++) {
            Log.d("GameAdapter", "After init - gem at " + i + ": " + gems[i]);
        }
    }

    private void initializeBoard() {
        // Fill board with random gems
        for (int i = 0; i < gems.length; i++) {
            gems[i] = getRandomGem();
            Log.d("GameAdapter", "Initialized gem at " + i + ": " + gems[i]);
        }
        
        // Ensure first element is initialized
        if (gems[0] == null) {
            gems[0] = getRandomGem();
            Log.d("GameAdapter", "Force initialized first gem: " + gems[0]);
        }
        
        // Keep initializing until there are no matches
        while (hasInitialMatches()) {
            for (int i = 0; i < gems.length; i++) {
                if (isPartOfMatch(i)) {
                    gems[i] = getRandomGem();
                    Log.d("GameAdapter", "Reinitialized gem at " + i + ": " + gems[i]);
                }
            }
            
            // Ensure first element is not null after reinitialization
            if (gems[0] == null) {
                gems[0] = getRandomGem();
                Log.d("GameAdapter", "Force reinitialized first gem: " + gems[0]);
            }
        }
    }

    private Integer getRandomGem() {
        return GEM_RESOURCES[random.nextInt(GEM_RESOURCES.length)];
    }

    private boolean hasInitialMatches() {
        // Check rows
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize - 2; col++) {
                int pos = row * gridSize + col;
                if (isHorizontalMatch(pos)) {
                    return true;
                }
            }
        }

        // Check columns
        for (int col = 0; col < gridSize; col++) {
            for (int row = 0; row < gridSize - 2; row++) {
                int pos = row * gridSize + col;
                if (isVerticalMatch(pos)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isPartOfMatch(int position) {
        if (position < 0 || position >= gems.length || gems[position] == null) {
            return false;
        }
        
        try {
            List<Integer> horizontalMatches = getHorizontalMatchPositions(position);
            List<Integer> verticalMatches = getVerticalMatchPositions(position);
            
            return !horizontalMatches.isEmpty() || !verticalMatches.isEmpty();
        } catch (Exception e) {
            Log.e("GameAdapter", "Error in isPartOfMatch at position " + position + ": " + e.getMessage());
            return false;
        }
    }

    private boolean isHorizontalMatch(int position) {
        return !getHorizontalMatchPositions(position).isEmpty();
    }

    private boolean isVerticalMatch(int position) {
        return !getVerticalMatchPositions(position).isEmpty();
    }

    public List<Integer> getHorizontalMatchPositions(int position) {
        List<Integer> positions = new ArrayList<>();
        int row = position / gridSize;
        int col = position % gridSize;
        
        // Проверяем, что позиция находится в пределах сетки
        if (position < 0 || position >= gems.length) {
            Log.e("GameAdapter", "Invalid position for horizontal match check: " + position);
            return positions;
        }
        
        if (col > gridSize - 3) return positions;
        
        // Проверяем минимальное совпадение (3 в ряд)
        if (gems[position] == null) {
            Log.e("GameAdapter", "Gem at position " + position + " is null");
            return positions;
        }
        
        // Проверяем, что следующие два элемента существуют и не null
        if (position + 1 >= gems.length || position + 2 >= gems.length) {
            Log.e("GameAdapter", "Position out of bounds for horizontal match: " + position);
            return positions;
        }
        
        if (gems[position + 1] == null || gems[position + 2] == null) {
            Log.e("GameAdapter", "Adjacent gems are null for horizontal match at position: " + position);
            return positions;
        }
        
        if (!gems[position].equals(gems[position + 1]) || !gems[position].equals(gems[position + 2])) {
            return positions;
        }
        
        // Добавляем первые три позиции
        positions.add(position);
        positions.add(position + 1);
        positions.add(position + 2);
        
        // Проверяем дополнительные совпадения (4, 5 и т.д. в ряд)
        for (int i = 3; col + i < gridSize; i++) {
            int nextPos = position + i;
            if (nextPos < gems.length && gems[nextPos] != null && gems[position].equals(gems[nextPos])) {
                positions.add(nextPos);
                Log.d("GameAdapter", "Found additional horizontal match at position " + nextPos);
            } else {
                break;
            }
        }
        
        if (positions.size() > 3) {
            Log.d("GameAdapter", "Found horizontal match with " + positions.size() + " gems starting at position " + position);
        }
        
        return positions;
    }

    public List<Integer> getVerticalMatchPositions(int position) {
        List<Integer> positions = new ArrayList<>();
        int row = position / gridSize;
        
        // Проверяем, что позиция находится в пределах сетки
        if (position < 0 || position >= gems.length) {
            Log.e("GameAdapter", "Invalid position for vertical match check: " + position);
            return positions;
        }
        
        if (row > gridSize - 3) return positions;
        
        // Проверяем минимальное совпадение (3 в ряд)
        if (gems[position] == null) {
            Log.e("GameAdapter", "Gem at position " + position + " is null");
            return positions;
        }
        
        // Проверяем, что следующие два элемента существуют и не null
        int secondPos = position + gridSize;
        int thirdPos = position + gridSize * 2;
        
        if (secondPos >= gems.length || thirdPos >= gems.length) {
            Log.e("GameAdapter", "Position out of bounds for vertical match: " + position);
            return positions;
        }
        
        if (gems[secondPos] == null || gems[thirdPos] == null) {
            Log.e("GameAdapter", "Adjacent gems are null for vertical match at position: " + position);
            return positions;
        }
        
        if (!gems[position].equals(gems[secondPos]) || !gems[position].equals(gems[thirdPos])) {
            return positions;
        }
        
        // Добавляем первые три позиции
        positions.add(position);
        positions.add(secondPos);
        positions.add(thirdPos);
        
        // Проверяем дополнительные совпадения (4, 5 и т.д. в ряд)
        for (int i = 3; row + i < gridSize; i++) {
            int nextPos = position + gridSize * i;
            if (nextPos < gems.length && gems[nextPos] != null && gems[position].equals(gems[nextPos])) {
                positions.add(nextPos);
                Log.d("GameAdapter", "Found additional vertical match at position " + nextPos);
            } else {
                break;
            }
        }
        
        if (positions.size() > 3) {
            Log.d("GameAdapter", "Found vertical match with " + positions.size() + " gems starting at position " + position);
        }
        
        return positions;
    }

    public List<Integer> getAllMatchPositions(int position) {
        Set<Integer> positions = new HashSet<>();
        
        positions.addAll(getHorizontalMatchPositions(position));
        positions.addAll(getVerticalMatchPositions(position));
        
        return new ArrayList<>(positions);
    }

    public void swapItems(int pos1, int pos2) {
        if (pos1 < 0 || pos1 >= gems.length || pos2 < 0 || pos2 >= gems.length) {
            Log.e("GameAdapter", "Invalid positions for swap: " + pos1 + ", " + pos2);
            return;
        }
        
        Log.d("GameAdapter", "Swapping gems at positions " + pos1 + " and " + pos2);
        Log.d("GameAdapter", "Gem at pos1: " + (gems[pos1] == null ? "null" : gems[pos1]) + 
                            ", Gem at pos2: " + (gems[pos2] == null ? "null" : gems[pos2]));
        
        // Проверяем, что оба элемента не null
        if (gems[pos1] == null || gems[pos2] == null) {
            Log.e("GameAdapter", "Cannot swap with null gem. Pos1: " + 
                  (gems[pos1] == null ? "null" : gems[pos1]) + ", Pos2: " + 
                  (gems[pos2] == null ? "null" : gems[pos2]));
            return;
        }
        
        Integer temp = gems[pos1];
        gems[pos1] = gems[pos2];
        gems[pos2] = temp;
        notifyDataSetChanged();
    }

    public void removeGem(int position) {
        if (position < 0 || position >= gems.length) {
            Log.e("GameAdapter", "Invalid position for removeGem: " + position);
            return;
        }
        
        // Special case for first gem - replace it with a new gem instead of setting to null
        if (position == 0) {
            gems[position] = getRandomGem();
            Log.d("GameAdapter", "First gem replaced instead of removed: " + gems[position]);
        } else {
            Log.d("GameAdapter", "Removing gem at position " + position + ": " + 
                               (gems[position] == null ? "null" : gems[position]));
            gems[position] = null;
        }
        notifyDataSetChanged();
    }

    public void dropGems() {
        Log.d("GameAdapter", "Dropping gems...");
        boolean anyDropped = false;
        
        // Проходим по всем столбцам
        for (int col = 0; col < gridSize; col++) {
            // Начинаем с нижней строки и двигаемся вверх
            for (int row = gridSize - 1; row >= 0; row--) {
                int pos = row * gridSize + col;
                
                if (pos >= gems.length) {
                    Log.e("GameAdapter", "Position out of bounds in dropGems: " + pos);
                    continue;
                }
                
                // Если ячейка пуста, ищем выше непустую ячейку для перемещения
                if (gems[pos] == null) {
                    for (int r = row - 1; r >= 0; r--) {
                        int upperPos = r * gridSize + col;
                        
                        if (upperPos < 0 || upperPos >= gems.length) {
                            Log.e("GameAdapter", "Upper position out of bounds in dropGems: " + upperPos);
                            continue;
                        }
                        
                        if (gems[upperPos] != null) {
                            // Перемещаем камень вниз
                            gems[pos] = gems[upperPos];
                            gems[upperPos] = null;
                            anyDropped = true;
                            break;
                        }
                    }
                }
            }
            
            // Заполняем пустые ячейки сверху новыми камнями
            for (int row = 0; row < gridSize; row++) {
                int pos = row * gridSize + col;
                
                if (pos >= gems.length) {
                    Log.e("GameAdapter", "Position out of bounds when filling new gems: " + pos);
                    continue;
                }
                
                if (gems[pos] == null) {
                    gems[pos] = GEM_RESOURCES[random.nextInt(GEM_RESOURCES.length)];
                    anyDropped = true;
                }
            }
        }
        
        if (anyDropped) {
            Log.d("GameAdapter", "Gems dropped and new ones added");
            notifyDataSetChanged();
        } else {
            Log.d("GameAdapter", "No gems were dropped");
        }
    }

    @Override
    public int getCount() {
        return gems.length;
    }

    @Override
    public Object getItem(int position) {
        return gems[position];
    }

    @Override
    public long getItemId(int position) {
        return gems[position] == null ? -1 : gems[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(cellSize, cellSize));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) convertView;
        }
        
        if (position < 0 || position >= gems.length) {
            Log.e("GameAdapter", "Invalid position in getView: " + position);
            return imageView;
        }
        
        if (gems[position] != null) {
            imageView.setImageResource(gems[position]);
            imageView.setTag(gems[position]);
        } else {
            imageView.setImageDrawable(null);
            imageView.setTag(null);
        }
        
        return imageView;
    }

    public void setCellSize(int size) {
        this.cellSize = size;
        Log.d("GameAdapter", "Cell size set to: " + size);
    }

    // Метод для сброса игры
    public void resetGame() {
        // Пересоздаем массив камней с текущим размером сетки
        this.gems = new Integer[gridSize * gridSize];
        Log.d("GameAdapter", "Resetting game with gridSize: " + gridSize + ", total elements: " + (gridSize * gridSize));
        initializeBoard();
        notifyDataSetChanged();
    }

    // Методы для сохранения и загрузки состояния игры
    public String saveGameState() {
        StringBuilder state = new StringBuilder();
        for (Integer gem : gems) {
            state.append(gem == null ? "null" : gem).append(",");
        }
        return state.toString();
    }
    
    public void loadGameState(String state) {
        if (state == null || state.isEmpty()) {
            initializeBoard();
            return;
        }
        
        String[] gemStates = state.split(",");
        for (int i = 0; i < Math.min(gemStates.length, gems.length); i++) {
            if (gemStates[i].equals("null")) {
                gems[i] = null;
            } else {
                try {
                    gems[i] = Integer.parseInt(gemStates[i]);
                } catch (NumberFormatException e) {
                    gems[i] = getRandomGem();
                }
            }
        }
        
        // Если сохраненное состояние короче текущего массива, заполняем оставшиеся ячейки
        if (gemStates.length < gems.length) {
            for (int i = gemStates.length; i < gems.length; i++) {
                gems[i] = getRandomGem();
            }
        }
        
        notifyDataSetChanged();
    }
}
