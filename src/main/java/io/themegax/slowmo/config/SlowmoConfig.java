package io.themegax.slowmo.config;

import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigEntries;
import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.data.Config;

@SuppressWarnings("all")
@ConfigEntries(includeAll = true)
public final class SlowmoConfig extends Config implements ConfigContainer {
    @ConfigEntry.BoundedInteger(min = 0, max = 2)
    @ConfigEntry.Slider
    private static int permissionLevel = 2;

    @ConfigEntry.BoundedInteger(min = 10, max = 1000)
    @ConfigEntry.Slider
    private static int maxClientTicks = 100;

    public static boolean tickrateCommands = true;

    public static boolean changeSound = true;

    public static boolean doClampPitch = true;

    @ConfigEntry.BoundedInteger(min = 1, max = 200)
    @ConfigEntry.Slider
    private static float pitchPercentage = 50f;

    public static boolean colorSaturation = true;

    @ConfigEntry.BoundedFloat(min = 1f, max = 10000f)
    @ConfigEntry.Slider
    private static float fadeTimeMillis = 2000f;

    public static boolean keepTickrateOnDeath = true;

    public static int getPermissionLevel() {
        return permissionLevel;
    }

    public static int getMaxClientTicks() {
        return maxClientTicks;
    }

    public static float getPitchPercentage(){
        return pitchPercentage/100;
    }

    public static float getFadeTimeMillis() {
        return fadeTimeMillis;
    }

    public SlowmoConfig(String modId) {
        super(modId);
    }
}
