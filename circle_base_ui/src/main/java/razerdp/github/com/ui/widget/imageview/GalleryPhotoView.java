package razerdp.github.com.ui.widget.imageview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.socks.library.KLog;

import uk.co.senab.photoview.PhotoView;


/**
 * Created by 澶х伅娉?on 2016/12/20.
 * <p>
 * 鐢ㄤ簬鍥剧墖娴忚椤甸潰鐨刾hotoview锛屼富瑕佽礋璐ｅ睍寮€鍔ㄧ敾鍜屽洖閫€鍔ㄧ敾鐨勮绠? * <p>
 * <p>
 * 鏈唬鐮侀儴鍒嗗弬鑰僢m-x photoview(鐢╯croller瀹炵幇鍔ㄧ敾)锛歨ttps://github.com/bm-x/PhotoView
 * 鏈唬鐮佸ぇ閮ㄥ垎鍙傝€僷hotoview鐨勫疄鐜版柟寮忥細https://github.com/chrisbanes/PhotoView
 */

public class GalleryPhotoView extends PhotoView {
    private static final int ANIMA_DURATION = 350;

    private BitmapTransform bitmapTransform;
    private OnEnterAnimaEndListener onEnterAnimaEndListener;
    private OnExitAnimaEndListener onExitAnimaEndListener;

    private boolean isPlayingEnterAnima = false;
    private boolean isPlayingExitAnima = false;

    private Point globalOffset;
    private float[] scaleRatios;
    private RectF clipBounds;

    public GalleryPhotoView(Context context) {
        super(context);
    }

