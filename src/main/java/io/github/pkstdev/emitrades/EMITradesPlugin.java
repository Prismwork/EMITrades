package io.github.pkstdev.emitrades;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import io.github.pkstdev.emitrades.recipe.VillagerTrade;
import io.github.pkstdev.emitrades.util.TradeProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

public class EMITradesPlugin implements EmiPlugin {
    public static final EmiRecipeCategory VILLAGER_TRADES
            = new EmiRecipeCategory(new Identifier("emitrades", "villager_trades"), EmiStack.of(Items.EMERALD));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(VILLAGER_TRADES);
        for (VillagerProfession profession : Registry.VILLAGER_PROFESSION) {
            int id = 0;
            Int2ObjectMap<TradeOffers.Factory[]> offers = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(profession);
            if (offers == null || offers.isEmpty()) continue;
            int level = 0;
            while (level < 5) {
                for (TradeOffers.Factory offer : offers.get(level + 1)) {
                    registry.addRecipe(new VillagerTrade(new TradeProfile(profession, offer, level + 1), id));
                    id++;
                }
                level++;
            }
        }
    }
}
