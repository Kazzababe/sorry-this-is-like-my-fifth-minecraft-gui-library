package ravioli.gravioli.gui.mask;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.gui.position.Position;

import java.util.Iterator;

public interface Mask extends Iterable<Position> {
    int getSize();

    Position[] getValidSlots();

    @NotNull Mask inverse();

    @Override
    default @NotNull Iterator<Position> iterator() {
        return new MaskIterator(this);
    }

    class MaskIterator implements Iterator<Position> {
        private final Mask mask;

        private int index;

        MaskIterator(@NotNull final Mask mask) {
            this.mask = mask;
        }

        @Override
        public boolean hasNext() {
            return this.index < this.mask.getValidSlots().length;
        }

        @Override
        public Position next() {
            return this.mask.getValidSlots()[this.index++];
        }
    }
}
