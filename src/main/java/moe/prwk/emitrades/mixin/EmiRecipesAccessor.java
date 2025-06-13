package moe.prwk.emitrades.mixin;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.registry.EmiRecipes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(value = EmiRecipes.class, remap = false)
public interface EmiRecipesAccessor {
    @Accessor("workstations")
    static Map<EmiRecipeCategory, List<EmiIngredient>> getWorkstations() {
        throw new AssertionError();
    }
}
