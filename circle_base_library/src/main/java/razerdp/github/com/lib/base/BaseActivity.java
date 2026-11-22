package razerdp.github.com.lib.base;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.socks.library.KLog;

import razerdp.github.com.lib.api.AppContext;
import razerdp.github.com.lib.helper.PermissionHelper;

/**
 * Created by 澶х伅娉?on 2017/3/22.
 * <p>
 * BaseActivity
 */

public abstract class BaseActivity extends AppCompatActivity {

    private PermissionHelper mPermissionHelper;

    protected boolean isAppInBackground = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KLog.i("褰撳墠鎵撳紑 :  " + this.getClass().getSimpleName());
        if (mPermissionHelper == null) {
            mPermissionHelper = new PermissionHelper(this);
        }
        ARouter.getInstance().inject(this);
        onHandleIntent(getIntent());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mPermissionHelper != null) {
            mPermissionHelper.handlePermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public PermissionHelper getPermissionHelper() {
        return mPermissionHelper;
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (AppContext.isAppBackground()) {
            isAppInBackground = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAppInBackground) {
            isAppInBackground = false;
        }
    }

    /**
     * run in {@link BaseActivity#onCreate(Bundle)} but before {@link AppCompatActivity#setContentView(int)}
     * <p>
     * <p>
     * 濡傛灉鏈塱ntent锛屽垯闇€瑕佸鐞嗚繖涓猧ntent锛堣鏂规硶鍦╫nCreate閲岄潰鎵ц锛屼絾鍦╯etContentView涔嬪墠璋冪敤锛?     *
     * @param intent
     * @return false:鍏抽棴activity
     */
    public abstract void onHandleIntent(Intent intent);

    protected <T extends View> T findView(@IdRes int id) {
        return (T) super.findViewById(id);
    }


    public Activity getActivity() {
        return BaseActivity.this;
    }

    /**
     * 闅愯棌鐘舵€佹爮
     * <p>
     * 鍦╯etContentView鍓嶈皟鐢?     */
    protected void hideStatusBar() {
        final int sdkVer = Build.VERSION.SDK_INT;
        if (sdkVer < 16) {
            //4.0鍙婁竴涓?            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPermissionHelper != null) {
            mPermissionHelper.handleDestroy();
        }
        mPermissionHelper = null;
    }

}

