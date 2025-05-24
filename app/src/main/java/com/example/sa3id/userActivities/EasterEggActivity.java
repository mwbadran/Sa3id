package com.example.sa3id.userActivities;

import static androidx.core.view.ViewCompat.performHapticFeedback;

import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sa3id.R;

public class EasterEggActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private ImageView catImage;
    private int currentCatIndex = 0;
    private float dx = 5f, dy = 5f;
    private float x = 0f, y = 0f;
    private int screenWidth, screenHeight;
    private ValueAnimator animator;
    private Vibrator vibrator;
    private boolean wasInCorner = false;

    private final int[] catImages = {
            R.drawable.kappa_3ly_1,
            R.drawable.kappa_3ly_2,
            R.drawable.kappa_3ly_3,
            R.drawable.kappa_3ly_4,
            R.drawable.kappa_3ly_5,
            R.drawable.kappa_3ly_6,
            R.drawable.kappa_3ly_7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easter_egg);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        View rootView = findViewById(android.R.id.content);
        catImage = findViewById(R.id.cat_image);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                screenWidth = rootView.getWidth() - catImage.getWidth();
                screenHeight = rootView.getHeight() - catImage.getHeight();
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                startAnimation();
            }
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.arabic_hussien_music_loop);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        catImage.setOnClickListener(v -> {
            currentCatIndex = (currentCatIndex + 1) % catImages.length;
            updateCatImage();
            //mediaPlayer.seekTo(0);
        });
    }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(16);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            x += dx;
            y += dy;

            boolean hitCorner = false;
            if (x <= 0 || x >= screenWidth) {
                dx = -dx;
                hitCorner = true;
            }
            if (y <= 0 || y >= screenHeight) {
                dy = -dy;
                hitCorner = true;
            }

            if (hitCorner && !wasInCorner) {
                //vibrator.vibrate(VibrationEffect.createOneShot(100, 255));
                catImage.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                currentCatIndex = (currentCatIndex + 1) % catImages.length;
                updateCatImage();
                wasInCorner = true;
            } else if (!hitCorner) {
                wasInCorner = false;
            }

            catImage.setX(x);
            catImage.setY(y);
        });
        animator.start();
    }

    private void updateCatImage() {
        catImage.setImageResource(catImages[currentCatIndex]);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (animator != null) {
            animator.cancel();
        }
    }
}