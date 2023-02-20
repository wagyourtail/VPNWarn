package xyz.wagyourtail.vpnwarn;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.IChatComponent;

import java.io.IOException;
import java.util.List;

public class GuiHardConfirmation extends GuiScreen {

    private final List<IChatComponent> message;
    private final String confirmType;

    private GuiTextField confirmField;

    private final Runnable confirmAction;
    private final Runnable cancelAction;
    public GuiHardConfirmation(List<IChatComponent> message, String confirmType, Runnable confirmAction, Runnable cancelAction) {
        this.message = message;
        this.confirmType = confirmType;
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;
    }


    @Override
    public void initGui() {
        super.initGui();
        this.confirmField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, this.height / 2 + 20, 200, 20);
        this.confirmField.setMaxStringLength(100);
        this.confirmField.setFocused(true);

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2 + 50, "Confirm"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 2 + 80, "Cancel"));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.confirmField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (this.confirmField.isFocused()) {
            this.confirmField.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 0) {
            if (this.confirmField.getText().equals(this.confirmType)) {
                this.confirmAction.run();
            }
        } else if (button.id == 1) {
            this.cancelAction.run();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int i = 0;
        for (; i < this.message.size(); i++) {
            this.drawCenteredString(this.fontRendererObj, this.message.get(i).getFormattedText(), this.width / 2, this.height / 2 - 50 + i * 10, 16777215);
        }

        this.drawCenteredString(this.fontRendererObj, "Please type \"" + this.confirmType + "\" to continue.", this.width / 2, this.height / 2 - 50 + i * 10, 16777215);

        confirmField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
