package razerdp.github.com.ui.widget.pullrecyclerview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.socks.library.KLog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.IOverScrollUpdateListener;
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;
import razerdp.github.com.lib.utils.ToolUtil;
import razerdp.github.com.ui.util.AnimUtils;
import razerdp.github.com.ui.util.UIHelper;
import razerdp.github.com.ui.util.ViewOffsetHelper;
import razerdp.github.com.baseuilib.R;
import razerdp.github.com.ui.widget.pullrecyclerview.interfaces.OnRefreshListener2;
import razerdp.github.com.ui.widget.pullrecyclerview.mode.Mode;
import razerdp.github.com.ui.widget.pullrecyclerview.mode.PullMode;
import razerdp.github.com.ui.widget.pullrecyclerview.wrapperadapter.FixedViewInfo;
import razerdp.github.com.ui.widget.pullrecyclerview.wrapperadapter.HeaderViewWrapperAdapter;

import static me.everything.android.ui.overscroll.IOverScrollState.STATE_BOUNCE_BACK;
import static me.everything.android.ui.overscroll.IOverScrollState.STATE_DRAG_END_SIDE;
import static me.everything.android.ui.overscroll.IOverScrollState.STATE_DRAG_START_SIDE;
import static me.everything.android.ui.overscroll.IOverScrollState.STATE_IDLE;
import static razerdp.github.com.ui.widget.pullrecyclerview.CircleRecyclerView.Status.DEFAULT;
import static razerdp.github.com.ui.widget.pullrecyclerview.CircleRecyclerView.Status.REFRESHING;


/**
 * Created by 澶х伅娉?on 2016/10/29.
 * <p>
 * 涓撲负鏈嬪弸鍦堥」鐩畾鍒剁殑涓嬫媺recyclerview
 * 鍘熷洜鍦ㄤ簬锛? * 1 - 鐩墠鑰岃█锛実it涓婂ぇ澶氭暟鐨剅v閮芥槸鐢ㄧ殑swiperefreshlayout
 * 2 - 澶у鏁皉v閮戒笉鏀寔涓嬫媺鍚庢敹鍥炰笅鎷夊ご閮ㄧ殑
 * 3 - 鍥犱负澶у鏁扮殑rv閮芥敮鎸佸お澶氬姛鑳戒簡锛屾樉寰楁湁鐐归噸锛屽苟涓旀湁浜涙兂瑕佺殑鍥炶皟鎴戜滑閮芥病娉曟嬁鍒? * <p>
 * 缁间笂鎵€杩帮紝骞茶剢鑷繁寮勪竴涓畻浜嗐€傘€傘€傘€? * <p>
 * 鐩爣锛? * 銆愬熀鏈姹傘€戝洜涓虹浉褰撲簬瀹氬埗锛屽彧涓烘湰椤圭洰鏈嶅姟锛屽洜姝や笉鑰冭檻閫氱敤鎬э紝鏇村鑰冭檻鎵╁睍鎬? * <p>
 * <p>
 * 1 - 涓嬫媺鍜屼笂鎷夋敮鎸佸洖璋? * 2 - 璺焛OS鏈嬪弸鍦堜笅鎷夊ご閮ㄤ竴鏍凤紝鏀寔澶撮儴鍦ㄥ埛鏂版椂鐨勫洖寮? * 3 - 婊戝姩鍒板簳閮ㄨ嚜鍔ㄥ姞杞芥洿澶? * 4 - addHeaderView銆? * <p>
 * 浣跨敤鐨勫簱锛歰verscroll-decor(https://github.com/EverythingMe/overscroll-decor)
 */

