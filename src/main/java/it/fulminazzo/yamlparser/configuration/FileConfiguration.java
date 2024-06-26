package it.fulminazzo.yamlparser.configuration;

import it.fulminazzo.fulmicollection.exceptions.GeneralCannotBeNullException;
import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.fulmicollection.utils.ClassUtils;
import it.fulminazzo.yamlparser.parsers.ArrayYAMLParser;
import it.fulminazzo.yamlparser.parsers.EnumYAMLParser;
import it.fulminazzo.yamlparser.parsers.SerializableYAMLParser;
import it.fulminazzo.yamlparser.parsers.YAMLParser;
import it.fulminazzo.yamlparser.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a YAML File configuration.
 */
public final class FileConfiguration extends SimpleConfiguration {
    private final static LinkedList<YAMLParser<?>> parsers = new LinkedList<>();
    private final @Nullable File file;

    /**
     * Instantiates a new File configuration.
     *
     * @param path the path
     */
    public FileConfiguration(@NotNull String path) {
        this(new File(path));
    }

    /**
     * Instantiates a new File configuration.
     *
     * @param file the file
     */
    public FileConfiguration(@NotNull File file) {
        super("", null);
        this.file = file.getAbsoluteFile();
        Map<?, ?> yaml;
        try {
            yaml = newYaml().load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.map.putAll(IConfiguration.generalToConfigMap(this, yaml));
        addParsers();
    }

    /**
     * Instantiates a new File configuration.
     *
     * @param inputStream the input stream
     */
    public FileConfiguration(InputStream inputStream) {
        this(null, inputStream);
    }

    /**
     * Instantiates a new File configuration.
     *
     * @param file        the file
     * @param inputStream the input stream
     */
    public FileConfiguration(@Nullable File file, InputStream inputStream) {
        super("", null);
        this.file = file == null ? null : file.getAbsoluteFile();
        Map<?, ?> yaml = newYaml().load(inputStream);
        this.map.putAll(IConfiguration.generalToConfigMap(this, yaml));
        addParsers();
    }

    /**
     * Saves the configuration to the file.
     */
    public void save() {
        if (file == null) throw new GeneralCannotBeNullException("Save file");
        try {
            if (!file.exists()) FileUtils.createNewFile(file);
            FileWriter writer = new FileWriter(file);
            newYaml().dump(IConfiguration.configToGeneralMap(this), writer);
            writer.close();
        } catch (Exception e) {
            if (e instanceof RuntimeException && e.getCause() instanceof RuntimeException)
                throw (RuntimeException) e.getCause();
            else throw new RuntimeException(e);
        }
    }

    /**
     * New yaml yaml.
     *
     * @return the YAML with parameters.
     */
    public static @NotNull Yaml newYaml() {
        Representer representer;
        try {
            representer = new Representer(new DumperOptions());
        } catch (NoSuchMethodError e) {
            representer = new Refl<>(Representer.class, new Object[0]).getObject();
        }
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(representer);
    }

    /**
     * Add all the parsers present in the package where {@link YAMLParser} is contained.
     */
    public static void addParsers() {
        addParsers(YAMLParser.class.getPackage().getName());
    }

    /**
     * Add all the parsers present in a package.
     *
     * @param packageName the package name
     */
    public static void addParsers(String packageName) {
        addParsers(getParsersFromPackage(packageName).toArray(new YAMLParser[0]));
    }

    /**
     * Add parsers.
     *
     * @param yamlParsers the YAML parsers
     */
    public static void addParsers(YAMLParser<?> @NotNull ... yamlParsers) {
        for (YAMLParser<?> yamlParser : yamlParsers)
            if (yamlParser != null && parsers.stream().noneMatch(p -> p.getOClass().equals(yamlParser.getOClass())))
                parsers.addLast(yamlParser);
    }

    /**
     * Remove parsers.
     *
     * @param packageName the package name
     */
    public static void removeParsers(String packageName) {
        removeParsers(getParsersFromPackage(packageName).toArray(new YAMLParser[0]));
    }

    /**
     * Remove all the parsers present in a package.
     *
     * @param yamlParsers the YAML parsers
     */
    public static void removeParsers(YAMLParser<?> @NotNull ... yamlParsers) {
        for (YAMLParser<?> yamlParser : yamlParsers)
            if (yamlParser != null) parsers.removeIf(p -> p.getOClass().equals(yamlParser.getOClass()));
    }

    /**
     * Gets all the parsers from package.
     *
     * @param packageName the package name
     * @return the parsers from package
     */
    public static @NotNull List<YAMLParser<?>> getParsersFromPackage(String packageName) {
        Set<Class<?>> classes = ClassUtils.findClassesInPackage(packageName);
        List<YAMLParser<?>> yamlParsers = new LinkedList<>();
        for (Class<?> clazz : classes)
            if (YAMLParser.class.isAssignableFrom(clazz))
                try {
                    clazz.getConstructor();
                    if (Modifier.isFinal(clazz.getModifiers()) ||
                            Modifier.isAbstract(clazz.getModifiers()) ||
                            !Modifier.isPublic(clazz.getModifiers())) continue;
                    try {
                        YAMLParser<?> parser = (YAMLParser<?>) clazz.getConstructor().newInstance();
                        yamlParsers.add(parser);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } catch (NoSuchMethodException ignored) {}
        return yamlParsers;
    }

    /**
     * Gets the parser from the associated class.
     *
     * @param <O>    the type of the parser
     * @param <E>    the type of the enum
     * @param oClass the class
     * @return the parser
     */
    @SuppressWarnings("unchecked")
    public static <O, E extends Enum<E>> YAMLParser<O> getParser(@Nullable Class<O> oClass) {
        if (oClass == null) return null;
        if (oClass.isEnum()) return (YAMLParser<O>) new EnumYAMLParser<E>(oClass);
        if (oClass.isArray()) return (YAMLParser<O>) new ArrayYAMLParser<O>();
        if (IConfiguration.class.isAssignableFrom(oClass)) return null;
        return (YAMLParser<O>) getParsers().stream()
                .filter(p -> p.getOClass().equals(oClass))
                .findFirst().orElseGet(() -> getParsers().stream()
                        .filter(p -> p.getOClass().isAssignableFrom(oClass))
                        .findFirst().orElse(null));
    }

    /**
     * From string file configuration.
     *
     * @param string the string
     * @return the file configuration
     */
    public static @NotNull FileConfiguration fromString(@NotNull String string) {
        return new FileConfiguration(new ByteArrayInputStream(string.getBytes()));
    }

    /**
     * Gets parsers.
     *
     * @return the parsers
     */
    public static @NotNull LinkedList<YAMLParser<?>> getParsers() {
        if (!parsers.isEmpty() && !(parsers.get(parsers.size() - 1) instanceof SerializableYAMLParser)) {
            parsers.removeIf(s -> s instanceof SerializableYAMLParser);
            parsers.add(new SerializableYAMLParser());
        }
        return parsers;
    }

    @Override
    public String toString() {
        return String.format("%s {file: %s, non-null: %s}", getClass().getSimpleName(),
                file == null ? null : file.getAbsolutePath(), nonNull);
    }
}
