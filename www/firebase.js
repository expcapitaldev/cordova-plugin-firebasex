var exec = require('cordova/exec');

var ensureBooleanFn = function (callback){
    return function(result){
        callback(ensureBoolean(result));
    }
};

var ensureBoolean = function(value){
    if(value === "true"){
        value = true;
    }else if(value === "false"){
        value = false;
    }
    return !!value;
};

var onAuthStateChangeCallback = function(){};
var onInstallationIdChangeCallback = function(){};

/***********************
 * Protected internals
 ***********************/
exports._onAuthStateChange = function(userSignedIn){
    onAuthStateChangeCallback(userSignedIn);
};

exports._onInstallationIdChangeCallback = function(installationId){
    onInstallationIdChangeCallback(installationId);
};

/**************
 * Public API
 **************/

// Notifications
exports.getToken = function (success, error) {
  exec(success, error, "FirebasePlugin", "getToken", []);
};

exports.getAPNSToken = function (success, error) {
  exec(success, error, "FirebasePlugin", "getAPNSToken", []);
};

exports.onMessageReceived = function (success, error) {
  exec(success, error, "FirebasePlugin", "onMessageReceived", []);
};

exports.onTokenRefresh = function (success, error) {
  exec(success, error, "FirebasePlugin", "onTokenRefresh", []);
};

exports.onApnsTokenReceived = function (success, error) {
    exec(success, error, "FirebasePlugin", "onApnsTokenReceived", []);
};

exports.subscribe = function (topic, success, error) {
  exec(success, error, "FirebasePlugin", "subscribe", [topic]);
};

exports.unsubscribe = function (topic, success, error) {
  exec(success, error, "FirebasePlugin", "unsubscribe", [topic]);
};

exports.unregister = function (success, error) {
  exec(success, error, "FirebasePlugin", "unregister", []);
};

exports.isAutoInitEnabled = function (success, error) {
    exec(success, error, "FirebasePlugin", "isAutoInitEnabled", []);
};

exports.setAutoInitEnabled = function (enabled, success, error) {
    exec(success, error, "FirebasePlugin", "setAutoInitEnabled", [!!enabled]);
};

exports.clearAllNotifications = function (success, error) {
    exec(success, error, "FirebasePlugin", "clearAllNotifications", []);
};

// Notifications - iOS-only
exports.onOpenSettings = function (success, error) {
  exec(success, error, "FirebasePlugin", "onOpenSettings", []);
};

exports.setBadgeNumber = function (number, success, error) {
    exec(success, error, "FirebasePlugin", "setBadgeNumber", [number]);
};

exports.getBadgeNumber = function (success, error) {
    exec(success, error, "FirebasePlugin", "getBadgeNumber", []);
};

exports.grantPermission = function (success, error, requestWithProvidesAppNotificationSettings) {
    exec(ensureBooleanFn(success), error, "FirebasePlugin", "grantPermission", [ensureBoolean(requestWithProvidesAppNotificationSettings)]);
};

exports.hasPermission = function (success, error) {
    exec(ensureBooleanFn(success), error, "FirebasePlugin", "hasPermission", []);
};

// Notifications - Android-only
exports.setDefaultChannel = function (options, success, error) {
    exec(success, error, "FirebasePlugin", "setDefaultChannel", [options]);
};

exports.createChannel = function (options, success, error) {
    exec(success, error, "FirebasePlugin", "createChannel", [options]);
};

exports.deleteChannel = function (channelID, success, error) {
    exec(success, error, "FirebasePlugin", "deleteChannel", [channelID]);
};

exports.listChannels = function (success, error) {
    exec(success, error, "FirebasePlugin", "listChannels", []);
};

// Analytics
exports.setAnalyticsCollectionEnabled = function (enabled, success, error) {
    exec(success, error, "FirebasePlugin", "setAnalyticsCollectionEnabled", [!!enabled]);
};

exports.isAnalyticsCollectionEnabled = function (success, error) {
    exec(success, error, "FirebasePlugin", "isAnalyticsCollectionEnabled", []);
};

exports.logEvent = function (name, params, success, error) {
  exec(success, error, "FirebasePlugin", "logEvent", [name, params]);
};

exports.setScreenName = function (name, success, error) {
  exec(success, error, "FirebasePlugin", "setScreenName", [name]);
};

exports.setUserId = function (id, success, error) {
  exec(success, error, "FirebasePlugin", "setUserId", [id]);
};

exports.setUserProperty = function (name, value, success, error) {
  exec(success, error, "FirebasePlugin", "setUserProperty", [name, value]);
};

// Installations
exports.getId = function (success, error) {
    exec(success, error, "FirebasePlugin", "getId", []);
};

exports.getInstallationId = function (success, error) {
    exec(success, error, "FirebasePlugin", "getInstallationId", []);
};

exports.getInstallationToken = function (success, error) {
    exec(success, error, "FirebasePlugin", "getInstallationToken", []);
};

exports.deleteInstallationId = function (success, error) {
    exec(success, error, "FirebasePlugin", "deleteInstallationId", []);
};

exports.registerInstallationIdChangeListener = function(fn){
    if(typeof fn !== "function") throw "The specified argument must be a function";
    onInstallationIdChangeCallback = fn;
};
