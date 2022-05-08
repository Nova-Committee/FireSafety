package committee.nova.firesafety.api.entity;

import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;
import java.util.function.Predicate;

public record ExtinguishableEntity(Predicate<Entity> entityCondition, Consumer<Entity> entityAction) {
}
