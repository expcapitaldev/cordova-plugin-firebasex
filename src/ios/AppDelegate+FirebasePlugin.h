#import "AppDelegate.h"

@import UserNotifications;
@import AuthenticationServices;

@interface AppDelegate (FirebasePlugin) <UIApplicationDelegate, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding>
+ (AppDelegate *_Nonnull) instance;
@property (nonatomic, strong) NSNumber * _Nonnull applicationInBackground;
@end
