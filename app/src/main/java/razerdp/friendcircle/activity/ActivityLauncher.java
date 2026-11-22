package razerdp.friendcircle.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.razerdp.github.com.common.entity.PhotoBrowseInfo;
import com.razerdp.github.com.common.entity.other.ServiceInfo;
import com.razerdp.github.com.common.router.RouterList;

import java.util.ArrayList;
import java.util.List;

import razerdp.friendcircle.activity.gallery.PhotoBrowseActivity;
import razerdp.friendcircle.activity.other.ServiceInfoActivity;
import razerdp.github.com.lib.common.entity.ImageInfo;
import razerdp.github.com.photoselect.PhotoSelectActivity;
import razerdp.github.com.publish.PublishActivity;
import razerdp.github.com.ui.util.SwitchActivityTransitionUtil;

/**
 * Created by 澶х伅娉?on 2017/3/1.
 * <p>
 * activity鍙戝皠鍣▇
 */

public class ActivityLauncher {

    /**
     * 鍙戝皠鍒板彂甯冩湅鍙嬪湀椤甸潰
     *
     * @param act
     * @param mode
     * @param requestCode
     */
    public static void startToPublishActivityWithResult(Activity act, @RouterList.PublishActivity int mode, @Nullable List<ImageInfo> selectedPhotos, int requestCode) {
        Intent intent = new Intent(act, PublishActivity.class);
        intent.putExtra(RouterList.PublishActivity.key_mode, mode);
        if (selectedPhotos != null) {
            intent.putParcelableArrayListExtra(RouterList.PublishActivity.key_photoList, (ArrayList<? extends Parcelable>) selectedPhotos);
        }
        act.startActivityForResult(intent, requestCode);
        SwitchActivityTransitionUtil.transitionVerticalIn(act);
    }


    public static void startToPhotoBrosweActivity(Activity act, @NonNull PhotoBrowseInfo info) {
        if (info == null) return;
        PhotoBrowseActivity.startToPhotoBrowseActivity(act, info);
    }

    /**
     * 鍙戝皠鍒伴€夋嫨鍥剧墖椤甸潰
     *
     * @param act
     */
    public static void startToPhotoSelectActivity(Activity act, int requestCode) {
        Intent intent = new Intent(act, PhotoSelectActivity.class);
        act.startActivityForResult(intent, requestCode);
        SwitchActivityTransitionUtil.transitionVerticalIn(act);
    }

    /**
     * 鍙戝皠鍒版湇鍔″櫒娑堟伅椤甸潰
     *
     * @param act
     */
    public static void startToServiceInfoActivity(Activity act, ServiceInfo info) {
        Intent intent = new Intent(act, ServiceInfoActivity.class);
        intent.putExtra("info", info);
        act.startActivity(intent);
        SwitchActivityTransitionUtil.transitionVerticalIn(act);
    }

}

