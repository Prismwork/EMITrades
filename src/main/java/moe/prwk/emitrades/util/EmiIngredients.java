package moe.prwk.emitrades.util;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//? if >=1.20.6 {
import net.minecraft.core.component.DataComponentPatch;
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.Range;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public final class EmiIngredients {
    private EmiIngredients() {}

    public static EmiIngredient rangedAmount(EmiStack stack, long minAmount, long maxAmount) {
        return new RangedAmount(stack, minAmount, maxAmount);
    }

    public static EmiIngredient rangedAmount(EmiStack stack, Range<Long> amountRange) {
        return new RangedAmount(stack, amountRange);
    }

    private static class RangedAmount implements EmiIngredient {
        private final EmiStack stack;
        private final Range<Long> amountRange;

        protected RangedAmount(EmiStack stack, long minAmount, long maxAmount) {
            this(stack, /*? if >=1.20.4 {*/Range.of(minAmount, maxAmount)/*?} else {*//*Range.between(minAmount, maxAmount)*//*?}*/);
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
            return amountRange.getMaximum();
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
            Font textRenderer = Minecraft.getInstance().font;
            EmiDrawContext context = EmiDrawContext.wrap(draw);
            if ((flags & RENDER_ICON) != 0) {
                stack.render(draw, x, y, delta, ~RENDER_AMOUNT);
            }
            if ((flags & RENDER_AMOUNT) != 0) {
                String count = !Objects.equals(amountRange.getMinimum(), amountRange.getMaximum()) ?
                        amountRange.toString("%1$s-%2$s") : amountRange.toString("%1$s");
                EmiRenderHelper.renderAmount(context, x + 14 - textRenderer.width(count), y, Component.literal(count));
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

    private static class StackWrapper extends EmiStack {
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

        //? if >=1.20.6 {
        @Override
        public DataComponentPatch getComponentChanges() {
            if (ingredient instanceof EmiStack stack) return stack.getComponentChanges();
            return DataComponentPatch.EMPTY;
        }
        //?} else {
        /*@Override
        public CompoundTag getNbt() {
            if (ingredient instanceof EmiStack stack) return stack.getNbt();
            return new CompoundTag();
        }
        *///?}

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
            if (ingredient instanceof EmiStack stack) return stack.getTooltipText();
            return List.of();
        }

        @Override
        public List<ClientTooltipComponent> getTooltip() {
            return ingredient.getTooltip();
        }

        @Override
        public Component getName() {
            if (ingredient instanceof EmiStack stack) return stack.getName();
            return Component.empty();
        }
    }

    public static EmiIngredient appendTooltips(EmiIngredient ingredient, ClientTooltipComponent... tooltips) {
        return new AppendTooltips(ingredient, tooltips);
    }

    public static EmiIngredient appendTooltips(EmiIngredient ingredient, List<ClientTooltipComponent> tooltips) {
        return new AppendTooltips(ingredient, tooltips);
    }

    private static class AppendTooltips implements EmiIngredient {
        private final EmiIngredient ingredient;
        private final List<ClientTooltipComponent> tooltips;

        protected AppendTooltips(EmiIngredient ingredient, ClientTooltipComponent... tooltips) {
            this(ingredient, List.of(tooltips));
        }

        protected AppendTooltips(EmiIngredient ingredient, List<ClientTooltipComponent> tooltips) {
            this.ingredient = ingredient;
            this.tooltips = tooltips.stream().toList();
        }

        @Override
        public List<EmiStack> getEmiStacks() {
            return ingredient.getEmiStacks();
        }

        @Override
        public EmiIngredient copy() {
            return new AppendTooltips(ingredient.copy(), tooltips);
        }

        @Override
        public long getAmount() {
            return ingredient.getAmount();
        }

        @Override
        public EmiIngredient setAmount(long amount) {
            ingredient.setAmount(amount);
            return this;
        }

        @Override
        public float getChance() {
            return ingredient.getChance();
        }

        @Override
        public EmiIngredient setChance(float chance) {
            ingredient.setChance(chance);
            return this;
        }

        @Override
        public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
            ingredient.render(draw, x, y, delta, flags);
        }

        @Override
        public List<ClientTooltipComponent> getTooltip() {
            List<ClientTooltipComponent> ret = ingredient.getTooltip();
            ret.addAll(tooltips);
            return ret;
        }
    }
}
