package moe.prwk.emitrades.recipes;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import moe.prwk.emitrades.EMITradesPlugin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTradeEntry
        implements EmiRecipe {
    protected final Trade trade;
    protected final ResourceLocation id;

    public AbstractTradeEntry(Trade trade, ResourceLocation id) {
        this.trade = trade;
        this.id = id;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMITradesPlugin.VILLAGER_TRADES;
    }
}
