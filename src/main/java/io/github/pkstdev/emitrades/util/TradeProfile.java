package io.github.pkstdev.emitrades.util;

import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

public record TradeProfile(VillagerProfession profession,
                           TradeOffers.Factory offer,
                           int level) {
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
}
