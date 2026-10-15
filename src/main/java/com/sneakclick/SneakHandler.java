package com.sneakclick;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class SneakHandler {
    // Прогресс приседания: 0.0 = стоим, 1.0 = сидим полностью
    private float sneakProgress = 0.0f;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!SneakClickConfig.INSTANCE.sneakEnabled.get()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        boolean isSneaking = player.isShiftKeyDown();
        float target = isSneaking ? 1.0f : 0.0f;
        float speed = SneakClickConfig.INSTANCE.sneakSpeed.get().floatValue();

        // Плавное движение к цели
        if (Math.abs(sneakProgress - target) > 0.001f) {
            float diff = target - sneakProgress;
            // Дельта * скорость. Чем меньше speed, тем медленнее
            sneakProgress += diff * speed * 0.3f;
        } else {
            sneakProgress = target;
        }
    }

    // Вызывается из SneakClick.java через MinecraftForge event bus
    @SubscribeEvent
    public void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!SneakClickConfig.INSTANCE.sneakEnabled.get()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // Высота ванильного приседания — 0.3 блока
        double vanillaHeight = 0.3;
        // Плавно растущая высота
        double smoothHeight = vanillaHeight * sneakProgress;
        // Разница с ванильным положением — компенсируем
        double realVanillaHeight = player.isShiftKeyDown() ? vanillaHeight : 0.0;
        double correction = realVanillaHeight - smoothHeight;

        // Сдвигаем камеру вниз — плавно
        // event.getPitch() — угол. Мы изменяем pitch на основе correction
        // 30 — множитель, можно настроить
        event.setPitch(event.getPitch() + (float)(correction * 30.0));
    }

    public float getSneakProgress() {
        return sneakProgress;
    }
}
