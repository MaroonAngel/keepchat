package net.maroonangel.keepchat.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    @Shadow
    CommandSuggestor commandSuggestor;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Overwrite
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount > 1.0D) {
            amount = 1.0D;
        }

        if (amount < -1.0D) {
            amount = -1.0D;
        }

        if (this.commandSuggestor.mouseScrolled(amount)) {
            return true;
        } else {
            if (hasShiftDown()) {
                amount *= 7.0D;
            } else
                amount *= 2.0D;

            this.client.inGameHud.getChatHud().scroll(amount);
            return true;
        }
    }

}
