package mods.clayium.gui;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class FDWithHandler<S, T> implements IFunctionalDrawer<T> {
    protected FDWithHandler.IFunctionalIterable<FDWithHandler.IFDHandler<S, T>, T> defaultHandlers = null;

    public FDWithHandler(FDWithHandler.IFunctionalIterable<FDWithHandler.IFDHandler<S, T>, T> defaultHandlers) {
        super();
        this.defaultHandlers = defaultHandlers;
    }

    public FDWithHandler(List<FDWithHandler.IFDHandler<S, T>> defaultHandlers) {
        this(new FDWithHandler.FunctionalList<>(defaultHandlers));
    }

    public FDWithHandler(FDWithHandler.IFDHandler<S, T> defaultHandler) {
        this(Arrays.asList(new FDWithHandler.IFDHandler[]{defaultHandler}));
    }

    public FDWithHandler() {
        this((FDWithHandler.IFDHandler<S, T>)null);
    }

    public T draw(T param) {
        return this.render(this.applyHandler(param));
    }

    public abstract T render(T var1);

    public T applyHandler(T param) {
        FDWithHandler.IFunctionalIterable<FDWithHandler.IFDHandler<S, T>, T> iterable = this.getHandlerIterator(param);
        if (iterable == null) {
            return param;
        } else {
            FDWithHandler.IFunctionalIterator<FDWithHandler.IFDHandler<S, T>, T> iterator = iterable.iterator(param);
            if (iterator == null) {
                return param;
            } else {
                while (iterator.hasNext(param)) {
                    FDWithHandler.IFDHandler<S, T> handler = iterator.next(param);
                    param = handler.apply(this.preApplyHandler(param), param);
                }

                return param;
            }
        }
    }

    public abstract S preApplyHandler(T var1);

    public FDWithHandler.IFunctionalIterable<FDWithHandler.IFDHandler<S, T>, T> getHandlerIterator(T param) {
        return this.defaultHandlers;
    }

    public static class FunctionalIterator<S, T> implements FDWithHandler.IFunctionalIterator<S, T> {
        Iterator<S> iterator;

        public FunctionalIterator(Iterator<S> iterator) {
            super();
            this.iterator = iterator;
        }

        public boolean hasNext(T param) {
            return this.iterator.hasNext();
        }

        public S next(T param) {
            return this.iterator.next();
        }
    }

    public static class FunctionalList<S, T> implements FDWithHandler.IFunctionalIterable<S, T> {
        Iterable<S> iterable;

        public FunctionalList(Iterable<S> iterable) {
            super();
            this.iterable = iterable;
        }

        public FDWithHandler.IFunctionalIterator<S, T> iterator(T param) {
            return new FDWithHandler.FunctionalIterator<>(this.iterable.iterator());
        }
    }

    public interface IFDHandler<S, T> {
        T apply(S var1, T var2);
    }

    public interface IFunctionalIterable<S, T> {
        FDWithHandler.IFunctionalIterator<S, T> iterator(T var1);
    }

    public interface IFunctionalIterator<S, T> {
        boolean hasNext(T var1);

        S next(T var1);
    }
}
