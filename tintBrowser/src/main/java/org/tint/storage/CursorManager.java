package org.tint.storage;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import org.tint.utils.Function;
import org.tint.utils.Predicate;

/**
 * User: Abhijit
 * Date: 2016-06-12
 */
public class CursorManager {
    public static abstract class AbstractCursor<T> {
        private final int offset;
        private final int max;
        private final Function<Cursor, T> cursorTFunction;

        protected AbstractCursor(int offset, int max, Function<Cursor, T> cursorTFunction) {
            this.offset = offset;
            this.max = max;
            this.cursorTFunction = cursorTFunction;
        }

        public final void query(Cursor cursor) {
            query(cursor, offset, max);
        }

        public final void query(final Cursor cursor, int offset, final int max) {
            if (cursor != null && cursor.moveToPosition(offset)) {
                Predicate<Integer> p;
                if (max != -1) {
                    p = new Predicate<Integer>() {
                        @Override
                        public boolean isSatisfiedBy(Integer itemsCollected) {
                            return itemsCollected < max && cursor.moveToPosition(itemsCollected);
                        }
                    };
                } else {
                    p = new Predicate() {
                        @Override
                        public boolean isSatisfiedBy(Object o) {
                            return cursor.moveToNext();
                        }
                    };
                }
                int itemsCollected = 0;
                while (p.isSatisfiedBy(itemsCollected)) {
                    execute(cursor);
                    itemsCollected++;
                }
            }
        }

        protected void execute(Cursor cursor) {
            getOutputFunction().apply(cursorTFunction.apply(cursor));
        }

        protected abstract Function<T, Void> getOutputFunction();
    }

    public static class ListCursor<T> extends AbstractCursor<T> {
        private final List<T> list = new ArrayList<T>();
        private final Function<Cursor, T> function;

        public ListCursor(Function<Cursor, T> function) {
            super(0, -1, function);
            this.function = function;
        }

        @Override
        protected void execute(Cursor cursor) {
            list.add(function.apply(cursor));
        }

        public List<T> getList() {
            return list;
        }

        @Override
        protected Function<T, Void> getOutputFunction() {
            return new Function<T, Void>() {
                @Override
                public Void apply(T t) {
                    list.add(t);
                    return null;
                }
            };
        }
    }

    public static class SingleItemCursor<T> extends AbstractCursor<T> {
        private T t;
        private final Function<Cursor, T> function;

        public SingleItemCursor(Function<Cursor, T> function) {
            super(0, 1, function);
            this.function = function;
        }

        @Override
        protected void execute(Cursor cursor) {
            t = function.apply(cursor);
        }

        @Override
        protected Function<T, Void> getOutputFunction() {
            return new Function<T, Void>() {
                @Override
                public Void apply(T t) {
                    SingleItemCursor.this.t = t;
                    return null;
                }
            };
        }

        public T getT() {
            return t;
        }
    }
}