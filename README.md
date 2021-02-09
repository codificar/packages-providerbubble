
# react-native-providerbubble

## Getting started

`$ npm install react-native-providerbubble --save`

### Mostly automatic installation

`$ react-native link react-native-providerbubble`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-providerbubble` and add `RNProviderbubble.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNProviderbubble.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import providerbubble.RNProviderbubblePackage;` to the imports at the top of the file
  - Add `new RNProviderbubblePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-providerbubble'
  	project(':react-native-providerbubble').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-providerbubble/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-providerbubble')
  	```


## Usage
```javascript
import RNProviderbubble from 'react-native-providerbubble';

// TODO: What to do with the module?
RNProviderbubble;
```
  