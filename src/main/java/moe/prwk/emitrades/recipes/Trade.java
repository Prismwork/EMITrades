package moe.prwk.emitrades.recipes;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/**
 * Core data class of the mod.<p>
 * May be representing a single trade or a set of trades of a specific
 * {@link VillagerProfession}, which is determined by the number of
 * entries in {@link Trade#offers()}.
 */
public interface Trade {
    AbstractVillager merchant();

    VillagerProfession profession();

    Map<Integer, VillagerTrades.ItemListing[]> offers();

    int level();

    record Impl(AbstractVillager merchant,
                VillagerProfession profession,
                Map<Integer, VillagerTrades.ItemListing[]> offers,
                int level) implements Trade {}

    VillagerProfession WANDERING_TRADER = new VillagerProfession(
            "wandering_trader",
            entry -> false,
            entry -> false,
            ImmutableSet.<Item>builder().build(),
            ImmutableSet.<Block>builder().build(),
            SoundEvents.WANDERING_TRADER_YES
    );
}
