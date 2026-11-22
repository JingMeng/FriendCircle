package razerdp.friendcircle.config;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 澶х伅娉?on 2016/10/27.
 * <p>
 * 甯搁噺瀹氫箟
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Define {

    String BMOB_APPID = "7fc6618e5572b09c6055ef4d53d0017a";


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LikeState.LIKE, LikeState.UNLIKE})
    @interface LikeState {
        int LIKE = 0x10;
        int UNLIKE = 0x11;
    }
}

