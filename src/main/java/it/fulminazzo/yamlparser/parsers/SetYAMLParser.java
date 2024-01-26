package it.fulminazzo.yamlparser.parsers;

import it.fulminazzo.yamlparser.configuration.IConfiguration;
import it.fulminazzo.fulmicollection.interfaces.functions.BiFunctionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Set YAML parser.
 *
 * @param <T> the type of the set elements
 */
@SuppressWarnings("unchecked")
public class SetYAMLParser<T> extends CollectionYAMLParser<T, Set<T>> {

    /**
     * Instantiates a new Set YAML parser.
     */
    public SetYAMLParser() {
        super((Class<Set<T>>) (Class<?>) Set.class);
    }

    /**
     * Gets loader.
     *
     * @return the loader
     */
    @Override
    protected @NotNull BiFunctionException<@NotNull IConfiguration, @NotNull String, @Nullable Set<T>> getLoader() {
        return (c, s) -> {
            @Nullable Collection<T> object = super.getLoader().apply(c, s);
            return new HashSet<>(object);
        };
    }
}
