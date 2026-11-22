package razerdp.friendcircle.ui.viewholder;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.razerdp.github.com.common.entity.CommentInfo;
import com.razerdp.github.com.common.entity.LikesInfo;
import com.razerdp.github.com.common.entity.MomentsInfo;
import com.razerdp.github.com.common.manager.LocalHostManager;
import com.socks.library.KLog;

import java.util.List;

import razerdp.friendcircle.R;
import razerdp.friendcircle.app.mvp.presenter.impl.MomentPresenter;
import razerdp.friendcircle.ui.widget.popup.CommentPopup;
import razerdp.friendcircle.ui.widget.popup.DeleteCommentPopup;
import razerdp.friendcircle.ui.widget.praisewidget.PraiseWidget;
import razerdp.github.com.lib.utils.StringUtil;
import razerdp.github.com.lib.utils.TimeUtil;
import razerdp.github.com.lib.utils.ToolUtil;
import razerdp.github.com.ui.base.adapter.BaseMultiRecyclerViewHolder;
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;
import razerdp.github.com.ui.util.UIHelper;
import razerdp.github.com.ui.util.ViewUtil;
import razerdp.github.com.ui.widget.commentwidget.CommentContentsLayout;
import razerdp.github.com.ui.widget.commentwidget.CommentWidget;
import razerdp.github.com.ui.widget.commentwidget.IComment;
import razerdp.github.com.ui.widget.common.ClickShowMoreLayout;

/**
 * Created by 澶х伅娉?on 2016/11/1.
 * <p>
 * 鏈嬪弸鍦堝熀鏈琲tem
 */
public abstract class CircleBaseViewHolder extends BaseMultiRecyclerViewHolder<MomentsInfo> implements BaseMomentVH<MomentsInfo> {


    //澶撮儴
    protected ImageView avatar;
    protected TextView nick;
    protected ClickShowMoreLayout userText;

    //搴曢儴
    protected TextView createTime;
    protected TextView deleteMoments;
    protected ImageView commentImage;
    protected FrameLayout menuButton;
    protected LinearLayout commentAndPraiseLayout;
    protected PraiseWidget praiseWidget;
    protected View line;
    protected CommentContentsLayout commentLayout;

    //鍐呭鍖?    protected LinearLayout contentLayout;

    private CommentPopup commentPopup;
    private DeleteCommentPopup deleteCommentPopup;

    private MomentPresenter momentPresenter;
    private int itemPosition;
    private MomentsInfo momentsInfo;


    public CircleBaseViewHolder(View itemView, int viewType) {
        super(itemView, viewType);
        onFindView(itemView);

        //header
        avatar = (ImageView) findView(avatar, R.id.avatar);
        nick = (TextView) findView(nick, R.id.nick);
        userText = (ClickShowMoreLayout) findView(userText, R.id.item_text_field);
        userText.setOnStateKeyGenerateListener(new ClickShowMoreLayout.OnStateKeyGenerateListener() {
            @Override
            public int onStateKeyGenerated(int originKey) {
                return originKey + itemPosition;
            }
        });

        //bottom
        createTime = (TextView) findView(createTime, R.id.create_time);
        deleteMoments = (TextView) findView(deleteMoments, R.id.tv_delete_moment);
        commentImage = (ImageView) findView(commentImage, R.id.menu_img);
        menuButton = (FrameLayout) findView(menuButton, R.id.menu_button);
        commentAndPraiseLayout = (LinearLayout) findView(commentAndPraiseLayout, R.id.comment_praise_layout);
        praiseWidget = (PraiseWidget) findView(praiseWidget, R.id.praise);
        line = findView(line, R.id.divider);
        commentLayout = (CommentContentsLayout) findView(commentLayout, R.id.comment_layout);
        commentLayout.setMode(CommentContentsLayout.Mode.EXPANDABLE);
        commentLayout.setOnCommentItemClickListener(onCommentItemClickListener);
        commentLayout.setOnCommentItemLongClickListener(onCommentItemLongClickListener);
        commentLayout.setOnCommentWidgetItemClickListener(onCommentWidgetItemClickListener);
        // FIXME: 2018/1/3 鏆傛椂鏈紑鍙戝畬
//        commentLayout.setMode(CommentContentsLayout.Mode.EXPANDABLE);
        //content
        contentLayout = (LinearLayout) findView(contentLayout, R.id.content);

        if (commentPopup == null) {
            commentPopup = new CommentPopup((Activity) getContext());
            commentPopup.setOnCommentPopupClickListener(onCommentPopupClickListener);
        }

        if (deleteCommentPopup == null) {
            deleteCommentPopup = new DeleteCommentPopup((Activity) getContext());
            deleteCommentPopup.setOnDeleteCommentClickListener(onDeleteCommentClickListener);
        }
    }

    public void setPresenter(MomentPresenter momentPresenter) {
        this.momentPresenter = momentPresenter;
    }

    public MomentPresenter getPresenter() {
        return momentPresenter;
    }

