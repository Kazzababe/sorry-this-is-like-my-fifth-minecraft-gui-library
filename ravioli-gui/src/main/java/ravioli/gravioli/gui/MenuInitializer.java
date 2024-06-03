package ravioli.gravioli.gui;

import com.google.common.base.Preconditions;
import lombok.Getter;

@Getter
public final class MenuInitializer {

    private int height;

    public void setHeight(final int height) {
        Preconditions.checkArgument(height >= 1 && height <= 10, "height must be between 1-10");

        this.height = height;
    }
}
