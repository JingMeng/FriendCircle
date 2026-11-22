
package razerdp.github.com.ui.util;

import androidx.core.view.ViewCompat;
import android.view.View;

/**
 * Created by 澶х伅娉?on 2017/02/28.
 * <p>
 * view鍋忕Щ甯姪绫? */
 public class ViewOffsetHelper {

    private final View mTarget;

    private int mOffsetTop;
    private int mOffsetLeft;

    public ViewOffsetHelper(View view) {
        mTarget = view;
    }

    /**
     * 涓嶅彲瑙佺殑鎯呭喌涓嬪氨娌″繀瑕佸仛offset浜?     *
     * @return
     */
    private boolean checkHasTarget() {
        return mTarget != null && mTarget.getVisibility() == View.VISIBLE;
    }

    /**
     * offset缁濆浣嶇疆锛屽叾瀹炲氨鏄褰曚笂涓€娆＄殑鍊艰繘琛岀浉鍑?     *
     * @param absoluteOffset
     * @return
     */
    public boolean absoluteOffsetTopAndBottom(int absoluteOffset) {
        if (checkHasTarget()) {
            if (mOffsetTop != absoluteOffset) {
                mOffsetTop = absoluteOffset;
                updateOffsets(true);
                return true;
            }
        }
        return false;
    }

    public void offsetTopAndBottom(int relativeOffset) {
        if (checkHasTarget()) {
            mOffsetTop += relativeOffset;
            updateOffsets(true);
        }
    }

    public boolean absoluteOffsetLeftAndRight(int absoluteOffset) {
        if (checkHasTarget()) {
            if (mOffsetLeft != absoluteOffset) {
                mOffsetLeft = absoluteOffset;
                updateOffsets(false);
                return true;
            }
        }
        return false;
    }

    public void offsetLeftAndRight(int relativeOffset) {
        if (checkHasTarget()) {
            mOffsetLeft += relativeOffset;
            updateOffsets(false);
        }
    }

    public int getTopAndBottomOffset() {
        return mOffsetTop;
    }

    public int getLeftAndRightOffset() {
        return mOffsetLeft;
    }


    private void updateOffsets(boolean isTopAndBottom) {
        if (isTopAndBottom) {
            ViewCompat.offsetTopAndBottom(mTarget, mOffsetTop - mTarget.getTop());
        }else {
            ViewCompat.offsetLeftAndRight(mTarget, mOffsetLeft - mTarget.getLeft());
        }
    }

}

