
# react-native-provider-bubble

## Testes Locais:

- git clone nesse repositório: `git clone https://git.codificar.com.br/packages/providerbubble`

- `cd providerbubble`

- use o comando `pwd` para saber o local, ou melhor:  copie o path `PATHAQUI=$(pwd)`

- va ao projeto que vai usar esse package e rode: `npm install -- save $PATHAQUI`

- rode em `npm install`.

- `rm -rf node_modules && yarn install && yarn android`


## Pre Requisitos 

Seu projeto precisa ter na parte nativa android no values/strings.xml os seguintes campos:

`````
<string name="app_name">Meu Aplicativo</string>
<string name="display_over_other_apps"> O Aplicativo está sendo exibido sobre outros apps</string>
<string name="display_over_other_apps_message">Se você não quiser que o Aplicativo use essa função, clique para abrir as configurações e desative-a.</string>
<string name="over_other_apps_title">Permitir que o seja exibido sobre outros apps?</string>
<string name="over_other_apps_message">Essa permissão é importante para que você possa usar todas as funcionalidades do app.</string>
   
`````

imagem do mipmap deve chamar ic_launcher.

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
  