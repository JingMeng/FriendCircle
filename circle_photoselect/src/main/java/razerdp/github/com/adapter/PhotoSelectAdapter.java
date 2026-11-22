package razerdp.github.com.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import razerdp.github.com.ui.imageloader.ImageLoadMnanger;
import razerdp.github.com.lib.utils.ToolUtil;
import razerdp.github.com.ui.util.UIHelper;
import razerdp.github.com.ui.util.ViewUtil;
import razerdp.github.com.ui.base.adapter.BaseRecyclerViewAdapter;
import razerdp.github.com.ui.base.adapter.BaseRecyclerViewHolder;
import razerdp.github.com.ui.widget.imageview.CheckImageView;
import razerdp.github.com.lib.common.entity.ImageInfo;
import com.razerdp.github.com.common.entity.photo.PhotoBrowserInfo;
import com.razerdp.github.com.common.router.RouterList;
import razerdp.github.com.photoselect.PhotoMultiBrowserActivity;
import razerdp.github.com.photoselect.R;

import static razerdp.github.com.photoselect.fragment.PhotoGridFragement.MAX_COUNT;

/**
 * Created by 澶х伅娉?on 2017/3/24.
 * <p>
 * 鍥剧墖閫夋嫨adapter
 */

public class PhotoSelectAdapter extends BaseRecyclerViewAdapter<ImageInfo> {
    private static final String TAG = "PhotoSelectAdapter";

    private final int itemDecoration;
    private List<ImageInfo> selectedRecordLists;
    private boolean selectable = true;
    private String curAlbumName;
    private int maxCount = MAX_COUNT;

    public PhotoSelectAdapter(@NonNull Context context, int itemDecoration, @NonNull List<ImageInfo> datas) {
        this(context, itemDecoration, datas, MAX_COUNT);
    }

    public PhotoSelectAdapter(@NonNull Context context, int itemDecoration, @NonNull List<ImageInfo> datas, int maxCount) {
        super(context, datas);
        this.itemDecoration = itemDecoration;
        this.selectedRecordLists = new ArrayList<>();
        this.maxCount = maxCount;
    }

    @Override
    protected int getViewType(int position, @NonNull ImageInfo data) {
        return 0;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.item_photo_select;
    }

    @Override
    protected BaseRecyclerViewHolder getViewHolder(ViewGroup parent, View rootView, int viewType) {
        return new InnerViewHolder(rootView, viewType);
    }

    private void onSelectPhoto(ImageInfo info) {
        if (info == null) return;
        if (!checkSelectListLength()) return;
        selectedRecordLists.add(info);
        callSelectListenerChange();
    }

    /**
     * 杩欓噷涓嶅彲浠ョ敤list.remove(Object)锛屽洜涓哄綋杩涜杩囬€夋嫨涔嬪悗锛屽唴閮ㄧ殑瀹炰緥涓嶄竴瀹氭槸鍚屼竴涓璞′簡銆?     * <p>
     * {@link razerdp.github.com.photoselect.fragment.PhotoGridFragement#updateSelectList(List)}浠ュ強{@link PhotoMultiBrowserActivity#finish()}
     */
    private void onUnSelectPhoto(ImageInfo info) {
        if (info == null) return;
        if (selectedRecordLists.size() <= 0) return;
        for (int i = 0; i < selectedRecordLists.size(); i++) {
            ImageInfo selectedInfo = selectedRecordLists.get(i);
            if (selectedInfo.compareTo(info) == 0) {
                selectedRecordLists.remove(i);
                break;
            }
        }
        callSelectListenerChange();
    }

    private void callSelectListenerChange() {
        final int size = selectedRecordLists.size();
        boolean hasChangeState = selectable;
        selectable = checkSelectListLength();
        hasChangeState = hasChangeState != selectable;
        if (hasChangeState) {
            notifyDataSetChanged();
        }
        if (onSelectCountChangeLisntenr != null) {
            onSelectCountChangeLisntenr.onSelectCountChange(size);
        }
    }

    private boolean checkSelectListLength() {
        return !(selectedRecordLists.size() >= maxCount || selectedRecordLists.size() < 0);
    }

    @Override
    public void updateData(List<ImageInfo> datas) {
        clearSelectRecord();
        super.updateData(datas);
    }

    public void updateSelections(List<ImageInfo> newDatas) {
        if (newDatas != null) {
            selectedRecordLists.clear();
            selectedRecordLists.addAll(newDatas);
            selectable = selectedRecordLists.size() != maxCount;
        }
    }

    public String getCurAlbumName() {
        return curAlbumName;
    }

    public void setCurAlbumName(String curAlbumName) {
        this.curAlbumName = curAlbumName;
    }

