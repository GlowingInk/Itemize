package ink.glowing.itemize.text;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleTextResolver implements TextResolver {
    protected final Key key;
    protected final ComponentDecoder<String, ? extends Component> decoder;

    public SimpleTextResolver(
            @NotNull Key key,
            @NotNull ComponentDecoder<String, ? extends Component> decoder
    ) {
        this.key = key;
        this.decoder = decoder;
    }

    @Override
    public @Nullable Component resolve(@NotNull String params) {
        return decoder.deserialize(params);
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
