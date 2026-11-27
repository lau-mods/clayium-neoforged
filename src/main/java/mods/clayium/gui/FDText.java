package mods.clayium.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class FDText<T> extends FDWithHandler<FDText<T>, T> {
    public static FontRenderer renderer = Minecraft.func_71410_x().field_71466_p;
    public String defaultString = "";
    public int defaultX = 0;
    public int defaultY = 0;
    public int defaultColor = -16777216;
    public int defaultMaximumLength = -1;
    public static FDText<Object> INSTANCE = new FDText<>();

    public FDText(FDWithHandler.IFunctionalIterable<FDWithHandler.IFDHandler<FDText<T>, T>, T> defaultHandlers) {
        super(defaultHandlers);
    }

    public FDText(List<FDWithHandler.IFDHandler<FDText<T>, T>> defaultHandlers) {
        super(defaultHandlers);
    }

    public FDText(FDWithHandler.IFDHandler<FDText<T>, T> defaultHandler) {
        super(defaultHandler);
    }

    public FDText() {
        super();
    }

    public T render(T param) {
        param = this.preRender(param);
        int maxLength = this.getMaximumLength(param);
        if (maxLength <= 0) {
            renderer.func_85187_a(this.getString(param), this.getX(param), this.getY(param), this.getColor(param), false);
        } else {
            renderer.func_78279_b(this.getString(param), this.getX(param), this.getY(param), maxLength, this.getColor(param));
        }

        return this.postRender(param);
    }

    public T preRender(T param) {
        return param;
    }

    public T postRender(T param) {
        return param;
    }

    public String getString(T param) {
        return this.defaultString;
    }

    public int getX(T param) {
        return this.defaultX;
    }

    public int getY(T param) {
        return this.defaultY;
    }

    public int getColor(T param) {
        return this.defaultColor;
    }

    public int getMaximumLength(T param) {
        return this.defaultMaximumLength;
    }

    public FDText<T> preApplyHandler(T param) {
        return this;
    }

    public FDText<T> getFDText(List<FDText.FDTextHandler<T>> preHandlers, int x, int y, int color, int maxLength, List<FDText.FDTextHandler<T>> postHandlers) {
        List<FDText.FDTextHandler<T>> list = new ArrayList<>();
        if (preHandlers != null) {
            list.addAll(preHandlers);
        }

        FDText.FDTextHandler<T> aHandler = new FDText.FDTextHandler<>();
        aHandler.handleX = aHandler.handleY = aHandler.handleColor = aHandler.handleMaximumLength = true;
        aHandler.defaultX = x;
        aHandler.defaultY = y;
        aHandler.defaultColor = color;
        aHandler.defaultMaximumLength = maxLength;
        list.add(aHandler);
        if (postHandlers != null) {
            list.addAll(postHandlers);
        }

        return new FDText<>(list);
    }

    public FDText<T> getFDText(FDText.FDTextHandler<T> preHandler, int x, int y, int color, int maxLength) {
        return this.getFDText(Arrays.asList(new FDText.FDTextHandler[]{preHandler}), x, y, color, maxLength, null);
    }

    public static FDText.FDTextHandler<Object> getStringHandler(String string) {
        FDText.FDTextHandler<Object> aHandler = new FDText.FDTextHandler<>();
        aHandler.handleString = true;
        aHandler.defaultString = string;
        return aHandler;
    }

    public static class FDTextHandler<T> implements FDWithHandler.IFDHandler<FDText<T>, T> {
        public String defaultString = "";
        public int defaultX = 0;
        public int defaultY = 0;
        public int defaultColor = -16777216;
        public int defaultMaximumLength = -1;
        public boolean handleString = false;
        public boolean handleX = false;
        public boolean handleY = false;
        public boolean handleColor = false;
        public boolean handleMaximumLength = false;

        public FDTextHandler() {
            super();
        }

        public T apply(FDText<T> functional, T param) {
            if (this.handleString) {
                functional.defaultString = this.applyString(param);
            }

            if (this.handleX) {
                functional.defaultX = this.applyX(param);
            }

            if (this.handleY) {
                functional.defaultY = this.applyY(param);
            }

            if (this.handleColor) {
                functional.defaultColor = this.applyColor(param);
            }

            if (this.handleMaximumLength) {
                functional.defaultMaximumLength = this.applyMaximumLength(param);
            }

            return param;
        }

        public String applyString(T param) {
            return this.defaultString;
        }

        public int applyX(T param) {
            return this.defaultX;
        }

        public int applyY(T param) {
            return this.defaultY;
        }

        public int applyColor(T param) {
            return this.defaultColor;
        }

        public int applyMaximumLength(T param) {
            return this.defaultMaximumLength;
        }
    }
}
