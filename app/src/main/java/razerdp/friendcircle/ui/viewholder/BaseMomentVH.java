package razerdp.friendcircle.ui.viewholder;

import androidx.annotation.NonNull;
import android.view.View;

/**
 * Created by 澶х伅娉?on 2016/11/1.
 *
 * 鎶借薄鍑虹殑vh鎺ュ彛
 */

public interface BaseMomentVH<T> {

    void onFindView(@NonNull View rootView);

    void onBindDataToView(@NonNull final T data,int position,int viewType);
}

