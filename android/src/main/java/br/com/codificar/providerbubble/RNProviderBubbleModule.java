
package br.com.codificar.providerbubble;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import androidx.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;

import android.view.View;
import android.view.WindowManager;

import android.graphics.PixelFormat;
import android.os.Build;
import android.net.Uri;
import android.provider.Settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.util.Calendar;

import 	java.text.SimpleDateFormat;
import 	java.text.DateFormat;


import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import static java.util.concurrent.TimeUnit.*;

import com.android.volley.RequestQueue;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.Volley;

import library.parse.VolleyHttpRequest;
import library.parse.AsyncTaskCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.codificar.providerbubble.BubbleService;
import br.com.codificar.providerbubble.RedisHandler;


public class RNProviderBubbleModule extends ReactContextBaseJavaModule implements AsyncTaskCompleteListener , LifecycleEventListener {

	public static final String REACT_CLASS = "RNProviderBubble";
	private static ReactApplicationContext reactContext = null;
	private static final int PERMISSION_OVERLAY_SCREEN = 78;

	private String id, token, status, changeStateURL, pingURL, redisURI, lastChannel, receivedUrl;
	private int pingSeconds = 15;
	private RequestQueue requestQueue;

	private Timer timerPing ;

	private static final String ONLINE = "1";
	private static final String OFFLINE = "0";
	private static final String INCOMING_REQUESTS = "incoming_requests";
	private static final String DATA = "data";
	private static final String ACCEPT_DATETIME_LIMIT = "accept_datetime_limit";
	private static final String REQUEST_ID = "request_id";
	private static final int PING = 10;
	private static final int TOGGLE = 10;
	private static final int RECEIVED = 11;

	private static boolean hostActive;
	private static String requestData;

	private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