public class CircleRecyclerView extends FrameLayout {
    private static final String TAG = "CircleRecyclerView";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DEFAULT, REFRESHING})
    @interface Status {
        int DEFAULT = 0;
        int REFRESHING = 1;
    }

    @Status
    private int currentStatus;

    private PullMode pullMode;

    private Mode mode = Mode.BOTH;

    //observer
    private InnerRefreshIconObserver iconObserver;

    //callback
    private OnRefreshListener2 onRefreshListener;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ImageView refreshIcon;

    private int refreshPosition;
    private PullRefreshFooter footerView;

    private boolean canPull;
    private boolean canLoadMore;


    public CircleRecyclerView(Context context) {
        this(context, null);
    }

    public CircleRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) return;
        GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xff323232, 0xff323232, 0xffffffff, 0xffffffff});
        setBackground(background);

        if (recyclerView == null) {
            recyclerView = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.view_recyclerview, this, false);
            //new鍑烘潵鐨剅ecyclerview骞舵病鏈夋粴鍔ㄦ潯锛屽師鍥狅細娌℃湁璧板埌View.initializeScrollbars(TypedArray a)
            //recyclerView = new RecyclerView(context);
            recyclerView.setBackgroundColor(Color.WHITE);
            linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            //娓叉煋浼樺寲锛屾斁鍒皉ender thread鍋氾紝锛坧refetch鍦╲25涔嬪悗鍙敤锛夛紝鏈哄瀷鍦ㄨ悵鑾夌偖(lollipop)鍚庢墠鍙互浜彈姝や紭鍖栵紙浜嬪疄涓婇粯璁ゆ槸寮€鍚殑锛?            //linearLayoutManager.setItemPrefetchEnabled(true);
            recyclerView.setLayoutManager(linearLayoutManager);
        }
        //鍙栨秷榛樿item鍙樻洿鍔ㄧ敾
        recyclerView.setItemAnimator(null);

        if (refreshIcon == null) {
            refreshIcon = new ImageView(context);
            refreshIcon.setBackgroundColor(Color.TRANSPARENT);
            refreshIcon.setImageResource(R.drawable.rotate_icon);
        }
        FrameLayout.LayoutParams iconParam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iconParam.leftMargin = UIHelper.dipToPx(12);

        addView(recyclerView, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
        addView(refreshIcon, iconParam);

        refreshPosition = UIHelper.dipToPx(90);

        iconObserver = new InnerRefreshIconObserver(refreshIcon, refreshPosition);

        footerView = new PullRefreshFooter(getContext());

        addFooterView(footerView);

        setMode(Mode.BOTH);
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 2) {
            throw new IllegalStateException("鍜冲挸锛屼笉鑳借秴杩囦袱涓獀iew鍝?);
        }
        super.onFinishInflate();
    }

    private void setCurrentStatus(@Status int status) {
        this.currentStatus = status;
    }

    public void compelete() {
        Log.i(TAG, "compelete");
        if (pullMode == PullMode.FROM_START && iconObserver != null) {
            iconObserver.catchResetEvent();
        }
        if (pullMode == PullMode.FROM_BOTTOM && footerView != null) {
            footerView.onFinish();
        }
        setCurrentStatus(DEFAULT);
    }

    public void autoRefresh() {
        if (!canRefresh() || iconObserver == null || onRefreshListener == null)
            return;
        setPullMode(PullMode.FROM_START);
        setCurrentStatus(REFRESHING);
        iconObserver.autoRefresh();
        onRefreshListener.onRefresh();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (onPreDispatchTouchListener != null) {
            onPreDispatchTouchListener.onPreTouch(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    boolean canRefresh() {
        return canPull && currentStatus != REFRESHING && (mode == Mode.REFRESH || mode == Mode.BOTH);
    }

    boolean canLoadMore() {
        return canLoadMore && currentStatus != REFRESHING && (mode == Mode.LOADMORE || mode == Mode.BOTH);
    }

    private void setPullMode(PullMode pullMode) {
        this.pullMode = pullMode;
    }

    //------------------------------------------get/set-----------------------------------------------

    public OnRefreshListener2 getOnRefreshListener() {
        return onRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener2 onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public OnPreDispatchTouchListener getOnPreDispatchTouchListener() {
        return onPreDispatchTouchListener;
    }

    public void setOnPreDispatchTouchListener(OnPreDispatchTouchListener onPreDispatchTouchListener) {
        this.onPreDispatchTouchListener = onPreDispatchTouchListener;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            if (mHeaderViewInfos.size() > 0 || mFooterViewInfos.size() > 0) {
                recyclerView.setAdapter(wrapHeaderRecyclerViewAdapterInternal(adapter, mHeaderViewInfos, mFooterViewInfos));
            } else {
                recyclerView.setAdapter(adapter);
            }
        }
        initOverScroll();
    }

    public void setCanPull(boolean canPull) {
        this.canPull = canPull;
    }

    public void setLoadMoreEnable(boolean canLoadMore) {
        if (recyclerView == null) return;
        this.canLoadMore = canLoadMore;
        if (canLoadMore) {
            recyclerView.addOnScrollListener(onScrollListener);
        } else {
            footerView.onFinish();
            recyclerView.removeOnScrollListener(onScrollListener);
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        switch (mode) {
            case REFRESH:
                setCanPull(true);
                setLoadMoreEnable(false);
                break;
            case BOTH:
                setCanPull(true);
                setLoadMoreEnable(true);
                break;
            case LOADMORE:
                setCanPull(false);
                setLoadMoreEnable(true);
                break;
        }
    }


    private void initOverScroll() {
        IOverScrollDecor decor = new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(
                recyclerView), 2f, 1f, 2f);
        decor.setOverScrollStateListener(new IOverScrollStateListener() {
            @Override
            public void onOverScrollStateChange(IOverScrollDecor decor,
                                                int oldState,
                                                int newState) {
                switch (newState) {
                    case STATE_IDLE:
                        // No over-scroll is in effect.
                        break;
                    case STATE_DRAG_START_SIDE:
                        // Dragging started at the left-end.
                        break;
                    case STATE_DRAG_END_SIDE:
                        // Dragging started at the right-end.
                        KLog.i("refreshState", "current state  >>>   " + currentStatus + "   refresh mode  >>>   " + pullMode);

                        break;
                    case STATE_BOUNCE_BACK:
                        if (oldState == STATE_DRAG_START_SIDE) {
                            // Dragging stopped -- view is starting to bounce back from the *left-end* onto natural position.
                        } else { // i.e. (oldState == STATE_DRAG_END_SIDE)
                            // View is starting to bounce back from the *right-end*.
                        }
                        break;
                }
            }
        });

        decor.setOverScrollUpdateListener(new IOverScrollUpdateListener() {
            @Override
            public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
                if (offset > 0) {
                    if (!canRefresh()) return;
                    iconObserver.catchPullEvent(offset);
                    if (offset >= refreshPosition && state == STATE_BOUNCE_BACK) {
                        if (currentStatus != REFRESHING) {
                            setCurrentStatus(REFRESHING);
                            if (onRefreshListener != null) {
                                Log.i(TAG, "refresh");
                                onRefreshListener.onRefresh();
                            }
                            setPullMode(PullMode.FROM_START);
                            iconObserver.catchRefreshEvent();
                        }
                    }
                } else if (offset < 0) {
                    //搴曢儴鐨刼verscroll
                }
            }
        });
    }

    /**
     * 鍒ゆ柇recyclerview鏄惁婊戝埌搴曢儴
     * <p>
     * 鍘熺悊锛氬垽鏂粦杩囩殑璺濈鍔犱笂灞忓箷涓婄殑鏄剧ず鐨勫尯鍩熸槸鍚︽瘮鏁翠釜鎺т欢楂樺害楂?     *
     * @return
     */
    public boolean isScrollToBottom() {
        return recyclerView != null && recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange();
    }


    public int findFirstVisibleItemPosition() {
        return linearLayoutManager.findFirstVisibleItemPosition();
    }


    /**
     * scroll listener
     */
    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            //fix issue #42
            //鎸夌収鍘熸潵鐨勪範鎯紝鎴戞槸褰揜ecyclerView婊戝姩鍋滄鐘舵€佷笅鎵嶆鏌ユ槸鍚﹁鑷姩鍔犲湪鏇村锛屼絾灏卞嚭鐜颁簡涓€涓棶棰橈紝濡?42鎻愬嚭鐨勶紝鎵嬫寚涓嶇寮€灞忓箷涓€鐩存粦鍔?            //灏变細鏃犳硶鍔犺浇鏇村锛屽湪iOS鏈嬪弸鍦堥噷锛屼笉绂诲紑灞忓箷鏄彲浠ョ户缁姞杞界殑锛屽洜姝ゅ皢杩欓噷鐨勯€昏緫绉诲姩鍒颁簡onScrolled锛屼笉瀵规粦鍔ㄧ姸鎬佽繘琛岀洃鍚簡
            /*if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (isScrollToBottom() && currentStatus != REFRESHING) {
                    onRefreshListener.onLoadMore();
                    KLog.i("loadmoretag", "loadmore");
                    pullMode = FROM_BOTTOM;
                    setCurrentStatus(REFRESHING);
                    footerView.onRefreshing();
                }
            }*/
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (isScrollToBottom() && canLoadMore()) {
                onRefreshListener.onLoadMore();
                Log.i("loadmoretag", "loadmore");
                setPullMode(PullMode.FROM_BOTTOM);
                setCurrentStatus(REFRESHING);
                footerView.onRefreshing();
            }
        }
    };


    /**
     * 鍒锋柊Icon鐨勫姩浣滆瀵熻€?     */

    private static class InnerRefreshIconObserver {
        private ViewOffsetHelper viewOffsetHelper;
        private ImageView refreshIcon;
        private final int refreshPosition;
        private RotateAnimation rotateAnimation;
        private ValueAnimator mValueAnimator;

        InnerRefreshIconObserver(ImageView refreshIcon, int refreshPosition) {
            this.refreshIcon = refreshIcon;
            this.refreshPosition = refreshPosition;

            viewOffsetHelper = new ViewOffsetHelper(refreshIcon);

            rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            rotateAnimation.setFillBefore(true);

        }

        void catchPullEvent(float offset) {
            if (checkHacIcon()) {
                refreshIcon.setRotation(-offset * 2);
                if (offset >= refreshPosition) {
                    offset = refreshPosition;
                }
                viewOffsetHelper.absoluteOffsetTopAndBottom((int) offset);
                adjustRefreshIconPosition();
            }
        }

        /**
         * 璋冩暣icon鐨勪綅缃晫闄?         */
        private void adjustRefreshIconPosition() {
            if (refreshIcon.getY() < 0) {
                refreshIcon.offsetTopAndBottom(Math.abs(refreshIcon.getTop()));
            } else if (refreshIcon.getY() > refreshPosition) {
                refreshIcon.offsetTopAndBottom(-(refreshIcon.getTop() - refreshPosition));
            }
        }

        void catchRefreshEvent() {
            if (checkHacIcon()) {
                refreshIcon.clearAnimation();
                if (refreshIcon.getTop() < refreshPosition) {
                    viewOffsetHelper.absoluteOffsetTopAndBottom(refreshPosition);
                }
                refreshIcon.startAnimation(rotateAnimation);
            }
        }

        void catchResetEvent() {
            KLog.i("refreshTop", " top  >>>  " + refreshIcon.getTop());
            if (mValueAnimator == null) {
                mValueAnimator = ValueAnimator.ofFloat(refreshPosition, 0);
                mValueAnimator.setInterpolator(new LinearInterpolator());
                mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float result = (float) animation.getAnimatedValue();
                        catchPullEvent(result);
                    }
                });
                mValueAnimator.addListener(new AnimUtils.SimpleAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        refreshIcon.clearAnimation();
                    }
                });
                mValueAnimator.setDuration(540);
            }

            refreshIcon.post(new Runnable() {
                @Override
                public void run() {
                    mValueAnimator.start();
                }
            });
        }

        private boolean checkHacIcon() {
            return refreshIcon != null;
        }

        void autoRefresh() {
            ValueAnimator animator = ValueAnimator.ofFloat(0, refreshPosition);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(540);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float result = (float) animation.getAnimatedValue();
                    catchPullEvent(result);
                }
            });
            animator.addListener(new AnimUtils.SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    catchRefreshEvent();
                }
            });
            animator.start();
        }
    }


    //------------------------------------------鍒嗗壊绾?----------------------------------------------
    //------------------------------------------鍒嗗壊绾?----------------------------------------------
    //------------------------------------------鍒嗗壊绾?----------------------------------------------

    /**
     * 浠ヤ笅涓簉ecyclerview 鐨刪eaderadapter瀹炵幇鏂规
     * <p>
     * 浠istview鐨刪eaderView鍜宖ooterView涓烘ā鏉垮仛鍑虹殑recyclerview鐨刪eader鍜宖ooter
     */
    private ArrayList<FixedViewInfo> mHeaderViewInfos = new ArrayList<>();
    private ArrayList<FixedViewInfo> mFooterViewInfos = new ArrayList<>();

    /**
     * 涓嶅畬缇庤В鍐虫柟娉曪細娣诲姞涓€涓猦eader锛屽垯浠?2寮€濮嬪噺1
     * header:-2~-98
     */
    private static final int ITEM_VIEW_TYPE_HEADER_START = -2;
    /**
     * 涓嶅畬缇庤В鍐虫柟娉曪細娣诲姞涓€涓猦eader锛屽垯浠?99寮€濮嬪噺1
     * footer:-99~-鏃犵┓
     */
    private static final int ITEM_VIEW_TYPE_FOOTER_START = -99;

    public void addHeaderView(View headerView) {
        final FixedViewInfo info = new FixedViewInfo(headerView, ITEM_VIEW_TYPE_HEADER_START - mHeaderViewInfos.size());
        if (mHeaderViewInfos.size() == Math.abs(ITEM_VIEW_TYPE_FOOTER_START - ITEM_VIEW_TYPE_HEADER_START)) {
            mHeaderViewInfos.remove(mHeaderViewInfos.size() - 1);
        }
        if (checkFixedViewInfoNotAdded(info, mHeaderViewInfos)) {
            mHeaderViewInfos.add(info);
        }
        checkAndNotifyWrappedViewAdd(recyclerView.getAdapter(), info, true);

    }

    private void checkAndNotifyWrappedViewAdd(RecyclerView.Adapter adapter, FixedViewInfo info, boolean isHeader) {
        //header鍜宖ooter鍙兘鍐峴etAdapter鍓嶄娇鐢紝濡傛灉鏄痵et浜嗕箣鍚庡啀鐢紝涓轰綍涓峚dd鏅€氱殑viewholder鑰岄潪瑕丠eadr鎴栬€協ooter鍛?        if (adapter != null) {
            if (!(adapter instanceof HeaderViewWrapperAdapter)) {
                adapter = wrapHeaderRecyclerViewAdapterInternal(adapter);
                if (isHeader) {
                    adapter.notifyItemInserted(((HeaderViewWrapperAdapter) adapter).findHeaderPosition(info.view));
                } else {
                    adapter.notifyItemInserted(((HeaderViewWrapperAdapter) adapter).findFooterPosition(info.view));
                }
            }
        }
    }

    public void addFooterView(View footerView) {
        final FixedViewInfo info = new FixedViewInfo(footerView, ITEM_VIEW_TYPE_FOOTER_START - mFooterViewInfos.size());
        if (checkFixedViewInfoNotAdded(info, mFooterViewInfos)) {
            mFooterViewInfos.add(info);
        }
        checkAndNotifyWrappedViewAdd(recyclerView.getAdapter(), info, false);
    }

    private boolean checkFixedViewInfoNotAdded(FixedViewInfo info, List<FixedViewInfo> infoList) {
        boolean result = true;
        if (ToolUtil.isListEmpty(infoList) || info == null) {
            result = true;
        } else {
            for (FixedViewInfo fixedViewInfo : infoList) {
                if (fixedViewInfo.view == info.view) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public int getHeaderViewCount() {
        return mHeaderViewInfos.size();
    }

    public int getFooterViewCount() {
        return mFooterViewInfos.size();
    }


    protected HeaderViewWrapperAdapter wrapHeaderRecyclerViewAdapterInternal(@NonNull RecyclerView.Adapter mWrappedAdapter,
                                                                             ArrayList<FixedViewInfo> mHeaderViewInfos,
                                                                             ArrayList<FixedViewInfo> mFooterViewInfos) {
        return new HeaderViewWrapperAdapter(recyclerView, mWrappedAdapter, mHeaderViewInfos, mFooterViewInfos);

    }

    protected HeaderViewWrapperAdapter wrapHeaderRecyclerViewAdapterInternal(@NonNull RecyclerView.Adapter mWrappedAdapter) {
        return wrapHeaderRecyclerViewAdapterInternal(mWrappedAdapter, mHeaderViewInfos, mFooterViewInfos);

    }


    //------------------------------------------interface-----------------------------------------------
    private OnPreDispatchTouchListener onPreDispatchTouchListener;

    public interface OnPreDispatchTouchListener {
        boolean onPreTouch(MotionEvent ev);
    }


}