    public GalleryPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public GalleryPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        bitmapTransform = new BitmapTransform();
        globalOffset = new Point();
    }

    @Override
    public void draw(Canvas canvas) {
        if (clipBounds != null) {
            canvas.clipRect(clipBounds);
            KLog.i("clip", "clipBounds  >>  " + clipBounds.toShortString());
            clipBounds = null;
        }
        super.draw(canvas);
    }

    public void playEnterAnima(final Rect from, @Nullable final OnEnterAnimaEndListener l) {
        this.onEnterAnimaEndListener = l;
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                playEnterAnimaInternal(from);
                getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }

    private void playEnterAnimaInternal(final Rect from) {
        if (isPlayingEnterAnima || from == null) return;

        final Rect tFrom = new Rect(from);
        final Rect to = new Rect();

        getGlobalVisibleRect(to, globalOffset);

        tFrom.offset(-globalOffset.x, -globalOffset.y);
        to.offset(-globalOffset.x, -globalOffset.y);

        scaleRatios = calculateRatios(tFrom, to);

        setPivotX(tFrom.centerX() / to.width());
        setPivotY(tFrom.centerY() / to.height());

        final AnimatorSet enterSet = new AnimatorSet();
        enterSet.play(ObjectAnimator.ofFloat(this, View.X, tFrom.left, to.left))
                .with(ObjectAnimator.ofFloat(this, View.Y, tFrom.top, to.top))
                .with(ObjectAnimator.ofFloat(this, View.SCALE_X, scaleRatios[0], 1f))
                .with(ObjectAnimator.ofFloat(this, View.SCALE_Y, scaleRatios[1], 1f));

        enterSet.setDuration(ANIMA_DURATION);
        enterSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isPlayingEnterAnima = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isPlayingEnterAnima = false;
                if (onEnterAnimaEndListener != null) {
                    onEnterAnimaEndListener.onEnterAnimaEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isPlayingEnterAnima = false;

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                isPlayingEnterAnima = true;

            }
        });
        enterSet.start();


    }


    public void playExitAnima(Rect to, @Nullable View alphaView, @Nullable final OnExitAnimaEndListener l) {
        this.onExitAnimaEndListener = l;
        playExitAnimaInternal(to, alphaView);
    }

    private void playExitAnimaInternal(final Rect to, @Nullable View alphaView) {
        if (isPlayingEnterAnima || to == null || mAttacher == null) {
            if (onExitAnimaEndListener != null) {
                onExitAnimaEndListener.onExitAnimaEnd();
            }
            return;
        }

        final float currentScale = getScale();
        if (currentScale > 1.0f) setScale(1.0f);

        final Rect from = getViewBounds();
        final Rect drawableBounds = getDrawableBounds(getDrawable());
        final Rect target = new Rect(to);
        from.offset(-globalOffset.x, -globalOffset.y);
        target.offset(-globalOffset.x, -globalOffset.y);
        if (drawableBounds == null) {
            if (onExitAnimaEndListener != null) {
                onExitAnimaEndListener.onExitAnimaEnd();
            }
            return;
        }

        //bitmap浣嶇Щ
        bitmapTransform.animaTranslate(from.centerX(), target.centerX(), from.centerY(), target.centerY());

        float scale = calculateScaleByCenterCrop(from, drawableBounds, target);
        //绛夋瘮缂╂斁
        bitmapTransform.animaScale(getScale(), scale, from.centerX(), from.centerY());

        if (alphaView != null) {
            bitmapTransform.animaAlpha(alphaView, 1.0f, 0);
        }

        if (target.width() < from.width() || target.height() < from.height()) {
            bitmapTransform.animaClip(from, target);
        }


        bitmapTransform.start(new OnAllFinishListener() {
            @Override
            public void onAllFinish() {
                if (onExitAnimaEndListener != null) {
                    onExitAnimaEndListener.onExitAnimaEnd();
                }
            }
        });


    }

    private float calculateScaleByCenterCrop(Rect from, Rect drawableBounds, Rect target) {
        int viewWidth = from.width();
        int viewHeight = from.height();
        int imageHeight = drawableBounds.height();
        int imageWidth = drawableBounds.width();

        float result;


        /**
         * 鍋囪鍘熷鍥剧墖楂榟锛屽w 锛孖mageview鐨勯珮y锛屽x
         * 鍒ゆ柇楂樺姣斾緥锛屽鏋滅洰鏍囬珮瀹芥瘮渚嬪ぇ浜庡師鍥撅紝鍒欏師鍥鹃珮搴︿笉鍙橈紝瀹藉害涓?w1 = (h * x) / y)鎷変几
         * 鐢诲竷瀹介珮(w1,h),鍦ㄥ師鍥剧殑((w - w1) / 2, 0)浣嶇疆杩涜鍒囧壊
         */
        int t = (viewHeight / viewWidth) - (imageHeight / imageWidth);

        if (t > 0) {
            int w1 = (imageHeight * viewWidth) / viewHeight;
            result = Math.min(target.width() * 1.0f / w1 * 1.0f, target.height() * 1.0f / imageHeight * 1.0f);

        } else {
            int h1 = (viewHeight * imageWidth) / viewWidth;
            result = Math.max(target.width() * 1.0f / imageWidth * 1.0f, target.height() * 1.0f / h1 * 1.0f);
        }

        return result;
    }

    /**
     * 鑾峰彇view鐨勫ぇ灏?     *
     * @return
     */
    public Rect getViewBounds() {
        Rect result = new Rect();
        getGlobalVisibleRect(result, globalOffset);
        KLog.i(result.toShortString());
        return result;
    }

    private Rect rectF2rect(RectF rectf) {
        Rect rect = new Rect();
        if (rectf == null) {
            rect.setEmpty();
        } else {
            rect.left = (int) rectf.left;
            rect.top = (int) rectf.top;
            rect.right = (int) rectf.right;
            rect.bottom = (int) rectf.bottom;
        }
        return rect;
    }

    private Rect getDrawableBounds(Drawable d) {
        if (d == null) return null;
        Rect result = new Rect();
        Rect tDrawableRect = d.getBounds();
        Matrix drawableMatrix = getImageMatrix();

        float[] values = new float[9];
        drawableMatrix.getValues(values);

        result.left = (int) values[Matrix.MTRANS_X];
        result.top = (int) values[Matrix.MTRANS_Y];
        result.right = (int) (result.left + tDrawableRect.width() * values[Matrix.MSCALE_X]);
        result.bottom = (int) (result.top + tDrawableRect.height() * values[Matrix.MSCALE_Y]);

        return result;
    }


    private float[] calculateRatios(Rect startBounds, Rect endBounds) {
        float[] result = new float[2];
        float widthRatio = startBounds.width() * 1.0f / endBounds.width() * 1.0f;
        float heightRatio = startBounds.height() * 1.0f / endBounds.height() * 1.0f;
        result[0] = widthRatio;
        result[1] = heightRatio;
        return result;
    }


    public OnEnterAnimaEndListener getOnEnterAnimaEndListener() {
        return onEnterAnimaEndListener;
    }

    public void setOnEnterAnimaEndListener(OnEnterAnimaEndListener onEnterAnimaEndListener) {
        this.onEnterAnimaEndListener = onEnterAnimaEndListener;
    }

    public OnExitAnimaEndListener getOnExitAnimaEndListener() {
        return onExitAnimaEndListener;
    }

    public void setOnExitAnimaEndListener(OnExitAnimaEndListener onExitAnimaEndListener) {
        this.onExitAnimaEndListener = onExitAnimaEndListener;
    }

    public interface OnEnterAnimaEndListener {
        void onEnterAnimaEnd();
    }

    public interface OnExitAnimaEndListener {
        void onExitAnimaEnd();
    }

    interface OnAllFinishListener {
        void onAllFinish();
    }

    private class BitmapTransform implements Runnable {

        static final float PRECISION = 10000f;

        View targetView;

        volatile boolean isRunning;

        Scroller translateScroller;
        Scroller scaleScroller;
        Scroller alphaScroller;
        Scroller clipScroller;

        Interpolator defaultInterpolator = new DecelerateInterpolator();


        int scaleCenterX;
        int scaleCenterY;

        float scaleX;
        float scaleY;

        float alpha;

        int dx;
        int dy;

        int preTranslateX;
        int preTranslateY;

        RectF mClipRect;
        RectF clipTo;
        RectF clipFrom;
        Matrix tempMatrix;

        OnAllFinishListener onAllFinishListener;


        BitmapTransform() {
            isRunning = false;
            translateScroller = new Scroller(getContext(), defaultInterpolator);
            scaleScroller = new Scroller(getContext(), defaultInterpolator);
            alphaScroller = new Scroller(getContext(), defaultInterpolator);
            clipScroller = new Scroller(getContext(), defaultInterpolator);
            mClipRect = new RectF();
            tempMatrix = new Matrix();
        }

        void animaScale(float fromX, float toX, float fromY, float toY, int centerX, int centerY) {
            this.scaleCenterX = centerX;
            this.scaleCenterY = centerY;
            scaleScroller.startScroll((int) (fromX * PRECISION), (int) (fromY * PRECISION), (int) ((toX - fromX) * PRECISION), (int) ((toY - fromY) * PRECISION), ANIMA_DURATION);
        }

        void animaScale(float from, float to, int centerX, int centerY) {
            animaScale(from, to, from, to, centerX, centerY);
        }

        void animaTranslate(int fromX, int toX, int fromY, int toY) {
            preTranslateX = 0;
            preTranslateY = 0;
            translateScroller.startScroll(0, 0, toX - fromX, toY - fromY, ANIMA_DURATION);
            KLog.i("animaTranslate", (toX - fromX), (toY - fromY));
        }

        void animaAlpha(View target, float fromAlpha, float toAlpha) {
            this.targetView = target;
            alphaScroller.startScroll((int) (fromAlpha * PRECISION), 0, (int) ((toAlpha - fromAlpha) * PRECISION), 0, ANIMA_DURATION);
        }

        void animaClip(Rect clipFrom, Rect clipTo) {
            this.clipFrom = new RectF(clipFrom);
            this.clipTo = new RectF(clipTo);
            KLog.i("clip", "clipFrom  >>>  " + clipFrom.toShortString());
            KLog.i("clip", "clipTo  >>>  " + clipTo.toShortString());

            if (!clipFrom.isEmpty() && !clipTo.isEmpty()) {
                //绠楀嚭瑁佸壀姣旂巼
                float dx = Math.min(1.0f, clipTo.width() * 1.0f / clipFrom.width() * 1.0f);
                float dy = Math.min(1.0f, clipTo.height() * 1.0f / clipFrom.height() * 1.0f);
                //鍥犱负scroller鏄璧峰鍊煎拰缁堢偣鍊间箣闂寸殑鏁板€煎奖鍝嶏紝鎵€浠ュ噺鍘?锛屽鏋滀负0锛屾剰鍛崇潃涓嶈鍓紝鍥犱负鐩爣鍊兼瘮寮€濮嬪€煎ぇ锛岃€岀敾甯冩棤娉曞啀鎵╁ぇ浜嗭紝鎵€浠ュ拷鐣?                dx = dx - 1;
                dy = dy - 1;
                //浠?寮€濮?涔樹互1w淇濊瘉绮惧害
                clipScroller.startScroll((int) (0 * PRECISION), (int) (0 * PRECISION), (int) (dx * PRECISION), (int) (dy * PRECISION), ANIMA_DURATION);
            }
        }

        @Override
        public void run() {

            boolean isAllFinish = true;

            if (scaleScroller.computeScrollOffset()) {
                scaleX = (float) scaleScroller.getCurrX() / PRECISION;
                scaleY = (float) scaleScroller.getCurrY() / PRECISION;

                isAllFinish = false;
            }

            if (translateScroller.computeScrollOffset()) {
                int curX = translateScroller.getCurrX();
                int curY = translateScroller.getCurrY();

                dx += curX - preTranslateX;
                dy += curY - preTranslateY;

                KLog.i("animaTranslate", dx, dy);

                preTranslateX = curX;
                preTranslateY = curY;

                isAllFinish = false;
            }

            if (alphaScroller.computeScrollOffset()) {
                alpha = (float) alphaScroller.getCurrX() / PRECISION;
                isAllFinish = false;
            }

            if (clipScroller.computeScrollOffset()) {

                float curX = Math.abs((float) clipScroller.getCurrX() / PRECISION);
                float curY = Math.abs((float) clipScroller.getCurrY() / PRECISION);

                KLog.i("clip", curX, curY);

                //绠楀嚭褰撳墠鐨勭Щ鍔ㄥ儚绱犵殑缁煎悎
                float dx = clipFrom.width() * curX;
                float dy = clipFrom.height() * curY;


                /*
                 瑁佸壀鍔ㄧ敾绠楁硶鏄竴涓€傘€傘€傚垵涓殑绠€鍗曟柟绋嬮

                 璁捐鍓繃绋嬩腑锛屽乏杈圭紭绉诲姩dl涓儚绱狅紝鍙宠竟缂樼Щ鍔╠r涓儚绱?                 鐢变笂闈x绠楀嚭dl+dr=dx;
                 鍥犱负d鍜宒r鐨勬瘮鐜囧彲鐭ワ紝鍥犳鍙互鐭ラ亾dLeft鍜宒Right鐨勭浉瀵归€熺巼
                 姣斿宸﹁竟瑁佸壀1鍍忕礌锛岃€屽彸杈瑰搴︽槸宸﹁竟鐨?鍊嶏紝鍒欏彸杈瑰簲璇ヨ鍓?鍍忕礌鎵嶈拷寰椾笂宸﹁竟
                 鑱旂珛鏂圭▼锛?                 1锛歞l+dr=dx;
                 2锛歞l/dr=a;
                 鍒欑敱2鍙緱
                 dl=a*dr;
                 浠ｅ叆1
                 dr*(a+1)=dx;
                 鍙互绠楀緱鍑篸r
                 鍐嶆浠ｅ叆1鍙煡
                 dl=dx-dr
                 */


                float ratiofLeftAndRight = Math.abs(clipFrom.left - clipTo.left) / Math.abs(clipFrom.right - clipTo.right);
                float ratiofTopAndBottom = Math.abs(clipFrom.top - clipTo.top) / Math.abs(clipFrom.bottom - clipTo.bottom);

                float dClipRight = dx / (ratiofLeftAndRight + 1);
                float dClipLeft = dx - dClipRight;
                float dClipBottom = dy / (ratiofTopAndBottom + 1);
                float dClipTop = dy - dClipBottom;


                mClipRect.left = clipFrom.left + dClipLeft;
                mClipRect.right = clipFrom.right - dClipRight;

                mClipRect.top = clipFrom.top + dClipTop;
                mClipRect.bottom = clipFrom.bottom - dClipBottom;

                if (!mClipRect.isEmpty()) {
                    clipBounds = mClipRect;
                }

                isAllFinish = false;

            }

            if (!isAllFinish) {
                setMatrixValue();
                postExecuteSelf();
            } else {
                isRunning = false;
                reset();
                if (onAllFinishListener != null) {
                    onAllFinishListener.onAllFinish();
                }
            }

        }

        private void setMatrixValue() {
            if (mAttacher == null) return;
            resetSuppMatrix();
            postMatrixScale(scaleX, scaleY, scaleCenterX, scaleCenterY);
            postMatrixTranslate(dx, dy);
            if (targetView != null) targetView.setAlpha(alpha);
            applyMatrix();
        }

        private void postExecuteSelf() {
            if (isRunning) post(this);
        }

        private void reset() {
            scaleCenterX = 0;
            scaleCenterY = 0;

            scaleX = 0;
            scaleY = 0;

            dx = 0;
            dy = 0;

            preTranslateX = 0;
            preTranslateY = 0;

            alpha = 0;
        }

        void stop(boolean reset) {
            removeCallbacks(this);
            scaleScroller.abortAnimation();
            translateScroller.abortAnimation();
            alphaScroller.abortAnimation();
            clipScroller.abortAnimation();

            isRunning = false;
            onAllFinishListener = null;
            if (reset) reset();
        }

        void start(@Nullable OnAllFinishListener onAllFinishListener) {
            if (isRunning) stop(false);
            this.onAllFinishListener = onAllFinishListener;
            isRunning = true;
            postExecuteSelf();
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        bitmapTransform.stop(true);
        super.onDetachedFromWindow();
    }


    //------------------------------------------tools block-----------------------------------------------
    private int getViewWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getViewHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int getDrawableIntrinsicWidth() {
        Drawable d = getDrawable();
        if (d == null) return 0;
        return d.getIntrinsicWidth();
    }

    private int getDrawableIntrinsicHeight() {
        Drawable d = getDrawable();
        if (d == null) return 0;
        return d.getIntrinsicHeight();
    }
}

