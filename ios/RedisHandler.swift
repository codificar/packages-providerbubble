//
//  RedisHandler.swift
//  UberCloneProvider
//
//  Created by Davi Borges on 04/01/2020.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import PSSRedisClient

class RedisHandler: NSObject, RedisManagerDelegate{
  
  enum RedisHandlerError: Error {
    case clientNotConnected
  }
  
  private var redisHost: String?
  private var redisPort: Int?
  private var redisPwd: String?
  
  private var redisInConnection: RedisClient?  // the Redis connection for subscribe
  private var redisOutConnection: RedisClient?  // the Redis connection for publish
  
  private var bridgeModule: RNProviderBubble?
  
  private static var singletonHandler: RedisHandler? // singleton instance
  private var canSub: Bool // control if client is already connected to subscribe
  
  /**
   * Get the singleton Redis handler instance
   *
   * @param redisURI the Redis connection URI
   * @param module the RNProviderBubbleModule instance
   *
   * @return the handler singleton instance
   */
  static func getInstance(redisURI: String, module: RNProviderBubble) throws -> RedisHandler {
    if(RedisHandler.singletonHandler == nil) {
      RedisHandler.singletonHandler = try RedisHandler(redisURI: redisURI, module: module)
    }
    return RedisHandler.singletonHandler!
  }
  
  /**
   * Create a new Redis connection client with a single listener for all channels subscribed
   *
   * @param redisURI the Redis connection URI - redis://[password@]host[:port][/databaseNumber]
   * @param module the RNProviderBubbleModule instance
   */
  private init(redisURI: String?, module: RNProviderBubble) throws {
    
    self.canSub = false;
    
    super.init()
    
    self.bridgeModule = module
    
    // Get Redis URI params
    let index = redisURI!.index(redisURI!.startIndex, offsetBy: 8)
    let uri = String(redisURI!.suffix(from: index))
    let params = uri.replacingOccurrences(of: "@", with: ":").components(separatedBy: ":")
    
    self.redisPwd = params[0]
    self.redisHost = params[1]
    self.redisPort = (params[2] as NSString).integerValue
    
    // Create redis connections
    self.redisInConnection = RedisClient(delegate: self)
    self.redisOutConnection = RedisClient(delegate: self)
    self.redisInConnection?.connect(host: self.redisHost!, port: self.redisPort!, pwd: self.redisPwd!)
    self.redisOutConnection?.connect(host: self.redisHost!, port: self.redisPort!, pwd: self.redisPwd!)
  }
  
  /**
   * Delegate the message received via subscribed channel to
   * the bridge module
   * @param channel the redis channel subscribed
   * @param message the message received
   */
  func subscriptionMessageReceived(channel: String, message: String) {
    debugPrint("REDIS: SUB message received")
    
    self.bridgeModule?.handleMessage(channel: channel, message: message)
  }
  
  func socketDidDisconnect(client: RedisClient, error: Error?) {
    debugPrint("REDIS: Disconnected (Error: \(String(describing: error?.localizedDescription)))")
    
    // Try to reconnect the client
    client.connect(host: self.redisHost!, port: self.redisPort!, pwd: self.redisPwd!)
  }
  
  func socketDidConnect(client: RedisClient) {
    debugPrint("REDIS: Socket connected");
    
    self.canSub = self.redisInConnection?.isConnected() ?? false
  }
  
  func socketDidSubscribe(socket: RedisClient, channel: String) {
    debugPrint("REDIS: Subscribed to \(channel)")
  }
  
  func socketDidReceivePong(socket: RedisClient) {
    //    debugPrint("REDIS: Pong received")
  }
  
  /**
   * Subscribe to a PubSub channel
   * (5 attempts, 1 second between each attempt)
   * @param channel the redis channel name
   */
  func subscribePubSub(channel: String, attempt: Int = 0) throws{
    if(self.canSub) {
      self.redisInConnection?.subscribe(to: channel)
    }
    else if(attempt < 5){
      debugPrint("REDIS: Waiting for socket connection to subscribe...")
      /* ENSURE this script is NOT running on the main thread, otherwise the sleep
       command will freeze the application. See: requiresMainQueueSetup */
      sleep(1)
      try self.subscribePubSub(channel: channel, attempt: attempt+1)
    }
    else {
      throw RedisHandlerError.clientNotConnected
    }
  }
  
  /**
   * Unsubscribe a PubSub channel
   *
   * @param channel the Redis channel name
   */
  func unsubscribePubSub(channel: String) throws {
    self.redisInConnection?.exec(args: ["UNSUBSCRIBE", channel], completion: self.messageReceived)
    debugPrint("REDIS: successfully unsubscribed from channel \(channel)")
  }
  
  func messageReceived(message: NSArray) {
    debugPrint("REDIS: return message received")
    if (message.firstObject as? NSError != nil) {
      let error = message.firstObject as! NSError
      let userInfo = error.userInfo
      
      if let possibleMessage = userInfo["message"] {
        if let actualMessage = possibleMessage as? String {
          debugPrint("REDIS: \(actualMessage)")
        }
      }
    } else {
      debugPrint("REDIS: Results: \(message.componentsJoined(by: " "))")
    }
  }
  
  /**
   * Publish message on a PubSub channel
   *
   * @param channel the Redis channel name
   * @param message the message to publish
   */
  func publishPubSub(_ channel: String, message: String) {
    debugPrint("DEBUG: publish on \(channel) message: \(message)")
    
    self.redisOutConnection?.exec(args: ["PUBLISH", channel, message], completion: self.messageReceived)
  }
  
}
