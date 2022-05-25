package committee.nova.firesafety.api.event;

import committee.nova.firesafety.api.FireSafetyApi;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;

import static committee.nova.firesafety.FireSafety.LOGGER;

public class FireSafetyExtensionEvent extends Event {
    private final HashMap<Short, FireSafetyApi.ExtinguishableBlock> extinguishableBlockList;
    private final HashMap<Short, FireSafetyApi.ExtinguishableEntity> extinguishableEntityList;
    private final HashMap<Short, FireSafetyApi.FireFightingWaterContainerItem> firefightingWaterContainerList;
    private final HashMap<Short, FireSafetyApi.FireDangerBlock> fireDangerBlockList;
    private final HashMap<Short, FireSafetyApi.FireDangerEntity> fireDangerEntityList;

    public FireSafetyExtensionEvent() {
        extinguishableBlockList = new HashMap<>();
        extinguishableEntityList = new HashMap<>();
        firefightingWaterContainerList = new HashMap<>();
        fireDangerBlockList = new HashMap<>();
        fireDangerEntityList = new HashMap<>();
    }

    public void addExtinguishable(short priority, FireSafetyApi.ExtinguishableBlock extinguishable) {
        if (extinguishableBlockList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new extinguishable block won't be added!", priority);
            return;
        }
        extinguishableBlockList.put(priority, extinguishable);
        LOGGER.info("Adding new extinguishable block with priority {}!", priority);
    }

    public void addExtinguishable(short priority, FireSafetyApi.ExtinguishableEntity extinguishable) {
        if (extinguishableEntityList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new extinguishable entity won't be added!", priority);
            return;
        }
        extinguishableEntityList.put(priority, extinguishable);
        LOGGER.info("Adding new extinguishable entity with priority {}!", priority);
    }

    public void addFireFightingWaterItem(short priority, FireSafetyApi.FireFightingWaterContainerItem container) {
        if (firefightingWaterContainerList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new firefighting container won't be added!", priority);
            return;
        }
        firefightingWaterContainerList.put(priority, container);
        LOGGER.info("Adding new firefighting container with priority {}!", priority);
    }

    public void addFireDanger(short priority, FireSafetyApi.FireDangerBlock danger) {
        if (fireDangerBlockList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new fire danger block won't be added!", priority);
            return;
        }
        fireDangerBlockList.put(priority, danger);
        LOGGER.info("Adding new fire danger block with priority {}!", priority);
    }

    public void addFireDanger(short priority, FireSafetyApi.FireDangerEntity danger) {
        if (fireDangerEntityList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new fire danger entity won't be added!", priority);
            return;
        }
        fireDangerEntityList.put(priority, danger);
        LOGGER.info("Adding new fire danger entity with priority {}!", priority);
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

    public HashMap<Short, FireSafetyApi.FireDangerBlock> getFireDangerBlockList() {
        return fireDangerBlockList;
    }

    public HashMap<Short, FireSafetyApi.FireDangerEntity> getFireDangerEntityList() {
        return fireDangerEntityList;
    }
}
