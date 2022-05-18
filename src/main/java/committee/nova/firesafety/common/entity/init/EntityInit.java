package committee.nova.firesafety.common.entity.init;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.common.entity.impl.projectile.WaterBombProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import static committee.nova.firesafety.common.tools.reference.EntityReference.WATER_BOMB;
import static committee.nova.firesafety.common.util.RegistryHandler.ENTITIES;
import static committee.nova.firesafety.common.util.RegistryHandler.debug;

public class EntityInit {
    public static final RegistryObject<EntityType<WaterBombProjectile>> waterBomb = ENTITIES.register(WATER_BOMB, () -> EntityType.Builder.of((EntityType<WaterBombProjectile> e, Level l) -> new WaterBombProjectile(e, l), MobCategory.MISC)
            .sized(0.25f, 0.25f).clientTrackingRange(10).fireImmune().build(FireSafety.MODID + "." + WATER_BOMB));

    public static void init() {
        debug("entities");
    }
}
