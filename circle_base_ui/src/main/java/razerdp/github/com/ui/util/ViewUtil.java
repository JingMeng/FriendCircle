package razerdp.github.com.ui.util;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;

/**
 * Created by 澶х伅娉?on 2017/3/16.
 */

public class ViewUtil {
    public static void setViewsClickListener(@NonNull View.OnClickListener listener, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(listener);
            }
        }
    }

    public static void setViewsVisible(int visible, View... views) {
        for (View view : views) {
            if (view.getVisibility() != visible) {
                view.setVisibility(visible);
            }
        }
    }

    public static void setViewsEnable(boolean enable, View... views) {
        for (View view : views) {
            view.setEnabled(enable);
        }
    }

    public static void setViewsEnableAndClickable(boolean enable,boolean clickable, View... views) {
        for (View view : views) {
            view.setEnabled(enable);
            view.setClickable(clickable);
        }
    }

    /**
     * 鎵╁ぇView鐨勮Е鎽稿拰鐐瑰嚮鍝嶅簲鑼冨洿,鏈€澶т笉瓒呰繃鍏剁埗View鑼冨洿锛圓BSListView鏃犳晥锛?     * 寤惰繜300姣鎵ц锛屼娇璇ユ柟娉曞彲浠ュ湪onCreate閲岄潰浣跨敤
     */
    public static void expandViewTouchDelegate(final View view, final int top, final int bottom, final int left, final int right) {
        if (view == null) {
            return;
        }
        if (view.getParent() != null) {

            ((View) view.getParent()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Rect bounds = new Rect();
                    view.setEnabled(true);
                    view.getHitRect(bounds);

                    bounds.top -= top;
                    bounds.bottom += bottom;
                    bounds.left -= left;
                    bounds.right += right;

                    Log.d("rect", "rect - top" + bounds.top + "  - right" + bounds.right);

                    TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                    if (View.class.isInstance(view.getParent())) {
                        ((View) view.getParent()).setTouchDelegate(touchDelegate);
                    }
                }
            }, 300);
        }
    }

    /**
     * 杩樺師View鐨勮Е鎽稿拰鐐瑰嚮鍝嶅簲鑼冨洿,鏈€灏忎笉灏忎簬View鑷韩鑼冨洿
     */
    public static void restoreViewTouchDelegate(final View view) {
        if (view == null) {
            return;
        }

        if (view.getParent() != null) {

            ((View) view.getParent()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Rect bounds = new Rect();
                    bounds.setEmpty();
                    TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                    if (View.class.isInstance(view.getParent())) {
                        ((View) view.getParent()).setTouchDelegate(touchDelegate);
                    }
                }
            }, 300);
        }
    }

}

