//
//  RNProviderBubble.m
//  UberCloneProvider
//
//  Created by Davi Borges on 04/01/2020.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

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


