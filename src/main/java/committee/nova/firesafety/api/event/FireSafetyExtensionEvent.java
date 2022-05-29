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

    public void addExtinguishable(String modName, short priority, FireSafetyApi.ExtinguishableBlock extinguishable) {
        if (extinguishableBlockList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new extinguishable block by {} won't be added!", priority, modName);
            return;
        }
        extinguishableBlockList.put(priority, extinguishable);
        LOGGER.info("Adding new extinguishable block by {} with priority {}!", modName, priority);
    }

    public void addExtinguishable(String modName, short priority, FireSafetyApi.ExtinguishableEntity extinguishable) {
        if (extinguishableEntityList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new extinguishable entity by {} won't be added!", priority, modName);
            return;
        }
        extinguishableEntityList.put(priority, extinguishable);
        LOGGER.info("Adding new extinguishable entity by {} with priority {}!", modName, priority);
    }

    public void addFireFightingWaterItem(String modName, short priority, FireSafetyApi.FireFightingWaterContainerItem container) {
        if (firefightingWaterContainerList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new firefighting container by {} won't be added!", priority, modName);
            return;
        }
        firefightingWaterContainerList.put(priority, container);
        LOGGER.info("Adding new firefighting container by {} with priority {}!", modName, priority);
    }

    public void addFireDanger(String modName, short priority, FireSafetyApi.FireDangerBlock danger) {
        if (fireDangerBlockList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new fire danger block by {} won't be added!", priority, modName);
            return;
        }
        fireDangerBlockList.put(priority, danger);
        LOGGER.info("Adding new fire danger block by {} with priority {}!", modName, priority);
    }

    public void addFireDanger(String modName, short priority, FireSafetyApi.FireDangerEntity danger) {
        if (fireDangerEntityList.containsKey(priority)) {
            LOGGER.warn("Duplicate priority value {}, new fire danger entity by {} won't be added!", priority, modName);
            return;
        }
        fireDangerEntityList.put(priority, danger);
        LOGGER.info("Adding new fire danger entity by {} with priority {}!", modName, priority);
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
