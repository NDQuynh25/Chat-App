package com.example.chatappfirebase.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chat_app_pref";
    public static final String KEY_IS_SIGNED_IN = "is_signed_in";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcm_token";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_ID = "sender_id";
    public static final String KEY_RECEIVER_ID = "receiver_id";
    public static final String KEY_MESSAGE = "message";

    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_CONVERSATION_ID = "conversation_id";
    public static final String KEY_SENDER_NAME = "sender_name";
    public static final String KEY_RECEIVER_NAME = "receiver_name";
    public static final String KEY_SENDER_IMAGE = "sender_image";
    public static final String KEY_RECEIVER_IMAGE = "receiver_image";
    public static final String KEY_LAST_MESSAGE = "last_message";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    Constants.REMOTE_MSG_AUTHORIZATION,
                    "key=AAAA_OLWZS8:APA91bH50ASKjt4xafbH-JxXcM7f3r2jE14wqSv8_WKM6PpJwfSiRksDaoP4PhJfDihj1ugNr1YUbZkjdy_WHFGVOG2lUHFGGrU7V_YFUFZEijgxjvGptU3_Zfyqh_owCXhatcqMOZDZ"
            );
            remoteMsgHeaders.put(
                    Constants.REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
