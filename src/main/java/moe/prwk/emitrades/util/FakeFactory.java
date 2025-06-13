package moe.prwk.emitrades.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class FakeFactory implements VillagerTrades.ItemListing {
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
