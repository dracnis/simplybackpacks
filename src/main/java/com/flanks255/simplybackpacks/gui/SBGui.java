package com.flanks255.simplybackpacks.gui;

import com.flanks255.simplybackpacks.SimplyBackpacks;
import com.flanks255.simplybackpacks.items.Backpack;
import com.flanks255.simplybackpacks.util.BackpackUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class SBGui extends ContainerScreen<SBContainer> {
    public SBGui(SBContainer container, PlayerInventory playerInventory, ITextComponent name) {
        super(container, playerInventory, name);

        Backpack tier = container.getTier();

        //1 in 100, vs 1 in 10k
        double chance = BackpackUtils.increasedAltChance(playerInventory.player.getUUID())? 0.01:0.0001;

        if (tier == Backpack.ULTIMATE && Math.random() < chance) {
            playerInventory.player.playSound(SoundEvents.COW_HURT, 0.5f, 1f);
            if (Math.random() < 0.5)
                GUI = new ResourceLocation(SimplyBackpacks.MODID, "textures/gui/ultimate_alt.png");
            else
                GUI = new ResourceLocation(SimplyBackpacks.MODID, "textures/gui/ultimate_alt2.png");
        }
        else
            GUI = tier.texture;
        imageWidth = tier.xSize;
        imageHeight = tier.ySize;


    }

    private final ResourceLocation GUI;

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(@Nonnull MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f ,1.0f);
        this.getMinecraft().textureManager.bind(GUI);
        blit(matrixStack, leftPos, topPos, 0,0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(@Nonnull MatrixStack matrixStack, int x, int y) {
        this.font.draw(matrixStack, this.title.getString(), 7,6,0x404040);
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground(matrixStack);
        super.render(matrixStack,p_render_1_, p_render_2_, p_render_3_);
        this.renderTooltip(matrixStack, p_render_1_, p_render_2_);
    }
}
