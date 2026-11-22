package razerdp.friendcircle.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;

import com.razerdp.github.com.common.entity.MomentsInfo;

import java.util.List;

import razerdp.friendcircle.app.mvp.presenter.impl.MomentPresenter;
import razerdp.friendcircle.ui.viewholder.CircleBaseViewHolder;
import razerdp.github.com.ui.base.adapter.BaseMultiRecyclerViewAdapter;
import razerdp.github.com.ui.base.adapter.BaseRecyclerViewHolder;

/**
 * Created by 澶х伅娉?on 2016/11/1.
 * <p>
 * 鏈嬪弸鍦坅dapter
 */

public class CircleMomentsAdapter extends BaseMultiRecyclerViewAdapter<CircleMomentsAdapter, MomentsInfo> {

    private MomentPresenter momentPresenter;

    public CircleMomentsAdapter(@NonNull Context context, @NonNull List<MomentsInfo> datas, MomentPresenter presenter) {
        super(context, datas);
        this.momentPresenter = presenter;
    }

    @Override
    protected void onInitViewHolder(BaseRecyclerViewHolder holder) {
        if (holder instanceof CircleBaseViewHolder) {
            ((CircleBaseViewHolder) holder).setPresenter(momentPresenter);
        }
    }
}

