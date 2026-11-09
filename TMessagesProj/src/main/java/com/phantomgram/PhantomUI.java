package com.phantomgram;

import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import org.telegram.messenger.AndroidUtilities;

/**
 * PhantomUI - iOS Styling, Physics-based Spring Animations & Haptics
 * Recreates the authentic iOS Telegram feel and 120 FPS fluidity.
 */
public class PhantomUI {

    // iOS Visual Constants
    public static final int IOS_BUBBLE_CORNER_RADIUS_DP = 18;
    public static final int IOS_TAIL_CORNER_RADIUS_DP = 4;
    public static final int IOS_BLUR_RADIUS_DEFAULT = 25;

    // iOS Spring Physics Parameters
    public static final float SPRING_STIFFNESS = 380f;     // Bouncy yet firm iOS feel
    public static final float SPRING_DAMPING = 0.78f;       // Natural iOS settling ratio

    /**
     * Applies authentic iOS Haptic Touch feedback.
     */
    public static void hapticImpact(View view) {
        if (!PhantomConfig.iosHapticFeedback || view == null) return;
        view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        );
    }

    public static void hapticSelection(View view) {
        if (!PhantomConfig.iosHapticFeedback || view == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK);
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        }
    }

    /**
     * Binds iOS physical spring animation on touch down / up (scale bounce).
     */
    public static void attachIosBounce(View view, float pressedScale) {
        if (!PhantomConfig.iosSpringAnimations || view == null) return;

        SpringAnimation scaleX = new SpringAnimation(view, DynamicAnimation.SCALE_X);
        SpringAnimation scaleY = new SpringAnimation(view, DynamicAnimation.SCALE_Y);

        SpringForce force = new SpringForce()
                .setStiffness(SPRING_STIFFNESS)
                .setDampingRatio(SPRING_DAMPING);

        scaleX.setSpring(force);
        scaleY.setSpring(force);

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    hapticSelection(view);
                    scaleX.animateToFinalPosition(pressedScale);
                    scaleY.animateToFinalPosition(pressedScale);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    scaleX.animateToFinalPosition(1.0f);
                    scaleY.animateToFinalPosition(1.0f);
                    break;
            }
            return false;
        });
    }

    /**
     * Animates view appearance using iOS spring transition.
     */
    public static void springSlideIn(View view, float fromY) {
        if (!PhantomConfig.iosSpringAnimations || view == null) return;

        view.setTranslationY(fromY);
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(180).start();

        SpringAnimation springY = new SpringAnimation(view, DynamicAnimation.TRANSLATION_Y, 0f);
        springY.getSpring()
                .setStiffness(SPRING_STIFFNESS)
                .setDampingRatio(SPRING_DAMPING);
        springY.start();
    }

    /**
     * Real-time hardware background blur (iOS Glassmorphism).
     */
    public static void applyIosBlur(View view, float radiusDp) {
        if (!PhantomConfig.iosBlurEffects || view == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            float px = AndroidUtilities.dp(radiusDp > 0 ? radiusDp : IOS_BLUR_RADIUS_DEFAULT);
            RenderEffect blur = RenderEffect.createBlurEffect(px, px, Shader.TileMode.CLAMP);
            view.setRenderEffect(blur);
        }
    }

    public static void clearIosBlur(View view) {
        if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.setRenderEffect(null);
        }
    }
}
