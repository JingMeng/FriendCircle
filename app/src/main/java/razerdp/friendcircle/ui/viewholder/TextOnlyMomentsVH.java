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
 *
 * 琛规湁鏂囧瓧鐨剉h
 *
 * @see MomentsType
 */

@LayoutId(id =  R.layout.moments_only_text)
public class TextOnlyMomentsVH extends CircleBaseViewHolder {

    public TextOnlyMomentsVH(View itemView, int viewType) {
        super(itemView, viewType);
    }

    @Override
    public void onFindView(@NonNull View rootView) {

    }

    @Override
    public void onBindDataToView(@NonNull MomentsInfo data, int position, int viewType) {

    }
}

