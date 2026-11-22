package razerdp.github.com.ui.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import razerdp.github.com.lib.base.BaseActivity;
import razerdp.github.com.lib.base.BaseFragment;
import razerdp.github.com.lib.helper.AppFileHelper;
import razerdp.github.com.lib.helper.PermissionHelper;
import razerdp.github.com.lib.utils.ImageSelectUtil;

/**
 * Created by 澶х伅娉?on 2017/4/10.
 * <p>
 * 鍥剧墖鎷嶇収鍜岄€夋嫨
 * <p>
 * 鍔″繀娉ㄦ剰fragment鍜宎ctivity锛屽洜涓篺ragment璋冪敤getActivity涓嶄細璋冪敤鍒皁nActivityResult
 */

public class PhotoHelper {
    private static String curTempPhotoPath;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({REQUEST_FROM_CAMERA, REQUEST_FROM_ALBUM, REQUEST_FROM_CROP})
    public @interface PhotoType {
    }

    public static final int REQUEST_FROM_CAMERA = 0x10;
    public static final int REQUEST_FROM_ALBUM = 0x11;
    public static final int REQUEST_FROM_CROP = 0x12;
    private static final int REQUEST_FROM_CAMERA_WITHO_OUT_CROP = 0x13;//涓嶆埅鍥剧殑request code
    private static final int REQUEST_FROM_ALBUM_WITHO_OUT_CROP = 0x14;//涓嶆埅鍥剧殑request code


    private static String cropPath;


    public static void fromCamera(Activity activity) {
        fromCamera(activity, true);
    }

    public static void fromCamera(Activity activity, boolean needCrop) {
        checkAndRequestCameraPermission(activity, needCrop);
    }

    public static void fromAlbum(Activity activity) {
        fromAlbum(activity, true);
    }

    public static void fromAlbum(Activity activity, boolean needCrop) {
        fromAlbumInternal(activity, needCrop);
    }

    public static String getCropImagePath() {
        return AppFileHelper.getAppTempPath() + AppFileHelper.createCropImageName();
    }

    public static void toCrop(Activity activity, Uri imageUri) {
        toCropInternal(activity, imageUri);
    }

    public static void fromCamera(Fragment fragment) {
        fromCamera(fragment, true);
    }

    public static void fromCamera(Fragment fragment, boolean needCrop) {
        checkAndRequestCameraPermission(fragment, needCrop);
    }

    public static void fromAlbum(Fragment fragment) {
        fromAlbum(fragment, true);
    }

    public static void fromAlbum(Fragment fragment, boolean needCrop) {
        fromAlbumInternal(fragment, needCrop);
    }

    public static void toCrop(Fragment fragment, Uri imageUri) {
        toCropInternal(fragment, imageUri);
    }


    private static void fromAlbumInternal(Object obj, boolean needCrop) {
        if (obj == null) return;
        cropPath = getCropImagePath();
        // 璺宠浆鍒扮郴缁熺浉鍐?        Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        albumIntent.setType("image/*");
        if (obj instanceof Fragment) {
            ((Fragment) obj).startActivityForResult(albumIntent, needCrop ? REQUEST_FROM_ALBUM : REQUEST_FROM_ALBUM_WITHO_OUT_CROP);
        } else if (obj instanceof Activity) {
            ((Activity) obj).startActivityForResult(albumIntent, needCrop ? REQUEST_FROM_ALBUM : REQUEST_FROM_ALBUM_WITHO_OUT_CROP);
        }
    }

    private static void checkAndRequestCameraPermission(final Object object, final boolean needCrop) {
        PermissionHelper permissionHelper = null;
        PermissionHelper.OnPermissionGrantListener onPermissionGrantListener = new PermissionHelper.OnPermissionGrantListener() {
            @Override
            public void onPermissionGranted(@PermissionHelper.PermissionResultCode int requestCode) {
                fromCameraInternal(object, needCrop);
            }

            @Override
            public void onPermissionsDenied(@PermissionHelper.PermissionResultCode int requestCode) {

            }
        };
        if (object instanceof Activity) {
            permissionHelper = object instanceof BaseActivity ? ((BaseActivity) object).getPermissionHelper() : new PermissionHelper((Activity) object);
        } else if (object instanceof Fragment) {
            permissionHelper = object instanceof BaseFragment ? ((BaseFragment) object).getPermissionHelper() : new PermissionHelper(((Fragment) object).getActivity());
        }
        if (permissionHelper != null) {
            permissionHelper.requestPermission(PermissionHelper.CODE_CAMERA, onPermissionGrantListener);
        }

    }

