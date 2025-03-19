package com.example.mobiiilkaaaaa;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
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

    public GameAdapter(Context context, int gridSize) {
        this.context = context;
        this.gridSize = gridSize;
        this.gems = new Integer[gridSize * gridSize];
        this.random = new Random();
        initializeBoard();
    }

    private void initializeBoard() {
        // Fill board with random gems
        for (int i = 0; i < gems.length; i++) {
            gems[i] = getRandomGem();
        }
        
        // Keep initializing until there are no matches
        while (hasInitialMatches()) {
            for (int i = 0; i < gems.length; i++) {
                if (isPartOfMatch(i)) {
                    gems[i] = getRandomGem();
                }
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
        gems[position] = null;
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
        
        if (convertView == null) {
            imageView = new ImageView(context);
            int cellSize = parent.getWidth() / gridSize;
            imageView.setLayoutParams(new ViewGroup.LayoutParams(cellSize, cellSize));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) convertView;
        }

        if (gems[position] != null) {
            imageView.setImageResource(gems[position]);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.INVISIBLE);
        }

        return imageView;
    }
}
