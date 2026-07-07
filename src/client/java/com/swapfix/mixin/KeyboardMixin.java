package com.swapfix.mixin;

import com.swapfix.SwapFixHandler;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * بنمسك ضغطة الكيبورد وهي لسه "خام" (raw) جاية من GLFW مباشرة،
 * يعني قبل ما ماين كرافت يستنى الـ tick التالي عشان يعالجها.
 *
 * ملاحظة: لو حصل خطأ "method not found" وقت الـ build (هتشوفه في
 * تبويب Actions على GitHub)، ده معناه إن اسم الميثود اتغيّر شوية في
 * نسختك بالظبط. ابعتلي رسالة الخطأ وهظبطها فورًا.
 */
@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void swapfix$onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        SwapFixHandler.onRawKey(key, scancode, action);
    }
}
