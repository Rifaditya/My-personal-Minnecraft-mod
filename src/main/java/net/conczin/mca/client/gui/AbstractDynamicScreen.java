package net.conczin.mca.client.gui;

import net.conczin.mca.client.resources.Icon;
import net.conczin.mca.entity.interaction.Constraint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractDynamicScreen extends Screen {
    protected static final float iconScale = 1.5f;

    // Tracks which page we're on in the GUI for sending button events
    private String activeScreen = "main";

    private int mouseX;
    private int mouseY;

    private Set<Constraint> constraints = new HashSet<>();

    protected AbstractDynamicScreen(Component title) {
        super(title);
    }

    public String getActiveScreen() {
        return activeScreen;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(Set<Constraint> constraints) {
        this.constraints = constraints;
        setLayout(activeScreen);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    protected abstract void buttonPressed(MCAButton button);

    protected void disableButton(String id) {
        children().forEach(b -> {
            if (b instanceof ButtonEx) {
                if (((ButtonEx) b).getApiButton().identifier().equals(id)) {
                    ((ButtonEx) b).active = false;
                }
            }
        });
    }

    protected void disableAllButtons() {
        this.children().forEach(b -> {
            if (b instanceof AbstractWidget) {
                if (b instanceof ButtonEx) {
                    if (!((ButtonEx) b).getApiButton().identifier().equals("gui.button.backarrow")) {
                        ((AbstractWidget) b).active = false;
                    }
                } else {
                    ((AbstractWidget) b).active = false;
                }
            }
        });
    }

    /**
     * Adds API buttons to the GUI screen provided.
     *
     * @param guiKey String key for the GUI's buttons
     */
    public void setLayout(String guiKey) {
        activeScreen = guiKey;

        clearWidgets();
        MCAScreens.getInstance().getScreen(guiKey).ifPresent(buttons -> {
            for (MCAButton b : buttons) {
                addRenderableWidget(new ButtonEx(b, this));
            }
        });
    }

    protected void drawIcon(GuiGraphics context, Identifier texture, String key) {
        // TODO: In 1.21.11, blit signature changed significantly
        // Need to research proper RenderType pattern
        // Icon icon = MCAScreens.getInstance().getIcon(key);
    }

    protected void drawHoveringIconText(GuiGraphics context, Component text, String key) {
        // 1.21.11: Use ClientTooltipComponent and DefaultTooltipPositioner
        Icon icon = MCAScreens.getInstance().getIcon(key);
        int x = icon.x() + (int) (8 * iconScale);
        int y = icon.y() + (int) (8 * iconScale);
        List<ClientTooltipComponent> components = List.of(
                ClientTooltipComponent.create(text.getVisualOrderText()));
        context.renderTooltip(font, components, x, y, DefaultTooltipPositioner.INSTANCE, null);
    }

    protected void drawHoveringIconText(GuiGraphics context, List<Component> textList, String key) {
        // 1.21.11: Use ClientTooltipComponent for multi-line tooltips
        Icon icon = MCAScreens.getInstance().getIcon(key);
        int x = icon.x() + (int) (8 * iconScale);
        int y = icon.y() + (int) (8 * iconScale);
        List<ClientTooltipComponent> components = textList.stream()
                .map(c -> ClientTooltipComponent.create(c.getVisualOrderText()))
                .collect(Collectors.toList());
        context.renderTooltip(font, components, x, y, DefaultTooltipPositioner.INSTANCE, null);
    }

    // checks if the mouse hovers over a specified button
    protected boolean hoveringOverIcon(String key) {
        Icon icon = MCAScreens.getInstance().getIcon(key);
        return hoveringOver(icon.x(), icon.y(), (int) (16 * iconScale), (int) (16 * iconScale));
    }

    // checks if the mouse hovers over a rectangle
    protected boolean hoveringOver(int x, int y, int w, int h) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    private enum Alignment {
        TOP_LEFT(0.0f, 0.0f),
        TOP(0.5f, 0.0f),
        TOP_RIGHT(1.0f, 0.0f),
        RIGHT(1.0f, 0.5f),
        BOTTOM_RIGHT(1.0f, 1.0f),
        BOTTOM(0.5f, 1.0f),
        BOTTOM_LEFT(0.0f, 1.0f),
        LEFT(0.0f, 0.5f),
        CENTER(0.5f, 0.5f);

        static final Map<String, Alignment> alignments = new HashMap<>();

        static {
            for (Alignment a : Alignment.values()) {
                alignments.put(a.name().toLowerCase(Locale.ENGLISH), a);
            }
        }

        final float h;
        final float v;

        Alignment(float h, float v) {
            this.h = h;
            this.v = v;
        }
    }

    private static class ButtonEx extends Button {
        private final MCAButton apiButton;

        public ButtonEx(MCAButton apiButton, AbstractDynamicScreen screen) {
            super((int) (screen.width * Alignment.alignments.get(apiButton.align()).h + apiButton.x()),
                    (int) (screen.height * Alignment.alignments.get(apiButton.align()).v + apiButton.y()),
                    apiButton.width(),
                    apiButton.height(),
                    Component.translatable(apiButton.identifier()),
                    a -> screen.buttonPressed(apiButton),
                    DEFAULT_NARRATION);
            this.apiButton = apiButton;

            // Remove the button if we specify it should not be present on constraint
            // failure
            // Otherwise we just mark the button as disabled.
            if (!apiButton.isValidForConstraint(screen.getConstraints())) {
                if (apiButton.hideOnFail()) {
                    visible = false;
                }
                active = false;
            }
        }

        public MCAButton getApiButton() {
            return apiButton;
        }

        // In 1.21.11, AbstractButton requires this method to be implemented
        @Override
        protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            // TODO: renderString doesn't exist on Button in 1.21.11
            // Empty for now - buttons won't render text until fixed
        }
    }
}
