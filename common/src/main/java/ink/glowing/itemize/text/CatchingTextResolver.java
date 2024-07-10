package ink.glowing.itemize.text;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CatchingTextResolver extends SimpleTextResolver {
    public CatchingTextResolver(
            @NotNull Key key,
            @NotNull ComponentDecoder<String, ? extends Component> decoder
    ) {
        super(key, decoder);
    }

    @Override
    public @Nullable Component resolve(@NotNull String params) {
        try {
            return super.resolve(params);
        } catch (Exception ex) {
            return null;
        }
    }
}
