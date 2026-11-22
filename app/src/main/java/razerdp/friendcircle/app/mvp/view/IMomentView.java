package razerdp.friendcircle.app.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.razerdp.github.com.common.entity.CommentInfo;
import com.razerdp.github.com.common.entity.LikesInfo;
import com.razerdp.github.com.common.entity.MomentsInfo;

import java.util.List;

import razerdp.github.com.lib.mvp.IBaseView;
import razerdp.github.com.ui.widget.commentwidget.CommentWidget;


/**
 * Created by 澶х伅娉?on 2016/12/7.
 */

public interface IMomentView extends IBaseView {

    void onLikeChange(int itemPos, List<LikesInfo> likeUserList);

    void onCommentChange(int itemPos, List<CommentInfo> commentInfoList);

    /**
     * 鍥犱负recyclerview閫氳繃浣嶇疆鎵惧埌itemview鏈夊彲鑳戒細鎵句笉鍒板搴旂殑View锛屾墍浠ヨ繖娆＄洿鎺ヤ紶鍊?     *
     * @param viewHolderRootView
     * @param itemPos
     * @param momentid
     * @param commentWidget
     */
    void showCommentBox(@Nullable View viewHolderRootView, int itemPos, String momentid, CommentWidget commentWidget);

    void onDeleteMomentsInfo(@NonNull MomentsInfo momentsInfo);

}

