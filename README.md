# SlowMo Lib

A Minecraft Fabric library to control time!

## Setup
Gradle Setup:
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
    modImplementation "io.github.themegax:SlowMo-Lib:${project.slowmolib_version}"
    include "io.github.themegax:SlowMo-Lib:${project.slowmolib_version}"
}

You can find the version number {here}
```

## How to Use
