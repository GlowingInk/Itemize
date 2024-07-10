package ink.glowing.itemize.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class ItemizePaperLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(
                new DefaultArtifact("org.spongepowered:configurate-yaml:4.1.2"), null)
        );
        resolver.addRepository(new RemoteRepository.Builder(
                "central", "default", "https://repo1.maven.org/maven2/"
        ).build());
        classpathBuilder.addLibrary(resolver);
    }
}
