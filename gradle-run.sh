#!/bin/bash

# Definir a versão do Gradle desejada
GRADLE_VERSION=7.6
GRADLE_HOME="./gradle"

# Verificar se o Gradle já está presente
if [ ! -d "$GRADLE_HOME" ]; then
  echo "Gradle não encontrado. Baixando..."
  mkdir -p "$GRADLE_HOME"
  wget https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip -P $GRADLE_HOME
  unzip $GRADLE_HOME/gradle-$GRADLE_VERSION-bin.zip -d $GRADLE_HOME
  rm $GRADLE_HOME/gradle-$GRADLE_VERSION-bin.zip
  echo "Gradle $GRADLE_VERSION baixado."
fi

# Executar o Gradle
$GRADLE_HOME/gradle-$GRADLE_VERSION/bin/gradle $@
