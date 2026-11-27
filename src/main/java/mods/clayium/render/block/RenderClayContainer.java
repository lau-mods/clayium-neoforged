package mods.clayium.render.block;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.ClayBuffer;
import mods.clayium.block.ClayContainer;
import mods.clayium.block.MultitrackBuffer;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilDirection;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderClayContainer implements ISimpleBlockRenderingHandler {
    public RenderClayContainer() {
        super();
    }

    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (modelId == this.getRenderId()) {
            ((ClayContainer)block).setInitialBlockBounds();
            int metadata1 = ((ClayContainer)block).metaMode != 0 ? 3 + metadata / 16 * 16 : metadata;
            Tessellator tessellator = Tessellator.field_78398_a;
            renderer.func_147775_a(block);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            renderer.func_147775_a(block);
            tessellator.func_78382_b();
            tessellator.func_78375_b(0.0F, -1.0F, 0.0F);
            renderer.func_147768_a(block, 0.0, 0.0, 0.0, block.func_149691_a(0, metadata1));
            if (block instanceof ClayContainer && ((ClayContainer)block).getOverlayIcon(0, metadata1) != null) {
                renderer.func_147768_a(block, 0.0, 0.0, 0.0, ((ClayContainer)block).getOverlayIcon(0, metadata1));
            }

            tessellator.func_78381_a();
            tessellator.func_78382_b();
            tessellator.func_78375_b(0.0F, 1.0F, 0.0F);
            renderer.func_147806_b(block, 0.0, 0.0, 0.0, block.func_149691_a(1, metadata1));
            if (block instanceof ClayContainer && ((ClayContainer)block).getOverlayIcon(1, metadata1) != null) {
                renderer.func_147806_b(block, 0.0, 0.0, 0.0, ((ClayContainer)block).getOverlayIcon(1, metadata1));
            }

            tessellator.func_78381_a();
            tessellator.func_78382_b();
            tessellator.func_78375_b(0.0F, 0.0F, -1.0F);
            renderer.func_147761_c(block, 0.0, 0.0, 0.0, block.func_149691_a(2, metadata1));
            if (block instanceof ClayContainer && ((ClayContainer)block).getOverlayIcon(2, metadata1) != null) {
                renderer.func_147761_c(block, 0.0, 0.0, 0.0, ((ClayContainer)block).getOverlayIcon(2, metadata1));
            }

            tessellator.func_78381_a();
            tessellator.func_78382_b();
            tessellator.func_78375_b(0.0F, 0.0F, 1.0F);
            renderer.func_147734_d(block, 0.0, 0.0, 0.0, block.func_149691_a(3, metadata1));
            if (block instanceof ClayContainer && ((ClayContainer)block).getOverlayIcon(3, metadata1) != null) {
                renderer.func_147734_d(block, 0.0, 0.0, 0.0, ((ClayContainer)block).getOverlayIcon(3, metadata1));
            }

            if (block instanceof ClayBuffer || block instanceof MultitrackBuffer) {
                renderer.func_147734_d(block, 0.0, 0.0, 0.0, ((ClayContainer)block).getInsertIcons(null, 0, 0, 0)[0]);
            }

            tessellator.func_78381_a();
            tessellator.func_78382_b();
            tessellator.func_78375_b(-1.0F, 0.0F, 0.0F);
            renderer.func_147798_e(block, 0.0, 0.0, 0.0, block.func_149691_a(4, metadata1));
            if (block instanceof ClayContainer && ((ClayContainer)block).getOverlayIcon(4, metadata1) != null) {
                renderer.func_147798_e(block, 0.0, 0.0, 0.0, ((ClayContainer)block).getOverlayIcon(4, metadata1));
            }

            tessellator.func_78381_a();
            tessellator.func_78382_b();
            tessellator.func_78375_b(1.0F, 0.0F, 0.0F);
            renderer.func_147764_f(block, 0.0, 0.0, 0.0, block.func_149691_a(5, metadata1));
            if (block instanceof ClayContainer && ((ClayContainer)block).getOverlayIcon(5, metadata1) != null) {
                renderer.func_147764_f(block, 0.0, 0.0, 0.0, ((ClayContainer)block).getOverlayIcon(5, metadata1));
            }

            tessellator.func_78381_a();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            renderer.func_147775_a(block);
        }
    }

    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (modelId != this.getRenderId()) {
            return false;
        } else {
            if (block instanceof ClayContainer) {
                if (!((ClayContainer)block).renderAsPipe(world, x, y, z)) {
                    Tessellator tessellator = Tessellator.field_78398_a;
                    int renderMode = !((ClayContainer)block).isTransparent() && !((ClayContainer)block).isOverlayTransparent() ? 2 : block.func_149701_w();
                    boolean underlay = ClayContainer.renderPass == 0 ? !((ClayContainer)block).isTransparent() : ((ClayContainer)block).isTransparent();
                    boolean overlay = ClayContainer.renderPass == 0
                        ? !((ClayContainer)block).isOverlayTransparent()
                        : ((ClayContainer)block).isOverlayTransparent();
                    boolean io = ClayContainer.renderPass == 0;
                    if (underlay) {
                        renderer.func_147784_q(block, x, y, z);
                    }

                    double d = 0.0;
                    if (renderMode == 1) {
                        d = 3.0E-4;
                    }

                    if (renderMode == 0) {
                        tessellator.func_78381_a();
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        GL11.glAlphaFunc(516, 0.1F);
                        tessellator.func_78382_b();
                    }

                    boolean flag = false;
                    float f3 = 0.5F;
                    float f4 = 1.0F;
                    float f5 = 0.8F;
                    float f6 = 0.6F;
                    TileClayContainer te = UtilBuilder.safeGetTileEntity(world, x, y, z) instanceof TileClayContainer
                        ? (TileClayContainer)UtilBuilder.safeGetTileEntity(world, x, y, z)
                        : null;
                    int l = block.func_149677_c(world, x, y, z);
                    IIcon[] insertIcons = ((ClayContainer)block).getInsertIcons(world, x, y, z);
                    IIcon[] extractIcons = ((ClayContainer)block).getExtractIcons(world, x, y, z);
                    IIcon filterIcon = ((ClayContainer)block).FilterIcon;
                    tessellator.func_78380_c(renderer.field_147855_j > 0.0 ? l : block.func_149677_c(renderer.field_147845_a, x, y - 1, z));
                    tessellator.func_78386_a(f3, f3, f3);
                    tessellator.func_78375_b(0.0F, -1.0F, 0.0F);
                    if (((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 0) != null && overlay) {
                        renderer.func_147768_a(
                            block, (double)x, (double)y - d, (double)z, ((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 0)
                        );
                    }

                    if (te != null && io) {
                        int route = te.insertRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 0) - 6];
                        if (insertIcons != null && route >= 0 && route < insertIcons.length) {
                            renderer.func_147768_a(block, (double)x, (double)y - d * 2.0, (double)z, insertIcons[route]);
                        }

                        route = te.extractRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 0) - 6];
                        if (extractIcons != null && route >= 0 && route < extractIcons.length) {
                            renderer.func_147768_a(block, (double)x, (double)y - d * 3.0, (double)z, extractIcons[route]);
                        }

                        if (filterIcon != null && te.filters[UtilDirection.direction2Side(te.getFrontDirection(), 0) - 6] != null) {
                            renderer.func_147768_a(block, (double)x, (double)y - d * 4.0, (double)z, filterIcon);
                        }
                    }

                    tessellator.func_78380_c(renderer.field_147857_k < 1.0 ? l : block.func_149677_c(renderer.field_147845_a, x, y + 1, z));
                    tessellator.func_78386_a(f4, f4, f4);
                    tessellator.func_78375_b(0.0F, 1.0F, 0.0F);
                    if (((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 1) != null && overlay) {
                        renderer.func_147806_b(
                            block, (double)x, (double)y + d, (double)z, ((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 1)
                        );
                    }

                    if (te != null && io) {
                        int routex = te.insertRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 1) - 6];
                        if (insertIcons != null && routex >= 0 && routex < insertIcons.length) {
                            renderer.func_147806_b(block, (double)x, (double)y + d * 2.0, (double)z, insertIcons[routex]);
                        }

                        routex = te.extractRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 1) - 6];
                        if (extractIcons != null && routex >= 0 && routex < extractIcons.length) {
                            renderer.func_147806_b(block, (double)x, (double)y + d * 3.0, (double)z, extractIcons[routex]);
                        }

                        if (filterIcon != null && te.filters[UtilDirection.direction2Side(te.getFrontDirection(), 1) - 6] != null) {
                            renderer.func_147806_b(block, (double)x, (double)y + d * 4.0, (double)z, filterIcon);
                        }
                    }

                    tessellator.func_78380_c(renderer.field_147851_l > 0.0 ? l : block.func_149677_c(renderer.field_147845_a, x, y, z - 1));
                    tessellator.func_78386_a(f5, f5, f5);
                    tessellator.func_78375_b(0.0F, 0.0F, -1.0F);
                    if (((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 2) != null && overlay) {
                        renderer.func_147761_c(
                            block, (double)x, (double)y, (double)z - d, ((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 2)
                        );
                    }

                    if (te != null && io) {
                        int routexx = te.insertRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 2) - 6];
                        if (insertIcons != null && routexx >= 0 && routexx < insertIcons.length) {
                            renderer.func_147761_c(block, (double)x, (double)y, (double)z - d * 2.0, insertIcons[routexx]);
                        }

                        routexx = te.extractRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 2) - 6];
                        if (extractIcons != null && routexx >= 0 && routexx < extractIcons.length) {
                            renderer.func_147761_c(block, (double)x, (double)y, (double)z - d * 3.0, extractIcons[routexx]);
                        }

                        if (filterIcon != null && te.filters[UtilDirection.direction2Side(te.getFrontDirection(), 2) - 6] != null) {
                            renderer.func_147761_c(block, (double)x, (double)y, (double)z - d * 4.0, filterIcon);
                        }
                    }

                    tessellator.func_78380_c(renderer.field_147853_m < 1.0 ? l : block.func_149677_c(renderer.field_147845_a, x, y, z + 1));
                    tessellator.func_78386_a(f5, f5, f5);
                    tessellator.func_78375_b(0.0F, 0.0F, 1.0F);
                    if (((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 3) != null && overlay) {
                        renderer.func_147734_d(
                            block, (double)x, (double)y, (double)z + d, ((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 3)
                        );
                    }

                    if (te != null && io) {
                        int routexxx = te.insertRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 3) - 6];
                        if (insertIcons != null && routexxx >= 0 && routexxx < insertIcons.length) {
                            renderer.func_147734_d(block, (double)x, (double)y, (double)z + d * 2.0, insertIcons[routexxx]);
                        }

                        routexxx = te.extractRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 3) - 6];
                        if (extractIcons != null && routexxx >= 0 && routexxx < extractIcons.length) {
                            renderer.func_147734_d(block, (double)x, (double)y, (double)z + d * 3.0, extractIcons[routexxx]);
                        }

                        if (filterIcon != null && te.filters[UtilDirection.direction2Side(te.getFrontDirection(), 3) - 6] != null) {
                            renderer.func_147734_d(block, (double)x, (double)y, (double)z + d * 4.0, filterIcon);
                        }
                    }

                    tessellator.func_78380_c(renderer.field_147859_h > 0.0 ? l : block.func_149677_c(renderer.field_147845_a, x - 1, y, z));
                    tessellator.func_78386_a(f6, f6, f6);
                    tessellator.func_78375_b(-1.0F, 0.0F, 0.0F);
                    if (((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 4) != null && overlay) {
                        renderer.func_147798_e(
                            block, (double)x - d, (double)y, (double)z, ((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 4)
                        );
                    }

                    if (te != null && io) {
                        int routexxxx = te.insertRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 4) - 6];
                        if (insertIcons != null && routexxxx >= 0 && routexxxx < insertIcons.length) {
                            renderer.func_147798_e(block, (double)x - d * 2.0, (double)y, (double)z, insertIcons[routexxxx]);
                        }

                        routexxxx = te.extractRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 4) - 6];
                        if (extractIcons != null && routexxxx >= 0 && routexxxx < extractIcons.length) {
                            renderer.func_147798_e(block, (double)x - d * 3.0, (double)y, (double)z, extractIcons[routexxxx]);
                        }

                        if (filterIcon != null && te.filters[UtilDirection.direction2Side(te.getFrontDirection(), 4) - 6] != null) {
                            renderer.func_147798_e(block, (double)x - d * 4.0, (double)y, (double)z, filterIcon);
                        }
                    }

                    tessellator.func_78380_c(renderer.field_147861_i < 1.0 ? l : block.func_149677_c(renderer.field_147845_a, x + 1, y, z));
                    tessellator.func_78386_a(f6, f6, f6);
                    tessellator.func_78375_b(1.0F, 0.0F, 0.0F);
                    if (((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 5) != null && overlay) {
                        renderer.func_147764_f(
                            block, (double)x + d, (double)y, (double)z, ((ClayContainer)block).getOverlayIcon(renderer.field_147845_a, x, y, z, 5)
                        );
                    }

                    if (te != null && io) {
                        int routexxxxx = te.insertRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 5) - 6];
                        if (insertIcons != null && routexxxxx >= 0 && routexxxxx < insertIcons.length) {
                            renderer.func_147764_f(block, (double)x + d * 2.0, (double)y, (double)z, insertIcons[routexxxxx]);
                        }

                        routexxxxx = te.extractRoutes[UtilDirection.direction2Side(te.getFrontDirection(), 5) - 6];
                        if (extractIcons != null && routexxxxx >= 0 && routexxxxx < extractIcons.length) {
                            renderer.func_147764_f(block, (double)x + d * 3.0, (double)y, (double)z, extractIcons[routexxxxx]);
                        }

                        if (filterIcon != null && te.filters[UtilDirection.direction2Side(te.getFrontDirection(), 5) - 6] != null) {
                            renderer.func_147764_f(block, (double)x + d * 4.0, (double)y, (double)z, filterIcon);
                        }
                    }

                    if (renderMode == 0) {
                        tessellator.func_78381_a();
                        GL11.glDisable(3042);
                        GL11.glAlphaFunc(516, 0.5F);
                        tessellator.func_78382_b();
                    }
                } else {
                    boolean pipingMode = ClayiumCore.proxy.renderAsPipingMode();
                    ClayContainer block1 = (ClayContainer)block;
                    TileClayContainer tex = UtilBuilder.safeGetTileEntity(world, x, y, z) instanceof TileClayContainer
                        ? (TileClayContainer)UtilBuilder.safeGetTileEntity(world, x, y, z)
                        : null;
                    Tessellator tessellatorx = Tessellator.field_78398_a;
                    float o = ClayContainer.pipeWidth;
                    renderer.field_152631_f = true;
                    block.func_149676_a(0.5F - o, 0.5F - o, 0.5F - o, 0.5F + o, 0.5F + o, 0.5F + o);
                    renderer.func_147775_a(block);
                    renderer.func_147784_q(block, x, y, z);
                    IIcon[] insertIconsx = block1.getInsertPipeIcons(world, x, y, z);
                    IIcon[] extractIconsx = block1.getExtractPipeIcons(world, x, y, z);
                    UtilDirection[] directions = new UtilDirection[]{
                        UtilDirection.NORTH, UtilDirection.SOUTH, UtilDirection.EAST, UtilDirection.WEST, UtilDirection.UP, UtilDirection.DOWN
                    };

                    for (UtilDirection direction : directions) {
                        if (block1.checkPipeConnection(world, x, y, z, direction)) {
                            block.func_149676_a(
                                0.5F - o + (direction.offsetX == 1 ? o * 2.0F : 0.0F) - (direction.offsetX == -1 ? 0.5F - o : 0.0F),
                                0.5F - o + (direction.offsetY == 1 ? o * 2.0F : 0.0F) - (direction.offsetY == -1 ? 0.5F - o : 0.0F),
                                0.5F - o + (direction.offsetZ == 1 ? o * 2.0F : 0.0F) - (direction.offsetZ == -1 ? 0.5F - o : 0.0F),
                                0.5F + o - (direction.offsetX == -1 ? o * 2.0F : 0.0F) + (direction.offsetX == 1 ? 0.5F - o : 0.0F),
                                0.5F + o - (direction.offsetY == -1 ? o * 2.0F : 0.0F) + (direction.offsetY == 1 ? 0.5F - o : 0.0F),
                                0.5F + o - (direction.offsetZ == -1 ? o * 2.0F : 0.0F) + (direction.offsetZ == 1 ? 0.5F - o : 0.0F)
                            );
                            renderer.func_147775_a(block);
                            renderer.func_147784_q(block, x, y, z);
                            if (pipingMode) {
                                int routexxxxxx = tex.insertRoutes[UtilDirection.direction2Side(tex.getFrontDirection(), direction.ordinal()) - 6];
                                if (insertIconsx != null && routexxxxxx >= 0 && routexxxxxx < insertIconsx.length) {
                                    renderer.func_147757_a(insertIconsx[routexxxxxx]);
                                    renderer.func_147784_q(block, x, y, z);
                                }

                                routexxxxxx = tex.extractRoutes[UtilDirection.direction2Side(tex.getFrontDirection(), direction.ordinal()) - 6];
                                if (extractIconsx != null && routexxxxxx >= 0 && routexxxxxx < extractIconsx.length) {
                                    renderer.func_147757_a(extractIconsx[routexxxxxx]);
                                    renderer.func_147784_q(block, x, y, z);
                                }

                                if (renderer.func_147744_b()) {
                                    renderer.func_147771_a();
                                }
                            }
                        }

                        block.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                    }

                    renderer.field_152631_f = false;
                }
            }

            return true;
        }
    }

    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    public int getRenderId() {
        return ClayiumCore.clayContainerRenderId;
    }
}
