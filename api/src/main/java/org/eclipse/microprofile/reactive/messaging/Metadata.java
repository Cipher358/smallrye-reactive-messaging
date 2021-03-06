package org.eclipse.microprofile.reactive.messaging;

import java.util.*;

/**
 * Message metadata containers.
 * <p>
 * This class stores message metadata that can be related to the transport layer or to the business / application.
 * <p>
 * Instances of this class are <strong>immutable</strong>. Modification operation returned new instances.
 * Contained instances are not constrained, but should be immutable. Only one instance of each class can be stored,
 * as the class is used to retrieve the metadata.
 * <p>
 * You can creates new instances using the {@link #of(Object...)} and {@link #from(Iterable) }methods.
 * <p>
 * <strong>IMPORTANT:</strong> Experimental.
 */
public class Metadata implements Iterable<Object> {

    private final Set<Object> backend;

    private static final Metadata EMPTY = new Metadata(Collections.emptySet());

    /**
     * {@link Metadata} instances must be created using the static factory methods.
     *
     * @param backend the backend, must not be {@code null}, must be immutable.
     */
    private Metadata(Set<Object> backend) {
        this.backend = Collections.unmodifiableSet(backend);
    }

    /**
     * Returns an empty set of metadata.
     *
     * @return the empty instance
     */
    public static Metadata empty() {
        return EMPTY;
    }

    /**
     * Returns an instance of {@link Metadata} containing a single value.
     *
     * @param metadata the metadata to be stored, must not be {@code null}. Should be immutable.
     * @return a new {@link Metadata} instance
     */
    static Metadata of(Object metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("`metadata` must not be `null`");
        }
        return new Metadata(Collections.singleton(metadata));
    }

    /**
     * Returns an instance of {@link Metadata} containing multiple values.
     *
     * @param metadata the metadata, must not be {@code null}, must not contain {@code null}. The contained
     *        metadata must not have the same class.
     * @return the new metadata
     */
    public static Metadata of(Object... metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("`metadata` must not be `null`");
        }
        Set<Object> set = addMetadataToSet(Arrays.asList(metadata));
        return new Metadata(set);
    }

    public static Metadata from(Iterable<Object> iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException("`iterable` must not be `null`");
        }
        if (iterable instanceof Metadata) {
            return (Metadata) iterable;
        }
        Set<Object> set = addMetadataToSet(iterable);

        if (set.isEmpty()) {
            return Metadata.empty();
        }
        return new Metadata(set);
    }

    private static Set<Object> addMetadataToSet(Iterable<Object> iterable) {
        Set<Object> set = new LinkedHashSet<>();
        for (Object meta : iterable) {
            if (meta == null) {
                throw new IllegalArgumentException("One of the item is `null`");
            }
            // Ensure that the class is not used.
            Optional<Object> contained = contains(set, meta);
            if (contained.isPresent()) {
                throw new IllegalArgumentException("Duplicated metadata detected: " + meta.getClass().getName());
            }
            set.add(meta);
        }
        return set;
    }

    private static Optional<Object> contains(Set<Object> set, Object meta) {
        return set.stream().filter(o -> o.getClass().equals(meta.getClass())).findAny();
    }

    /**
     * Creates a new instance of {@link Metadata} with the current entries, plus {@code meta}.
     * If the current set of metadata contains already an instance of the class of {@code meta}, the value is replaced
     * in the returned {@link Metadata}.
     *
     * @param meta the metadata to be added, must not be {@code null}.
     * @return the new instance of {@link Metadata}
     */
    public Metadata with(Object meta) {
        if (meta == null) {
            throw new IllegalArgumentException("`meta` must not be `null`");
        }
        Set<Object> copy = new LinkedHashSet<>(backend);
        contains(backend, meta).ifPresent(copy::remove);
        copy.add(meta);
        return new Metadata(copy);
    }

    /**
     * Creates a new instance of {@link Metadata} with the current entries, minus the entry associated with the given class.
     * If there is no instance of the class in the current set of metadata, the same entries are composing returned instance
     * of metadata.
     *
     * @param clazz instance from this class are removed from the metadata.
     * @return the new instance of {@link Metadata}
     */
    public Metadata without(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("`clazz` must not be `null`");
        }
        Set<Object> copy = new LinkedHashSet<>(backend);
        copy.stream()
                .filter(o -> o.getClass().equals(clazz))
                .findAny()
                .ifPresent(copy::remove);
        return new Metadata(copy);
    }

    /**
     * Copies the current {@link Metadata} instance.
     *
     * @return the new instance.
     */
    public Metadata copy() {
        Set<Object> copy = new LinkedHashSet<>(backend);
        return new Metadata(copy);
    }

    /**
     * @return an iterator to traverse the set of metadata. This method will never return {@code null}.
     */
    @Override
    public Iterator<Object> iterator() {
        return backend.iterator();
    }
}
