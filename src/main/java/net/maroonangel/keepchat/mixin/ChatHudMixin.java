package net.maroonangel.keepchat.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralTextContent;
//import net.minecraft.class_2588;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Shadow
    public void addMessage(Text message) {}

    @Shadow
    public void scroll(int scroll) {}

    @Shadow
    private void removeMessage(int messageId) {}

    @Shadow
    public int getWidth() {return 0;}

    @Shadow
    public int getHeight() {return 0;}

    @Shadow
    private boolean isChatFocused() {return false;}

    @Shadow
    public double getChatScale() {return 0d;}



    @Shadow
    private MinecraftClient client;

    @Shadow
    private List<String> messageHistory = Lists.newArrayList();
    @Shadow
    private List<ChatHudLine<Text>> messages;
    @Shadow
    private List<ChatHudLine<OrderedText>> visibleMessages;
    @Shadow
    private Deque<Text> messageQueue = Queues.newArrayDeque();
    @Shadow
    private int scrolledLines;
    @Shadow
    private boolean hasUnreadNewMessages;
    @Shadow
    private long lastMessageAddedTime;

    @Shadow
    public void addToMessageHistory(String message) {}

    @Shadow
    public boolean isChatHidden() {
        return true;
    }

    @Shadow
    private void processMessageQueue() {}

    @Shadow
    private int getVisibleLineCount() {
        return 0;
    }

    private List<ChatHudLine<Text>> textMessages;

    @Shadow
    private static double getMessageOpacityMultiplier(int x) {
        return 0.0D;
    }



    @Overwrite
    public void clear(boolean clearHistory) {
        /*
        if (this.messageHistory.size() > 0) {
            System.out.println(this.messageHistory.get(this.messageHistory.size() - 1));
            if (!this.messageHistory.get(this.messageHistory.size() - 1).equals("==================")) {
                this.addMessage(new OrderedText(new LiteralTextContent("==================")));
                this.addToMessageHistory("==================");
            }
        }
         */
    }


    @Overwrite
    private void addMessage(Text message, int messageId, int timestamp, boolean bl) {
        if (messageId != 0) {
            this.removeMessage(messageId);
        }

        int i = MathHelper.floor((double)this.getWidth() / this.getChatScale());
        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(message, i, this.client.textRenderer);
        boolean bl2 = this.isChatFocused();

        OrderedText orderedText;
        for(Iterator var8 = list.iterator(); var8.hasNext(); this.visibleMessages.add(0, new ChatHudLine(timestamp, orderedText, messageId))) {
            orderedText = (OrderedText)var8.next();
            if (bl2 && this.scrolledLines > 0) {
                this.hasUnreadNewMessages = true;
                this.scroll(1);
            }
        }

        while(this.visibleMessages.size() > 500) {
            this.visibleMessages.remove(this.visibleMessages.size() - 1);
        }

        if (!bl) {
            this.messages.add(0, new ChatHudLine(timestamp, message, messageId));
            if (message.getString().contains("whispers to you: "))
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.8f, 0.4f));

            while(this.messages.size() > 500) {
                this.messages.remove(this.messages.size() - 1);
            }
        }
    }
}
