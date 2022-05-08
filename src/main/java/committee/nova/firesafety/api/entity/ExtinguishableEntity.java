package committee.nova.firesafety.api.entity;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Predicate;

public record ExtinguishableEntity(@Nonnull Predicate<Entity> entityCondition, @Nonnull Consumer<Entity> entityAction) {
}
