package committee.nova.firesafety.common.tools.reference;

import committee.nova.firesafety.FireSafety;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagKeyReference {
    public static final TagKey<EntityType<?>> UNDETECTABLE = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(FireSafety.MODID, "undetectable"));
    public static final TagKey<EntityType<?>> IGNORED = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(FireSafety.MODID, "ignored"));
    public static final TagKey<EntityType<?>> BURNING = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(FireSafety.MODID, "burning"));
    public static final TagKey<Block> FDS_IGNORED = BlockTags.create(new ResourceLocation(FireSafety.MODID, "fds_ignored"));
    public static final TagKey<Fluid> FIREFIGHTING = FluidTags.create(new ResourceLocation(FireSafety.MODID, "firefighting"));
}
