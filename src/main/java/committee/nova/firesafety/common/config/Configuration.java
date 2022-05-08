package committee.nova.firesafety.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Configuration {
    public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.IntValue fireAlarmMonitoringWidth;
    public static final ForgeConfigSpec.IntValue fireAlarmMonitoringHeight;
    public static final ForgeConfigSpec.DoubleValue blockExtinguishingPossibility;
    public static final ForgeConfigSpec.DoubleValue entityExtinguishingPossibility;
    public static final ForgeConfigSpec.IntValue waterConsumption;
    public static final ForgeConfigSpec.DoubleValue freezeDamage;
    public static final ForgeConfigSpec.IntValue extinguishDelay;
    public static final ForgeConfigSpec COMMON_CONFIG;

    static {
        builder.comment("FireSafety Configuration");
        fireAlarmMonitoringWidth = builder.comment("The width of the fire alarm monitoring area = 2 * (the value) + 1")
                .defineInRange("fireAlarmMonitoringWidth", 3, 1, 50);
        fireAlarmMonitoringHeight = builder.comment("The height of the fire alarm monitoring area")
                .defineInRange("fireAlarmMonitoringHeight", 10, 1, 384);
        blockExtinguishingPossibility = builder.comment("The possibility of extinguishing the flame on a block with each spray of water from a fire extinguisher", "Default is 55%")
                .defineInRange("blockExtinguishingPossibility", .75, 0, 1.0);
        entityExtinguishingPossibility = builder.comment("The possibility of extinguishing the flame on an entity with each spray of water from a fire extinguisher", "Default is 55%")
                .defineInRange("entityExtinguishingPossibility", .75, 0, 1.0);
        waterConsumption = builder.comment("The water consumption of each spray from a fire extinguisher", "Default is 50mB")
                .defineInRange("waterConsumption", 50, 1, 1000);
        freezeDamage = builder.comment("The freeze damage per spray on the \"burning\" tagged entity")
                .defineInRange("freezeDamage", 5.0, 0, 100);
        extinguishDelay = builder.comment("After how many tick the fire started can the extinguisher try to extinguish")
                .defineInRange("extinguishDelay", 25, 0, 120);
        COMMON_CONFIG = builder.build();
    }
}
