package io.github.prismwork.emitrades.recipe;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.prismwork.emitrades.util.TradeProfile;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// TODO: idk what to write here, its just a todo
public class ProfessionTrades extends AbstractVillagerTrade {
    public ProfessionTrades(TradeProfile profile) {
        super(profile);
    }

    @Override
    public @Nullable Identifier getId() {
        return new Identifier(
                "emitrades",
                "villager_trades/"
                        + profile.profession().id().substring(
                                profile.profession().id().lastIndexOf(":") + 1
                )
        );
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return null;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return null;
    }

    @Override
    public int getDisplayWidth() {
        return 0;
    }

    @Override
    public int getDisplayHeight() {
        return 0;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

    }
}
