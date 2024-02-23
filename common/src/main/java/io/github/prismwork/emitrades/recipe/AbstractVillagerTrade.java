package io.github.prismwork.emitrades.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import io.github.prismwork.emitrades.EMITradesPlugin;
import io.github.prismwork.emitrades.util.TradeProfile;

public abstract class AbstractVillagerTrade implements EmiRecipe {
    protected final TradeProfile profile;

    public AbstractVillagerTrade(TradeProfile profile) {
        this.profile = profile;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMITradesPlugin.VILLAGER_TRADES;
    }
}
