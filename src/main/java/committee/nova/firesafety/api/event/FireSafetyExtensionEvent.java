package committee.nova.firesafety.api.event;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.api.FireSafetyApi;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;

public class FireSafetyExtensionEvent extends Event {
    private final HashMap<Short, FireSafetyApi.ExtinguishableBlock> extinguishableBlockList;
    private final HashMap<Short, FireSafetyApi.ExtinguishableEntity> extinguishableEntityList;
    private final HashMap<Short, FireSafetyApi.FireFightingWaterContainerItem> firefightingWaterContainerList;

    public FireSafetyExtensionEvent() {
        extinguishableBlockList = new HashMap<>();
        extinguishableEntityList = new HashMap<>();
        firefightingWaterContainerList = new HashMap<>();
    }

    public void addExtinguishable(short priority, FireSafetyApi.ExtinguishableBlock extinguishable) {
        if (extinguishableBlockList.containsKey(priority)) {
            FireSafety.LOGGER.warn("Duplicate priority value {}, new extinguishable block won't be added!", priority);
            return;
        }
        extinguishableBlockList.put(priority, extinguishable);
        FireSafety.LOGGER.info("Adding new extinguishable block with priority {}!", priority);
    }

    public void addExtinguishable(short priority, FireSafetyApi.ExtinguishableEntity extinguishable) {
        if (extinguishableEntityList.containsKey(priority)) {
            FireSafety.LOGGER.warn("Duplicate priority value {}, new extinguishable entity won't be added!", priority);
            return;
        }
        extinguishableEntityList.put(priority, extinguishable);
        FireSafety.LOGGER.info("Adding new extinguishable entity with priority {}!", priority);
    }

    public void addFireFightingWaterItem(short priority, FireSafetyApi.FireFightingWaterContainerItem container) {
        if (firefightingWaterContainerList.containsKey(priority)) {
            FireSafety.LOGGER.warn("Duplicate priority value {}, new firefighting container won't be added!", priority);
            return;
        }
        firefightingWaterContainerList.put(priority, container);
        FireSafety.LOGGER.info("Adding new firefighting container with priority {}!", priority);
    }

    public HashMap<Short, FireSafetyApi.FireFightingWaterContainerItem> getFirefightingWaterContainerList() {
        return firefightingWaterContainerList;
    }

    public HashMap<Short, FireSafetyApi.ExtinguishableEntity> getExtinguishableEntityList() {
        return extinguishableEntityList;
    }

    public HashMap<Short, FireSafetyApi.ExtinguishableBlock> getExtinguishableBlockList() {
        return extinguishableBlockList;
    }
}
