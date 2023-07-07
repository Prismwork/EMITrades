package io.github.prismwork.emitrades.util;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

public interface TradeProfile {
    TradeOffers.Factory offer();

    VillagerProfession profession();

    int level();

    MerchantEntity villager();

    record DefaultImpl(VillagerProfession profession,
                       TradeOffers.Factory offer,
                       int level,
                       MerchantEntity villager) implements TradeProfile {
        @Override
        public TradeOffers.Factory offer() {
            return offer;
        }

        @Override
        public VillagerProfession profession() {
            return profession;
        }

        @Override
        public int level() {
            return level;
        }

        @Override
        public MerchantEntity villager() {
            return villager;
        }
    }
}
