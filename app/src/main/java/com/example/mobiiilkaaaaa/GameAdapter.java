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

import java.util.Random;

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

    private boolean isPartOfMatch(int position) {
        return isHorizontalMatch(position) || isVerticalMatch(position);
    }

    private boolean isHorizontalMatch(int position) {
        int row = position / gridSize;
        int col = position % gridSize;
        
        if (col > gridSize - 3) return false;
        
        return gems[position].equals(gems[position + 1]) &&
               gems[position].equals(gems[position + 2]);
    }

    private boolean isVerticalMatch(int position) {
        int row = position / gridSize;
        
        if (row > gridSize - 3) return false;
        
        return gems[position].equals(gems[position + gridSize]) &&
               gems[position].equals(gems[position + gridSize * 2]);
    }

    public void swapItems(int pos1, int pos2) {
        Integer temp = gems[pos1];
        gems[pos1] = gems[pos2];
        gems[pos2] = temp;
        notifyDataSetChanged();
    }

    public void removeGem(int position) {
        // Special case for first gem - replace it with a new gem instead of setting to null
        if (position == 0) {
            gems[position] = getRandomGem();
            Log.d("GameAdapter", "First gem replaced instead of removed: " + gems[position]);
        } else {
            gems[position] = null;
        }
        notifyDataSetChanged();
    }

    public void dropGems() {
        // Process each column
        for (int col = 0; col < gridSize; col++) {
            int emptyRow = gridSize - 1;
            
            // Move existing gems down
            for (int row = gridSize - 1; row >= 0; row--) {
                int pos = row * gridSize + col;
                if (gems[pos] != null) {
                    if (row != emptyRow) {
                        gems[emptyRow * gridSize + col] = gems[pos];
                        gems[pos] = null;
                    }
                    emptyRow--;
                }
            }
            
            // Fill empty spaces with new gems
            for (int row = emptyRow; row >= 0; row--) {
                gems[row * gridSize + col] = getRandomGem();
            }
        }
        
        // Ensure first element is always initialized
        if (gems[0] == null) {
            gems[0] = getRandomGem();
            Log.d("GameAdapter", "Ensuring first gem is initialized after drop: " + gems[0]);
        }
        
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
            imageView.setPadding(2, 2, 2, 2);
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
