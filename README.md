# SlowMo Lib
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/io.github.themegax/slowmo-lib?nexusVersion=2&server=https%3A%2F%2Fs01.oss.sonatype.org)

A Minecraft Fabric library to control time!

## Setup

**`build.gradle`**
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
    maven {
        name = 'Ladysnake Mods'
        url = 'https://ladysnake.jfrog.io/artifactory/mods'
        content {
            includeGroup 'io.github.ladysnake'
            includeGroupByRegex 'io\\.github\\.onyxstudios.*'
        }
    }
    
}

dependencies {
    (...)
    modImplementation "io.github.themegax:slowmo-lib:${project.slowmolib_version}" // Required
    include "io.github.themegax:slowMo-lib:${project.slowmolib_version}" //Optional jar-in-jar
}
```

**`gradle.properties`**
```groovy
# Dependencies
	slowmolib_version = 1.0.5+1.19
```

You can find the latest version number on the shield on top of the page.

## How to use the API

**Usage example**
```groovy
(...)
import io.themegax.slowmo.api.TickrateApi;

public class ExampleItem extends Item {
	final static float DEFAULT_TICKRATE = TickrateApi.getDefaultTickrate();
	
	public ExampleItem(Settings settings) {  
	   super(settings);  
	}
	
	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (user instanceof PlayerEntity player) {
			MinecraftServer server = world.getServer();
			float serverTickrate = TickrateApi.getServerTickrate(MinecraftServer server);
			float playerTickrate = TickrateApi.getPlayerTickrate(player);
			
			if (getServerTickrate == DEFAULT_TICKRATE) {
				TickrateApi.setServerTickrate(serverTickrate*1.5f, server);
				TickrateApi.setPlayerTickrate(playerTickrate*1.5f, player);
			}
			else {
				TickrateApi.setServerTickrate(DEFAULT_TICKRATE, server);
				TickrateApi.setPlayerTickrate(DEFAULT_TICKRATE, player);
			}
		}
	}
}
```
A practical implementation of this API is found [here](https://github.com/TheMegax/Chronos-Mod).
