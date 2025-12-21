package net.conczin.mca.client.gui;

import net.conczin.mca.client.book.Book;
import net.conczin.mca.client.book.pages.Page;
import net.conczin.mca.client.gui.widget.ExtendedPageTurnWidget;
import net.conczin.mca.util.compat.ButtonWidget;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

public class ExtendedBookScreen extends Screen {
    private final Book book;
    private int pageIndex;
    private PageButton nextPageButton;
    private PageButton previousPageButton;

    public ExtendedBookScreen(Book book) {
        super(GameNarrator.NO_TITLE);
        this.book = book;
        book.open();
        book.setPage(0, false);
    }

    public boolean setPage(int index) {
        int i = Mth.clamp(index, 0, this.book.getPageCount() - 1);
        if (i != this.pageIndex) {
            book.setPage(i, false);
            this.pageIndex = i;
            this.updatePageButtons();
            return true;
        } else {
            return false;
        }
    }

    protected boolean jumpToPage(int page) {
        return setPage(page);
    }

    @Override
    protected void init() {
        addCloseButton();
        addPageButtons();
    }

    protected void addCloseButton() {
        addRenderableWidget(new ButtonWidget(width / 2 - 100, 196, 200, 20, CommonComponents.GUI_DONE,
                (buttonWidget) -> this.minecraft.setScreen(null)));
    }

    protected void addPageButtons() {
        int i = (width - 192) / 2;
        nextPageButton = addRenderableWidget(new ExtendedPageTurnWidget(i + 116, 159, true,
                (buttonWidget) -> goToNextPage(), book.hasPageTurnSound(), book.getBackground()));
        previousPageButton = addRenderableWidget(new ExtendedPageTurnWidget(i + 43, 159, false,
                (buttonWidget) -> goToPreviousPage(), book.hasPageTurnSound(), book.getBackground()));
        updatePageButtons();
    }

    protected void goToPreviousPage() {
        if (book.getPage(this.pageIndex).previousPage()) {
            if (this.pageIndex > 0) {
                --this.pageIndex;
                book.setPage(this.pageIndex, true);
            }
            this.updatePageButtons();
        }
    }

    protected void goToNextPage() {
        if (book.getPage(this.pageIndex).nextPage()) {
            if (this.pageIndex < book.getPageCount() - 1) {
                ++this.pageIndex;
                book.setPage(this.pageIndex, false);
            }
            this.updatePageButtons();
        }
    }

    private void updatePageButtons() {
        this.nextPageButton.visible = this.pageIndex < book.getPageCount() - 1;
        this.previousPageButton.visible = this.pageIndex > 0;
    }

    // In 1.21.11, keyPressed signature or implementation changed
    // Disabling custom implementation for now
    // @Override
    // public boolean keyPressed(int keyCode, int scanCode, int modifiers) { ... }

    public Font getTextRenderer() {
        return font;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(context, mouseX, mouseY, partialTick);

        // background
        int i = (width - 192) / 2;
        // In 1.21.11, blit signature changed - using RenderType version
        context.blit(net.minecraft.client.renderer.RenderType::guiTextured, book.getBackground(), i, 2, 0, 0, 192, 192,
                256, 256);

        // page number
        if (book.showPageCount()) {
            Component pageIndexText = Component
                    .translatable("book.pageIndicator", this.pageIndex + 1, Math.max(book.getPageCount(), 1))
                    .withStyle(book.getTextFormatting());
            int k = font.width(pageIndexText);
            context.drawString(font, pageIndexText, i - k + 192 - 44, 18, 0, getBook().hasTextShadow());
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Page page = book.getPage(pageIndex);
        if (page != null) {
            page.render(this, context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // In 1.21.11, handleComponentClicked signature may have changed
    // Disabling custom implementation for now
    // @Override
    // public boolean handleComponentClicked(Style style) { ... }

    public Book getBook() {
        return book;
    }
}
