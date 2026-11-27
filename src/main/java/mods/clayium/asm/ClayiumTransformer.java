package mods.clayium.asm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ClayiumTransformer implements IClassTransformer, Opcodes {
    private static List<String> targets = new ArrayList<>();
    private static List<ClayiumTransformer.MethodKey> methodList = new ArrayList<>();
    private AbstractInsnNode[] test;
    private AbstractInsnNode[] test2;
    private Logger logger = LogManager.getLogger("ClayiumTransformer");

    public ClayiumTransformer() {
        super();
    }

    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (!targets.contains(name) && !targets.contains(transformedName)) {
            return bytes;
        } else {
            try {
                return this.hookMethod(bytes);
            } catch (Exception var5) {
                var5.printStackTrace();
                throw new RuntimeException("failed : ClayiumTransformer loading  name=" + name + " transformedName=" + transformedName + "", var5);
            }
        }
    }

    private byte[] replaceClass(byte[] bytes, String targetClass) throws IOException {
        ZipFile zf = null;
        InputStream zi = null;

        try {
            zf = new ZipFile(ClayiumTransformerLoadingPlugin.location);
            ZipEntry ze = zf.getEntry(targetClass);
            if (ze != null) {
                zi = zf.getInputStream(ze);
                int len = (int)ze.getSize();
                bytes = new byte[len];
                int MAX_READ = 1024;
                int readed = 0;

                while (readed < len) {
                    int readsize = MAX_READ;
                    if (len - readed < MAX_READ) {
                        readsize = len - readed;
                    }

                    int ret = zi.read(bytes, readed, readsize);
                    if (ret == -1) {
                        break;
                    }

                    readed += ret;
                }
            }

            return bytes;
        } catch (Exception var15) {
            zi = new FileInputStream(ClayiumTransformerLoadingPlugin.location + "\\" + targetClass);
            int len = zi.available();
            if (zi != null) {
                bytes = new byte[len];
                int MAX_READ = 1024;
                int readed = 0;

                while (readed < len) {
                    int readsizex = MAX_READ;
                    if (len - readed < MAX_READ) {
                        readsizex = len - readed;
                    }

                    int ret = zi.read(bytes, readed, readsizex);
                    if (ret == -1) {
                        break;
                    }

                    readed += ret;
                }

                zi.close();
                return bytes;
            }
        } finally {
            if (zi != null) {
                zi.close();
            }

            if (zf != null) {
                zf.close();
            }
        }

        return bytes;
    }

    private MethodNode getMethodNode(ClassNode classNode, String className, String methodName, String methodDesc) {
        if (classNode != null && classNode.name != null && classNode.name.equals(className)) {
            ClayiumTransformer.MethodKey key = new ClayiumTransformer.MethodKey(className, methodName, methodDesc);
            if (methodList.contains(key)) {
                return null;
            } else {
                for (MethodNode curMnode : classNode.methods) {
                    if (methodName.equals(curMnode.name) && methodDesc.equals(curMnode.desc)) {
                        methodList.add(key);
                        this.logger.info("Get MethodNode (" + className + ", " + methodName + ", " + methodDesc + ")");
                        return curMnode;
                    }
                }

                return null;
            }
        } else {
            return null;
        }
    }

    private AbstractInsnNode convertInsnD2F(AbstractInsnNode insn) {
        if (insn instanceof LdcInsnNode && ((LdcInsnNode)insn).cst instanceof Double) {
            double d = (Double)((LdcInsnNode)insn).cst;
            return (AbstractInsnNode)(d == 2.0 ? new InsnNode(13) : new LdcInsnNode((float)d));
        } else if (insn.getOpcode() == 14) {
            return new InsnNode(11);
        } else {
            return insn.getOpcode() == 15 ? new InsnNode(12) : null;
        }
    }

    private byte[] hookMethod(byte[] bytes) {
        boolean flag = false;
        boolean deobf = false;
        ClassNode cnode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(cnode, 0);
        MethodNode mnode = this.getMethodNode(cnode, "net/minecraft/client/multiplayer/PlayerControllerMP", "getBlockReachDistance", "()F");
        if (mnode == null) {
            mnode = this.getMethodNode(cnode, "net/minecraft/client/multiplayer/PlayerControllerMP", "func_78757_d", "()F");
            deobf = false;
        } else {
            deobf = true;
        }

        if (mnode != null) {
            flag = true;
            List<AbstractInsnNode> targetInsnList = new ArrayList<>();

            for (AbstractInsnNode inst : mnode.instructions.toArray()) {
                if (inst.getNext() != null && inst.getNext().getOpcode() == 174) {
                    targetInsnList.add(inst);
                }
            }

            for (AbstractInsnNode targetInsn : targetInsnList) {
                mnode.instructions
                    .insert(targetInsn, new MethodInsnNode(184, "mods/clayium/item/gadget/GadgetLongArm", "hookBlockReachDistance", "(F)F", false));
            }
        }

        mnode = this.getMethodNode(cnode, "net/minecraft/client/renderer/EntityRenderer", "getMouseOver", "(F)V");
        if (mnode == null) {
            mnode = this.getMethodNode(cnode, "net/minecraft/client/renderer/EntityRenderer", "func_78473_a", "(F)V");
            deobf = false;
        } else {
            deobf = true;
        }

        if (mnode != null) {
            flag = true;
            List<AbstractInsnNode> targetInsnList = new ArrayList<>();
            int d0var = -1;
            int d1var = -1;

            for (AbstractInsnNode insn : mnode.instructions.toArray()) {
                if (d0var == -1
                    && insn.getOpcode() == 182
                    && insn instanceof MethodInsnNode
                    && ((MethodInsnNode)insn).desc.equals("()F")
                    && ((MethodInsnNode)insn).name.equals(deobf ? "getBlockReachDistance" : "func_78757_d")
                    && ((MethodInsnNode)insn).owner.equals("net/minecraft/client/multiplayer/PlayerControllerMP")) {
                    AbstractInsnNode insn0 = insn.getNext();
                    if (insn0 != null && insn0.getOpcode() == 141) {
                        insn0 = insn0.getNext();
                        if (insn0 != null && insn0.getOpcode() == 57 && insn0 instanceof VarInsnNode) {
                            d0var = ((VarInsnNode)insn0).var;
                        }
                    }
                }

                if (d1var == -1 && insn.getOpcode() == 24 && insn instanceof VarInsnNode && ((VarInsnNode)insn).var == d0var) {
                    AbstractInsnNode insn0 = insn.getNext();
                    if (insn0 != null && insn0.getOpcode() == 57 && insn0 instanceof VarInsnNode) {
                        d1var = ((VarInsnNode)insn0).var;
                    }
                }

                if (insn.getOpcode() == 182
                    && insn instanceof MethodInsnNode
                    && ((MethodInsnNode)insn).desc.equals("(DDD)Lnet/minecraft/util/Vec3;")
                    && ((MethodInsnNode)insn).name.equals(deobf ? "addVector" : "func_72441_c")
                    && ((MethodInsnNode)insn).owner.equals("net/minecraft/util/Vec3")) {
                    d0var = -2;
                    d1var = -2;
                }

                AbstractInsnNode insn0 = insn.getNext();
                if (insn0 != null
                    && insn0.getOpcode() == 57
                    && insn0 instanceof VarInsnNode
                    && d0var == ((VarInsnNode)insn0).var
                    && this.convertInsnD2F(insn) != null) {
                    targetInsnList.add(insn);
                }

                insn0 = insn.getNext();
                if (insn0 != null
                    && insn0.getOpcode() == 57
                    && insn0 instanceof VarInsnNode
                    && d1var == ((VarInsnNode)insn0).var
                    && this.convertInsnD2F(insn) != null) {
                    targetInsnList.add(insn);
                }

                insn0 = insn.getPrevious();
                if (insn0 != null && insn0.getOpcode() == 24 && insn0 instanceof VarInsnNode && d0var == ((VarInsnNode)insn0).var) {
                    insn0 = insn.getNext();
                    if (insn0 != null && insn0.getOpcode() == 151 && this.convertInsnD2F(insn) != null) {
                        targetInsnList.add(insn);
                    }
                }
            }

            for (AbstractInsnNode targetInsn : targetInsnList) {
                AbstractInsnNode insn0x = this.convertInsnD2F(targetInsn);
                mnode.instructions.set(targetInsn, insn0x);
                mnode.instructions.insert(insn0x, new MethodInsnNode(184, "mods/clayium/item/gadget/GadgetLongArm", "hookBlockReachDistance", "(F)F", false));
                mnode.instructions.insert(insn0x.getNext(), new InsnNode(141));
            }

            AbstractInsnNode[] array = mnode.instructions.toArray();
            this.logger.info("Transformed " + mnode.name);
        }

        mnode = this.getMethodNode(
            cnode, "net/minecraft/network/NetHandlerPlayServer", "processUseEntity", "(Lnet/minecraft/network/play/client/C02PacketUseEntity;)V"
        );
        if (mnode == null) {
            mnode = this.getMethodNode(
                cnode, "net/minecraft/network/NetHandlerPlayServer", "func_147340_a", "(Lnet/minecraft/network/play/client/C02PacketUseEntity;)V"
            );
            deobf = false;
        } else {
            deobf = true;
        }

        if (mnode != null) {
            flag = true;
            List<AbstractInsnNode> targetInsnList = new ArrayList<>();
            int flagvar = -1;
            int d0var = -1;

            for (AbstractInsnNode insn : mnode.instructions.toArray()) {
                if (flagvar == -1
                    && d0var == -1
                    && insn.getOpcode() == 182
                    && insn instanceof MethodInsnNode
                    && ((MethodInsnNode)insn).desc.equals("(Lnet/minecraft/entity/Entity;)Z")
                    && ((MethodInsnNode)insn).name.equals(deobf ? "canEntityBeSeen" : "func_70685_l")
                    && ((MethodInsnNode)insn).owner.equals("net/minecraft/entity/player/EntityPlayerMP")) {
                    AbstractInsnNode insn0x = insn.getNext();
                    if (insn0x != null && insn0x.getOpcode() == 54 && insn0x instanceof VarInsnNode) {
                        flagvar = ((VarInsnNode)insn0x).var;
                    }
                }

                AbstractInsnNode insn0x = insn.getNext();
                if (flagvar >= 0 && d0var == -1 && insn0x != null && insn0x.getOpcode() == 57 && insn0x instanceof VarInsnNode) {
                    if (this.convertInsnD2F(insn) != null) {
                        d0var = ((VarInsnNode)insn0x).var;
                        targetInsnList.add(insn);
                    }
                } else if (insn0x != null
                    && insn0x.getOpcode() == 57
                    && insn0x instanceof VarInsnNode
                    && d0var == ((VarInsnNode)insn0x).var
                    && this.convertInsnD2F(insn) != null) {
                    targetInsnList.add(insn);
                }
            }

            for (AbstractInsnNode targetInsn : targetInsnList) {
                AbstractInsnNode insn0x = this.convertInsnD2F(targetInsn);
                mnode.instructions.set(targetInsn, insn0x);
                mnode.instructions.insert(insn0x, new VarInsnNode(25, 0));
                insn0x = insn0x.getNext();
                mnode.instructions
                    .insert(
                        insn0x,
                        new FieldInsnNode(
                            180,
                            "net/minecraft/network/NetHandlerPlayServer",
                            deobf ? "playerEntity" : "field_147369_b",
                            "Lnet/minecraft/entity/player/EntityPlayerMP;"
                        )
                    );
                insn0x = insn0x.getNext();
                mnode.instructions
                    .insert(
                        insn0x,
                        new MethodInsnNode(
                            184, "mods/clayium/item/gadget/GadgetLongArm", "hookBlockReachDistanceSq", "(FLnet/minecraft/entity/player/EntityPlayer;)F", false
                        )
                    );
                insn0x = insn0x.getNext();
                mnode.instructions.insert(insn0x, new InsnNode(141));
            }

            AbstractInsnNode[] array = mnode.instructions.toArray();
            this.logger.info("Transformed " + mnode.name);
        }

        if (flag) {
            ClassWriter cw = new ClassWriter(3);
            cnode.accept(cw);
            bytes = cw.toByteArray();
        }

        return bytes;
    }

    static {
        targets.add("net.minecraft.client.multiplayer.PlayerControllerMP");
        targets.add("net.minecraft.client.renderer.EntityRenderer");
        targets.add("net.minecraft.network.NetHandlerPlayServer");
    }

    private static class MethodKey {
        String className;
        String methodName;
        String methodDesc;

        public MethodKey(String className, String methodName, String methodDesc) {
            super();
            this.className = className;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + (this.className == null ? 0 : this.className.hashCode());
            result = 31 * result + (this.methodDesc == null ? 0 : this.methodDesc.hashCode());
            return 31 * result + (this.methodName == null ? 0 : this.methodName.hashCode());
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (this.getClass() != obj.getClass()) {
                return false;
            } else {
                ClayiumTransformer.MethodKey other = (ClayiumTransformer.MethodKey)obj;
                if (this.className == null) {
                    if (other.className != null) {
                        return false;
                    }
                } else if (!this.className.equals(other.className)) {
                    return false;
                }

                if (this.methodDesc == null) {
                    if (other.methodDesc != null) {
                        return false;
                    }
                } else if (!this.methodDesc.equals(other.methodDesc)) {
                    return false;
                }

                if (this.methodName == null) {
                    if (other.methodName != null) {
                        return false;
                    }
                } else if (!this.methodName.equals(other.methodName)) {
                    return false;
                }

                return true;
            }
        }
    }
}
