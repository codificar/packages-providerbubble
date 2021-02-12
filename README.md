
# react-native-provider-bubble

## Testes Locais:

git clone esse repo

entra nele da pwd , copia o path.

no projeto que vai usar npm -- save 'path'

da npm install agora.

## Getting started

`$ npm install react-native-provider-bubble --save`

### Mostly automatic installation

`$ react-native link react-native-provider-bubble`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-provider-bubble` and add `RNProviderBubble.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNProviderBubble.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import br.com.codificar.providerbubble.RNProviderBubblePackage;` to the imports at the top of the file
  - Add `new RNProviderBubblePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-provider-bubble'
  	project(':react-native-provider-bubble').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-provider-bubble/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-provider-bubble')
  	```


## Usage
```javascript
import RNProviderBubble from 'react-native-provider-bubble';

// TODO: What to do with the module?
RNProviderBubble;
```
  