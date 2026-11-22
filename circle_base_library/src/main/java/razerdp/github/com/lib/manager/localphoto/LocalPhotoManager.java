package razerdp.github.com.lib.manager.localphoto;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.socks.library.KLog;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import razerdp.github.com.lib.api.AppContext;
import razerdp.github.com.lib.helper.AppFileHelper;
import razerdp.github.com.lib.helper.AppSetting;
import razerdp.github.com.lib.manager.ThreadPoolManager;
import razerdp.github.com.lib.thirdpart.WeakHandler;
import razerdp.github.com.lib.utils.FileUtil;
import razerdp.github.com.lib.utils.GsonUtil;
import razerdp.github.com.lib.utils.TimeUtil;
import razerdp.github.com.lib.common.entity.ImageInfo;

/**
 * Created by 澶х伅娉?on 2017/3/23.
 * <p>
 * 鏈湴鐓х墖绠＄悊绫?鍗曚緥)
 */

public enum LocalPhotoManager {
    INSTANCE;
    private static final String TAG = "LocalPhotoManager";
    public static final String LOCAL_FILE_NAME = "LocalPhotoFile";
    private static final String ALL_PHOTO_TITLE = "鎵€鏈夌収鐗?;
    private static final String QUERY_ORDER = " ASC";
    private static final boolean SCAN_EXTERNAL_SD = true;
    //1鍒嗛挓鍐呬笉鍐嶆壂鎻?    private static final long SCAN_INTERVAL = 60 * 1000;

    private WeakHandler handler = new WeakHandler();

    boolean isScaning;
    long lastScanTime;


    //璁块棶绯荤粺鏁版嵁搴撶殑澶у浘鏌ヨ鏁版嵁
    public static final String[] STORE_IMGS = {MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.ORIENTATION,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_TAKEN};
    //璁块棶绯荤粺鏁版嵁搴撶殑灏忓浘鏌ヨ鏁版嵁
    public static final String[] THUMBNAIL_STORE_IMAGE = {MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.DATA};

    private static final LinkedHashMap<String, List<ImageInfo>> sALBUM = new LinkedHashMap<>();
    private static final LinkedHashMap<String, List<ImageInfo>> sWRITE_ALBUM = new LinkedHashMap<>();

    private final ProgressRunnable progressRunnable = new ProgressRunnable();

    public synchronized void scanImg(@Nullable final OnScanListener listener) {
        ThreadPoolManager.execute(new Runnable() {
            @Override
            public void run() {
                scanImgInternal(listener);
            }
        });
    }

