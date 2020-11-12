package net.maroonangel.keepchat.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.org.apache.xpath.internal.operations.Or;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import static net.minecraft.client.gui.DrawableHelper.fill;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Shadow
    public void addMessage(Text message) {}

    @Shadow
    public void scroll(double amount) {}

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
        if (this.messageHistory.size() > 0) {
            System.out.println(this.messageHistory.get(this.messageHistory.size() - 1));
            if (!this.messageHistory.get(this.messageHistory.size() - 1).equals("==================")) {
                this.addMessage(new TranslatableText("=================="));
                this.addToMessageHistory("==================");
            }
        }
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
                this.scroll(1.0D);
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

    @Overwrite
    public void render(MatrixStack matrices, int tickDelta) {
        if (!this.isChatHidden()) {
            this.processMessageQueue();
            int i = this.getVisibleLineCount();
            int j = this.visibleMessages.size();
            if (j > 0) {
                boolean bl = false;
                if (this.isChatFocused()) {
                    bl = true;
                }

                double d = this.getChatScale();
                int k = MathHelper.ceil((double)this.getWidth() / d);
                RenderSystem.pushMatrix();
                RenderSystem.translatef(2.0F, 8.0F, 0.0F);
                RenderSystem.scaled(d, d, 1.0D);
                double e = this.client.options.chatOpacity * 0.8999999761581421D + 0.10000000149011612D;
                double f = this.client.options.textBackgroundOpacity;
                double g = 9.0D * (this.client.options.chatLineSpacing + 1.0D);
                double h = -8.0D * (this.client.options.chatLineSpacing + 1.0D) + 4.0D * this.client.options.chatLineSpacing;
                int l = 0;

                int m;
                int x;
                int aa;
                int ab;
                for(m = 0; m + this.scrolledLines < this.visibleMessages.size() && m < i; ++m) {
                    ChatHudLine<OrderedText> chatHudLine = (ChatHudLine)this.visibleMessages.get(m + this.scrolledLines);
                    List<ChatHudLine<Text>> lineList = null;
                    StringBuilder lineText = new StringBuilder();

                    boolean started = false;
                    for (ChatHudLine<Text> line : messages) {

                        //System.out.println(line.getId());
                        //System.out.println(chatHudLine.getText());


                        if (line.getCreationTick() == chatHudLine.getCreationTick()) {
                            String t = line.getText().getString();
                            if (!started)
                                lineText.append(t).append(" ");
                            if (t.startsWith("<"))
                                started = true;
                        }
                    }

                    if (chatHudLine != null) {
                        x = tickDelta - chatHudLine.getCreationTick();
                        if (x < 200 || bl) {
                            double o = bl ? 1.0D : getMessageOpacityMultiplier(x);
                            aa = (int)(255.0D * o * e);
                            ab = (int)(255.0D * o * f);
                            ++l;



                            if (!lineText.toString().equals("") && MinecraftClient.getInstance().currentScreen != null && MinecraftClient.getInstance().currentScreen instanceof ChatScreen) {
                                TextFieldWidget test = ((ChatScreenAccessor) MinecraftClient.getInstance().currentScreen).getChatField();

                                if ((test.getText().equals("/msg") || test.getText().equals("/w")) && !lineText.toString().contains("whispers to you: ")
                                        && !lineText.toString().contains("You whisper to "))
                                    aa = (int) (80.0D * o * e);
                            }

                            if (!lineText.toString().equals("") && lineText.toString().contains(": Triggered ["))
                                aa = (int) (100.0D * o * e);



                            if (aa > 3) {
                                //int r = false;
                                double s = (double)(-m) * g;
                                matrices.push();
                                matrices.translate(0.0D, 0.0D, 50.0D);
                                fill(matrices, -2, (int)(s - g), 0 + k + 4, (int)s, ab << 24);
                                RenderSystem.enableBlend();
                                matrices.translate(0.0D, 0.0D, 50.0D);

                                this.client.textRenderer.drawWithShadow(matrices, (OrderedText)chatHudLine.getText(), 0.0F, (float)((int)(s + h)), 16777215 + (aa << 24));
                                RenderSystem.disableAlphaTest();
                                RenderSystem.disableBlend();
                                matrices.pop();
                            }
                        }
                    }
                }

                int w;
                if (!this.messageQueue.isEmpty()) {
                    m = (int)(128.0D * e);
                    w = (int)(255.0D * f);
                    matrices.push();
                    matrices.translate(0.0D, 0.0D, 50.0D);
                    fill(matrices, -2, 0, k + 4, 9, w << 24);
                    RenderSystem.enableBlend();
                    matrices.translate(0.0D, 0.0D, 50.0D);
                    this.client.textRenderer.drawWithShadow(matrices, new TranslatableText("chat.queue", new Object[]{this.messageQueue.size()}), 0.0F, 1.0F, 16777215 + (m << 24));
                    matrices.pop();
                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                }

                if (bl) {
                    this.client.textRenderer.getClass();
                    int v = 9;
                    RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
                    w = j * v + j;
                    x = l * v + l;
                    int y = this.scrolledLines * x / j;
                    int z = x * x / w;
                    if (w != x) {
                        aa = y > 0 ? 170 : 96;
                        ab = this.hasUnreadNewMessages ? 13382451 : 3355562;
                        fill(matrices, 0, -y, 2, -y - z, ab + (aa << 24));
                        fill(matrices, 2, -y, 1, -y - z, 13421772 + (aa << 24));
                    }
                }

                RenderSystem.popMatrix();
            }
        }
    }

}
