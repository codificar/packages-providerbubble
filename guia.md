

# Crie sua própria blibioteca/componente react-native e publicar no npm.

Objetivo desse artigo é mostrar o básico necessário para se criar o pacote e como transferir migrar um código de um projeto para um pacote de forma que ele possa ser usa de forma modular em outros projetos pessoais ou de uma empresa. 

Este artigo foi criado para documentar o processo de migração de um código de um projeto da codificar para um pacote para ser reutilizado. O intuito é de se compartilhar internamente com a equipe da codificar e externamente para agregar ao ecossistema desenvolvedor.

Cobriremos esse artigo em português com intuito de se ensinar esse processo que se acha com maior facilidade em ingles e não em pt-br.

#### pré-requisitos

- ter *node* instalado

`curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.36.0/install.sh | bash`

`nvm install node`

- um conhecimento básico em *swift*, *android* e *javascript*

#### Indice
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

- instalar global para uso por linha de comando da blibioteca:

`npm install -g react-native-create-library`

- use ou crie uma pasta para se organizar 

`mkdir pacotes && cd pacotes`

- rodar o comando para criar o pacote , lembra de criar corretamente o identificador do seu aplicativo, visto que este precisa de ser único.

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

é muito provável que as funções utilizadas no seu projeto usem recursos nativos, deverão então ser programadas tanto em ios quanto em android também. Por isso será necessário programar arquivos especificos. Irei nessa parte do documento enumerar e dar uma leve noção sobre esses arquivos. Arquivos estes necessários para se criar um modulo pacote tanto para android quanto ios. Não é o objetivo desse artigo explicar essa parte do processo em profundidade.

### 2.1. android

RNPacoteACriarPackage.java
RNPacoteACriarModule.java
RNPacoteACriarManager.java

### 2.2. ios

RNPacoteACriar.h
RNPacoteACriar.m

### 2.3. javascript

index.js

## 3. Migrando o seu código para estrutura de um pacote.

### 3.1. Primeiros Passos

### 3.2. Dificuldades

### 3.3. Dicas

## 1.4. Publicando no npm.

Depois que o código tenha sido redigido , se deve subir ele para o github, usaremos o endereço desse git do código quando for publicar o componente no npm.

Se você não tiver nenhuma noção de git , recomendo ler um tutorial básico, ou ver algum tutorial na internet. 

Após criado o repositório executar as seguintes instruções para sincronizar o código com o repositório do github:

`git add .` </br>
`git commit -a -m "initial commit"` </br>
`git push -u origin master`

Caso sejá sucedido sem nenhum erro ficara atualizado o código no seu repositorio git.

### 4.1. Publicando componente
Depois de desenvolvido e subido ao github, se você desejar instala-los e usa-los em outros projetos ( ou para que outros instalem ou usem ) ,que é o real intuito de se fazer isso, seus componentes devem ser publicados no registro de componentes do npm : *npm registry*.

### 4.2. npm registry

O que é o *npm registry* ?

Seria o equivalemente de um centro administrativo de registro de pacotes. Ele gerencia diversos plugins de incontaveis desenvolvedor ao redor do mundo para que esses pacotes sejam instalados pelo npm install. Pensa nisso como um cartório usado para concentrar todos os dados de pacotes , kkk , ou não também. Precisou de saber algo ou ter acesso a um documento ( pacote ) deve consultar no cartório. É próximo mesmo que não sejá a mesma coisa.

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

Comando: `npm whoami` confirmar que logou.

Abra em seu browser https://npmjs.com/~username para averiguar o registro/criação do novo usuário.

### 4.3. Preparar para o Lançamento

#### 4.3.1. gitignore e .npmignore

Defina quais arquivos não são desejaveis de serem mantidos/atualizado/subido no github no seu .gitignore

Defina quais arquivos não são desejaveis de serem empacotados quando publicados em .npmignore

### 4.4. package.json

O arquivo package.json define todas a informações de seu pacote publicado, incluindo informações como: nome, versão, autores, descrição, dependencias, e mais.

Tudo especifico para funcionamento de seu projeto será referido em seu package.json

exemplo do que foi criado nesse tutorial:

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
As atualizações são diferentes da primeira publicação observe abaixo.

### 5.1. Primeiro Lançamento

Na publicação execute:

`npm publish`

Pronto. Verifique online para checar se deu certo. 
Acesse o link (<package> é o nome do pacote publicado):
https://www.npmjs.com/package/<package>

### 5.2. Atualizando publicação

Será preciso executar 2 comandos:

`npm version <update_type>`
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