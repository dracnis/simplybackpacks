package com.flanks255.simplybackpacks.gui;

import com.flanks255.simplybackpacks.SimplyBackpacks;
import com.flanks255.simplybackpacks.inventory.FilterItemHandler;
import com.flanks255.simplybackpacks.items.BackpackItem;
import com.flanks255.simplybackpacks.network.FilterMessage;
import com.flanks255.simplybackpacks.network.ToggleMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class FilterContainer extends AbstractContainerMenu {
    public final FilterItemHandler filterHandler;

    private int slotID;
    private final Player playerEntity;
    private final ItemStack stack;
    public static FilterContainer fromNetwork(final int windowId, final Inventory playerInventory, FriendlyByteBuf extra) {
        CompoundTag nbt = extra.readAnySizeNbt();

        FilterItemHandler handler = new FilterItemHandler();
        handler.deserializeNBT(nbt);
        return new FilterContainer(windowId, playerInventory, handler);
    }

    public FilterContainer(int windowId, Inventory playerInventory, FilterItemHandler handlerIn) {
        super(SimplyBackpacks.FILTERCONTAINER.get(), windowId);

        this.playerEntity = playerInventory.player;
        this.stack = findBackpack(this.playerEntity);
        this.filterHandler = handlerIn;


        addPlayerSlots(playerInventory);
    }

    @Override
    public boolean stillValid(@Nonnull Player playerIn) {
        if (this.slotID == -106)
            return playerIn.getOffhandItem().getItem() instanceof BackpackItem; //whoops guess you can...
        return playerIn.getInventory().getItem(this.slotID).getItem() instanceof BackpackItem;
    }

    private ItemStack findBackpack(Player playerEntity) {
        Inventory inv = playerEntity.getInventory();

        if (playerEntity.getMainHandItem().getItem() instanceof BackpackItem) {
            for (int i = 0; i <= 8; i++) {
                ItemStack stack = inv.getItem(i);
                if (stack == playerEntity.getMainHandItem()) {
                    this.slotID = i;
                    return stack;
                }
            }
        } else if (playerEntity.getOffhandItem().getItem() instanceof BackpackItem) {
            this.slotID = -106;
            return playerEntity.getOffhandItem();
        }
        else {
            for (int i = 0; i <= 8; i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.getItem() instanceof BackpackItem) {
                    this.slotID = i;
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }


    @Override
    @Nonnull
    public void clicked(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull Player playerIn) {
        if (slotId >= 0 && getSlot(slotId).getItem() == playerIn.getMainHandItem())
            return;

        if (clickTypeIn == ClickType.SWAP)
            return;

        if (slotId >= 0) getSlot(slotId).container.setChanged();
        super.clicked(slotId, dragType, clickTypeIn, playerIn);
    }

    public int getFilterOpts() {
        return this.stack.getOrCreateTag().getInt("Filter-OPT");
    }

    public boolean getPickup() {
        return this.stack.getOrCreateTag().getBoolean("Pickup");
    }

    public boolean togglePickup() {
        CompoundTag nbt = this.stack.getOrCreateTag();

        boolean Pickup = !nbt.getBoolean("Pickup");
        nbt.putBoolean("Pickup",Pickup);

        if (this.playerEntity.getCommandSenderWorld().isClientSide)
            SimplyBackpacks.NETWORK.sendToServer(new ToggleMessage());
        return Pickup;
    }

    public int setFilterOpts(int newOpts) {
        CompoundTag nbt = this.stack.getOrCreateTag();
        nbt.putInt("Filter-OPT", newOpts);
        this.stack.setTag(nbt);
        if (this.playerEntity.getCommandSenderWorld().isClientSide)
            SimplyBackpacks.NETWORK.sendToServer(new FilterMessage(newOpts));
        return newOpts;
    }

    public void saveFilter(int newOpts) {
        CompoundTag nbt = this.stack.getOrCreateTag();
        nbt.putInt("Filter-OPT", newOpts);
        this.stack.setTag(nbt);
    }


    public void addPlayerSlots(Inventory playerInventory) {

        int originX = 7;
        int originY = 83;

        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = originX + col * 18;
                int y = originY + row * 18;
                this.addSlot(new Slot(playerInventory, (col + row * 9) + 9, x + 1, y + 1));
            }
        }
        //Hot-bar
        for (int col = 0; col < 9; col++) {
            int x = originX + col * 18;
            int y = originY + 58;
            this.addSlot(new Slot(playerInventory, col, x + 1, y + 1));
        }
    }

    @Override
    public boolean clickMenuButton(Player playerIn, int id) {
        if (getCarried().isEmpty())
            this.filterHandler.removeItem(id);
        else {
            ItemStack fake = getCarried().copy();
            fake.setCount(1);
            this.filterHandler.setItem(id, fake);
        }
        return true;
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player playerIn, int index) {
        Slot slot = this.slots.get(index);
        for (int i = 0; i < this.filterHandler.getSlots(); i++) {
            if (this.filterHandler.getStackInSlot(i).isEmpty()) {
                ItemStack fake = slot.getItem().copy();
                fake.setCount(1);
                this.filterHandler.setItem(i, fake);
                break;
            }
        }

        return ItemStack.EMPTY;
    }
}