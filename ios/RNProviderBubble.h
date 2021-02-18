
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"

#else
#import <RCTBridgeModule.h>
#import <RCTEventEmitter.h>
#endif

@interface RCT_EXTERN_MODULE(RNProviderBubble, RCTEventEmitter)

    RCT_EXTERN_METHOD(setupProviderContext : (NSString *)id token:(NSString *)token status:(NSString *)status redisURI:(NSString *)redisURI changeStateURL:(NSString *)changeStateURL pingURL:(NSString *)pingURL  pingSeconds:(NSString *)pingSeconds)

    RCT_EXTERN_METHOD(
                      startService: (RCTPromiseResolveBlock)resolve
                      rejecter: (RCTPromiseRejectBlock)reject
                      )

    RCT_EXTERN_METHOD(
                      stopService: (RCTPromiseResolveBlock)resolve
                      rejecter: (RCTPromiseRejectBlock)reject
                      )

    RCT_EXTERN_METHOD(
                      finishRequest: (RCTPromiseResolveBlock)resolve
                      rejecter: (RCTPromiseRejectBlock)reject
                      )

    RCT_EXTERN_METHOD(
                      publishMessage : (NSString *)channel
                      message:(NSString *)message
                      resolver: (RCTPromiseResolveBlock)resolve
                      rejecter: (RCTPromiseRejectBlock)reject
                      )

    RCT_EXTERN_METHOD(onHostDestroy  : (UIApplication)application)

    RCT_EXTERN_METHOD(canDrawOverlays : (RCTPromiseResolveBlock)resolve
                      rejecter: (RCTPromiseRejectBlock)reject
                    )

@end
  