    public List<ImageInfo> getSelectedRecordLists() {
        return new ArrayList<>(selectedRecordLists);
    }

    private void clearSelectRecord() {
        selectedRecordLists.clear();
        selectable = true;
    }

    private class InnerViewHolder extends BaseRecyclerViewHolder<ImageInfo> {

        private CheckImageView checkImageView;
        private InnerClickEventClass clickEventClass;
        private View maskView;

        InnerViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            checkImageView = (CheckImageView) findViewById(R.id.iv_photo_select);
            maskView = findViewById(R.id.iv_photo_mask);
            setCheckImageViewLayoutParams();
            clickEventClass = new InnerClickEventClass();
        }

        /**
         * 鏍规嵁灞忓箷瀹藉害璁剧疆iv鐨勫ぇ灏?         */
        private void setCheckImageViewLayoutParams() {
            int itemDercorationSum = 5 * itemDecoration;
            int screenWidth = UIHelper.getScreenWidthPix(getContext());
            final int size = (screenWidth - itemDercorationSum) >> 2;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.width = params.height = size;
            checkImageView.setLayoutParams(params);

            FrameLayout.LayoutParams maskParams = new FrameLayout.LayoutParams(size, size);
            maskParams.width = maskParams.height = size;
            maskView.setLayoutParams(maskParams);
        }

        @Override
        public void onBindData(ImageInfo data, int position) {
            String url = TextUtils.isEmpty(data.thumbnailPath) ? data.imagePath : data.thumbnailPath;
            boolean isSelected = checkIsSelect(data);
            checkImageView.setCanSelect(isSelected || selectable);
            ViewUtil.setViewsVisible((isSelected || selectable) ? View.GONE : View.VISIBLE, maskView);
            checkImageView.setSelected(isSelected);
            ImageLoadMnanger.INSTANCE.loadImage(checkImageView, url);
            setupSelectChangeListener(data);
            clickEventClass.setCurPos(position);
            checkImageView.setOnClickListener(clickEventClass);

        }

        private void setupSelectChangeListener(ImageInfo data) {
            CheckImageView.OnSelectedChangeListener listener = checkImageView.getOnSelectedChangeListener();
            InnerSelectChangeClass innerSelectChangeClass;
            if (!(listener instanceof InnerSelectChangeClass)) {
                innerSelectChangeClass = new InnerSelectChangeClass();
            } else {
                innerSelectChangeClass = (InnerSelectChangeClass) listener;
            }
            checkImageView.setOnSelectedChangeListener(innerSelectChangeClass);
            innerSelectChangeClass.setData(data);
        }

        private boolean checkIsSelect(ImageInfo imageInfo) {
            if (imageInfo == null) return false;
            if (ToolUtil.isListEmpty(selectedRecordLists)) return false;
            for (ImageInfo localSelectedPhoto : selectedRecordLists) {
                if (localSelectedPhoto.compareTo(imageInfo) == 0) {
                    return true;
                }
            }
            return false;
        }

        class InnerSelectChangeClass implements CheckImageView.OnSelectedChangeListener {

            private ImageInfo data;

            public InnerSelectChangeClass() {
            }

            public ImageInfo getData() {
                return data;
            }

            public void setData(ImageInfo data) {
                this.data = data;
            }

            @Override
            public void onSelectChange(boolean isSelect) {
                if (isSelect) {
                    onSelectPhoto(data);
                } else {
                    onUnSelectPhoto(data);
                }
            }
        }

        class InnerClickEventClass implements View.OnClickListener {

            private int curPos;

            @Override
            public void onClick(View v) {
                PhotoBrowserInfo info = PhotoBrowserInfo.create(curPos, curAlbumName, selectedRecordLists);
                ARouter.getInstance()
                       .build(RouterList.PhotoMultiBrowserActivity.path)
                       .withParcelable(RouterList.PhotoMultiBrowserActivity.key_browserinfo, info)
                       .withInt(RouterList.PhotoMultiBrowserActivity.key_maxSelectCount, maxCount)
                       .navigation((Activity) getContext(), RouterList.PhotoMultiBrowserActivity.requestCode);
            }

            public void setCurPos(int curPos) {
                this.curPos = curPos;
            }

        }
    }

    private OnSelectCountChangeLisntenr onSelectCountChangeLisntenr;

    public OnSelectCountChangeLisntenr getOnSelectCountChangeLisntenr() {
        return onSelectCountChangeLisntenr;
    }

    public void setOnSelectCountChangeLisntenr(OnSelectCountChangeLisntenr onSelectCountChangeLisntenr) {
        this.onSelectCountChangeLisntenr = onSelectCountChangeLisntenr;
    }

    public interface OnSelectCountChangeLisntenr {
        void onSelectCountChange(int count);
    }
}