    private synchronized void scanImgInternal(@Nullable OnScanListener listener) {
        if (isScaning) {
            callError(listener, "scan task is running", new LPException("scan task is running"));
            return;
        }
        isScaning = true;
        callStart(listener);
        lastScanTime = AppSetting.loadLongPreferenceByKey(AppSetting.APP_LAST_SCAN_IMG_TIME, 0);

        boolean callImmediately = checkLocalSerializableFile();
        //濡傛灉鏈湴鏂囦欢宸茬粡鏈変簡锛岄偅涔堝彲浠ョ珛鍗冲洖璋冿紝鎻愰珮鐢ㄦ埛浣撻獙銆?        //鐒跺悗鍐嶅悗鍙版壂涓€娆℃洿鏂版湰鍦版枃浠惰褰?        if (callImmediately) {
            callProgress(listener, 100);
            callFinish(listener);
            reset();
            return;
        }
        long curTime = System.currentTimeMillis();
        if (curTime - lastScanTime <= SCAN_INTERVAL) {
            if (new File(AppFileHelper.getAppDataPath().concat(LOCAL_FILE_NAME)).exists()) {
                callError(listener, "1鍒嗛挓鍐呬笉搴旇鍐嶆鎵弿", new IllegalStateException("1鍒嗛挓鍐呬笉搴旇鍐嶆鎵弿"));
                reset();
                return;
            }
        }
        scan(sALBUM, listener);
    }

    private void scanAsync(final LinkedHashMap<String, List<ImageInfo>> sALBUM, @Nullable final OnScanListener listener) {
        ThreadPoolManager.execute(new Runnable() {
            @Override
            public void run() {
                scan(sALBUM, listener);
            }
        });
    }

    private void scan(LinkedHashMap<String, List<ImageInfo>> sALBUM, @Nullable OnScanListener listener) {
        if (sALBUM == null) return;
        KLog.i(TAG, "isWrite  >>  " + (sALBUM == sWRITE_ALBUM));
        Cursor cursor = AppContext.getAppContext()
                .getContentResolver()
                .query(SCAN_EXTERNAL_SD ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                        STORE_IMGS,
                        null,
                        null,
                        MediaStore.Images.ImageColumns.DATE_TAKEN.concat(QUERY_ORDER));
        if (cursor == null) {
            callError(listener, "cursor涓虹┖", null);
            reset();
            return;
        }
        sALBUM.clear();
        //鏋勯€爐humb鐨勬煡璇㈣鍙?where id = xxx)
        final String[] thumbWhereQuery = new String[1];
        List<ImageInfo> allImageInfoLists = new ArrayList<>();
        final int cursorCount = cursor.getCount();
        sALBUM.put(ALL_PHOTO_TITLE, allImageInfoLists);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            int imgId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
            String imgPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION));
            String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
            long dateTaken = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));

            File imageFile = new File(imgPath);
            if (!imageFile.exists() || imageFile.isDirectory() || imageFile.length() <= 0) continue;

            thumbWhereQuery[0] = String.valueOf(imgId);
            String thumbImgPath = getThumbPath(thumbWhereQuery);

            List<ImageInfo> imageInfoList = sALBUM.get(albumName);
            ImageInfo imageInfo = new ImageInfo(imgPath, thumbImgPath, albumName, dateTaken, orientation);
            try {
                if (imageInfo.checkValided()) {
                    allImageInfoLists.add(imageInfo.clone());
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                KLog.e(e);
            }
            if (imageInfoList == null) {
                imageInfoList = new ArrayList<>();
                sALBUM.put(albumName, imageInfoList);
            } else {
                if (imageInfo.checkValided()) {
                    imageInfoList.add(imageInfo);
                }
            }
            callProgress(listener, (int) (cursor.getPosition() * 100.0f / cursorCount));
        }
        lastScanTime = System.currentTimeMillis();
        AppSetting.saveLongPreferenceByKey(AppSetting.APP_LAST_SCAN_IMG_TIME, lastScanTime);
        cursor.close();
        callFinish(listener);
        reset();
        KLog.i(TAG, "scan瀹屾垚");
        if (sALBUM == sWRITE_ALBUM && !sALBUM.isEmpty()) {
            sALBUM.clear();
            sALBUM.putAll(sWRITE_ALBUM);
        }
        //浜嬪疄涓奿o娴佺殑閫熷害涔熸槸鏉犳潬鐨勶紝鎵€浠ヨ繖閲屽彲浠ラ噰鍙栧啓鍏ュ埌鏈湴鏂囦欢鐨勬柟娉曟潵瀛樺偍鎵弿缁撴灉
        ThreadPoolManager.execute(new WriteToLocalRunnable(sALBUM));
    }

    private void reset() {
        isScaning = false;
        progressRunnable.reset();
    }

    public void registerContentObserver(Handler handler) {
        AppContext.getAppContext()
                .getContentResolver()
                .registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, new MediaImageContentObserver(handler));
    }

    private boolean checkLocalSerializableFile() {
        if (!sALBUM.isEmpty()) return true;
        File file = new File(AppFileHelper.getAppDataPath().concat(LOCAL_FILE_NAME));
        if (file.exists()) {
            KLog.i(TAG, "褰撳墠鏃堕棿  >>>  " + TimeUtil.longToTimeStr(System.currentTimeMillis(), TimeUtil.YYYYMMDDHHMMSS));
            KLog.i(TAG, "鏂囦欢淇敼鏃堕棿  >>>  " + TimeUtil.longToTimeStr(file.lastModified(), TimeUtil.YYYYMMDDHHMMSS));
            //姣?0鍒嗛挓杩涜涓€娆￠潤榛樻壂鎻?            boolean reScanSilent = System.currentTimeMillis() - file.lastModified() > 30 * TimeUtil.MINUTE * 1000;
            //姣忓懆杩涜涓€娆″洖璋冩壂鎻?            boolean reScanFullAndCallBack = System.currentTimeMillis() - file.lastModified() > 24 * 7 * TimeUtil.HOUR * 1000;
            KLog.i(TAG, "闇€瑕侀噸鏂版壂鎻忥紵  >>>  " + reScanSilent);
            try {
                LinkedHashMap<String, List<ImageInfo>> map = GsonUtil.INSTANCE.toLinkHashMap(FileUtil.Read(file.getAbsolutePath()),
                        new TypeToken<LinkedHashMap<String, List<ImageInfo>>>() {
                        }.getType());
                if (!map.isEmpty()) {
                    sALBUM.clear();
                    sALBUM.putAll(map);
                    if (reScanFullAndCallBack) {
                        KLog.i(TAG, "鍏ㄩ噺鍥炶皟鎵弿");
                        return false;
                    }
                    if (reScanSilent) {
                        KLog.i(TAG, "闈欓粯鎵弿");
                        scanAsync(sWRITE_ALBUM, null);
                    }
                    return true;
                }
            } catch (Exception e) {
                KLog.e(e);
                return false;
            }
        }
        return false;
    }

    public boolean hasData() {
        return !sALBUM.isEmpty();
    }

    private String getThumbPath(String[] whereQuery) {
        String result = null;
        //閫氳繃澶у浘鐨刬d锛屽苟涓旀瀯閫燾ursor鐨勬煡璇㈣鍙ユ潵鑾峰彇澶у浘瀵瑰簲鐨勫皬鍥?        Cursor cursor = AppContext.getAppContext()
                .getContentResolver()
                .query(SCAN_EXTERNAL_SD ? MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI : MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI,
                        THUMBNAIL_STORE_IMAGE,
                        MediaStore.Images.Thumbnails.IMAGE_ID.concat(" = ?"),
                        whereQuery,
                        null);
        if (cursor == null) return null;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
        }
        cursor.close();
        return result;
    }

    public LinkedHashMap<String, List<ImageInfo>> getLocalImagesMap() {
        return new LinkedHashMap<>(sALBUM);
    }

    public List<ImageInfo> getLocalImages(String albumName) {
        List<ImageInfo> infos = sALBUM.get(albumName);
        if (infos != null) {
            return new ArrayList<ImageInfo>(infos);
        }
        return new ArrayList<ImageInfo>();
    }

    public String getAllPhotoTitle() {
        return ALL_PHOTO_TITLE;
    }

    public void writeToLocal() {
        KLog.i(TAG, "鍥惧簱璁板綍鍐欏叆鏈湴");
        ThreadPoolManager.execute(new WriteToLocalRunnable(sALBUM));
    }

    private void callStart(final OnScanListener listener) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onStart();
                }
            });
        }
    }

    private void callFinish(final OnScanListener listener) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFinish();
                }
            });
        }
    }

    private void callError(final OnScanListener listener, final String message, final Exception e) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onError(new LPException(message, e));
                }
            });
        }
    }

    private void callProgress(OnScanListener listener, int progress) {
        KLog.i(TAG, "progress  >>  " + progress);
        if (listener instanceof OnScanProgresslistener) {
            if (progressRunnable.getListener() == null) {
                progressRunnable.setListener((OnScanProgresslistener) listener);
            }
            progressRunnable.setProgress(progress);
            handler.post(progressRunnable);
        }
    }

    public interface OnScanListener {
        void onStart();

        void onFinish();

        void onError(LPException e);
    }

    public abstract static class OnScanProgresslistener implements OnScanListener {
        public abstract void onProgress(int progress);
    }

    private static class ProgressRunnable implements Runnable {

        int progress;
        OnScanProgresslistener listener;

        public ProgressRunnable() {
            this(null);
        }

        public ProgressRunnable(OnScanProgresslistener listener) {
            progress = 0;
            this.listener = listener;
        }

        public void setListener(OnScanProgresslistener listener) {
            this.listener = listener;
        }

        public OnScanProgresslistener getListener() {
            return listener;
        }

        private void setProgress(int progress) {
            this.progress = progress;
        }

        private void reset() {
            this.progress = 0;
            this.listener = null;
        }


        @Override
        public void run() {
            if (listener != null) {
                listener.onProgress(progress);
            }
        }
    }

    private static class WriteToLocalRunnable implements Runnable {
        LinkedHashMap<String, List<ImageInfo>> write;

        public WriteToLocalRunnable(LinkedHashMap<String, List<ImageInfo>> write) {
            if (write != null) {
                this.write = new LinkedHashMap<>(write);
            } else {
                this.write = new LinkedHashMap<>();
            }
        }

        @Override
        public void run() {
            if (!write.isEmpty()) {
                FileUtil.writeToFile(AppFileHelper.getAppDataPath().concat(LOCAL_FILE_NAME), GsonUtil.INSTANCE.toString(write).getBytes());
            }
        }
    }

    /**
     * 鏈洃鍚瓨鍦ㄤ簬鏁翠釜app鐢熷懡鏈?     */
    class MediaImageContentObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MediaImageContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.compareTo(MediaStore.Images.Media.EXTERNAL_CONTENT_URI) != 0) return;
            KLog.i(TAG, "鐩戝惉鍒扮郴缁熷浘搴撴洿鏂? >>>  " + uri.toString());

            //鏌ヨ5鍒嗛挓鍐呮洿鏂扮殑鏁版嵁
            long queryTime = System.currentTimeMillis() - (5 * 60 * 1000);
            final String[] whereQuery = {String.valueOf(queryTime)};


            Cursor cursor = AppContext.getAppContext()
                    .getContentResolver()
                    .query(uri, STORE_IMGS, MediaStore.Images.ImageColumns.DATE_TAKEN.concat(" > ?"),
                            whereQuery, MediaStore.Images.ImageColumns.DATE_TAKEN.concat(QUERY_ORDER));
            if (cursor == null) return;
            KLog.i(TAG, "鏌ヨ鍒? >>  " + cursor.getCount() + " 鏉℃暟鎹?);
            // FIXME: 2017/3/24 娌￠敊銆傘€傘€備粬鍠电殑鍙堟槸涓婇潰鐨勯噸澶嶆楠わ紝鏈夌┖鎶婂畠鎶藉彇鍑烘潵
            final String[] thumbWhereQuery = new String[1];
            List<ImageInfo> allImageInfoLists = sALBUM.get(ALL_PHOTO_TITLE);
            if (cursor.moveToLast()) {
                int imgId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
                String imgPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION));
                String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                long dateTaken = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));

                if (!new File(imgPath).exists()) return;

                thumbWhereQuery[0] = String.valueOf(imgId);
                String thumbImgPath = getThumbPath(thumbWhereQuery);

                List<ImageInfo> imageInfoList = sALBUM.get(albumName);
                ImageInfo imageInfo = new ImageInfo(imgPath, thumbImgPath, albumName, dateTaken, orientation);
                try {
                    if (allImageInfoLists != null && imageInfo.checkValided()) {
                        allImageInfoLists.add(imageInfo.clone());
                    }
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    KLog.e(e);
                }
                if (imageInfoList == null) {
                    imageInfoList = new ArrayList<>();
                    sALBUM.put(albumName, imageInfoList);
                } else {
                    if (imageInfo.checkValided()) {
                        imageInfoList.add(imageInfo);
                    }
                }

                KLog.i(TAG, "鎴愬姛鍒锋柊鍒颁竴鏉℃暟鎹?>>>  " + imageInfo.toString());
            }
            cursor.close();
        }
    }

}

