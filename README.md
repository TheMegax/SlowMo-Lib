# SlowMo Lib

A Minecraft Fabric library to control time!

## Setup
Put this on build.gradle
```groovy
repositories {
    mavenCentral()

    maven {
        url 'https://jitpack.io'
    }
    maven {
        url "https://maven.terraformersmc.com/"
    }
    maven {
        url "https://maven.shedaniel.me/"
    }
    
}

dependencies {
    (...)
    modImplementation "io.github.themegax:SlowMo-Lib:${project.slowmolib_version}" // Required
    include "io.github.themegax:SlowMo-Lib:${project.slowmolib_version}" //Optional jar-in-jar
}
```

You can find the version number {here}

## How to use the API

```groovy

```