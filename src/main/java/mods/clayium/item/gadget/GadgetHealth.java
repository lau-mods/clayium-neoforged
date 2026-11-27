package mods.clayium.item.gadget;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public class GadgetHealth extends GadgetOrdinal {
    private AttributeModifier mod;
    private static UUID uuid = UUID.fromString("066bfee5-6cd5-4166-93d7-dfcc32c337a8");

    public GadgetHealth() {
        super("Health0", "Health1", "Health2");
    }

    public void update(int itemIndex, Entity entity, boolean isRemote) {
        if (this.mod == null) {
            this.mod = new AttributeModifier(uuid, "GadgetHealth", 20.0, 0);
        }

        if (itemIndex >= 0) {
            if (entity instanceof EntityLivingBase) {
                Multimap map = HashMultimap.create();
                map.put(
                    SharedMonsterAttributes.field_111267_a.func_111108_a(),
                    new AttributeModifier(
                        uuid, this.mod.func_111166_b(), this.mod.func_111164_d() * (double)(itemIndex + 1) * (double)(itemIndex + 1), this.mod.func_111169_c()
                    )
                );
                ((EntityLivingBase)entity).func_110140_aT().func_111147_b(map);
            }
        } else if (entity instanceof EntityLivingBase) {
            Multimap map = HashMultimap.create();
            map.put(SharedMonsterAttributes.field_111267_a.func_111108_a(), this.mod);
            ((EntityLivingBase)entity).func_110140_aT().func_111148_a(map);
        }
    }
}
