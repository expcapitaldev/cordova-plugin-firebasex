var exec = require('cordova/exec');

var ensureBoolean = function(value){
    if(value === "true"){
        value = true;
    }else if(value === "false"){
        value = false;
    }
    return !!value;
};

var execAsPromise = function (command, args) {
    if (args === void 0) { args = []; }
    return new Promise(function (resolve, reject) {
        exec(resolve, reject, 'FirebasePlugin', command, args);
    });
};

var onInstallationIdChangeCallback = function(){};

/***********************
 * Protected internals
 ***********************/
exports._onInstallationIdChangeCallback = function(installationId){
    onInstallationIdChangeCallback(installationId);
};

/**************
 * Public API
 **************/

// Notifications
exports.getToken = function () {
    return execAsPromise('getToken');
};

exports.getAPNSToken = function () {
    return execAsPromise('getAPNSToken');
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

exports.subscribe = function (topic) {
    return execAsPromise('subscribe', [topic]);
};

exports.unsubscribe = function (topic) {
    return execAsPromise('unsubscribe', [topic]);
};

exports.unregister = function () {
    return execAsPromise('unregister');
};

exports.isAutoInitEnabled = function () {
    return execAsPromise('isAutoInitEnabled');
};

exports.setAutoInitEnabled = function (enabled) {
    return execAsPromise('setAutoInitEnabled', [!!enabled]);
};

exports.clearAllNotifications = function () {
    return execAsPromise('clearAllNotifications');
};

// Notifications - iOS-only
exports.onOpenSettings = function () {
    return execAsPromise('onOpenSettings');
};

exports.setBadgeNumber = function (number) {
    return execAsPromise('setBadgeNumber', [number]);
};

exports.getBadgeNumber = function () {
    return execAsPromise('getBadgeNumber');
};

exports.grantPermission = function (requestWithProvidesAppNotificationSettings) {
    return execAsPromise('grantPermission', [ensureBoolean(requestWithProvidesAppNotificationSettings)])
        .then(function (value) { return ensureBoolean(value); });
};

exports.grantCriticalPermission = function () {
    return execAsPromise('grantCriticalPermission')
        .then(function (value) { return ensureBoolean(value); });
};

exports.hasPermission = function () {
    return execAsPromise('hasPermission')
        .then(function (value) { return ensureBoolean(value); });
};

exports.hasCriticalPermission = function () {
    return execAsPromise('hasCriticalPermission')
        .then(function (value) { return ensureBoolean(value); });
};

// Notifications - Android-only
exports.setDefaultChannel = function (options) {
    return execAsPromise('setDefaultChannel', [options]);
};

exports.createChannel = function (options) {
    return execAsPromise('createChannel', [options]);
};

exports.deleteChannel = function (channelID) {
    return execAsPromise('deleteChannel', [channelID]);
};

exports.listChannels = function () {
    return execAsPromise('listChannels');
};

// Analytics
exports.setAnalyticsCollectionEnabled = function (enabled) {
    return execAsPromise('setAnalyticsCollectionEnabled', [!!enabled]);
};

exports.isAnalyticsCollectionEnabled = function () {
    return execAsPromise('isAnalyticsCollectionEnabled');
};

exports.logEvent = function (name, params) {
    return execAsPromise('logEvent', [name, params]);
};

exports.setScreenName = function (name) {
    return execAsPromise('setScreenName', [name]);
};

exports.setUserId = function (id) {
    return execAsPromise('setUserId', [id]);
};

exports.setUserProperty = function (name, value) {
    return execAsPromise('setUserProperty', [name, value]);
};

// Installations
exports.getId = function () {
    return execAsPromise('getId');
};

exports.getInstallationId = function () {
    return execAsPromise('getInstallationId');
};

exports.getAppInstanceId = function () {
    return execAsPromise('getAppInstanceId');
};

exports.getInstallationToken = function () {
    return execAsPromise('getInstallationToken');
};

exports.deleteInstallationId = function () {
    return execAsPromise('deleteInstallationId');
};

exports.registerInstallationIdChangeListener = function(fn){
    if(typeof fn !== "function") throw "The specified argument must be a function";
    onInstallationIdChangeCallback = fn;
};
