package io.github.pkstdev.emitrades.util;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

public interface ITradeProfile {
    TradeOffers.Factory offer();

    VillagerProfession profession();

    int level();

    MerchantEntity villager();
}