    private static void fromCameraInternal(Object object, boolean needCrop) {
        if (object == null) return;
        cropPath = getCropImagePath();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        curTempPhotoPath = AppFileHelper.getCameraPath() + AppFileHelper.createShareImageName();
        Uri uri = CompatHelper.getUri(new File(curTempPhotoPath));
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        if (CompatHelper.isOverM()) {
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        if (object instanceof Activity) {
            ((Activity) object).startActivityForResult(cameraIntent, needCrop ? REQUEST_FROM_CAMERA : REQUEST_FROM_CAMERA_WITHO_OUT_CROP);
        } else if (object instanceof Fragment) {
            ((Fragment) object).startActivityForResult(cameraIntent, needCrop ? REQUEST_FROM_CAMERA : REQUEST_FROM_CAMERA_WITHO_OUT_CROP);
        }
    }

    private static void toCropInternal(Object obj, Uri imageUri) {
        if (obj == null || imageUri == null) return;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");// mUri鏄凡缁忛€夋嫨鐨勫浘鐗嘦ri
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 瑁佸壀妗嗘瘮渚?        intent.putExtra("aspectY", 1);
//        intent.putExtra("outputX", imageCover.getWidth());// 杈撳嚭鍥剧墖澶у皬
//        intent.putExtra("outputY", imageCover.getHeight());

        //姝ゅ浣跨敤uri.fromFile锛岃€屼笉瑕佺敤fileProvider锛屽洜涓洪潪export锛岃€屽悗闈㈣嚜鍔ㄦ巿鏉冿紝鎵€浠ヤ笉浼氬嚭鐜板畨鍏ㄥ紓甯?        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cropPath)));
        intent.putExtra("return-data", false);
        if (CompatHelper.isOverM()) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        if (obj instanceof Activity) {
            ((Activity) obj).startActivityForResult(intent, REQUEST_FROM_CROP);
        } else if (obj instanceof Fragment) {
            ((Fragment) obj).startActivityForResult(intent, REQUEST_FROM_CROP);
        }
    }


    public static void handleActivityResult(Activity act, int requestCode, int resultCode, Intent data, @Nullable PhotoCallback callback) {
        boolean needCrop = true;
        switch (requestCode) {
            case REQUEST_FROM_CAMERA_WITHO_OUT_CROP:
                needCrop = false;
            case REQUEST_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    File file = new File(curTempPhotoPath);
                    if (file.exists()) {
                        if (needCrop) {
                            toCrop(act, CompatHelper.getUri(file));
                        } else {
                            callFinish(callback, curTempPhotoPath);
                        }
                    } else {
                        callError(callback, "can not get the taked photo");
                    }
                }
                break;
            case REQUEST_FROM_ALBUM_WITHO_OUT_CROP:
                needCrop = false;
            case REQUEST_FROM_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        final Uri photoUri = data.getData();
                        if (photoUri == null) {
                            callError(callback, "鍔犺浇鍥剧墖澶辫触");
                            return;
                        }
                        try {
                            if (CompatHelper.isOverN()) {
                                if (needCrop) {
                                    //7.0鐢ㄧ殑鏄痗ontent uri
                                    toCrop(act, photoUri);
                                } else {
                                    callFinish(callback, ImageSelectUtil.getPath(act, photoUri));
                                }
                            } else {
                                //闈?.0鐢ㄧ殑file uri
                                String path = ImageSelectUtil.getPath(act, photoUri);
                                if (needCrop) {
                                    toCrop(act, CompatHelper.getUri(new File(path)));
                                } else {
                                    callFinish(callback, path);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            callError(callback, "鍥剧墖涓嶅瓨鍦?);
                        }
                    } else {
                        callError(callback, "鍥剧墖涓嶅瓨鍦?);
                    }
                }
                break;
            case REQUEST_FROM_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        callError(callback, "鍥剧墖涓嶅瓨鍦?);
                        return;
                    }
                    callFinish(callback, cropPath);
                } else {
                    callError(callback, "鍙栨秷");
                }
                break;

        }
    }

    public static void handleActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data, @Nullable PhotoCallback callback) {
        boolean needCrop = true;
        switch (requestCode) {
            case REQUEST_FROM_CAMERA_WITHO_OUT_CROP:
                needCrop = false;
            case REQUEST_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    File file = new File(curTempPhotoPath);
                    if (file.exists()) {
                        if (needCrop) {
                            toCrop(fragment, CompatHelper.getUri(file));
                        } else {
                            callFinish(callback, curTempPhotoPath);
                        }
                    } else {
                        callError(callback, "can not get the taked photo");
                    }
                }
                break;
            case REQUEST_FROM_ALBUM_WITHO_OUT_CROP:
                needCrop = false;
            case REQUEST_FROM_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        final Uri photoUri = data.getData();
                        if (photoUri == null) {
                            callError(callback, "鍔犺浇鍥剧墖澶辫触");
                            return;
                        }
                        try {
                            if (CompatHelper.isOverN()) {
                                if (needCrop) {
                                    //7.0鐢ㄧ殑鏄痗ontent uri
                                    toCrop(fragment, photoUri);
                                } else {
                                    callFinish(callback, ImageSelectUtil.getPath(fragment.getContext(), photoUri));
                                }
                            } else {
                                //闈?.0鐢ㄧ殑file uri
                                String path = ImageSelectUtil.getPath(fragment.getContext(), photoUri);
                                if (needCrop) {
                                    toCrop(fragment, CompatHelper.getUri(new File(path)));
                                } else {
                                    callFinish(callback, path);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            callError(callback, "鍥剧墖涓嶅瓨鍦?);
                        }
                    } else {
                        callError(callback, "鍥剧墖涓嶅瓨鍦?);
                    }
                }
                break;
            case REQUEST_FROM_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        callError(callback, "鍥剧墖涓嶅瓨鍦?);
                        return;
                    }
                    callFinish(callback, cropPath);
                } else {
                    callError(callback, "鍙栨秷");
                }
                break;

        }
    }

    private static void callFinish(PhotoCallback callback, String filePath) {
        if (callback != null) {
            callback.onFinish(filePath);
        }
    }

    private static void callError(PhotoCallback callback, String errorMsg) {
        if (callback != null) {
            callback.onError(errorMsg);
        }
    }

    public interface PhotoCallback {
        void onFinish(String filePath);

        void onError(String msg);
    }

}

