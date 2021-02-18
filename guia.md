

# Crie sua própria biblioteca/componente react-native e publicar no npm.

Objetivo desse artigo é mostrar o básico necessário para se criar o pacote e como transferir migrar um código de um projeto para um pacote de forma que ele possa ser usado de forma modular em outros projetos pessoais ou de uma empresa. 

Este artigo foi criado para documentar o processo de migração de um código de um projeto da codificar para um pacote para ser reutilizado. O intuito é de se compartilhar internamente com a equipe da codificar e externamente para agregar ao ecossistema desenvolvedor.

Cobrimos esse artigo em português com intuito de se ensinar esse processo que se acha com maior facilidade em inglês e não em pt-br.

#### pré-requisitos

- ter *node* instalado

`curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.36.0/install.sh | bash`

`nvm install node`

- um conhecimento básico em *swift*, *android* e *javascript*
- instalar módulo *npm* do *react-native*

`npm install -g react-native-cli`

##### conselho de amigo

- Use *yarn* ao invés de npm. O yarn tem velocidades superiores e comandos otimizados e é bom conhecer uma ferramenta nova. Também busca o conteúdo e pacotes do mesmo *registry* e permite que você paraleliza o download dos pacotes com um simples `yarn install`. Use o comando a seguir para baixar o yarn

`npm install -g yarn.`

