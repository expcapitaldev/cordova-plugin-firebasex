package org.apache.cordova.firebase;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.media.AudioAttributes;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FirebasePlugin extends CordovaPlugin {

    protected static FirebasePlugin instance = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    protected static Context applicationContext = null;
    private static Activity cordovaActivity = null;

    protected static final String TAG = "FirebasePlugin";
    protected static final String SETTINGS_NAME = "settings";
    private static final String ANALYTICS_COLLECTION_ENABLED = "firebase_analytics_collection_enabled";

    private static boolean inBackground = true;
    private static ArrayList<Bundle> notificationStack = null;
    private static CallbackContext notificationCallbackContext;
    private static CallbackContext tokenRefreshCallbackContext;

    private static NotificationChannel defaultNotificationChannel = null;
    public static String defaultChannelId = null;
    public static String defaultChannelName = null;

    @Override
    protected void pluginInitialize() {
        instance = this;
        cordovaActivity = this.cordova.getActivity();
        applicationContext = cordovaActivity.getApplicationContext();
        final Bundle extras = cordovaActivity.getIntent().getExtras();
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    Log.d(TAG, "Starting Firebase plugin");

                    if(getMetaDataFromManifest(ANALYTICS_COLLECTION_ENABLED)){
                        setPreference(ANALYTICS_COLLECTION_ENABLED, true);
                    }

                    FirebaseApp.initializeApp(applicationContext);
                    mFirebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext);

                    if (extras != null && extras.size() > 1) {
                        if (FirebasePlugin.notificationStack == null) {
                            FirebasePlugin.notificationStack = new ArrayList<Bundle>();
                        }
                        if (extras.containsKey("google.message_id")) {
                            extras.putString("messageType", "notification");
                            extras.putString("tap", "background");
                            notificationStack.add(extras);
                            Log.d(TAG, "Notification message found on init: " + extras.toString());
                        }
                    }
                    defaultChannelId = getStringResource("default_notification_channel_id");
                    defaultChannelName = getStringResource("default_notification_channel_name");
                    createDefaultChannel();
                }catch (Exception e){
                    handleExceptionWithoutContext(e);
                }
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try{
            if (action.equals("getId")) {
                this.getInstallationId(args, callbackContext);
            } else if (action.equals("getToken")) {
                this.getToken(args, callbackContext);
            } else if (action.equals("hasPermission")) {
                this.hasPermission(callbackContext);
            }else if (action.equals("subscribe")) {
                this.subscribe(callbackContext, args.getString(0));
            } else if (action.equals("unsubscribe")) {
                this.unsubscribe(callbackContext, args.getString(0));
            } else if (action.equals("isAutoInitEnabled")) {
                isAutoInitEnabled(callbackContext);
            } else if (action.equals("setAutoInitEnabled")) {
                setAutoInitEnabled(callbackContext, args.getBoolean(0));
            } else if (action.equals("unregister")) {
                this.unregister(callbackContext);
            } else if (action.equals("onMessageReceived")) {
                this.onMessageReceived(callbackContext);
            } else if (action.equals("onTokenRefresh")) {
                this.onTokenRefresh(callbackContext);
            } else if (action.equals("logEvent")) {
                this.logEvent(callbackContext, args.getString(0), args.getJSONObject(1));
            } else if (action.equals("setScreenName")) {
                this.setScreenName(callbackContext, args.getString(0));
            } else if (action.equals("setUserId")) {
                this.setUserId(callbackContext, args.getString(0));
            } else if (action.equals("setUserProperty")) {
                this.setUserProperty(callbackContext, args.getString(0), args.getString(1));
            } else if (action.equals("setAnalyticsCollectionEnabled")) {
                this.setAnalyticsCollectionEnabled(callbackContext, args.getBoolean(0));
            } else if (action.equals("isAnalyticsCollectionEnabled")) {
                this.isAnalyticsCollectionEnabled(callbackContext);
            } else if (action.equals("clearAllNotifications")) {
                this.clearAllNotifications(callbackContext);
            } else if (action.equals("createChannel")) {
                this.createChannel(callbackContext, args.getJSONObject(0));
            } else if (action.equals("deleteChannel")) {
                this.deleteChannel(callbackContext, args.getString(0));
            } else if (action.equals("listChannels")) {
                this.listChannels(callbackContext);
            } else if (action.equals("setDefaultChannel")) {
                this.setDefaultChannel(callbackContext, args.getJSONObject(0));
            } else if (action.equals("grantPermission")
                    || action.equals("grantCriticalPermission")
                    || action.equals("hasCriticalPermission")
                    || action.equals("setBadgeNumber")
                    || action.equals("getBadgeNumber")
                    ) {
                // Stubs for other platform methods
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
            } else if (action.equals("deleteInstallationId")) {
                this.deleteInstallationId(args, callbackContext);
            } else if (action.equals("getInstallationId")) {
                this.getInstallationId(args, callbackContext);
            } else if (action.equals("getAppInstanceId")) {
                this.getAppInstanceId(callbackContext);
            } else if (action.equals("getInstallationToken")) {
                this.getInstallationToken(args, callbackContext);
            } else if (action.equals("isGoogleMobileServicesAvailable")) {
                this.isGoogleMobileServicesAvailable(args, callbackContext);
            } else{
                callbackContext.error("Invalid action: " + action);
                return false;
            }
        }catch(Exception e){
            handleExceptionWithContext(e, callbackContext);
            return false;
        }
        return true;
    }

    @Override
    public void onPause(boolean multitasking) {
        FirebasePlugin.inBackground = true;
    }

    @Override
    public void onResume(boolean multitasking) {
        FirebasePlugin.inBackground = false;
    }

    @Override
    public void onReset() {
        FirebasePlugin.notificationCallbackContext = null;
        FirebasePlugin.tokenRefreshCallbackContext = null;
    }

    @Override
    public void onDestroy() {
        instance = null;
        cordovaActivity = null;
        applicationContext = null;
        super.onDestroy();
    }

    /**
     * Get a string from resources without importing the .R package
     *
     * @param name Resource Name
     * @return Resource
     */
    private String getStringResource(String name) {
        return applicationContext.getString(
                applicationContext.getResources().getIdentifier(
                        name, "string", applicationContext.getPackageName()
                )
        );
    }

    private void onMessageReceived(final CallbackContext callbackContext) {
        FirebasePlugin.notificationCallbackContext = callbackContext;
        if (FirebasePlugin.notificationStack != null) {
            for (Bundle bundle : FirebasePlugin.notificationStack) {
                FirebasePlugin.sendMessage(bundle, applicationContext);
            }
            FirebasePlugin.notificationStack.clear();
        }
    }

    private void onTokenRefresh(final CallbackContext callbackContext) {
        FirebasePlugin.tokenRefreshCallbackContext = callbackContext;

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            try {
                                if (task.isSuccessful() || task.getException() == null) {
                                    String currentToken = task.getResult();
                                    if (currentToken != null) {
                                        FirebasePlugin.sendToken(currentToken);
                                    }
                                }else if(task.getException() != null){
                                    callbackContext.error(task.getException().getMessage());
                                }else{
                                    callbackContext.error("Task failed for unknown reason");
                                }
                            } catch (Exception e) {
                                handleExceptionWithContext(e, callbackContext);
                            }
                        };
                    });
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    public static void sendMessage(Bundle bundle, Context context) {
        if (!FirebasePlugin.hasNotificationsCallback()) {
            String packageName = context.getPackageName();
            if (FirebasePlugin.notificationStack == null) {
                FirebasePlugin.notificationStack = new ArrayList<Bundle>();
            }
            notificationStack.add(bundle);

            return;
        }

        final CallbackContext callbackContext = FirebasePlugin.notificationCallbackContext;
        if(bundle != null){
            // Pass the message bundle to the receiver manager so any registered receivers can decide to handle it
            boolean wasHandled = FirebasePluginMessageReceiverManager.sendMessage(bundle);
            if (wasHandled) {
                Log.d(TAG, "Message bundle was handled by a registered receiver");
            }else if (callbackContext != null) {
                JSONObject json = new JSONObject();
                Set<String> keys = bundle.keySet();
                for (String key : keys) {
                    try {
                        json.put(key, bundle.get(key));
                    } catch (JSONException e) {
                        handleExceptionWithContext(e, callbackContext);
                        return;
                    }
                }
                FirebasePlugin.instance.sendPluginResultAndKeepCallback(json, callbackContext);
            }
        }
    }

    public static void sendToken(String token) {
        if (FirebasePlugin.tokenRefreshCallbackContext == null) {
            return;
        }

        final CallbackContext callbackContext = FirebasePlugin.tokenRefreshCallbackContext;
        if (callbackContext != null && token != null) {
            FirebasePlugin.instance.sendPluginResultAndKeepCallback(token, callbackContext);
        }
    }

    public static boolean inBackground() {
        return FirebasePlugin.inBackground;
    }

    public static boolean hasNotificationsCallback() {
        return FirebasePlugin.notificationCallbackContext != null;
    }

    @Override
    public void onNewIntent(Intent intent) {
        try {
            super.onNewIntent(intent);
            final Bundle data = intent.getExtras();
            if (data != null && data.containsKey("google.message_id")) {
                data.putString("messageType", "notification");
                data.putString("tap", "background");
                Log.d(TAG, "Notification message on new intent: " + data.toString());
                FirebasePlugin.sendMessage(data, applicationContext);
            }
        }catch (Exception e){
            handleExceptionWithoutContext(e);
        }
    }


    private void getToken(JSONArray args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            try {
                                if (task.isSuccessful() || task.getException() == null) {
                                    String currentToken = task.getResult();
                                    callbackContext.success(currentToken);
                                }else if(task.getException() != null){
                                    callbackContext.error(task.getException().getMessage());
                                }else{
                                    callbackContext.error("Task failed for unknown reason");
                                }
                            } catch (Exception e) {
                                handleExceptionWithContext(e, callbackContext);
                            }
                        };
                    });

                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void hasPermission(final CallbackContext callbackContext) {
        if(cordovaActivity == null) return;
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(cordovaActivity);
                    boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
                    callbackContext.success(conformBooleanForPluginResult(areNotificationsEnabled));
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void subscribe(final CallbackContext callbackContext, final String topic) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    handleTaskOutcome(FirebaseMessaging.getInstance().subscribeToTopic(topic), callbackContext);
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void unsubscribe(final CallbackContext callbackContext, final String topic) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    handleTaskOutcome(FirebaseMessaging.getInstance().unsubscribeFromTopic(topic), callbackContext);
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void unregister(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    handleTaskOutcome(FirebaseMessaging.getInstance().deleteToken(), callbackContext);
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void isAutoInitEnabled(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    boolean isEnabled = FirebaseMessaging.getInstance().isAutoInitEnabled();
                    callbackContext.success(conformBooleanForPluginResult(isEnabled));
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void setAutoInitEnabled(final CallbackContext callbackContext, final boolean enabled) {
        final FirebasePlugin self = this;
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    FirebaseMessaging.getInstance().setAutoInitEnabled(enabled);
                    callbackContext.success();
                } catch (Exception e) {
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private Bundle createBundleFromJSONObject(final JSONObject params) throws JSONException {
        final Bundle bundle = new Bundle();
        Iterator<String> iter = params.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            Object obj = params.get(key);
            if (obj instanceof Integer) {
                bundle.putInt(key, (Integer) obj);
            } else if (obj instanceof Double) {
                bundle.putDouble(key, (Double) obj);
            } else if (obj instanceof Float) {
                bundle.putFloat(key, (Float) obj);
            } else if (obj instanceof JSONObject) {
                Bundle item = this.createBundleFromJSONObject((JSONObject) obj);
                bundle.putBundle(key, item);
            } else if (obj instanceof JSONArray) {
                JSONArray objArr = (JSONArray) obj;
                ArrayList<Bundle> bundleArray = new ArrayList<Bundle>(objArr.length());
                for (int idx = 0; idx < objArr.length(); idx++) {
                    Object tmp = objArr.get(idx);
                    if (tmp instanceof JSONObject) {
                        Bundle item = createBundleFromJSONObject(objArr.getJSONObject(idx));
                        bundleArray.add(item);
                    }
                }
                bundle.putParcelableArrayList(key, bundleArray);
            } else {
                bundle.putString(key, obj.toString());
            }
        }
        return bundle;
    }

    private void logEvent(final CallbackContext callbackContext, final String name, final JSONObject params)
            throws JSONException {
        final Bundle bundle = this.createBundleFromJSONObject(params);

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    mFirebaseAnalytics.logEvent(name, bundle);
                    callbackContext.success();
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void setScreenName(final CallbackContext callbackContext, final String name) {
        // This must be called on the main thread
        cordovaActivity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, name);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    callbackContext.success();
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void setUserId(final CallbackContext callbackContext, final String id) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    mFirebaseAnalytics.setUserId(id);
                    callbackContext.success();
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void setUserProperty(final CallbackContext callbackContext, final String name, final String value) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    mFirebaseAnalytics.setUserProperty(name, value);
                    callbackContext.success();
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void setAnalyticsCollectionEnabled(final CallbackContext callbackContext, final boolean enabled) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    mFirebaseAnalytics.setAnalyticsCollectionEnabled(enabled);
                    setPreference(ANALYTICS_COLLECTION_ENABLED, enabled);
                    callbackContext.success();
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                    e.printStackTrace();
                }
            }
        });
    }

    private void isAnalyticsCollectionEnabled(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    callbackContext.success(conformBooleanForPluginResult(getPreference(ANALYTICS_COLLECTION_ENABLED)));
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                    e.printStackTrace();
                }
            }
        });
    }

    public void clearAllNotifications(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    NotificationManager nm = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancelAll();
                    callbackContext.success();
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    public void createChannel(final CallbackContext callbackContext, final JSONObject options) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    createChannel(options);
                    callbackContext.success();
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    protected static NotificationChannel createChannel(final JSONObject options) throws JSONException {
        NotificationChannel channel = null;
        // only call on Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = options.getString("id");
            Log.i(TAG, "Creating channel id="+id);

            if(channelExists(id)){
                deleteChannel(id);
            }

            NotificationManager nm = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            String packageName = cordovaActivity.getPackageName();

            String name = options.optString("name", "");
            Log.d(TAG, "Channel "+id+" - name="+name);

            int importance = options.optInt("importance", NotificationManager.IMPORTANCE_HIGH);
            Log.d(TAG, "Channel "+id+" - importance="+importance);

            channel = new NotificationChannel(id,
                    name,
                    importance);

            // Description
            String description = options.optString("description", "");
            Log.d(TAG, "Channel "+id+" - description="+description);
            channel.setDescription(description);

            // Light
            boolean light = options.optBoolean("light", true);
            Log.d(TAG, "Channel "+id+" - light="+light);
            channel.enableLights(light);

            int lightColor = options.optInt("lightColor", -1);
            if (lightColor != -1) {
                Log.d(TAG, "Channel "+id+" - lightColor="+lightColor);
                channel.setLightColor(lightColor);
            }

            // Visibility
            int visibility = options.optInt("visibility", NotificationCompat.VISIBILITY_PUBLIC);
            Log.d(TAG, "Channel "+id+" - visibility="+visibility);
            channel.setLockscreenVisibility(visibility);

            // Badge
            boolean badge = options.optBoolean("badge", true);
            Log.d(TAG, "Channel "+id+" - badge="+badge);
            channel.setShowBadge(badge);

            int usage = options.optInt("usage", AudioAttributes.USAGE_NOTIFICATION_RINGTONE);
            Log.d(TAG, "Channel "+id+" - usage="+usage);

            int streamType = options.optInt("streamType", -1);
            Log.d(TAG, "Channel "+id+" - streamType="+streamType);

            // Sound
            String sound = options.optString("sound", "default");
            AudioAttributes.Builder audioAttributesBuilder = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(usage);

            if(streamType != -1) {
                audioAttributesBuilder.setLegacyStreamType(streamType);
            }

            AudioAttributes audioAttributes = audioAttributesBuilder.build();
            if ("ringtone".equals(sound)) {
                channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), audioAttributes);
                Log.d(TAG, "Channel "+id+" - sound=ringtone");
            } else if (!sound.contentEquals("false")) {
                if(!sound.contentEquals("default")){
                    Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/raw/" + sound);
                    channel.setSound(soundUri, audioAttributes);
                    Log.d(TAG, "Channel "+id+" - sound="+sound);
                } else {
                    channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);
                    Log.d(TAG, "Channel "+id+" - sound=default");
                }
            } else {
                channel.setSound(null, null);
                Log.d(TAG, "Channel "+id+" - sound=none");
            }

            // Vibration: if vibration setting is an array set vibration pattern, else set enable vibration.
            JSONArray pattern = options.optJSONArray("vibration");
            if (pattern != null) {
                int patternLength = pattern.length();
                long[] patternArray = new long[patternLength];
                for (int i = 0; i < patternLength; i++) {
                    patternArray[i] = pattern.optLong(i);
                }
                channel.enableVibration(true);
                channel.setVibrationPattern(patternArray);
                Log.d(TAG, "Channel "+id+" - vibrate="+pattern);
            } else {
                boolean vibrate = options.optBoolean("vibration", true);
                channel.enableVibration(vibrate);
                Log.d(TAG, "Channel "+id+" - vibrate="+vibrate);
            }

            // Create channel
            nm.createNotificationChannel(channel);
        }
        return channel;
    }

    protected static void createDefaultChannel() throws JSONException {
        JSONObject options = new JSONObject();
        options.put("id", defaultChannelId);
        options.put("name", defaultChannelName);
        createDefaultChannel(options);
    }

    protected static void createDefaultChannel(final JSONObject options) throws JSONException {
        defaultNotificationChannel = createChannel(options);
    }

    public void setDefaultChannel(final CallbackContext callbackContext, final JSONObject options) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    deleteChannel(defaultChannelId);

                    String id = options.optString("id", null);
                    if(id != null){
                        defaultChannelId = id;
                    }

                    String name = options.optString("name", null);
                    if(name != null){
                        defaultChannelName = name;
                    }
                    createDefaultChannel(options);
                    callbackContext.success();
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    public void deleteChannel(final CallbackContext callbackContext, final String channelID) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    deleteChannel(channelID);
                    callbackContext.success();
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    protected static void deleteChannel(final String channelID){
        // only call on Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.deleteNotificationChannel(channelID);
        }
    }

    public void listChannels(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    List<NotificationChannel> notificationChannels = listChannels();
                    JSONArray channels = new JSONArray();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        for (NotificationChannel notificationChannel : notificationChannels) {
                            JSONObject channel = new JSONObject();
                            channel.put("id", notificationChannel.getId());
                            channel.put("name", notificationChannel.getName());
                            channels.put(channel);
                        }
                    }
                    callbackContext.success(channels);
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    public static List<NotificationChannel> listChannels(){
        List<NotificationChannel> notificationChannels = null;
        // only call on Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationChannels = nm.getNotificationChannels();
        }
        return notificationChannels;
    }

    public static boolean channelExists(String channelId){
        boolean exists = false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            List<NotificationChannel> notificationChannels = FirebasePlugin.listChannels();
            if(notificationChannels != null){
                for (NotificationChannel notificationChannel : notificationChannels) {
                    if(notificationChannel.getId().equals(channelId)){
                        exists = true;
                    }
                }
            }
        }
        return exists;
    }

    //
    // Installations
    //
    private void deleteInstallationId(JSONArray args, CallbackContext callbackContext) throws JSONException {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    handleTaskOutcome(FirebaseInstallations.getInstance().delete(), callbackContext);
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    private void getInstallationId(JSONArray args, CallbackContext callbackContext) throws JSONException {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    handleTaskOutcomeWithStringResult(FirebaseInstallations.getInstance().getId(), callbackContext);
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

	private void getAppInstanceId(CallbackContext callbackContext) throws JSONException {
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					handleTaskOutcomeWithStringResult(mFirebaseAnalytics.getAppInstanceId(), callbackContext);
				} catch (Exception e) {
					handleExceptionWithContext(e, callbackContext);
				}
			}
		});
	}

    private void getInstallationToken(JSONArray args, CallbackContext callbackContext) throws JSONException {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    FirebaseInstallations.getInstance().getToken(/* forceRefresh */true)
                            .addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                                    if (task.isSuccessful() || task.getException() == null) {
                                        callbackContext.success(task.getResult().getToken());
                                    }else if(task.getException() != null){
                                        callbackContext.error(task.getException().getMessage());
                                    }else{
                                        callbackContext.error("Task failed for unknown reason");
                                    }
                                }
                            });
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }

    public void isGoogleMobileServicesAvailable(JSONArray args, CallbackContext callbackContext) throws JSONException {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    final int isGoogleMobileServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext);
                    callbackContext.success(isGoogleMobileServicesAvailable);
                } catch (Exception e) {
                    handleExceptionWithContext(e, callbackContext);
                }
            }
        });
    }


    /*
     * Helper methods
     */
    protected static void handleExceptionWithContext(Exception e, CallbackContext context) {
        String msg = e.toString();
        Log.e(TAG, msg);
        context.error(msg);
    }

    protected static void handleExceptionWithoutContext(Exception e){
        String msg = e.toString();
        Log.e(TAG, msg);
        if (instance != null) {
            instance.logErrorToWebview(msg);
        }
    }

	protected void sendPluginResultAndKeepCallback(String result, CallbackContext callbackContext){
		PluginResult pluginresult = new PluginResult(PluginResult.Status.OK, result);
		sendPluginResultAndKeepCallback(pluginresult, callbackContext);
	}

    protected void sendPluginResultAndKeepCallback(JSONArray result, CallbackContext callbackContext){
        PluginResult pluginresult = new PluginResult(PluginResult.Status.OK, result);
        sendPluginResultAndKeepCallback(pluginresult, callbackContext);
    }

    protected void sendPluginResultAndKeepCallback(JSONObject result, CallbackContext callbackContext){
        PluginResult pluginresult = new PluginResult(PluginResult.Status.OK, result);
        sendPluginResultAndKeepCallback(pluginresult, callbackContext);
    }

    protected void sendPluginResultAndKeepCallback(PluginResult pluginresult, CallbackContext callbackContext){
        pluginresult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginresult);
    }

    protected void logErrorToWebview(String msg){
        Log.e(TAG, msg);
        executeGlobalJavascript("console.error(\""+TAG+"[native]: "+escapeDoubleQuotes(msg)+"\")");
    }

    private String escapeDoubleQuotes(String string){
        String escapedString = string.replace("\"", "\\\"");
        escapedString = escapedString.replace("%22", "\\%22");
        return escapedString;
    }

    private void executeGlobalJavascript(final String jsString){
        if(cordovaActivity == null) return;
        cordovaActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + jsString);
            }
        });
    }

    private boolean getMetaDataFromManifest(String name) throws Exception{
        return applicationContext.getPackageManager().getApplicationInfo(applicationContext.getPackageName(), PackageManager.GET_META_DATA).metaData.getBoolean(name);
    }

    private void setPreference(String name, boolean value){
        SharedPreferences settings = cordovaActivity.getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    private boolean getPreference(String name){
        boolean result;
        try{
            SharedPreferences settings = cordovaActivity.getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
            result = settings.getBoolean(name, false);
        }catch (Exception e){
            try{
                result = getMetaDataFromManifest(name);
            }catch (Exception e2){
                result = false;
            }
        }
        return result;
    }

    private void handleTaskOutcome(@NonNull Task<Void> task, CallbackContext callbackContext) {
        try {
            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    try {
                        if (task.isSuccessful() || task.getException() == null) {
                            callbackContext.success();
                        }else if(task.getException() != null){
                            callbackContext.error(task.getException().getMessage());
                        }else{
                            callbackContext.error("Task failed for unknown reason");
                        }
                    } catch (Exception e) {
                        handleExceptionWithContext(e, callbackContext);
                    }
                };
            });
        } catch (Exception e) {
            handleExceptionWithContext(e, callbackContext);
        }
    }

    private void handleTaskOutcomeWithStringResult(@NonNull Task<String> task, CallbackContext callbackContext) {
        try {
            task.addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    try {
                        if (task.isSuccessful() || task.getException() == null) {
                            callbackContext.success(task.getResult());
                        }else if(task.getException() != null){
                            callbackContext.error(task.getException().getMessage());
                        }else{
                            callbackContext.error("Task failed for unknown reason");
                        }
                    } catch (Exception e) {
                        handleExceptionWithContext(e, callbackContext);
                    }
                };
            });
        } catch (Exception e) {
            handleExceptionWithContext(e, callbackContext);
        }
    }

	private int conformBooleanForPluginResult(boolean result){
		return result ? 1 : 0;
	}
}
