package mods.clayium.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import mods.clayium.core.ClayiumCore;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StatCollector;

public class UtilLocale {
    static String[] CENumerals = new String[]{"u", "m", "", "k", "M", "G", "T", "P", "E", "Z", "Y"};
    static String[] SNumerals = new String[]{"", "k", "M", "G", "T", "P", "E", "Z", "Y"};
    private static final int maxLineTooltip = 12;

    public UtilLocale() {
        super();
    }

    public static String ClayEnergyNumeral(double ce, boolean flag) {
        double n = ce * 10.0;
        String s = "";
        if (n == 0.0) {
            return String.valueOf(n);
        } else {
            if (n < 0.0) {
                n--;
                s = "-";
            }

            int k = (int)Math.floor(Math.log10(n));
            int p = Math.min(k / 3, CENumerals.length - 1);
            int d = (int)(n * 1000.0 / Math.pow(10.0, (double)(p * 3)));
            return s + ClayEnergyNumeral(d, p, p == 0 || flag);
        }
    }

    public static String ClayEnergyNumeral(double ce) {
        return ClayEnergyNumeral(ce, true);
    }

    public static String ClayEnergyNumeral(long ce, boolean flag) {
        long n = ce * 10L;
        String s = "";
        if (n == 0L) {
            return String.valueOf(n);
        } else {
            if (n < 0L) {
                n--;
                s = "-";
            }

            int k = (int)Math.floor(Math.log10((double)n));
            int p = Math.min(k / 3, CENumerals.length - 1);
            int d = (int)((double)n * 1000.0 / Math.pow(10.0, (double)(p * 3)));
            return s + ClayEnergyNumeral(d, p, p == 0 || flag);
        }
    }

    public static String ClayEnergyNumeral(long ce) {
        return ClayEnergyNumeral(ce, true);
    }

    protected static String ClayEnergyNumeral(int d, int p, boolean flag) {
        if (d % 10 != 0 || !flag) {
            return d / 1000 + "." + d / 100 % 10 + d / 10 % 10 + d % 10 + CENumerals[p];
        } else if (d % 100 != 0) {
            return d / 1000 + "." + d / 100 % 10 + d / 10 % 10 + CENumerals[p];
        } else {
            return d % 1000 != 0 ? d / 1000 + "." + d / 100 % 10 + CENumerals[p] : d / 1000 + CENumerals[p];
        }
    }

    protected static String StackSizeNumeral(long stackSize, boolean flag) {
        String s = "";
        long n = stackSize;
        if (stackSize == 0L) {
            return String.valueOf(stackSize);
        } else {
            if (stackSize < 0L) {
                n = stackSize - 1L;
                s = "-";
            }

            int k = (int)Math.floor(Math.log10((double)n));
            if (k < 5) {
                return s + String.valueOf(n);
            } else {
                int p = Math.min(k / 3, SNumerals.length - 1);
                int d = (int)((double)n / Math.pow(10.0, (double)(k - 2)));
                boolean flag1 = flag && k % 3 <= 1 && d % 10 == 0;
                boolean flag2 = flag1 && k % 3 == 0 && d / 10 % 10 == 0;
                return s
                    + String.valueOf(d / 100)
                    + (flag2 ? "" : (k % 3 == 0 ? "." : "") + String.valueOf(d / 10 % 10))
                    + (flag1 ? "" : (k % 3 == 1 ? "." : "") + String.valueOf(d % 10))
                    + SNumerals[p];
            }
        }
    }

    public static String StackSizeNumeral(long stackSize) {
        return StackSizeNumeral(stackSize, false);
    }

    public static String CAResonanceNumeral(double resonance) {
        if (resonance < 1.0) {
            return String.format("%.4f", resonance);
        } else if (resonance < 10.0) {
            return String.format("%.3f", resonance);
        } else if (resonance < 100.0) {
            return String.format("%.2f", resonance);
        } else {
            return resonance < 1000.0 ? String.format("%.1f", resonance) : StackSizeNumeral((long)resonance);
        }
    }

    public static String laserNumeral(long laser) {
        return StackSizeNumeral(laser);
    }

    @SideOnly(Side.CLIENT)
    public static String laserGui(long laser) {
        return I18n.func_135052_a("gui.Common.clayLaser", new Object[]{laserNumeral(laser)});
    }

    @SideOnly(Side.CLIENT)
    public static String tierGui(int tier) {
        return I18n.func_135052_a("gui.Common.tier", new Object[]{tier});
    }

    public static String craftTimeNumeral(long craftTime) {
        return StackSizeNumeral(craftTime, true);
    }

    public static String rfNumeral(long rf) {
        return String.format("%,d", rf);
    }

    public static String localizeUnsafe(String str) {
        String ret = StatCollector.func_74838_a(str);
        return ret != null && ret != "" && !ret.equals(str) ? ret : null;
    }

    public static String localizeAndFormat(String str, Object... args) {
        String ret = localizeUnsafe(str);
        if (ret == null) {
            return null;
        } else {
            try {
                return String.format(ret, args);
            } catch (IllegalFormatException var4) {
                ClayiumCore.logger.catching(var4);
                return str;
            }
        }
    }

    public static boolean canLocalize(String str) {
        return StatCollector.func_94522_b(str);
    }

    public static List<String> localizeTooltip(String str) {
        boolean flag = true;
        int i = 0;
        List<String> ret = new ArrayList<>();

        while (flag && i < 12) {
            String loc = localizeUnsafe(str + ".line" + ++i);
            if (loc != null) {
                if (loc.equals("__DETAIL__")) {
                    if (!GuiScreen.func_146272_n()) {
                        ret.add(localizeUnsafe("tooltip.HoldShiftForDetails"));
                        flag = false;
                    }
                } else {
                    ret.add(loc);
                }
            } else {
                flag = false;
            }
        }

        return ret;
    }
}