		@Override
		public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
			switch (requestCode){
				case PERMISSION_OVERLAY_SCREEN:
					try {
						Intent intentStart = new Intent(BubbleService.FOREGROUND);
						intentStart.setClass(getReactApplicationContext(), BubbleService.class);
						getReactApplicationContext().startService(intentStart);
						emitCanDrawOverlays();
					} catch (Exception ignored) {
					}
					break;
				default:
					break;
			}
		}
	};


	/**
	 * Monitor app lifecycle
	 */
	@Override
	public void onHostResume() {
		// Activity `onResume`
		hostActive = true;
	}


	@Override
	public void onHostPause() {
		// Activity `onPause`
		hostActive = false;
	}

	@Override
	public void onHostDestroy() {
		try {
			// Set provider offline
			if(status != null && status.equals(ONLINE))
			{
				this.toggleService(false);
				RedisHandler.getInstance(this.redisURI, this)
					.unsubscribePubSub("provider."+id);

				HashMap<String, String> map = new HashMap<>();
				map.put("url", changeStateURL);
				map.put("id", id);
				map.put("token", token);

				if(requestQueue == null)
					requestQueue = Volley.newRequestQueue(Objects.requireNonNull(getCurrentActivity()));

				requestQueue.add(new VolleyHttpRequest(Method.POST, map, TOGGLE, this, null));
			}

			if (timerPing != null) {
				timerPing.cancel();
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		switch (serviceCode){
			case PING:

				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(response);
					if (jsonObject.has(INCOMING_REQUESTS)) {
						JSONArray jsonArray = jsonObject.getJSONArray(INCOMING_REQUESTS);
						if (jsonArray.length() > 0 ) {
							JSONObject jsonLikeRedis = new JSONObject();
							jsonLikeRedis.put("data", jsonObject);

							handleMessage("ping", jsonLikeRedis.toString());
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Log.d("PING", response);
				break;
			case RECEIVED:
				JSONObject jsonObjectReceived = null;

				try {
					jsonObjectReceived = new JSONObject(response);

					if (jsonObjectReceived.has("time_error")) {
						if (jsonObjectReceived.getBoolean("time_error") == true)
							emitWrongDateTime();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Log.d("RECEIVED", response);
			default:
				break;
		}
	}

	public RNProviderBubbleModule(ReactApplicationContext reactContext) {
		super(reactContext);

		this.reactContext = reactContext;
		this.reactContext.addActivityEventListener(mActivityEventListener);
		BubbleService.currentActivity = getCurrentActivity();
		reactContext.addLifecycleEventListener(this);
	}


	// @Override
	// public List<Class<? extends JavaScriptModule>> createJSModules() {
	// 	return Collections.emptyList();
	// }

	@Override
	public String getName() {
		return REACT_CLASS;
	}

	@Override
	public Map<String, Object> getConstants() {
		// Export any constants to be used in your native module
		// https://facebook.github.io/react-native/docs/native-modules-android.html#the-toast-module
		final Map<String, Object> constants = new HashMap<>();
		//  constants.put("EXAMPLE_CONSTANT", "example");

		return constants;
	}

	@ReactMethod
	public void openActivityMapsAndroid(String url){
		Uri gmmIntentUri = Uri.parse("google.navigation:q=" + url);
		Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
		mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mapIntent.setPackage("com.google.android.apps.maps");
		if (mapIntent.resolveActivity(reactContext.getPackageManager()) != null) {
			reactContext.startActivity(mapIntent);
		}
	}

	@ReactMethod
	public void setupProviderContext(String id, String token, String status, String redisURI, String changeStateURL, String pingURL, String pingSeconds, String receivedUrl) {
		if(this.id == null || this.id != id) {
			this.id = id;
			this.token = token;
			this.status = status;
			this.redisURI = redisURI;
			this.changeStateURL = changeStateURL;
			this.pingURL = pingURL;
			this.lastChannel = "construct";
			this.receivedUrl = receivedUrl;

			try {
				this.pingSeconds = Integer.parseInt(pingSeconds);
				BubbleService.currentActivity = getCurrentActivity();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}

			startPingProvider();
		}
	}

	// timer de checagem de status de corrida atual
	private class TimerPingProvider extends TimerTask {
		@Override
		public void run() {
			try {
				postPing();
			}
			catch (Exception ex){
				ex.printStackTrace();
			}

			try {
				subRedis();
			}
			catch (Exception ex){
				ex.printStackTrace();
			}

		
		}
	}

	public void subRedis(){
		Log.d("subRedis:", this.redisURI);
		RedisHandler.getInstance(this.redisURI, this)
				.subscribePubSub("provider."+id);
	}

	public void postPing() throws Exception {

		HashMap<String, String> map = new HashMap<>();
			map.put("url", pingURL);
			map.put("id", id);
			map.put("provider_id", id);
			map.put("token", token);

		if(requestQueue == null)
			requestQueue = Volley.newRequestQueue(Objects.requireNonNull(getCurrentActivity()));

		requestQueue.add(new VolleyHttpRequest(Method.POST, map, PING, this, null));

	}

	public void startPingProvider() {
		Log.d("startPingProvider:", String.valueOf(this.pingSeconds));
		if (timerPing != null) {
			timerPing.cancel();
		}

		timerPing = new Timer();
		timerPing.scheduleAtFixedRate(new TimerPingProvider(), 0, this.pingSeconds*1000);

	}

	/**
	 * Handle messages received by pubSub Redis client
	 *
	 * @param channel the Redis PubSub subscribed cannel
	 * @param message the message received
	 */
	public void handleMessage(String channel, String message) {
		if (this.checkPingTime(this.getRideParameter(message, ACCEPT_DATETIME_LIMIT))) {
			Log.d("###", "Com tempo");

			lastChannel = channel;
			BubbleService.startRequestBubble(getReactApplicationContext(), 2);
			emitRequest(channel ,message);
		} else {
			Log.d("###", "Sem tempo");
		}

		this.postReceived(channel, this.getRideParameter(message, REQUEST_ID));
	}

	/**
	 * Emit the request to the react-native module
	 *
	 * @param request the new service request
	 */
	public static void emitRequest(String channel, String request) {
		WritableMap map = Arguments.createMap();
		map.putString("channel", channel);
		map.putString("data", request);
		emitDeviceEvent("handleRequest", map);
	}

	@ReactMethod
	public void startService(Promise promise) {
		String result = "Success";
		try {
			this.toggleService(true);
			this.status = ONLINE;

			subRedis();

		} catch (Exception e) {
			promise.reject(e);
			result = "Failed";
			return;
		}
		promise.resolve(result);
	}

	@ReactMethod
	public void stopService(Promise promise) {
		String result = "Success";
		try {
			this.toggleService(false);
			
			this.status = OFFLINE;

			RedisHandler.getInstance(this.redisURI, this)
				.unsubscribePubSub("provider."+id);

		} catch (Exception e) {
			promise.reject(e);
			result = "Failed";
			return;
		}
		promise.resolve(result);
	}

	@ReactMethod
	public void finishRequest(Promise promise) {
		String result = "Success";
		try {

			BubbleService.stopRequestBubble(getReactApplicationContext(), 2);

		} catch (Exception e) {
			promise.reject(e);
			result = "Failed";
			return;
		}
		promise.resolve(result);
	}

	@ReactMethod
	public void openActivityOverOtherApps(Promise promise) {
		String result = "Success";
		try {
			// Call activity to get permission to overlay other apps
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
			intent.setData(Uri.parse("package:" + reactContext.getPackageName()));
			if (intent.resolveActivity(reactContext.getPackageManager()) != null) {
				reactContext.startActivityForResult(intent, PERMISSION_OVERLAY_SCREEN, null);
			}
			/**
			 * Finalize service previously started.
			 */
			this.toggleService(false);
		} catch (Exception e) {
			promise.reject(e);
			result = "Failed";
			return;
		}
		promise.resolve(result);
	}

	@ReactMethod
	public void canDrawOverlays(Promise promise) {
		String result = "cannot";
		try {
			// Call activity to get permission to overlay other apps
			if(canDrawOverlays(getReactApplicationContext()))
				result = "can";

		} catch (Exception e) {
			promise.reject(e);
			result = "cannot";
			return;
		}
		promise.resolve(result);
	}

	/**
	 * Workaround for Android O
	 * https://stackoverflow.com/a/46174872
	 */
	public static boolean canDrawOverlays(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
		else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			return Settings.canDrawOverlays(context);
		} else {
			if (Settings.canDrawOverlays(context)) return true;
			try {
				WindowManager mgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
				if (mgr == null) return false; //getSystemService might return null
				View viewToAdd = new View(context);
				WindowManager.LayoutParams params = new WindowManager.LayoutParams(0, 0, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
						WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
						WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
				viewToAdd.setLayoutParams(params);
				mgr.addView(viewToAdd, params);
				mgr.removeView(viewToAdd);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	private static void emitDeviceEvent(String eventName, @Nullable WritableMap eventData) {
		// A method for emitting from the native side to JS
		// https://facebook.github.io/react-native/docs/native-modules-android.html#sending-events-to-javascript
		reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, eventData);
	}

	public static void emitShowOverAppsAlert(){
		WritableMap map = Arguments.createMap();
		map.putBoolean("result",true);
		emitDeviceEvent("emitShowOverAppsAlert", map);
		// emitDeviceEvent("emitShowOverAppsAlert", null);
	}

	public static void emitCanDrawOverlays(){
		WritableMap map = Arguments.createMap();
		map.putBoolean("result", canDrawOverlays(reactContext));
		emitDeviceEvent("canDrawOverlays", map);
	}

	/**
	 * Start/stop the bubble sertvice.
	 */
	public void toggleService(boolean start) {

		Intent intent = new Intent(BubbleService.FOREGROUND);
		intent.setClass(this.getReactApplicationContext(), BubbleService.class);
		if(start) {
			this.getReactApplicationContext().startService(intent);
		}
		else {
			this.getReactApplicationContext().stopService(intent);
		}
	}

	/**
	 * Check time to handle request
	 *
	 * @param datetime
	 */
	public boolean checkPingTime(String datetime) {
		try {
			if (datetime == "")
				return false;

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Date date = new Date();
			Date dateSend = dateFormat.parse(datetime.trim());

			Log.d("###now", date.toString());
			Log.d("###timeToCompare", dateSend.toString());

			if (date.before(dateSend)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	/**
	 * Get a ride parameter by key
	 *
	 * @param message
	 * @param key
	 */
	public String getRideParameter(String message, String key) {
		String parameter = "";

		try {
			JSONObject ride = this.parseRide(message);

			if (ride != null) {
				return ride.getString(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return parameter;
	}

	/**
	 * Parse ride string to JSONObject
	 *
	 * @param ride
	 */
	public JSONObject parseRide (String ride) {
		JSONObject jsonObject = null;

		try {
			jsonObject = new JSONObject(ride);
			JSONObject data = jsonObject.getJSONObject(DATA);
			JSONArray jsonArray = data.getJSONArray(INCOMING_REQUESTS);
			if (jsonArray.length() > 0 ) {
				return jsonArray.getJSONObject(0);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonObject;
	}

	/**
	 * Notifying the server that the request has been received
	 *
	 * @param channel
	 * @param request_id
	 */
	public void postReceived(String channel, String request_id) {
		try {
			if (request_id == "")
				return;
			
			HashMap<String, String> map = new HashMap<>();
			map.put("url", receivedUrl);
			map.put("provider_id", id);
			map.put("token", token);
			map.put("request_id", request_id);
			map.put("channel", channel);
			map.put("device_date", this.getDeviceCurrentDate());

			if(requestQueue == null)
				requestQueue = Volley.newRequestQueue(Objects.requireNonNull(getCurrentActivity()));

			requestQueue.add(new VolleyHttpRequest(Method.POST, map, RECEIVED, this, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get device current date
	 */
	public String getDeviceCurrentDate() {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			String dateNow = dateFormat.format(date);

			return dateNow;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Emit wrong date time event
	 */
	public static void emitWrongDateTime() {
		WritableMap map = Arguments.createMap();
		map.putBoolean("time_error", true);
		emitDeviceEvent("deviceWrongDate", map);
	}
}