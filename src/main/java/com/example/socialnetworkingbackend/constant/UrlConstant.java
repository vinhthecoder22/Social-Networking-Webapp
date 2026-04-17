package com.example.socialnetworkingbackend.constant;

public class UrlConstant {

    public static class Auth {
        private static final String PRE_FIX = "/auth";
        public static final String REGISTER = PRE_FIX + "/register";
        public static final String LOGIN = PRE_FIX + "/login";
        public static final String LOGOUT = PRE_FIX + "/logout";
        public static final String REFRESH_TOKEN = PRE_FIX + "/refresh-token";
        public static final String ME = PRE_FIX + "/me";
        public static final String UPLOAD_PROFILE_PICTURE = PRE_FIX + "/upload-profile-picture";

        public static final String FORGOT_PASSWORD = PRE_FIX + "/forgot-password";
        public static final String SEND_OTP = FORGOT_PASSWORD + "/send-otp-code";
        public static final String VERIFY_OTP = FORGOT_PASSWORD + "/verify-otp-code";
        public static final String CHANGE_PASSWORD = FORGOT_PASSWORD + "/confirm-new-password";

        private Auth() {
        }
    }

    public static class User {
        public static final String PRE_FIX = "/users";
        public static final String CREATE_USER = PRE_FIX;
        public static final String GET_USER = PRE_FIX + "/{userId}";
        public static final String GET_CURRENT_USER = PRE_FIX + "/current";
        public static final String GET_ALL_USERS = PRE_FIX;
        public static final String UPDATE_USER = PRE_FIX + "/{id}";
        public static final String DELETE_USER = PRE_FIX + "/{id}";
        public static final String CHANGE_PASSWORD = PRE_FIX + "/change-password";

        private User() {
        }
    }

    public static class Media {
        private static final String PRE_FIX = "/media";
        public static final String GET_MEDIA_BY_RESOURCE_TYPE = PRE_FIX;
        public static final String GET_MEDIA_BY_PUBLIC_ID = PRE_FIX + "/{publicId}";
        public static final String GET_AUDIO_BY_TITLE = PRE_FIX + "/audio";
        public static final String UPLOAD_MEDIA_VIDEO = PRE_FIX + "/upload/video";
        public static final String UPLOAD_MEDIA_IMAGE = PRE_FIX + "/upload/image";
        public static final String UPLOAD_MEDIA_AUDIO = PRE_FIX + "/upload/audio";
        public static final String UPLOAD_MULTI_MEDIA_IMAGE = PRE_FIX + "/upload/multi/image";
        public static final String DELETE_MEDIA = PRE_FIX + "/delete";
        public static final String UPLOAD_AVATAR = PRE_FIX + "/upload/avatar";
    }

    public static class Post {
        public static final String PRE_FIX = "/posts";
        public static final String CREATE_NEW_POST = PRE_FIX;
        public static final String GET_ALL_POST_BY_TITLE = PRE_FIX + "/search";
        public static final String GET_POST = PRE_FIX + "/{id}";
        public static final String GET_TRENDING_POST = PRE_FIX + "/trending";
        public static final String UPDATE_POST = PRE_FIX + "/{id}";
        public static final String DELETE_POST = PRE_FIX + "/{id}";
        public static final String REACTION_FOR_POST = PRE_FIX + "/{postId}/reactions";
        public static final String CANCEL_REACTION_OF_POST = PRE_FIX + "/{postId}/reactions";
        public static final String GET_REACTIONS = PRE_FIX + "/{postId}/reactions";
        public static final String GET_NEWSFEED = PRE_FIX + "/newsfeed";

        private Post() {
        }
    }

    public static class Reaction {
        private Reaction() {
        }
    }

    public static class Follow {
        public static final String PRE_FIX = "/follows";
        public static final String EXECUTING_FOLLOW = PRE_FIX + "/following";
        public static final String UNFOLLOW = PRE_FIX + "/remove/following";
        public static final String REMOVE_FOLLOWER = PRE_FIX + "/remove/follower";
        public static final String GET_FOLLOWINGS = PRE_FIX + "/me/followings";
        public static final String GET_FOLLOWERS = PRE_FIX + "/me/followers";

        private Follow() {
        }
    }

    public static class Share {
        public static final String PRE_FIX = "/posts/{postId}/share";
        public static final String SHARE_POST = PRE_FIX;

        private Share() {
        }
    }

    public static class Comment {
        public static final String PRE_FIX = "/posts/{postId}/comments";
        public static final String ADD_COMMENT = PRE_FIX;
        public static final String REPLY_COMMENT = PRE_FIX + "/reply";
        public static final String GET_COMMENTS = PRE_FIX;
        public static final String GET_REPLIES = PRE_FIX + "/{commentId}/replies";
        public static final String GET_COMMENT_WITH_REPLIES = PRE_FIX + "/{commentId}/with-replies";
        public static final String UPDATE_COMMENT = PRE_FIX + "/{commentId}";
        public static final String DELETE_COMMENT = PRE_FIX + "/{commentId}";

        private Comment() {
        }
    }

    public static class UserSetting {
        public static final String PRE_FIX = "/user-settings";
        public static final String UPDATE_SETTING = PRE_FIX;

        private UserSetting() {
        }
    }

    public static class Role {
        public static final String PRE_FIX = "/roles";
        public static final String CREATE_ROLE = PRE_FIX;
        public static final String GET_ROLES = PRE_FIX;
        public static final String GET_ROLE_BY_ID = PRE_FIX + "/{id}";
        public static final String UPDATE_ROLE = PRE_FIX + "/{id}";
        public static final String DELETE_ROLE = PRE_FIX + "/{id}";

        private Role() {
        }
    }

}

