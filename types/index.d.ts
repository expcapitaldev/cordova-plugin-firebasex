interface IFirebaseChannelOptions {
    id: string;
    name?: string;
    description?: string;
    sound?: string;
    vibration?: boolean | number[];
    light?: boolean;
    lightColor?: string;
    importance?: 0 | 1 | 2 | 3 | 4;
    badge?: boolean;
    visibility?: -1 | 0 | 1;
}

interface IFirebaseMessageReceived {
    tap?: 'foreground' | 'background';
    messageType: 'notification' | 'data';
    deeplink?: string;
}

interface IFirebasePlugin {
    getId(): Promise<string>;

    getToken(): Promise<string>;

    onTokenRefresh(
        success: (value: string) => void,
        error: (err: string) => void
    ): void;

    getAPNSToken(): Promise<string>;

    onApnsTokenReceived(
        success: (value: string) => void,
        error: (err: string) => void
    ): void;

    onMessageReceived(
        success: (data: IFirebaseMessageReceived) => void,
        error: (err: string) => void
    ): void;

    onOpenSettings(): Promise<void>;

    grantPermission(requestWithProvidesAppNotificationSettings?: boolean): Promise<boolean>;

    hasPermission(): Promise<boolean>;

    unregister(): Promise<void>;

    setBadgeNumber(badgeNumber: number): Promise<void>;

    getBadgeNumber(): Promise<number>;

    clearAllNotifications(): Promise<void>;

    subscribe(topic: string): Promise<void>;

    unsubscribe(topic: string): Promise<void>;

    isAutoInitEnabled(): Promise<boolean>;

    setAutoInitEnabled(enabled: boolean): Promise<void>;

    createChannel(channel: IFirebaseChannelOptions): Promise<void>;

    setDefaultChannel(channel: IFirebaseChannelOptions): Promise<void>;

    deleteChannel(channel: string): Promise<void>;

    listChannels(): Promise<{ id: string; name: string }[]>;

    setAnalyticsCollectionEnabled(setEnabled: boolean): Promise<void>;

    logEvent(eventName: string, eventProperties: object): Promise<void>;

    setScreenName(screenName: string): Promise<void>;

    setUserId(userId: string): Promise<void>;

    setUserProperty(userName: string, userValue: string): Promise<void>;

    getInstallationId(): Promise<string>;

    getAppInstanceId(): Promise<string>;

    registerInstallationIdChangeListener(
        callback: (installationId: string) => void,
    ): void;

    // see https://developers.google.com/android/reference/com/google/android/gms/common/api/CommonStatusCodes
    isGoogleMobileServicesAvailable(): Promise<number>;
}

interface Window {
    FirebasePlugin: IFirebasePlugin;
}
