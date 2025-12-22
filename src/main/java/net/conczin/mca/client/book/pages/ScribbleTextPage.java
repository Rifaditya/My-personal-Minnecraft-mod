package net.conczin.mca.client.book.pages;

import com.mojang.blaze3d.systems.RenderSystem;
import net.conczin.mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ScribbleTextPage extends TextPage {
    final Identifier scribble;

    public ScribbleTextPage(Identifier scribble, String name, int page) {
        super(name, page);
        this.scribble = scribble;
    }

    public ScribbleTextPage(Identifier scribble, Component text) {
        super(text);
        this.scribble = scribble;
    }

    public void render(ExtendedBookScreen screen, GuiGraphics context, int mouseX, int mouseY, float delta) {
        // scribble
        int i = (screen.width - 192) / 2;
        // TODO: In 1.21.11, RenderSystem.enableBlend/disableBlend removed
        // context.blit(scribble, i + 28, 32, 0, 0, 128, 128, 128, 128);

        super.render(screen, context, mouseX, mouseY, delta);
    }
}
