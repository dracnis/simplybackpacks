package com.flanks255.simplybackpacks.inventory;

import com.flanks255.simplybackpacks.SimplyBackpacks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class FilterItemHandler extends ItemStackHandler {
    public FilterItemHandler() {
        super(16);
    }

    public void removeItem(int slot) {
        this.setStackInSlot(slot, ItemStack.EMPTY);
        onContentsChanged(slot);
    }

    public void setItem(int slot, ItemStack item) {
        if (!SimplyBackpacks.filterItem(item))
            return;
        else {
            this.setStackInSlot(slot, item);
            onContentsChanged(slot);
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        BackpackManager.get().markDirty();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!SimplyBackpacks.filterItem(stack))
            return stack;

        return super.insertItem(slot, stack, simulate);
    }
}