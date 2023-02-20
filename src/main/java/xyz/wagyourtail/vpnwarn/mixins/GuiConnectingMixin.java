package xyz.wagyourtail.vpnwarn.mixins;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.vpnwarn.GuiHardConfirmation;
import xyz.wagyourtail.vpnwarn.VPNWarn;

import java.util.Arrays;

@Mixin(GuiConnecting.class)
public abstract class GuiConnectingMixin extends GuiScreen {

    @Unique
    private boolean isUsingVPN = false;

    @Unique
    private static boolean override = false;

    @Inject(method = "connect", at = @At("HEAD"), cancellable = true)
    private void connect(String ip, int port, CallbackInfo ci) {
        if (!override && VPNWarn.isUsingVPN()) {
            isUsingVPN = true;
            ci.cancel();
        } else {
            override = false;
        }
    }

    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true)
    private void initGui(CallbackInfo ci) {
        if (isUsingVPN) {
            this.mc.displayGuiScreen(new GuiHardConfirmation(
                Arrays.asList(new IChatComponent[] {
                    new ChatComponentText("You are using a VPN, are you sure you want to connect to this server?")
                }),
                "take the risk",
                () -> {
                    override = true;
                    this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, this.mc.getCurrentServerData()));
                },
                () -> {
                    this.mc.displayGuiScreen(null);
                    this.mc.setServerData(null);
                }
            ));
            isUsingVPN = false;
            ci.cancel();
        }
    }

}
