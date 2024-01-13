package io.github.prismwork.emitrades.util;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ListEmiStack extends EmiStack {
    private final List<? extends EmiIngredient> ingredients;
    private final List<EmiStack> fullList;
    private long amount;
    private float chance = 1;

    public ListEmiStack(List<? extends EmiIngredient> ingredients, long amount) {
        this.ingredients = ingredients;
        this.fullList = ingredients.stream().flatMap(i -> i.getEmiStacks().stream()).toList();
        if (fullList.isEmpty()) {
            throw new IllegalArgumentException("ListEmiIngredient cannot be empty");
        }
        this.amount = amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListEmiStack other) {
            return other.getEmiStacks().equals(this.getEmiStacks());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fullList.hashCode();
    }

    @Override
    public EmiStack copy() {
        EmiStack stack = new ListEmiStack(ingredients, amount);
        stack.setChance(chance);
        return stack;
    }

    @Override
    public boolean isEmpty() {
        return ingredients.isEmpty();
    }

    @Override
    public String toString() {
        return "ListEmiStack" + getEmiStacks();
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return fullList;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public EmiStack setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public EmiStack setChance(float chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public NbtCompound getNbt() {
        return new NbtCompound();
    }

    @Override
    public Object getKey() {
        return ingredients;
    }

    @Override
    public Identifier getId() {
        return new Identifier("emitrades", "list_emi_stack");
    }

    @Override
    public List<Text> getTooltipText() {
        return null;
    }

    @Override
    public void render(DrawContext draw, int x, int y, float delta, int flags) {
        int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
        EmiIngredient current = ingredients.get(item);
        if ((flags & RENDER_ICON) != 0) {
            current.render(draw, x, y, delta, ~RENDER_AMOUNT);
        }
        if ((flags & RENDER_AMOUNT) != 0) {
            current.copy().setAmount(amount).render(draw, x, y, delta, RENDER_AMOUNT);
        }
        if ((flags & RENDER_INGREDIENT) != 0) {
            EmiRender.renderIngredientIcon(this, draw, x, y);
        }
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        List<TooltipComponent> tooltip = Lists.newArrayList();
        tooltip.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emi.accepts"))));
        tooltip.add(new IngredientTooltipComponent(ingredients));
        int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
        tooltip.addAll(ingredients.get(item).copy().setAmount(amount).getTooltip());
        return tooltip;
    }

    @Override
    public Text getName() {
        return EmiPort.literal("");
    }
}
