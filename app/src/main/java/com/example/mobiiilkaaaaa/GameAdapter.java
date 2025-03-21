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
        return isHorizontalMatch(position) || isVerticalMatch(position);
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
        
        if (col > gridSize - 3) return positions;
        
        // Проверяем минимальное совпадение (3 в ряд)
        if (gems[position] == null || 
            gems[position + 1] == null || 
            gems[position + 2] == null ||
            !gems[position].equals(gems[position + 1]) || 
            !gems[position].equals(gems[position + 2])) {
            return positions;
        }
        
        // Добавляем первые три позиции
        positions.add(position);
        positions.add(position + 1);
        positions.add(position + 2);
        
        // Проверяем дополнительные совпадения (4, 5 и т.д. в ряд)
        for (int i = 3; col + i < gridSize; i++) {
            if (gems[position + i] != null && gems[position].equals(gems[position + i])) {
                positions.add(position + i);
                Log.d("GameAdapter", "Found additional horizontal match at position " + (position + i));
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
        
        if (row > gridSize - 3) return positions;
        
        // Проверяем минимальное совпадение (3 в ряд)
        if (gems[position] == null || 
            gems[position + gridSize] == null || 
            gems[position + gridSize * 2] == null ||
            !gems[position].equals(gems[position + gridSize]) || 
            !gems[position].equals(gems[position + gridSize * 2])) {
            return positions;
        }
        
        // Добавляем первые три позиции
        positions.add(position);
        positions.add(position + gridSize);
        positions.add(position + gridSize * 2);
        
        // Проверяем дополнительные совпадения (4, 5 и т.д. в ряд)
        for (int i = 3; row + i < gridSize; i++) {
            if (gems[position + gridSize * i] != null && gems[position].equals(gems[position + gridSize * i])) {
                positions.add(position + gridSize * i);
                Log.d("GameAdapter", "Found additional vertical match at position " + (position + gridSize * i));
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
        // Process each column
        for (int col = 0; col < gridSize; col++) {
            int emptyRow = gridSize - 1;
            
            // Move existing gems down
            for (int row = gridSize - 1; row >= 0; row--) {
                int pos = row * gridSize + col;
                
                if (pos < 0 || pos >= gems.length) {
                    Log.e("GameAdapter", "Invalid position during drop: " + pos);
                    continue;
                }
                
                if (gems[pos] != null) {
                    if (emptyRow != row) {
                        // Move this gem down to the empty space
                        int emptyPos = emptyRow * gridSize + col;
                        
                        if (emptyPos < 0 || emptyPos >= gems.length) {
                            Log.e("GameAdapter", "Invalid empty position during drop: " + emptyPos);
                            continue;
                        }
                        
                        Log.d("GameAdapter", "Moving gem from position " + pos + " to " + emptyPos);
                        gems[emptyPos] = gems[pos];
                        gems[pos] = null;
                    }
                    emptyRow--;
                }
            }
            
            // Fill empty spaces with new gems
            for (int row = emptyRow; row >= 0; row--) {
                int pos = row * gridSize + col;
                
                if (pos < 0 || pos >= gems.length) {
                    Log.e("GameAdapter", "Invalid position when filling empty spaces: " + pos);
                    continue;
                }
                
                gems[pos] = getRandomGem();
                Log.d("GameAdapter", "Added new gem at position " + pos + ": " + gems[pos]);
            }
        }
        
        // Ensure first element is always initialized
        if (gems[0] == null) {
            gems[0] = getRandomGem();
            Log.d("GameAdapter", "Ensuring first gem is initialized after drop: " + gems[0]);
        }
        
        Log.d("GameAdapter", "Finished dropping gems");
        notifyDataSetChanged();
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
        
        int row = position / gridSize;
        int col = position % gridSize;
        
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(cellSize, cellSize));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        
        Log.d("GameAdapter", "Getting view for position " + position + " (row: " + row + ", col: " + col + ")");
        
        if (position >= 0 && position < gems.length) {
            if (gems[position] != null) {
                imageView.setImageResource(gems[position]);
                imageView.setVisibility(View.VISIBLE);
                Log.d("GameAdapter", "Displaying gem at " + position + " (row: " + row + ", col: " + col + ")");
            } else {
                imageView.setVisibility(View.INVISIBLE);
                Log.d("GameAdapter", "No gem to display at " + position + " (row: " + row + ", col: " + col + ")");
            }
        } else {
            Log.e("GameAdapter", "Invalid position: " + position);
            imageView.setVisibility(View.INVISIBLE);
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
