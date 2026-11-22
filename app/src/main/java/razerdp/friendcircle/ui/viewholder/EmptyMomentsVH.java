package razerdp.friendcircle.ui.viewholder;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.razerdp.github.com.common.entity.MomentsInfo;

import razerdp.friendcircle.R;
import razerdp.github.com.ui.base.adapter.LayoutId;


/**
 * Created by 澶х伅娉?on 2016/11/3.
 * <p>
 * 绌哄唴瀹圭殑vh
 *
 * @see MomentsType
 */

@LayoutId(id = R.layout.moments_empty_content)
public class EmptyMomentsVH extends CircleBaseViewHolder {


    public EmptyMomentsVH(View itemView, int viewType) {
        super(itemView, viewType);
    }

    @Override
    public void onFindView(@NonNull View rootView) {

    }

    @Override
    public void onBindDataToView(@NonNull MomentsInfo data, int position, int viewType) {

    }
}

