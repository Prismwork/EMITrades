package moe.prwk.emitrades.recipes;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

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

    static Trade impl(AbstractVillager merchant,
                      VillagerProfession profession,
                      VillagerTrades.ItemListing offer,
                      int level) {
        return impl(merchant, profession, Map.of(0, new VillagerTrades.ItemListing[]{offer}), level);
    }

    static Trade impl(AbstractVillager merchant,
                      VillagerProfession profession,
                      Map<Integer, VillagerTrades.ItemListing[]> offers,
                      int level) {
        return new Impl(merchant, profession, offers, level);
    }

    record Impl(AbstractVillager merchant,
                VillagerProfession profession,
                Map<Integer, VillagerTrades.ItemListing[]> offers,
                int level) implements Trade {}

    @ApiStatus.Internal
    VillagerProfession WANDERING_TRADER = new VillagerProfession(
            "wandering_trader",
            entry -> false,
            entry -> false,
            ImmutableSet.<Item>builder().build(),
            ImmutableSet.<Block>builder().build(),
            SoundEvents.WANDERING_TRADER_YES
    );

    @ApiStatus.Internal
    final class FakeFactory implements VillagerTrades.ItemListing {
        public final ItemStack first;
        public final ItemStack second;
        public final ItemStack sell;
        public final int maxUses;
        public final int villagerXp;

        public FakeFactory(MerchantOffer offer) {
            first = offer.getBaseCostA();
            second = offer.getCostB();
            sell = offer.getResult();
            maxUses = offer.getMaxUses();
            villagerXp = offer.getXp();
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
            throw new AssertionError("This should never arise");
        }
    }
}
