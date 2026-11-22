package razerdp.github.com.ui.util;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.FloatRange;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.socks.library.KLog;

import razerdp.github.com.lib.api.AppContext;

/**
 * Created by 澶х伅娉?on 2016/10/26.
 * <p>
 * ui宸ュ叿绫? */
public class UIHelper {

    // =============================================================tools
    // methods

    /**
     * dip杞琾x
     */
    public static int dipToPx(float dip) {
        return (int) (dip * AppContext.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * px杞琩ip
     */
    public static int pxToDip(float pxValue) {
        final float scale = AppContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 灏唖p鍊艰浆鎹负px鍊?     */
    public static int sp2px(float spValue) {
        final float fontScale = AppContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 鑾峰彇灞忓箷鍒嗚鲸鐜囷細瀹?     */
    public static int getScreenWidthPix(@Nullable Context context) {
        if (context == null) context = AppContext.getAppContext();
        int width = context.getResources().getDisplayMetrics().widthPixels;
        return width;
    }

    /**
     * 鑾峰彇灞忓箷鍒嗚鲸鐜囷細楂?     */
    public static int getScreenHeightPix(@Nullable Context context) {
        if (context == null) context = AppContext.getAppContext();
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return height;
    }

    /**
     * 鑾峰彇鐘舵€佹爮鐨勯珮搴?     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources()
                    .getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 闅愯棌杞敭鐩?     */
    public static void hideInputMethod(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) view.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(e);
        }
    }

    public static void hideInputMethod(final View view, long delayMillis) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideInputMethod(view);
            }
        }, delayMillis);
    }

    /**
     * 鏄剧ず杞敭鐩?     */
    public static void showInputMethod(View view) {
        if (view == null) return;
        if (view instanceof EditText) view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            boolean success = imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            KLog.i("showSoftKeyboard", " isSuccess   >>>   " + success);
        }
    }

    /**
     * 鏄剧ず杞敭鐩?     */
    public static void showInputMethod(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 澶氬皯鏃堕棿鍚庢樉绀鸿蒋閿洏
     */
    public static void showInputMethod(final View view, long delayMillis) {
        if (view != null)
        // 鏄剧ず杈撳叆娉?        {
            view.postDelayed(new Runnable() {

                @Override
                public void run() {
                    UIHelper.showInputMethod(view);
                }
            }, delayMillis);
        }
    }


    /**
     * Toast灏佽
     */
    public static void ToastMessage(String msg) {
        Toast.makeText(AppContext.getAppContext(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 璋冩暣绐楀彛鐨勯€忔槑搴?     *
     * @param from
     * @param to
     */
    private void dimBackground(Activity act, @FloatRange(from = 0.0f, to = 1.0f) final float from, @FloatRange(from = 0.0f, to = 1.0f) final float to) {
        if (act == null || act.isFinishing()) return;
        final Window window = act.getWindow();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = (Float) animation.getAnimatedValue();
                window.setAttributes(params);
            }
        });

        valueAnimator.start();
    }


    /**
     * =============================================================
     * 璧勬簮宸ュ叿
     */

    public static int getResourceColor(int colorResId) {
        try {
            return AppContext.getResources().getColor(colorResId);
        } catch (Exception e) {
            return Color.TRANSPARENT;
        }
    }
}