    @Override
    public void onBindData(MomentsInfo data, int position) {
        if (data == null) {
            KLog.e("鏁版嵁鏄┖鐨勶紒锛侊紒锛?);
            findView(userText, R.id.item_text_field);
            userText.setText("杩欎釜鍔ㄦ€佺殑鏁版嵁鏄┖鐨勩€傘€傘€傘€侽MG");
            return;
        }
        this.momentsInfo = data;
        this.itemPosition = position;
        //閫氱敤鏁版嵁缁戝畾
        onBindMutualDataToViews(data);
        //鐐瑰嚮浜嬩欢
        menuButton.setOnClickListener(onMenuButtonClickListener);
        menuButton.setTag(R.id.momentinfo_data_tag_id, data);
        deleteMoments.setOnClickListener(onDeleteMomentClickListener);
        //浼犻€掑埌瀛愮被
        onBindDataToView(data, position, getViewType());
    }

    private void onBindMutualDataToViews(MomentsInfo data) {
        //header
        ImageLoadMnanger.INSTANCE.loadImage(avatar, data.getAuthor().getAvatar());
        nick.setText(data.getAuthor().getNick());
        userText.setText(data.getContent().getText());
        ViewUtil.setViewsVisible(StringUtil.noEmpty(data.getContent().getText()) ?
                View.VISIBLE : View.GONE, userText);

        //bottom
        createTime.setText(TimeUtil.getTimeStringFromBmob(data.getCreatedAt()));
        ViewUtil.setViewsVisible(TextUtils.equals(momentsInfo.getAuthor().getUserid(), LocalHostManager.INSTANCE.getUserid()) ?
                View.VISIBLE : View.GONE, deleteMoments);
        boolean needPraiseData = addLikes(data.getLikesList());
        boolean needCommentData = commentLayout.addComments(data.getCommentList());
        praiseWidget.setVisibility(needPraiseData ? View.VISIBLE : View.GONE);
        commentLayout.setVisibility(needCommentData ? View.VISIBLE : View.GONE);
        line.setVisibility(needPraiseData && needCommentData ? View.VISIBLE : View.GONE);
        commentAndPraiseLayout.setVisibility(needCommentData || needPraiseData ? View.VISIBLE : View.GONE);

    }


    /**
     * 娣诲姞鐐硅禐
     *
     * @param likesList
     * @return ture=鏄剧ず鐐硅禐锛宖alse=涓嶆樉绀虹偣璧?     */
    private boolean addLikes(List<LikesInfo> likesList) {
        if (ToolUtil.isListEmpty(likesList)) {
            return false;
        }
        praiseWidget.setDatas(likesList);
        return true;
    }

    /**
     * ==================  click listener block
     */

    private CommentContentsLayout.OnCommentWidgetItemClickListener onCommentWidgetItemClickListener = new CommentContentsLayout.OnCommentWidgetItemClickListener() {
        @Override
        public void onCommentItemClicked(@NonNull IComment comment, CharSequence text) {
            if (comment instanceof CommentInfo) {
                UIHelper.ToastMessage("鐐瑰嚮鐨勭敤鎴?锛?銆?" + text + " 銆?);
            }
        }
    };

    private CommentContentsLayout.OnCommentItemClickListener onCommentItemClickListener = new CommentContentsLayout.OnCommentItemClickListener() {
        @Override
        public void onCommentWidgetClick(@NonNull CommentWidget widget) {
            IComment comment = widget.getData();
            CommentInfo commentInfo = null;
            if (comment instanceof CommentInfo) {
                commentInfo = (CommentInfo) comment;
            }
            if (commentInfo == null) return;
            if (commentInfo.canDelete()) {
                deleteCommentPopup.showPopupWindow(commentInfo);
            } else {
                momentPresenter.showCommentBox(null, itemPosition, momentsInfo.getMomentid(), widget);
            }
        }
    };

    private CommentContentsLayout.OnCommentItemLongClickListener onCommentItemLongClickListener = new CommentContentsLayout.OnCommentItemLongClickListener() {
        @Override
        public boolean onCommentWidgetLongClick(@NonNull CommentWidget widget) {
            return false;
        }
    };

    private DeleteCommentPopup.OnDeleteCommentClickListener onDeleteCommentClickListener = new DeleteCommentPopup.OnDeleteCommentClickListener() {
        @Override
        public void onDelClick(CommentInfo commentInfo) {
            momentPresenter.deleteComment(itemPosition, commentInfo.getCommentid(), momentsInfo.getCommentList());
        }
    };

    private View.OnClickListener onDeleteMomentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            momentPresenter.deleteMoments(v.getContext(), momentsInfo);
        }
    };


    private View.OnClickListener onMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MomentsInfo info = (MomentsInfo) v.getTag(R.id.momentinfo_data_tag_id);
            if (info != null) {
                commentPopup.updateMomentInfo(info);
                commentPopup.showPopupWindow(commentImage);
            }
        }
    };


    private CommentPopup.OnCommentPopupClickListener onCommentPopupClickListener = new CommentPopup.OnCommentPopupClickListener() {
        @Override
        public void onLikeClick(View v, @NonNull MomentsInfo info, boolean hasLiked) {
            if (hasLiked) {
                momentPresenter.unLike(itemPosition, info.getLikesObjectid(), info.getLikesList());
            } else {
                momentPresenter.addLike(itemPosition, info.getMomentid(), info.getLikesList());
            }

        }

        @Override
        public void onCommentClick(View v, @NonNull MomentsInfo info) {
            momentPresenter.showCommentBox(itemView, itemPosition, info.getMomentid(), null);
        }
    };

    /**
     * ============  tools method block
     */


    protected final View findView(View view, int resid) {
        if (resid > 0 && itemView != null && view == null) {
            return itemView.findViewById(resid);
        }
        return view;
    }


}

