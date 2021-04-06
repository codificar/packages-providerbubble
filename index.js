
import { NativeModules } from 'react-native';

const { RNProviderBubble } = NativeModules;

export default {
	setupProviderContext (id, token, status, redisURI, changeStateURL, pingUrl, pingSeconds, receivedUrl, isCheckTimeEnabled = false) {
		return RNProviderBubble.setupProviderContext(id, token, status, redisURI, changeStateURL, pingUrl, pingSeconds, receivedUrl, isCheckTimeEnabled)
	},
	startService () {
		return RNProviderBubble.startService()
	},
	stopService () {
		return RNProviderBubble.stopService()
	},
	finishRequest() {
		return RNProviderBubble.finishRequest();
	},
	openActivityOverOtherApps (){
		return RNProviderBubble.openActivityOverOtherApps()
	},
	openActivityMapsAndroid(url) {
		return RNProviderBubble.openActivityMapsAndroid(url)
	},
	canDrawOverlays (){
		return RNProviderBubble.canDrawOverlays()
	},
	setupProviderContextClear(){
		return RNProviderBubble.setupProviderContextClear()
	}
}