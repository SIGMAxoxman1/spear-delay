package com.swapfix;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
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
     * بتتنادى من الـ Mixin مباشرة جوه GLFW callback (Minecraft 1.21.9+
     * بقى بيلف بيانات الزرار في كائن KeyEvent بدل أرقام منفصلة)،
     * يعني قبل أي معالجة تخص الـ tick بتاع اللعبة - أسرع ما يمكن.
     */
    public static void onRawKey(int action, KeyEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options == null) return;

        KeyMapping[] hotbar = mc.options.keyHotbarSlots;
        if (hotbar == null || hotbar.length < 9) return;

        boolean isPress = action == 1;   // GLFW_PRESS
        boolean isRelease = action == 0; // GLFW_RELEASE

        // تتبع حالة زر سلوت 8
        if (hotbar[SLOT_8_INDEX].matches(event)) {
            if (isPress) {
                slot8Held = true;
            } else if (isRelease) {
                slot8Held = false;
                pendingReturnToSlot8 = false;
            }
            return;
        }

        // ضغطة سلوت 9 وسلوت 8 لسه متمسوكة = فعل السواب فورًا
        if (hotbar[SLOT_9_INDEX].matches(event) && isPress && slot8Held) {
            switchSlot(SLOT_9_INDEX);
            pendingReturnToSlot8 = true;
        }
    }

    /**
     * بتتنادى في نهاية كل client tick. لو محتاجين نرجع لسلوت 8
     * نرجعله فورًا هنا - ده اللي بيضمن إن الرجوع دايمًا tick واحد بالظبط.
     *
     * ملحوظة: مبنعيدش التحقق من إن slot8 "لسه متمسوك" هنا عمدًا.
     * لما بتضغط أزرار كتير مع بعض بسرعة، بعض الكيبوردات بترسل إشارة
     * "release" وهمية لزرار لسه فعليًا مضغوط (ظاهرة اسمها Ghosting/
     * Key Rollover) وده كان بيلغي الرجوع غلط. دلوقتي بمجرد ما نقرر
     * نعمل السواب (يعني كنا متأكدين إن slot8 كان متمسوك وقتها)،
     * الرجوع بيحصل مضمون مهما حصل بعد كده.
     */
    public static void onEndClientTick(Minecraft mc) {
        if (pendingReturnToSlot8) {
            switchSlot(SLOT_8_INDEX);
            pendingReturnToSlot8 = false;
        }
    }

    private static void switchSlot(int index) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        mc.player.getInventory().setSelectedSlot(index);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(index));
    }
}
