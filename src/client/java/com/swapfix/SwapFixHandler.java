package com.swapfix;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

/**
 * هنا كل منطق التبديل.
 *
 * الفكرة:
 * 1) لما نمسك ضغطة slot8 (raw, مش عن طريق tick) نسجل إنها متضغوطة.
 * 2) لما نمسك ضغطة slot9 وslot8 لسه متضغوطة نبدل فورًا لسلوت 9
 *    ونعلّم إن فيه رجوع لازم يحصل.
 * 3) في نهاية نفس الـ client tick (END_CLIENT_TICK) لو لسه slot8
 *    متضغوطة نرجع لسلوت 8 فورًا. كده الرجوع دايمًا exactly 1 tick
 *    ومش معتمد على تكرار الضغطة (key repeat) اللي بيسببه نظام التشغيل
 *    وهو السبب الحقيقي للتأخير العشوائي.
 */
public final class SwapFixHandler {

    // انديكسات الهوت بار: سلوت 8 = index 7 ، سلوت 9 = index 8 (0-based)
    private static final int SLOT_8_INDEX = 7;
    private static final int SLOT_9_INDEX = 8;

    private static boolean slot8Held = false;
    private static boolean pendingReturnToSlot8 = false;

    private SwapFixHandler() {}

    /**
     * بتتنادى من الـ Mixin مباشرة جوه GLFW callback، يعني قبل أي
     * معالجة تخص الـ tick بتاع ماين كرافت - أسرع ما يمكن.
     */
    public static void onRawKey(int key, int scancode, int action) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options == null) return;

        KeyMapping[] hotbar = mc.options.keyHotbarSlots;
        if (hotbar == null || hotbar.length < 9) return;

        boolean isPress = action == 1;   // GLFW_PRESS
        boolean isRelease = action == 0; // GLFW_RELEASE

        // تتبع حالة زر سلوت 8
        if (hotbar[SLOT_8_INDEX].matches(key, scancode)) {
            if (isPress) {
                slot8Held = true;
            } else if (isRelease) {
                slot8Held = false;
                pendingReturnToSlot8 = false;
            }
            return;
        }

        // ضغطة سلوت 9 وسلوت 8 لسه متمسوكة = فعل السواب فورًا
        if (hotbar[SLOT_9_INDEX].matches(key, scancode) && isPress && slot8Held) {
            switchSlot(SLOT_9_INDEX);
            pendingReturnToSlot8 = true;
        }
    }

    /**
     * بتتنادى في نهاية كل client tick. لو محتاجين نرجع لسلوت 8
     * نرجعله فورًا هنا - ده اللي بيضمن إن الرجوع دايمًا tick واحد بالظبط.
     */
    public static void onEndClientTick(Minecraft mc) {
        if (pendingReturnToSlot8 && slot8Held) {
            switchSlot(SLOT_8_INDEX);
            pendingReturnToSlot8 = false;
        }
    }

    private static void switchSlot(int index) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        mc.player.getInventory().selected = index;
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(index));
    }
}
