package io.themegax.slowmo;

import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigEntries;
import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.data.Config;

@ConfigEntries
public final class SlowmoConfig extends Config implements ConfigContainer {
    @ConfigEntry.BoundedInteger(min = 0, max = 2)
    @ConfigEntry.Slider
    private static int permissionLevel = 2;

    @ConfigEntry.BoundedInteger(min = 20, max = 1000)
    @ConfigEntry.Slider
    private static int maxClientTicks = 100;

    public static boolean tickrateCommands = true;

    public static boolean changeSound = true;

    public static int getPermissionLevel() {
        return permissionLevel;
    }

    public static int getMaxClientTicks() {
        return maxClientTicks;
    }


    public SlowmoConfig(String modId) {
        super(modId);
    }
}
