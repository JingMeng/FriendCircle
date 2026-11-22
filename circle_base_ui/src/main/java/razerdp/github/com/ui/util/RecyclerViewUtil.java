package razerdp.github.com.ui.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * Created by 澶х伅娉?on 2017/6/1.
 */

public class RecyclerViewUtil {
    /**
     * 鍒ゆ柇recyclerview鏄惁婊戝埌搴曢儴
     * <p>
     * 鍘熺悊锛氬垽鏂粦杩囩殑璺濈鍔犱笂灞忓箷涓婄殑鏄剧ず鐨勫尯鍩熸槸鍚︽瘮鏁翠釜鎺т欢楂樺害楂?     *
     * @return
     */
    public static boolean isScrollToBottom(@NonNull RecyclerView recyclerView) {
        return recyclerView != null && recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange();
    }

    public static boolean isOnTop(@NonNull RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) return false;
        if (layoutManager instanceof LinearLayoutManager) {
            int pos = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            if (((LinearLayoutManager) layoutManager).findViewByPosition(pos).getTop() == 0 && pos == 0) {
                return true;
            }
            return ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition() == 0;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int aa[] = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
            View child0 = recyclerView.getChildAt(0);
            if (child0 == null) return false;
            int decoratedHeight = layoutManager.getTopDecorationHeight(recyclerView.getChildAt(0));
            return recyclerView.getChildAt(0).getY() - decoratedHeight == 0f && aa[0] == 0;
        }
        return false;
    }
}

