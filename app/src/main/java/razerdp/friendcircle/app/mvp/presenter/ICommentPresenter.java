package razerdp.friendcircle.app.mvp.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import razerdp.friendcircle.app.mvp.callback.OnCommentChangeCallback;

/**
 * Created by 澶х伅娉?on 2016/12/6.
 */

public interface ICommentPresenter {


    /**
     * 娣诲姞璇勮
     */
    void addComment(@NonNull String momentsId,
                    @NonNull String authorId,
                    @Nullable String replyUserId,
                    @NonNull String content,
                    @NonNull OnCommentChangeCallback onCommentChangeCallback);

    void deleteComment(@NonNull String commentid, @NonNull final OnCommentChangeCallback onCommentChangeCallback);
}