#### Índice
  - [1 Criando seu pacote](#1-criando-seu-pacote)
  - [2 Noções sobre a programação de um pacote.](#2-noções-sobre-a-programação-de-um-pacote)
    - [2.1. android](#21-android)
    - [2.2. ios](#22-ios)
    - [2.3. javascript](#23-javascript)
  - [3. Migrando o seu código para estrutura de um pacote.](#3-migrando-o-seu-código-para-estrutura-de-um-pacote)
    - [3.1. Primeiros Passos](#31-primeiros-passos)
    - [3.2. Dificuldades](#32-dificuldades)
    - [3.3. Dicas](#33-dicas)
  - [1.4. Publicando no npm.](#14-publicando-no-npm)
    - [4.1. Component Publishing](#41-component-publishing)
    - [4.2. npm registry](#42-npm-registry)
      - [4.2.1. Global Switching](#421-global-switching)
      - [4.2.2. Create/login npm registry account](#422-createlogin-npm-registry-account)
    - [4.3. Preparing for Release](#43-preparing-for-release)
      - [4.3.1. gitignore and.npmignore](#431-gitignore-andnpmignore)
    - [4.4. package.json](#44-packagejson)
    - [4.5. Writing readme.md](#45-writing-readmemd)
  - [5. Release](#5-release)
    - [5.1. First Release](#51-first-release)
    - [5.2. Update Publication](#52-update-publication)

## 1 Criando seu pacote

Para criar um pacote é necessário de se instalar o pacote *react-native-create-library* , que cria a estrutura básica inicial de um pacote *react-native*.

- instalar global para uso por linha de comando da biblioteca:

`npm install -g react-native-create-library`

- use ou crie uma pasta para se organizar 

`mkdir pacotes && cd pacotes`

- rodar o comando para criar o pacote , lembrando de criar corretamente o identificador do seu aplicativo, visto que este precisa de ser único.

`react-native-create-library --package-identifier 	br.com.codificar.pacote_a_criar --platforms android,ios pacote_a_criar`

`cd pacote_a_criar`

- agora use o comando `tree` para verificar a estrutura de pastas que o comando criou para você:

- caso não tenha o tree use : 

`alias tree="find . -print | sed -e 's;[^/]*/;|____;g;s;____|; |;g'"`

- testando : 

`tree .` 

```````
.
|____index.js
|____ios
| |____RNPacoteACriar.xcodeproj
| | |____project.pbxproj
| |____RNPacoteACriar.podspec
| |____RNPacoteACriar.m
| |____RNPacoteACriar.xcworkspace
| | |____contents.xcworkspacedata
| |____RNPacoteACriar.h
|____README.md
|____.gitignore
|____package.json
|____android
| |____build.gradle
| |____src
| | |____main
| | | |____AndroidManifest.xml
| | | |____java
| | | | |____br
| | | | | |____com
| | | | | | |____codificar
| | | | | | | |____pacote_a_criar
| | | | | | | | |____RNPacoteACriarPackage.java
| | | | | | | | |____RNPacoteACriarModule.java
|____.gitattributess
```````

## 2 Noções sobre a programação de um pacote.

É muito provável que as funções utilizadas no seu projeto usem recursos nativos, deverão então ser programadas tanto em iOS quanto em Android também. Por isso será necessário programar arquivos específicos. Irei nessa parte do documento enumerar e dar uma leve noção sobre esses arquivos. Arquivos estes necessários para se criar um módulo pacote tanto para android quanto ios. Não é o objetivo deste artigo explicar essa parte do processo em profundidade.

### 2.1. android

Estes são os arquivos que existem em um nativo Android que referenciamos para o código react-native.

**RNPacoteACriarPackage.java**: Uma forma de header e padrão para o código do react encontrar o seu código nativo. Importa os código react do facebook para encontrar o módulo, é a ponte que liga o js com o java.

**RNPacoteACriarModule.java**: Aonde que se programa tudo do projeto, coloca as funções, e organizar corretamente o código, importa o que vai usar etc.
### 2.2. ios

Estes são os arquivos que existem em um nativo ios que referenciamos para o código react-native:

**RNPacoteACriar.h**
Header do Projeto que possibilita de exportar e achar o package com o mecanismo que o react-native implementa, é a ponte que liga o js com o código iOS.

**RNPacoteACriar.m**
Onde se coloca os RCT_EXPORT e as funções códigos que se utilizam. No meu caso eu juntei o .h e o .m pois estava trabalhando com código de outros funcionários da empresa e swift. Vou falar mais sobre isso abaixo.
### 2.3. javascript

Estes são os arquivos que existem em um nativo *Android* que referenciamos para o código react-native:

**index.js** - O index funciona como a ponta que conecta todos os arquivos anteriores, quando você usar a função da biblioteca ele verifica aqui e usando a biblioteca do react procura nas bibliotecas nativas com nomes próximos que se remetem ao código normal.

## 3. Migrando o seu código para estrutura de um pacote.

Caso o código já exista é possível criar um pacote para ele, para isso seguiremos certos passos que serão narrados aqui.

### 3.1. Primeiros Passos

- Criar um projeto com o comando citado [acima](#1-criando-seu-pacote);
- Para a estrutura criada copiamos o código android, em main, java , etc ...
  - Copiar o Package, Module e Manager já existentes
  - Renomear as classes desses arquivos acima;
  - Renomear o pacote desses arquivos acima;
  - Refatorar trocando todos os locais que refere aos nomes antigos;
  - Copiar outros arquivos java que possam estar sendo utilizados;

- Para o index.js você usar o arquivo que funcionava de bridge no seu projeto;
  - Copie as copie as funções para index.js;
  - Renomear o nome do NativeModules para o nome novo gerado pelo pacote npm que usamos que cria;
  - Refatorar as referências antigas para os nomes novos;

- Para ios é melhor você criar um projeto do zero no Xcode
  - ir copiando códigos antigos com os nomes sugeridos pela ferramenta
  - recomendo começar ios apenas após acabar android.
  
### 3.2. Passos Intermediários e Dificuldades Encontradas

- Para testar e verificar o que falta usaremos os dois comandos abaixo. 

`npm install -- save <path to Library>`

`react-native link <module name created in node module>`

- Primeira dificuldade encontrada foi correlação entre o código, que é removido do projeto para modularizar, com outros funções e códigos nativos que podem acabar atrapalhando a modularização

- Verificar a compilação do código para testar e adicionar as dependências necessárias, em android eu encontrei esses arquivos em : `<path to project>/android/app/build.gradle` , é melhor ir brincando com isso e testando. Campo de dependencies { ... }, e lembrar de outras partes como o BUCK e as bibliotecas *libraries* de cada projeto. (CADA CASO é um CASO). Antes de renomear é melhor você fazer um backup com o que tem de iOS que é mais complicado, para o iOS temos que remover pelo XCode e criar pelo XCode devido algumas intrinsidades existentes em programar para iOS.
  
- As referências ao código que anterior fazia parte do projeto devem ser alteradas. Para isso você pode apenas ir mudando os imports para ter de refatorar menos código em seu projeto react-native ( não esquecer de fazer um ctrl+f e procurar em todo o seu projeto).
  - ao invés de `<path to project>/<path to code file.js>` para `<path to project>/<library name registred in node_modules>`, isso é possível com os códigos citados acima.


#### 3.2.1 Android

- Uso de recursos do Projeto que vai usar a biblioteca de código no android. Isso é uma das dificuldades que é provável que você vai encontrar. Nesses casos você tem que pegar os recursos por contexto. Ex: `context.getResources().getIdentifier("app_id", "drawable", context.getPackageName());` e `Context context = getApplicationContext();`.

- O projeto também precisava de abrir atividade do background. E para isso era necessário acessar código fonte do nativo android do projeto, para isso pegamos o nome do pacote `String here = getApplicationContext().getPackageName();` e com o nome do pacote pegamos a classe `Class.forName(here + ".MainActivity")` e com ela chamei o intent `Intent mainIntent = new Intent(this, Class.forName(here + ".MainActivity"));` e com esse acesso ao intent fazemos as outras operações.

- Certos códigos no android precisam de ser referenciados pelo Manifest, no caso do que eu fiz, eu usei um serviço e precisei de adicionar o mesmo. `<service android:name="package.name.Service" />`

- Os caminhos e paths devem sempre ser coerentes com package, caso contrário pode prejudicar achar classes e compilar.
#### 3.2.2 iOS

- Em ios na hora de remover o código do projeto que já possuía ele, deu muito erro com o Auth Mach-O Linker, foi deletando o que podia pelo xcode e depois `pod deintegrate` e `pod install`, e dando *clean* no projeto, até corrigir o que precisava, além de também usar o `react-native link`.

- Como era usado código swift no nativo de ios foi necessário usar de uma biblioteca chamada *react-native-swift* e rodar `react-native swiftify` to link correctly to ios.

- E também, por usar swift como mencionado acima, tive de alterar o podspec para adicionar o podfile para o local do código considerando raiz da pasta do package e colocar os arquivos .h,.m e swift,   `s.source_files  = "ios/**/*.{h,m,swift}"`.
  
- Não esquecer de colocar `package = JSON.parse(File.read(File.join(__dir__, 'package.json')))` no início do podspec.

- Mudar os podfiles do que você está usando para o podspec da biblioteca ao invés do podfile.
### 3.3. Dicas

- Esse tipo de migração toma tempo e requer que o projeto seja homologado, faça isso com paciência e espaço para erros e experimentação.
  
- Use um IDE de preferência o Android Studio que vai ajudar a importar o que faltar em mudanças.
  
- É uma boa oportunidade para testar o seu projeto e verificar falhas , testar em versões antigas de android e apis. Afinal, grande parte dos usuários não usam sistemas mais recentes.
  
- Prioritariamente é melhor que seja documentado seu código. Afinal, não adianta você ter um código sem comentários e explicações para reutilizar e compartilhar em outros projetos. Se as pessoas não entenderem o seu código podem acabar se quebrando ele, então talvez seja melhor comentar o código primeiro.
  
- Procure uma biblioteca que tenha código nativo e estude um pouquinho como ela faz X,Y e Z. Afinal, uma base comparativa sempre ajuda.

- Aprender a verificar se foi carregado corretamente os códigos da sua biblioteca para ir testando e debugando, no Android Studio é de um jeito e no XCode em outro. Pasta Development Pods no Xcode e no Android Studio nas pastas na raiz perto de app quando se abre tem tudo.
  
## 1.4. Publicando no npm.

Depois que o código tenha sido redigido , se deve subir ele para o github, usaremos o endereço desse git do código quando for publicar o componente no npm.

Se você não tiver nenhuma noção de git , recomendo ler um tutorial básico, ou ver algum tutorial na internet. 

Após criado o repositório executar as seguintes instruções para sincronizar o código com o repositório do github:

`git add .` </br>
`git commit -a -m "initial commit"` </br>
`git push -u origin master`

Caso seja sucedido sem nenhum erro ficará atualizado o código no seu repositório git.

### 4.1. Publicando componente
Depois de desenvolvido e subido ao github, se você desejar instalá-los e usá-los em outros projetos ( ou para que outros instalem ou usem ) ,que é o real intuito de se fazer isso, seus componentes devem ser publicados no registro de componentes do npm : *npm registry*.

### 4.2. npm registry

O que é o *npm registry* ?

Seria o equivalente de um centro administrativo de registro de pacotes. Ele gerencia diversos plugins de incontáveis desenvolvedores ao redor do mundo para que esses pacotes sejam instalados pelo npm install. Pensa nisso como um cartório usado para concentrar todos os dados de pacotes , kkk , ou não também. Precisou de saber algo ou ter acesso a um documento ( pacote ) que deve consultar no cartório. É próximo mesmo que não seja a mesma coisa.

O site oficial do registro: http://registry.npmjs.org/

Observe você consegue ver qual registro está usando em seu pc com o comando:

`npm config get registry`

#### 4.2.2. Criar/logar em sua conta do npm registry

Para publicar o seu componente no npm registry, você deve ser um usuário registrado do npm registry.

Para adicionar um novo usuário:

`npm adduser`

Ou se você já possui um usuário, logar no registro npm com:

`npm login`

Formas de verificar se está logado e está tudo ok.

confirmar que logou, comando: `npm whoami` 

Abra em seu browser https://npmjs.com/~username para averiguar o registro/criação do novo usuário.

### 4.3. Preparar para o Lançamento

#### 4.3.1. gitignore e .npmignore

Defina quais arquivos não são desejáveis de serem mantidos/atualizado/subido no github no seu .gitignore

Defina quais arquivos não são desejáveis de serem empacotados quando publicados em .npmignore

### 4.4. package.json

O arquivo package.json define todas as informações de seu pacote publicado, incluindo informações como: nome, versão, autores, descrição, dependências, e mais.

Tudo específico para funcionamento de seu projeto será referido em seu package.json

exemplo do que foi criado neste tutorial:

```````package.json
{
  "name": "react-native-pacote-a-criar",
  "version": "1.0.0",
  "description": "",
  "main": "index.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "keywords": [
    "react-native"
  ],
  "author": "",
  "license": "",
  "peerDependencies": {
    "react-native": "^0.41.2"
  }
}
```````

Obviamente é um pacote bem vazio, afinal nada foi programado no exemplo de como criar um pacote mais acima.

### 4.5. escrevendo o README.md

O README.md é o arquivo que funciona como documento explicando como o pacote deve ser utilizado, qual seu uso, explicar as coisas triviais para o uso de leigos.

## 5. Lançamento

Assim que estiver tudo pronto já se pode publicar.
As atualizações são diferentes da primeira publicação observada abaixo.

### 5.1. Primeiro Lançamento

Na publicação execute:

`npm publish`

Pronto. Verifique online para checar se deu certo. 
Acesse o link (<package> é o nome do pacote publicado):
https://www.npmjs.com/package/<package>

### 5.2. Atualizando publicação

Será preciso executar 2 comandos:

`npm version <update_type>`</br>
`npm publish`

| update_type | scene                                                                                                                               | Version Number Rule                 | Give an example      |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------- | -------------------- |
| -           | First                                                                                                                               | Release                             | Version number 1.0.0 | 1.0.0 |
| path        | Quando corrigindo bugs, mudanças menores                                                                                            | Incrementando o 3º número da versão | 1.0.0 -> 1.0.1       |
| minor       | Quando adicionando novas funcionalidades que não impactam funções existentes usando o pacote                                        | Incrementando o 2º número da versão | 1.0.3 -> 1.1.3       |
| major       | Quando adicionando muitas novas funcionalidades online que impactam e podem estragar projetos que usem funções existentes do pacote | Incrementando o 1º número da versão | 1.0.3 -> 2.0.0       |

#### 5.2.1 Observação

Use 1.0.0 como a primeira versão publicada para evitar conflitos.

## Referências: 
- https://programmer.group/develop-your-own-react-native-component-and-publish-it-to-npm.html
- https://medium.com/wix-engineering/creating-a-native-module-in-react-native-93bab0123e46
- https://www.npmjs.com/package/create-react-native-app
- https://codificar.com.br/
- Stack Overflow
- E o chefe Raphael Cangucu
