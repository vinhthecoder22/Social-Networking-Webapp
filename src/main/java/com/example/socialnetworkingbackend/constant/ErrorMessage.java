package com.example.socialnetworkingbackend.constant;

public class ErrorMessage {

    public static final String ERR_EXCEPTION_GENERAL = "exception.general";
    public static final String UNAUTHORIZED = "exception.unauthorized";
    public static final String FORBIDDEN = "exception.forbidden";
    public static final String FORBIDDEN_UPDATE_DELETE = "exception.forbidden.update-delete";

    //error validation dto
    public static final String INVALID_SOME_THING_FIELD = "invalid.general";
    public static final String INVALID_FORMAT_SOME_THING_FIELD = "invalid.general.format";
    public static final String INVALID_SOME_THING_FIELD_IS_REQUIRED = "invalid.general.required";
    public static final String NOT_BLANK_FIELD = "invalid.general.not-blank";
    public static final String INVALID_FORMAT_PASSWORD = "invalid.password-format";
    public static final String INVALID_DATE = "invalid.date-format";
    public static final String INVALID_DATE_FEATURE = "invalid.date-future";
    public static final String INVALID_DATETIME = "invalid.datetime-format";
    public static final String INVALID_PASSWORD = "invalid.password";
    public static final String INVALID_EMAIL = "invalid.email";

    public static class Auth {
        public static final String ERR_INCORRECT_USERNAME = "exception.auth.incorrect.username";
        public static final String ERR_INCORRECT_PASSWORD = "exception.auth.incorrect.password";
        public static final String ERR_ACCOUNT_NOT_ENABLED = "exception.auth.account.not.enabled";
        public static final String ERR_ACCOUNT_LOCKED = "exception.auth.account.locked";
        public static final String INVALID_REFRESH_TOKEN = "exception.auth.invalid.refresh.token";
        public static final String INVALID_ACCESS_TOKEN = "exception.auth.invalid.access.token";
        public static final String INVALID_JWT_SIGNATURE = "exception.auth.invalid.jwt.signature";
        public static final String EXPIRED_REFRESH_TOKEN = "exception.auth.expired.refresh.token";
        public static final String EXPIRED_ACCESS_TOKEN = "exception.auth.expired.access.token";
        public static final String ERR_ALREADY_LOGGED_IN = "exception.auth.already.logged";
        public static final String ERR_ALREADY_EXISTS_USERNAME = "exception.auth.already.exists.username";
        public static final String ERR_ALREADY_EXISTS_EMAIL = "exception.auth.already.exists.email";
        public static final String ERR_REFRESH_TOKEN = "exception.auth.refresh.token.error";
        public static final String ERR_ACCESS_TOKEN = "exception.auth.access.token.error";
    }

    public static class User {
        public static final String ERR_NOT_FOUND_USERNAME = "exception.user.not.found.username";
        public static final String ERR_NOT_FOUND_ID = "exception.user.not.found.id";
        public static final String ERR_NOT_FOUND_EMAIL = "exception.user.not.found.email";
    }

    public static class Media {
        public static final String ERR_NOT_FOUND_MEDIA = "exception.media.not.found.publicId";
        public static final String ERR_INVALID_MEDIA_TYPE = "exception.media.upload.invalid.format";
        public static final String ERR_MAX_SIZE_UPLOAD_VIDEO = "exception.media.upload.maxsize_video";
        public static final String ERR_MAX_SIZE_UPLOAD_AUDIO = "exception.media.upload.maxsize_audio";
        public static final String ERR_MAX_SIZE_UPLOAD_IMAGE = "exception.media.upload.maxsize_image";
        public static final String ERR_MAX_SIZE_REQUEST_MEDIA = "exception.media.upload.maxsize_media";
        public static final String ERR_VIDEO_NOT_MULTIPLE_NOT_ALLOWED = "exception.video.multipleNotAllowed";
        public static final String ERR_AUDIO_UPLOAD_FORMAT = "exception.audio.upload.format";

    }

    public static class Post {
        public static final String ERR_NOT_FOUND_ID = "exception.post.not.found.id";
        public static final String ERR_NOT_FOUND_ORIGINAL_POST = "exception.post.not.found.original";
        public static final String ERR_FILES_NULL = "exception.files.null";
        public static final String ERR_FILES_INVALID_FORMAT = "exception.files.invalid.format";
        public static final String ERR_FILES_NSFW = "exception.files.nsfw";
    }

    public static class Reaction {
        public static final String ERR_NOT_FOUND_ID = "exception.reaction.not_found";
        public static final String ERR_DUPLICATE = "exception.reaction.duplicate";
        public static final String ERR_NOT_FOUND = "exception.reaction.not.found";
    }

    public static class Follow {
        public static final String ERR_NOT_FOUND_ID = "exception.follow.not_found";
        public static final String ERR_DUPLICATE = "exception.follow.duplicate";
        public static final String ERR_FOLLOW_YOURSELF = "exception.follow.yourself";
        public static final String ERR_NOT_FOUND_FOLLOWING = "exception.follow.not.found.following";
        public static final String ERR_REMOVE_FOLLOWER = "exception.follower.not.found.follower";
        public static final String ERR_UNFOLLOW_USER = "exception.follow.not.found.unfollow";
    }

    public static class OtpForgotPassword {
        public static final String ERR_NOT_FOUND = "exception.otp.not.found";
        public static final String ERR_SEND_FAILED = "exception.otp.send.failed";
        public static final String ERR_OTP_EXPIRED = "exception.otp.expired";
        public static final String ERR_VERIFY_FAILED = "exception.otp.verify";
        public static final String ERR_NOT_VERIFIED = "exception.otp.not.verified";
        public static final String ERR_CHANGE_PASSWORD_EXPIRED = "exception.changed.password.confirm.expired";
        public static final String ERR_PASSWORD_NOT_MATCHED = "exception.changed.password.not.match";
        public static final String ERR_DELAY_GET_OTP = "exception.otp.delay.get";
        public static final String ERR_OLD_PASSWORD_INCORRECT = "exception.changed.password.old.password.incorrect";
    }

    public static class Comment {
        public static final String ERR_NOT_FOUND_ID = "exception.comment.not.found.id";
        public static final String ERR_NOT_FOUND_COMMENT_IN_POST = "exception.comment.not.found.in.post";
        public static final String ERR_PARENT_COMMENT_NOT_FOUND = "exception.comment.parent.comment.not.found";
        public static final String ERR_NOT_HAVE_PERMISSION = "exception.comment.not.have.permission";
    }

    public static class Role{
        public static final String ERR_NOT_FOUND = "exception.role.not.found";
    }

    public static class Notification {
        public static final String ERR_NOT_FOUND_ID = "exception.notification.not.found.id";
        public static final String ERR_DUPLICATE = "exception.notification.duplicate";
        public static final String ERR_NOT_FOUND = "exception.notification.not.found";
        public static final String ERR_INVALID_FORMAT = "exception.notification.invalid.format";
    }
}

