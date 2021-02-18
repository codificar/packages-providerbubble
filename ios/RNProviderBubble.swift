//
//  RNProviderBubble.swift
//  UberCloneProvider
//
//  Created by Davi Borges on 04/01/2020.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

@objc(RNProviderBubble)
class RNProviderBubble: RCTEventEmitter{
  
  static var id: String?
  static var token: String?
  static var status: String?
  static var redisURI: String?
  static var changeStateURL: String?
  static var pingURL: String?
  static var pingSeconds: Int! = 0
  static let ONLINE: String = "1"
  static let OFFLINE: String = "0"
  
  // Array of event names that we can listen to
  override func supportedEvents() -> [String]! {
    return ["handleRequest"]
  }
  
  // true if the class must be initialized on the main thread
  // false if the class can be initialized on a background thread
  override static func requiresMainQueueSetup() -> Bool {
    return false
  }
  
  @objc
  func onHostDestroy (_ application: UIApplication) {
    // Set provider offline before app is killed
    if(RNProviderBubble.status == RNProviderBubble.ONLINE) {
      debugPrint("DEBUG: Provider is exiting application online...")
      let params = ["id":RNProviderBubble.id, "token":RNProviderBubble.token] as! Dictionary<String, String>
      var request = URLRequest(url: URL(string: RNProviderBubble.changeStateURL!)!)
      request.httpMethod = "POST"
      request.httpBody = try? JSONSerialization.data(withJSONObject: params, options: [])
      request.addValue("application/json", forHTTPHeaderField: "Content-Type")
      let semaphore = DispatchSemaphore(value: 0)
      let session = URLSession.shared
      let task = session.dataTask(with: request, completionHandler: { data, response, error -> Void in
        if error != nil {
          debugPrint("DEBUG: Error logging out provider")
          //          debugPrint("DEBUG: (String(describing: error))")
        } else {
          debugPrint("DEBUG: provider logget out")
        }
        semaphore.signal()
      })
      task.resume()
      semaphore.wait()
    }
  }
  
  @objc
  func setupProviderContext(_ id: String, token: String, status: String, redisURI: String, changeStateURL: String, pingURL: String , pingSeconds: String ) {
    if(RNProviderBubble.id == nil || RNProviderBubble.id != id) {
      RNProviderBubble.id = id;
      RNProviderBubble.token = token;
      RNProviderBubble.status = status;
      RNProviderBubble.redisURI = redisURI;
      RNProviderBubble.changeStateURL = changeStateURL;
      RNProviderBubble.pingURL = pingURL;
      RNProviderBubble.pingSeconds = Int(pingSeconds);
      
      startPingProvider()

    }
  }

  func startPingProvider() -> Void {
    
    print("FIRE!!!")
    let semaphore = DispatchSemaphore(value: 0)
    
    if(RNProviderBubble.status == RNProviderBubble.ONLINE) {
      debugPrint("DEBUG: Provider is exiting application online...")
      let params = ["id":RNProviderBubble.id, "token":RNProviderBubble.token] as! Dictionary<String, String>
      var request = URLRequest(url: URL(string: RNProviderBubble.pingURL!)!)
      request.httpMethod = "POST"
      request.httpBody = try? JSONSerialization.data(withJSONObject: params, options: [])
      request.addValue("application/json", forHTTPHeaderField: "Content-Type")
      
      let session = URLSession.shared
      let task = session.dataTask(with: request, completionHandler: { data, response, error -> Void in
        if error != nil {
          debugPrint("DEBUG: Ping Provider")
        } else {
          debugPrint("DEBUG: Ping Provider else")
        }
        semaphore.signal()
      })
      task.resume()
      semaphore.wait()
    }
    
    DispatchQueue.main.asyncAfter(deadline: .now() + DispatchTimeInterval.seconds(RNProviderBubble.pingSeconds)) {
    self.startPingProvider()
    }
  }

  @objc
  func canDrawOverlays(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
    do {
      resolve("can")
    } catch {
      reject("cannot", "Cannot draw overlays" , error)
    }
  }

  
  @objc
  func startService(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
    do {
      let channel = "provider." + RNProviderBubble.id!;
      try RedisHandler.getInstance(redisURI: RNProviderBubble.redisURI!, module: self).subscribePubSub(channel: channel)
      RNProviderBubble.status = RNProviderBubble.ONLINE
      resolve("Successfully connected and subscribed")
    } catch {
      reject("ER_SUB", "Failed to connect/subscribe", error)
    }
  }
  
  @objc
  func stopService(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
    do {
      let channel = "provider." + RNProviderBubble.id!;
      try RedisHandler.getInstance(redisURI: RNProviderBubble.redisURI!, module: self).unsubscribePubSub(channel: channel)
      RNProviderBubble.status = RNProviderBubble.OFFLINE
      resolve("Successfully unsubscribed")
    } catch {
      reject("ER_UNSUB", "Failed to unsubscribe", error)
    }
  }

  @objc
  func finishRequest(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
    do {
      if(RNProviderBubble.id != nil){
      let channel = "provider." + RNProviderBubble.id!;
      try RedisHandler.getInstance(redisURI: RNProviderBubble.redisURI!, module: self).unsubscribePubSub(channel: channel)
      RNProviderBubble.status = RNProviderBubble.OFFLINE
      resolve("Successfully unsubscribed")
      }
    } catch {
      reject("ER_UNSUB", "Failed to unsubscribe", error)
    }
  }
  
  /**
   * Handle the message received via subscribed channel
   * @param channel the redis channel subscribed
   * @param message the message received
   */
  func handleMessage(channel: String, message: String) {
    // TODO: verify if host is active?
    sendEvent(withName: "handleRequest", body: ["data": message])
  }
  
  /**
   * Publish message on a PubSub channel
   *
   * @param channel the Redis channel name
   * @param message the message to publish
   */
  @objc
  func publishMessage(_ channel: String, message: String,
                      resolver resolve: RCTPromiseResolveBlock,
                      rejecter reject: RCTPromiseRejectBlock
    ) -> Void {
    do {
      try RedisHandler.getInstance(redisURI: RNProviderBubble.redisURI!, module: self).publishPubSub(channel, message: message)
      resolve("Message successfully published on channel \(channel)")
    } catch {
      reject("ER_PUB", "Falied to publish message", error)
    }
  }
}
