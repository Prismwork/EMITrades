package io.github.prismwork.emitrades.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

// TODO: internal rework for less boilerplate
public interface TradeProfile {
    Int2ObjectMap<TradeOffers.Factory[]> offers();

    TradeOffers.Factory defaultOffer();

    VillagerProfession profession();

    int level();

    MerchantEntity villager();

    record DefaultImpl(VillagerProfession profession,
                       TradeOffers.Factory offer,
                       int level,
                       MerchantEntity villager) implements TradeProfile {
        @Override
        public Int2ObjectMap<TradeOffers.Factory[]> offers() {
            throw new UnsupportedOperationException();
        }

        public TradeOffers.Factory defaultOffer() {
            return offer;
        }
    }

    record ProfessionImpl(VillagerProfession profession,
                          Int2ObjectMap<TradeOffers.Factory[]> offers,
                          MerchantEntity villager) implements TradeProfile {
        @Override
        public TradeOffers.Factory defaultOffer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int level() {
            throw new UnsupportedOperationException();
        }
    }
}
