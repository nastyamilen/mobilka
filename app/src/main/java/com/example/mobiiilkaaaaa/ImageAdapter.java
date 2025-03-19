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
            // Анимация обмена
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(view1, "translationX", view2.getX() - view1.getX());
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(view2, "translationX", view1.getX() - view2.getX());

            animator1.setDuration(300);
            animator2.setDuration(300);

            animator1.start();
            animator2.start();

            animator1.addUpdateListener(animation -> {
                view1.invalidate();
                view2.invalidate();
            });

            animator1.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // После завершения анимации меняем элементы местами
                    swapItems(position1, position2);

                    // Проверяем, есть ли совпадения после обмена
                    if (checkMatches()) {
                        removeMatches(onComplete);
                    } else {
                        // Если совпадений нет, возвращаем элементы на место
                        swapItemsWithAnimation(position1, position2, onComplete);
                    }
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

        for (int position : positions) {
            View view = getViewByPosition(position);
            if (view != null) {
                viewsToAnimate.add(view);
            }
        }

        // Анимация исчезновения
        for (View view : viewsToAnimate) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            animator.setDuration(300);
            animator.start();

            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // После завершения анимации удаляем элементы
                    removeItems(positions);
                    onComplete.run();
                }
            });
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
        for (int col = 0; col < 8; col++) {
            int emptyRow = 7; // Начинаем с нижней строки

            // Проходим столбец снизу вверх
            for (int row = 7; row >= 0; row--) {
                int position = row * 8 + col;

                // Если текущий элемент не пустой
                if (gemImages[position] != null) {
                    // Если есть пустое место ниже, перемещаем элемент
                    if (row != emptyRow) {
                        gemImages[emptyRow * 8 + col] = gemImages[position];
                        gemImages[position] = null;

                        // Анимация падения
                        animateFall(position, emptyRow * 8 + col);
                    }
                    emptyRow--;
                }
            }

            // Заполняем оставшиеся пустоты новыми элементами
            for (int row = emptyRow; row >= 0; row--) {
                int position = row * 8 + col;
                gemImages[position] = getRandomGemWithoutMatches(position);

                // Анимация появления нового элемента
                animateAppear(position);
            }
        }

        notifyDataSetChanged(); // Обновляем GridView
        onComplete.run(); // Вызываем обратный вызов после завершения
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