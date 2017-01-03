package csnowstack.likeireadtablayout.design;

import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;

/**
 * Created by cq on 2016/12/30.
 */

public class AnimationUtils {
    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    /**
     * Linear interpolation between {@code startValue} and {@code endValue} by {@code fraction}.
     */
    static float lerp(float startValue, float endValue, float fraction) {
        return startValue + (fraction * (endValue - startValue));
    }

    static int lerp(int startValue, int endValue, float fraction,boolean isLeftPoint) {
        if((endValue>startValue&& !isLeftPoint) ||(endValue<startValue&&isLeftPoint)){
            return fraction>0.5?endValue:startValue + Math.round(fraction *2* (endValue - startValue));
        }else {
            return fraction > 0.5 ? startValue + Math.round((fraction - 0.5f) * 2 * (endValue - startValue)) : startValue;
        }
    }

    static class AnimationListenerAdapter implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
