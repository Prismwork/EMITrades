package moe.prwk.emitrades;

import com.mojang.logging.LogUtils;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

@EmiEntrypoint
public class EMITradesPlugin implements EmiPlugin {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final EmiRecipeCategory VILLAGER_TRADES
            = new EmiRecipeCategory(ResourceLocation.parse("emitrades:villager_trades"), EmiStack.of(Items.EMERALD));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(VILLAGER_TRADES);
    }
}
