package mods.clayium.plugin.multipart;

import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.MultiPartRegistry.IPartConverter;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import java.util.Arrays;
import mods.clayium.block.CBlocks;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class RegisterMultipart implements IPartFactory, IPartConverter {
    public RegisterMultipart() {
        super();
    }

    public TMultiPart createPart(String name, boolean client) {
        return name.equals(PANCablePart.type) ? new PANCablePart() : null;
    }

    public void init() {
        MultiPartRegistry.registerConverter(this);
        MultiPartRegistry.registerParts(this, new String[]{PANCablePart.type});
    }

    public Iterable<Block> blockTypes() {
        return Arrays.asList(CBlocks.blockPANCable);
    }

    public TMultiPart convert(World world, BlockCoord pos) {
        Block b = world.func_147439_a(pos.x, pos.y, pos.z);
        return b == CBlocks.blockPANCable ? new PANCablePart() : null;
    }
}
