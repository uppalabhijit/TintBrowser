package org.tint.storage;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import org.tint.utils.Function;
import org.tint.utils.IOUtils;
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

        public final List<T> execute(Cursor cursor) {
            return execute(cursor, offset, max);
        }

        public final List<T> execute(final Cursor cursor, int offset, final int max) {
            List<T> list = new ArrayList<T>();
            try {
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
                        list.add(cursorTFunction.apply(cursor));
                        itemsCollected++;
                    }
                }
            } finally {
                IOUtils.closeQuietly(cursor);
            }
            return list;
        }
    }

    public static class ListCursor<T> extends AbstractCursor<T> {
        private final List<T> list = new ArrayList<T>();
        private final Function<Cursor, T> function;

        public ListCursor(Function<Cursor, T> function) {
            super(0, -1, function);
            this.function = function;
        }

        public final List<T> query(Cursor cursor) {
            return execute(cursor);
        }
    }

    public static class SingleItemCursor<T> extends AbstractCursor<T> {
        private T t;
        private final Function<Cursor, T> function;

        public SingleItemCursor(Function<Cursor, T> function) {
            super(0, 1, function);
            this.function = function;
        }

        public final T query(Cursor cursor) {
            List<T> list = execute(cursor);
            if (list.size() > 0) {
                return list.get(0);
            }
            return null;
        }
    }
}