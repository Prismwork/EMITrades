package moe.prwk.emitrades.util;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.Range;

import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public final class EmiIngredients {
    private EmiIngredients() {}

    public static EmiIngredient rangedAmount(EmiStack stack, long minAmount, long maxAmount) {
        return new RangedAmount(stack, minAmount, maxAmount);
    }

    public static EmiIngredient rangedAmount(EmiStack stack, Range<Long> amountRange) {
        return new RangedAmount(stack, amountRange);
    }

    public static class RangedAmount implements EmiIngredient {
        private final EmiStack stack;
        private final Range<Long> amountRange;

        protected RangedAmount(EmiStack stack, long minAmount, long maxAmount) {
            this(stack, Range.of(minAmount, maxAmount));
        }

        protected RangedAmount(EmiStack stack, Range<Long> amountRange) {
            this.stack = stack;
            this.amountRange = amountRange;
        }

        @Override
        public List<EmiStack> getEmiStacks() {
            return Lists.newArrayList(new Iterator<>() {
                private long cursor = amountRange.getMinimum();

                @Override
                public boolean hasNext() {
                    return cursor <= amountRange.getMaximum();
                }

                @Override
                public EmiStack next() {
                    EmiStack ret = stack.copy().setAmount(cursor);
                    cursor++;
                    return ret;
                }
            });
        }

        @Override
        public EmiIngredient copy() {
            return new RangedAmount(stack, amountRange);
        }

        @Override
        public long getAmount() {
            return amountRange.getMinimum();
        }

        @Override
        public EmiIngredient setAmount(long amount) {
            return this;
        }

        @Override
        public float getChance() {
            return stack.getChance();
        }

        @Override
        public EmiIngredient setChance(float chance) {
            stack.setChance(chance);
            return this;
        }

        @Override
        public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
            EmiDrawContext context = EmiDrawContext.wrap(draw);
            if ((flags & RENDER_ICON) != 0) {
                stack.render(draw, x, y, delta, ~RENDER_AMOUNT);
            }
            if ((flags & RENDER_AMOUNT) != 0) {
                String count = amountRange.toString("%1$s-%2$s");
                EmiRenderHelper.renderAmount(context, x, y, Component.literal(count));
            }
            if ((flags & RENDER_INGREDIENT) != 0) {
                EmiRender.renderIngredientIcon(this, draw, x, y);
            }
        }

        @Override
        public List<ClientTooltipComponent> getTooltip() {
            return stack.getTooltip();
        }
    }

    public static EmiStack asStack(EmiIngredient ingredient) {
        return new StackWrapper(ingredient);
    }

    public static class StackWrapper extends EmiStack {
        private final EmiIngredient ingredient;

        protected StackWrapper(EmiIngredient ingredient) {
            this.ingredient = ingredient;
        }

        @Override
        public EmiStack copy() {
            return new StackWrapper(ingredient);
        }

        @Override
        public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
            ingredient.render(draw, x, y, delta, flags);
        }

        @Override
        public boolean isEmpty() {
            return ingredient.isEmpty();
        }

        @Override
        public DataComponentPatch getComponentChanges() {
            if (ingredient instanceof EmiStack stack) return stack.getComponentChanges();
            return DataComponentPatch.EMPTY;
        }

        @Override
        public Object getKey() {
            return ingredient;
        }

        @Override
        public ResourceLocation getId() {
            return VersionUtil.identifier("emitrades", "emi_stack_wrapper");
        }

        @Override
        public List<Component> getTooltipText() {
            return List.of();
        }

        @Override
        public List<ClientTooltipComponent> getTooltip() {
            return ingredient.getTooltip();
        }

        @Override
        public Component getName() {
            return Component.empty();
        }
    }
}
