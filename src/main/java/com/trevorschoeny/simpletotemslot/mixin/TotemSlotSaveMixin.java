package com.trevorschoeny.simpletotemslot.mixin;

import com.trevorschoeny.simpletotemslot.TotemSlot;
import com.trevorschoeny.simpletotemslot.TotemSlotSettings;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.level.gameevent.GameEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The totem slot's death save, the last resort after the hands. Extends vanilla's own
 * pre-death check, {@code checkTotemDeathProtection}, and repeats its revival exactly:
 *
 * <ul>
 *   <li><b>Timing.</b> The check runs the moment a hit would be lethal, before
 *       {@code die()} and before anything drops. Injecting at its RETURN keeps that:
 *       the player is saved before death, so nothing drops.</li>
 *   <li><b>Hands first.</b> Vanilla has already checked both hands. If a held totem
 *       saved the player, this does nothing, so a hand totem is always used first.</li>
 *   <li><b>Vanilla revival.</b> Heal to 1, the totem's {@code DEATH_PROTECTION} effects,
 *       the spinning-totem animation, the use stat and advancement, then one totem is
 *       used up.</li>
 *   <li><b>Same bypass rule.</b> No save against damage that bypasses invulnerability
 *       ({@code /kill}), as a held totem can't either.</li>
 * </ul>
 *
 * Works in survival and creative: the slot's contents are server-authoritative.
 */
@Mixin(LivingEntity.class)
public abstract class TotemSlotSaveMixin {

    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"), cancellable = true)
    private void simpleTotemSlot$save(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;                              // a hand totem already saved them
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return; // vanilla won't save here either
        if (!TotemSlotSettings.enabled()) return;                       // mod off: vanilla death rules
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;

        ItemStack totem = TotemSlot.get(player);
        DeathProtection protection = totem.get(DataComponents.DEATH_PROTECTION);
        if (protection == null) return;                                 // slot empty

        ItemStack used = totem.copy();
        TotemSlot.consume(player);

        // The same revival as vanilla's checkTotemDeathProtection.
        if (self instanceof ServerPlayer sp) {
            sp.awardStat(Stats.ITEM_USED.get(used.getItem()));
            CriteriaTriggers.USED_TOTEM.trigger(sp, used);
            self.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        }
        self.setHealth(1.0F);
        protection.applyEffects(used, self);                            // the component's death_effects
        self.level().broadcastEntityEvent(self, (byte) 35);             // spinning-totem animation
        cir.setReturnValue(true);
    }
}
