package committee.nova.firesafety.common.tools.reference;

import committee.nova.firesafety.FireSafety;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagKeyReference {
    public static final TagKey<EntityType<?>> UNDETECTABLE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(FireSafety.MODID, "undetectable"));
    public static final TagKey<EntityType<?>> IGNORED = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(FireSafety.MODID, "ignored"));
    public static final TagKey<EntityType<?>> BURNING = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(FireSafety.MODID, "burning"));
    public static final TagKey<Block> FDS_IGNORED = BlockTags.create(new ResourceLocation(FireSafety.MODID, "fds_ignored"));
    public static final TagKey<Fluid> FIREFIGHTING = FluidTags.create(new ResourceLocation(FireSafety.MODID, "firefighting"));
}
