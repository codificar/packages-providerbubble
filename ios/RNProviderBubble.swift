//
//  RNProviderBubble.swift
//  UberCloneProvider
//
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
	static var receivedUrl: String?
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
	func setupProviderContext(_ id: String, token: String, status: String, redisURI: String, changeStateURL: String, pingURL: String , pingSeconds: String, receivedUrl: String ) {
		if(RNProviderBubble.id == nil || RNProviderBubble.id != id) {
			RNProviderBubble.id = id;
			RNProviderBubble.token = token;
			RNProviderBubble.status = status;
			RNProviderBubble.redisURI = redisURI;
			RNProviderBubble.changeStateURL = changeStateURL;
			RNProviderBubble.pingURL = pingURL;
			RNProviderBubble.pingSeconds = Int(pingSeconds);
			RNProviderBubble.receivedUrl = receivedUrl;
			
			startPingProvider()

		}
	}

  	func startPingProvider() -> Void {
		// print("FIRE!!!")
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
					debugPrint(error)
					debugPrint("DEBUG: Ping Provider error")
				} else {
					if let json = try? JSONSerialization.jsonObject(with: data!, options: [.allowFragments]) as! AnyObject {
						debugPrint(json)
						do{
                            if let array = json["incoming_requests"] as? NSArray{
                                if array.count > 0 {

                                    self.handleMessage(
                                        channel: "ping",
                                        message:  "{ \"data\" : " + RNProviderBubble.stringify(json: json) + "}"
                                    );
                                }
                            }
						}
						catch{
						}
					}
					debugPrint("DEBUG: Ping Provider ok")
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
    
    static func stringify(json: Any, prettyPrinted: Bool = false) -> String {
		var options: JSONSerialization.WritingOptions = []
		if prettyPrinted {
			options = JSONSerialization.WritingOptions.prettyPrinted
		}
        

		do {
			let data = try JSONSerialization.data(withJSONObject: json, options: options)
			if let string = String(data: data, encoding: String.Encoding.utf8) {
				return string
			}
		} catch {
			print(error)
		}

		return ""
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
        let acceptDatetime = self.getRideParameter(message: message, key: "accept_datetime_limit")
        
        if (acceptDatetime != "" && self.checkPingTime(datetime: acceptDatetime) == true) {
            sendEvent(withName: "handleRequest", body: ["data": message])
        }

		let rideId = self.getRideParameter(message: message, key: "request_id")

		if (rideId != "") {
			self.postRequestReceived(channel: channel, request_id: rideId)
		}
  	}
  
	/**
	* Publish message on a PubSub channel
	*
	* @param channel the Redis channel name
	* @param message the message to publish
	*/
	@objc
	func publishMessage(
		_ channel: String,
		message: String,
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

	/**
	 * Check time to handle request
	 */
	func checkPingTime(datetime: String) -> Bool {
		let now = Date()

		let dateFormatter = DateFormatter()
		dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
		let toCompare = dateFormatter.date(from:datetime.trimmingCharacters(in: .whitespacesAndNewlines))!
        debugPrint(now)
        debugPrint(toCompare)
		return (now < toCompare)
	}

	/**
	 * Get ride parameter by key
	 */
    func getRideParameter(message: String, key: String) -> String {
		let data = Data(message.utf8)

		do {
			// make sure this JSON is in the format we expect
			if let json = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]{
				// try to read out a string array
				let rideData = json["data"] as! [String: Any]
				let incRequests = rideData["incoming_requests"]  as! [Any]
				let rideObj = incRequests[0] as! [String: Any]

				if (key == "request_id") {
					let rideId = rideObj[key] as! Int
					return String(rideId)
				}
				
				return rideObj[key] as! String
			}
		} catch let error as NSError {
			print("Failed to load: \(error.localizedDescription)")
		}

		return ""
	}

	/**
	 * Notifying the server that the request has been received
	 */
	func postRequestReceived(channel: String, request_id: String) -> Void {
		let params = ["provider_id":RNProviderBubble.id, "token":RNProviderBubble.token, "request_id": request_id, "channel": channel] as! Dictionary<String, String>
		var request = URLRequest(url: URL(string: RNProviderBubble.receivedUrl!)!)
		request.httpMethod = "POST"
		request.httpBody = try? JSONSerialization.data(withJSONObject: params, options: [])
		request.addValue("application/json", forHTTPHeaderField: "Content-Type")

		let session = URLSession.shared
		let task = session.dataTask(with: request, completionHandler: { data, response, error -> Void in
			if error != nil {
				debugPrint("DEBUG: postRequestReceived error")
			} else {
				debugPrint("DEBUG: postRequestReceived success")
			}
		})
		task.resume()
	}
}
