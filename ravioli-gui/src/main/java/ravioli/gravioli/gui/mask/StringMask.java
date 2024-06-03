package ravioli.gravioli.gui.mask;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.gui.position.Position;

import java.util.ArrayList;
import java.util.List;

public class StringMask implements Mask {
    private final String originalMaskString;
    private final Position[] validSlots;

    public StringMask(@NotNull final String maskString) {
        this.originalMaskString = maskString;
        this.validSlots = this.findValidSlots(maskString);
    }

    @Override
    public int getSize() {
        return this.validSlots.length;
    }

    @Override
    public Position[] getValidSlots() {
        return this.validSlots;
    }

    @Override
    public @NotNull StringMask inverse() {
        final String newMask = this.originalMaskString.replace("1", "2")
                .replace("0", "1")
                .replace("2", "0");

        return new StringMask(newMask);
    }

    private Position[] findValidSlots(@NotNull final String maskString) {
        final String[] lines = maskString.split(" ");
        final List<Position> slots = new ArrayList<>();

        for (int y = 0, slot = 0; y < lines.length; y++) {
            final String line = lines[y];

            for (int x = 0; x < 9; x++, slot++) {
                if (x >= line.length()) {
                    continue;
                }
                final String lineChar = String.valueOf(line.charAt(x));
                final Integer lineBit = Ints.tryParse(lineChar);

                if (lineBit == null || (lineBit != 0 && lineBit != 1)) {
                    continue;
                }
                if (lineBit == 1) {
                    slots.add(
                        new Position(slot)
                    );
                }
            }
        }
        return slots.toArray(Position[]::new);
    }

    @Override
    public boolean equals(@Nullable final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final StringMask otherMask = (StringMask) other;

        return new EqualsBuilder()
            .append(this.originalMaskString, otherMask.originalMaskString)
            .isEquals();
    }
}
