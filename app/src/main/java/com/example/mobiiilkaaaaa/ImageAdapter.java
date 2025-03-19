package com.example.mobiiilkaaaaa;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private Integer[] gemImages; // Массив для хранения изображений каждого элемента
    private Integer[] initialGems = { // Начальные изображения
            R.drawable.gem_red, R.drawable.gem_blue, R.drawable.gem_green,
            R.drawable.gem_yellow, R.drawable.gem_purple
    };

    public ImageAdapter(Context context) {
        this.context = context;
        gemImages = new Integer[64]; // 8x8 grid
        initializeGrid(); // Заполняем сетку начальными изображениями
    }

    // Заполняем сетку начальными изображениями без совпадений
    private void initializeGrid() {
        for (int i = 0; i < gemImages.length; i++) {
            gemImages[i] = getRandomGemWithoutMatches(i);
        }
    }

    // Получаем случайный элемент, который не создает совпадений
    private int getRandomGemWithoutMatches(int position) {
        Set<Integer> forbiddenGems = new HashSet<>();

        // Проверяем соседние элементы слева
        if (position % 8 >= 2) {
            if (gemImages[position - 1] != null && gemImages[position - 2] != null) {
                if (gemImages[position - 1].equals(gemImages[position - 2])) {
                    forbiddenGems.add(gemImages[position - 1]);
                }
            }
        }

        // Проверяем соседние элементы сверху
        if (position >= 16) {
            if (gemImages[position - 8] != null && gemImages[position - 16] != null) {
                if (gemImages[position - 8].equals(gemImages[position - 16])) {
                    forbiddenGems.add(gemImages[position - 8]);
                }
            }
        }

        // Выбираем случайный элемент, который не запрещен
        int randomGem;
        do {
            randomGem = initialGems[(int) (Math.random() * initialGems.length)];
        } while (forbiddenGems.contains(randomGem));

        return randomGem;
    }

    // Метод для обмена двух элементов с анимацией
    public void swapItemsWithAnimation(int position1, int position2, Runnable onComplete) {
        View view1 = getViewByPosition(position1);
        View view2 = getViewByPosition(position2);

        if (view1 != null && view2 != null) {
            float x1 = view1.getX();
            float x2 = view2.getX();
            float y1 = view1.getY();
            float y2 = view2.getY();

            // Анимация обмена
            ObjectAnimator animatorX1 = ObjectAnimator.ofFloat(view1, "x", x1, x2);
            ObjectAnimator animatorY1 = ObjectAnimator.ofFloat(view1, "y", y1, y2);
            ObjectAnimator animatorX2 = ObjectAnimator.ofFloat(view2, "x", x2, x1);
            ObjectAnimator animatorY2 = ObjectAnimator.ofFloat(view2, "y", y2, y1);

            animatorX1.setDuration(300);
            animatorY1.setDuration(300);
            animatorX2.setDuration(300);
            animatorY2.setDuration(300);

            animatorX1.start();
            animatorY1.start();
            animatorX2.start();
            animatorY2.start();

            animatorX1.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // После завершения анимации меняем элементы местами
                    swapItems(position1, position2);
                    view1.setX(x1);
                    view1.setY(y1);
                    view2.setX(x2);
                    view2.setY(y2);
                    notifyDataSetChanged();
                    onComplete.run();
                }
            });
        }
    }

    // Метод для проверки совпадений
    private boolean checkMatches() {
        boolean hasMatches = false;
        for (int i = 0; i < gemImages.length; i++) {
            if (checkHorizontalMatch(i) || checkVerticalMatch(i)) {
                hasMatches = true;
            }
        }
        return hasMatches;
    }

    // Проверка горизонтальных совпадений
    private boolean checkHorizontalMatch(int position) {
        int row = position / 8;
        int col = position % 8;
        if (col < 6 && gemImages[position] != null) {
            if (gemImages[position].equals(gemImages[position + 1]) && gemImages[position].equals(gemImages[position + 2])) {
                return true;
            }
        }
        return false;
    }

    // Проверка вертикальных совпадений
    private boolean checkVerticalMatch(int position) {
        int row = position / 8;
        int col = position % 8;
        if (row < 6 && gemImages[position] != null) {
            if (gemImages[position].equals(gemImages[position + 8]) && gemImages[position].equals(gemImages[position + 16])) {
                return true;
            }
        }
        return false;
    }

    // Метод для удаления совпадений
    private void removeMatches(Runnable onComplete) {
        Set<Integer> positionsToRemove = new HashSet<>();
        for (int i = 0; i < gemImages.length; i++) {
            if (checkHorizontalMatch(i)) {
                positionsToRemove.add(i);
                positionsToRemove.add(i + 1);
                positionsToRemove.add(i + 2);
            }
            if (checkVerticalMatch(i)) {
                positionsToRemove.add(i);
                positionsToRemove.add(i + 8);
                positionsToRemove.add(i + 16);
            }
        }

        if (!positionsToRemove.isEmpty()) {
            int[] positions = positionsToRemove.stream().mapToInt(Integer::intValue).toArray();
            removeItemsWithAnimation(positions, onComplete);
        } else {
            onComplete.run();
        }
    }

    // Метод для удаления элементов с анимацией
    public void removeItemsWithAnimation(int[] positions, Runnable onComplete) {
        Set<View> viewsToAnimate = new HashSet<>();
        int animationsCompleted = 0;
        final int totalAnimations = positions.length;

        for (int position : positions) {
            View view = getViewByPosition(position);
            if (view != null) {
                viewsToAnimate.add(view);
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
                animator.setDuration(300);
                animator.start();

                animator.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        animationsCompleted++;
                        if (animationsCompleted == totalAnimations) {
                            removeItems(positions);
                            onComplete.run();
                        }
                    }
                });
            }
        }
    }

    // Метод для удаления элементов
    public void removeItems(int... positions) {
        for (int position : positions) {
            if (position >= 0 && position < gemImages.length) {
                gemImages[position] = null; // Удаляем элемент
            }
        }
        fillEmptySpaces(() -> {
            notifyDataSetChanged(); // Обновляем GridView
        });
    }

    private void animateFall(int fromPosition, int toPosition) {
        View fromView = getViewByPosition(fromPosition);
        View toView = getViewByPosition(toPosition);

        if (fromView != null && toView != null) {
            // Анимация перемещения вниз
            ObjectAnimator animator = ObjectAnimator.ofFloat(fromView, "translationY", toView.getY() - fromView.getY());
            animator.setDuration(300); // Длительность анимации
            animator.start();

            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // После завершения анимации обновляем View
                    fromView.setTranslationY(0);
                    notifyDataSetChanged();
                }
            });
        }
    }

    private void animateAppear(int position) {
        View view = getViewByPosition(position);

        if (view != null) {
            // Анимация появления (из прозрачного в непрозрачный)
            view.setAlpha(0f); // Начальное состояние: полностью прозрачный
            ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            animator.setDuration(300); // Длительность анимации
            animator.start();
        }
    }

    // Метод для заполнения пустот
    public void fillEmptySpaces(Runnable onComplete) {
        boolean needsRefill;
        do {
            needsRefill = false;
            // Падение существующих элементов
            for (int col = 0; col < 8; col++) {
                for (int row = 7; row > 0; row--) {
                    int currentPos = row * 8 + col;
                    if (gemImages[currentPos] == null) {
                        // Ищем ближайший непустой элемент сверху
                        for (int aboveRow = row - 1; aboveRow >= 0; aboveRow--) {
                            int abovePos = aboveRow * 8 + col;
                            if (gemImages[abovePos] != null) {
                                // Перемещаем элемент вниз
                                gemImages[currentPos] = gemImages[abovePos];
                                gemImages[abovePos] = null;
                                animateFall(abovePos, currentPos);
                                needsRefill = true;
                                break;
                            }
                        }
                    }
                }
            }

            // Заполнение пустых ячеек новыми элементами
            for (int i = 0; i < gemImages.length; i++) {
                if (gemImages[i] == null) {
                    gemImages[i] = getRandomGemWithoutMatches(i);
                    animateAppear(i);
                    needsRefill = true;
                }
            }
        } while (needsRefill);

        notifyDataSetChanged();
        onComplete.run();
    }

    // Метод для получения View по позиции
    private View getViewByPosition(int position) {
        GridView gridView = ((MainActivity) context).findViewById(R.id.gridView);
        if (gridView != null) {
            int firstVisiblePosition = gridView.getFirstVisiblePosition();
            int lastVisiblePosition = gridView.getLastVisiblePosition();

            // Проверяем, находится ли позиция в видимой области
            if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
                return gridView.getChildAt(position - firstVisiblePosition);
            }
        }
        return null; // Если элемент не виден, возвращаем null
    }

    // Метод для обмена двух элементов
    public void swapItems(int position1, int position2) {
        int temp = gemImages[position1];
        gemImages[position1] = gemImages[position2];
        gemImages[position2] = temp;
    }

    @Override
    public int getCount() {
        return gemImages.length;
    }

    @Override
    public Object getItem(int position) {
        return gemImages[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        // Убедимся, что gemImages[position] не равен null
        if (gemImages[position] != null) {
            imageView.setImageResource(gemImages[position]);
        } else {
            // Если элемент null, устанавливаем изображение по умолчанию
            imageView.setImageResource(R.drawable.gem_red); // Замените на ваше изображение по умолчанию
        }

        return imageView;
    }
}