package mods.clayium.worldgen;

import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import mods.clayium.block.CBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.common.config.Configuration;

public class ClayOreGenerator implements IWorldGenerator {
    static int clayOreVeinNumber = 8;
    static int clayOreVeinSize = 24;
    static int clayOreVeinMinY = 24;
    static int clayOreVeinMaxY = 88;
    static boolean generateDenseClayOreVein = true;
    static int denseClayOreVeinSize = 10;
    static int largeDenseClayOreVeinNumber = 2;
    static int largeDenseClayOreVeinSize = 6;
    static int largeDenseClayOreVeinMinY = 10;
    static int largeDenseClayOreVeinMaxY = 16;

    public ClayOreGenerator() {
        super();
    }

    public static void loadConfig(Configuration cfg) {
        String category = "worldgen";
        clayOreVeinNumber = cfg.getInt("ClayOreVeinNumber", category, 8, 0, 99, "Number of Clay Ore Veins per Chunk");
        clayOreVeinSize = cfg.getInt("ClayOreVeinSize", category, 24, 0, 50, "");
        clayOreVeinMinY = cfg.getInt("ClayOreVeinMinY", category, 24, 0, 255, "");
        clayOreVeinMaxY = cfg.getInt("ClayOreVeinMaxY", category, 88, 0, 255, "");
        generateDenseClayOreVein = cfg.getBoolean("GenerateDenseClayOreVein", category, true, "");
        denseClayOreVeinSize = cfg.getInt("DenseClayOreVeinSize", category, 10, 0, 50, "");
        largeDenseClayOreVeinNumber = cfg.getInt("LargeDenseClayOreVeinNumber", category, 2, 0, 99, "Number of Large Dense Clay Ore Veins per Chunk");
        largeDenseClayOreVeinSize = cfg.getInt("LargeDenseClayOreVeinSize", category, 6, 0, 50, "");
        largeDenseClayOreVeinMinY = cfg.getInt("LargeDenseClayOreVeinMinY", category, 10, 0, 255, "");
        largeDenseClayOreVeinMaxY = cfg.getInt("LargeDenseClayOreVeinMaxY", category, 16, 0, 255, "");
    }

    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (world.field_73011_w instanceof WorldProviderSurface) {
            this.generateOre(world, random, chunkX << 4, chunkZ << 4);
        }
    }

    private void generateOre(World world, Random random, int x, int z) {
        for (int i = 0; i < clayOreVeinNumber; i++) {
            int genX = x + random.nextInt(16);
            int genY = clayOreVeinMinY + random.nextInt(clayOreVeinMaxY - clayOreVeinMinY);
            int genZ = z + random.nextInt(16);
            new WorldGenMinable(CBlocks.blockClayOre, 0, clayOreVeinSize, Blocks.field_150348_b).func_76484_a(world, random, genX, genY, genZ);
            if (generateDenseClayOreVein) {
                new WorldGenMinable(CBlocks.blockClayOre, 1, denseClayOreVeinSize, CBlocks.blockClayOre).func_76484_a(world, random, genX, genY, genZ);
                if (world.func_147439_a(genX + 8, genY, genZ + 8) == CBlocks.blockClayOre
                    && world.func_72805_g(genX + 8, genY, genZ + 8) == 1
                    && random.nextInt(2) == 0) {
                    world.func_147465_d(genX + 8, genY, genZ + 8, CBlocks.blockClayOre, 2, 2);
                }
            }
        }

        for (int ix = 0; ix < largeDenseClayOreVeinNumber; ix++) {
            int genX = x + random.nextInt(16);
            int genY = largeDenseClayOreVeinMinY + random.nextInt(largeDenseClayOreVeinMaxY - largeDenseClayOreVeinMinY);
            int genZ = z + random.nextInt(16);
            new WorldGenMinable(CBlocks.blockClayOre, 2, largeDenseClayOreVeinSize, Blocks.field_150348_b).func_76484_a(world, random, genX, genY, genZ);
        }
    }
}
