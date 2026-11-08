package top.lqsnow.blockracing.toolkit.menu;

import java.util.function.Predicate;

/** Keeps a legacy storage item safe while its slot is used for navigation. */
public final class ReservedInventorySlot<T> {
    private final int slot;
    private T pending;
    public ReservedInventorySlot(int slot) { this.slot = slot; }

    public T[] install(T[] contents, T control, Predicate<T> isControl, Predicate<T> isEmpty) {
        T[] result = contents.clone();
        T original = result[slot];
        if (!isEmpty.test(original) && !isControl.test(original)) {
            if (pending != null) throw new IllegalStateException("Navigation slot contains unexpected storage");
            pending = original;
        }
        result[slot] = control;
        return flush(result, isEmpty);
    }

    public T[] flush(T[] contents, Predicate<T> isEmpty) {
        if (pending == null) return contents;
        for (int index = 0; index < contents.length; index++) {
            if (index != slot && isEmpty.test(contents[index])) {
                T[] result = contents.clone();
                result[index] = pending;
                pending = null;
                return result;
            }
        }
        return contents;
    }

    public T[] savedContents(T[] contents) {
        T[] result = contents.clone();
        result[slot] = pending;
        return result;
    }

    public T pending() { return pending; }
}
