package tools.jackson.databind.ser;

import java.io.Serializable;
import java.util.List;

import tools.jackson.databind.*;
import tools.jackson.databind.deser.DeserializerFactory;
import tools.jackson.databind.type.*;

/**
 * Abstract class that defines API for objects that can be registered
 * (via {@code ObjectMapper} configuration process,
 * using {@link tools.jackson.databind.cfg.MapperBuilder})
 * to participate in constructing {@link ValueSerializer} instances
 * (including but not limited to {@link BeanSerializer}s).
 * This is typically done by modules that want alter some aspects of
 * the typical serialization process.
 *<p>
 * Sequence in which callback methods are called is as follows:
 * <ol>
 *  <li>After factory has collected tentative set of properties (instances of
 *     <code>BeanPropertyWriter</code>) is sent for modification via
 *     {@link #changeProperties}. Changes can include removal, addition and
 *     replacement of suggested properties.
 *  <li>Resulting set of properties are ordered (sorted) by factory, as per
 *     configuration, and then {@link #orderProperties} is called to allow
 *     modifiers to alter ordering.
 *  <li>After all bean properties and related information is accumulated,
 *     {@link #updateBuilder} is called with builder, to allow builder state
 *     to be modified (including possibly replacing builder itself if necessary)
 *  <li>Once all bean information has been determined,
 *     factory creates default {@link BeanSerializer} instance and passes
 *     it to modifiers using {@link #modifySerializer} (or type-specic alternative
 *     {@code modifyXxxSerializer()} method), for possible
 *     modification or replacement (by any {@link tools.jackson.databind.ValueSerializer}
 *     instance)
 * </ol>
 *<p>
 * Default method implementations are "no-op"s, meaning that methods are implemented
 * but have no effect.
 *<p>
 * NOTE: In Jackson 2.x was named {@code BeanSerializerModifier}
 */
public abstract class ValueSerializerModifier
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Method called by {@link BeanSerializerFactory} with tentative set
     * of discovered properties.
     * Implementations can add, remove or replace any of passed properties.
     *
     * Properties <code>List</code> passed as argument is modifiable, and returned List must
     * likewise be modifiable as it may be passed to multiple registered
     * modifiers.
     */
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
            BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        return beanProperties;
    }

    /**
     * Method called by {@link BeanSerializerFactory} with set of properties
     * to serialize, in default ordering (based on defaults as well as
     * possible type annotations).
     * Implementations can change ordering any way they like.
     *
     * Properties <code>List</code> passed as argument is modifiable, and returned List must
     * likewise be modifiable as it may be passed to multiple registered
     * modifiers.
     */
    public List<BeanPropertyWriter> orderProperties(SerializationConfig config,
            BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        return beanProperties;
    }

    /**
     * Method called by {@link BeanSerializerFactory} after collecting all information
     * regarding POJO to serialize and updating builder with it, but before constructing
     * serializer.
     * Implementations may choose to modify state of builder (to affect serializer being
     * built), or even completely replace it (if they want to build different kind of
     * serializer). Typically, however, passed-in builder is returned, possibly with
     * some modifications.
     */
    public BeanSerializerBuilder updateBuilder(SerializationConfig config,
            BeanDescription beanDesc, BeanSerializerBuilder builder) {
        return builder;
    }

    /**
     * Method called by {@link BeanSerializerFactory} after constructing default
     * bean serializer instance with properties collected and ordered earlier.
     * Implementations can modify or replace given serializer and return serializer
     * to use. Note that although initial serializer being passed is of type
     * {@link BeanSerializer}, modifiers may return serializers of other types;
     * and this is why implementations must check for type before casting.
     *<p>
     * NOTE: this method gets called for serializer of those non-POJO types that
     * do not go through any of more specific <code>modifyXxxSerializer</code>
     * methods; mostly for JDK types like {@link java.util.Iterator} and such.
     */
    public ValueSerializer<?> modifySerializer(SerializationConfig config,
            BeanDescription beanDesc, ValueSerializer<?> serializer) {
        return serializer;
    }

    /*
    /**********************************************************************
    /* Callback methods for other types
    /**********************************************************************
     */

    /**
     * Method called by {@link DeserializerFactory} after it has constructed the
     * standard serializer for given
     * {@link ArrayType}
     * to make it possible to either replace or augment this serializer with
     * additional functionality.
     *
     * @param config Configuration in use
     * @param valueType Type of the value serializer is used for.
     * @param beanDesc Details of the type in question, to allow checking class annotations
     * @param serializer Default serializer that would be used.
     *
     * @return Serializer to use; either <code>serializer</code> that was passed
     *   in, or an instance method constructed.
     */
    public ValueSerializer<?> modifyArraySerializer(SerializationConfig config,
            ArrayType valueType, BeanDescription beanDesc, ValueSerializer<?> serializer) {
        return serializer;
    }

    public ValueSerializer<?> modifyCollectionSerializer(SerializationConfig config,
            CollectionType valueType, BeanDescription beanDesc, ValueSerializer<?> serializer) {
        return serializer;
    }

    public ValueSerializer<?> modifyCollectionLikeSerializer(SerializationConfig config,
            CollectionLikeType valueType, BeanDescription beanDesc, ValueSerializer<?> serializer) {
        return serializer;
    }

    public ValueSerializer<?> modifyMapSerializer(SerializationConfig config,
            MapType valueType, BeanDescription beanDesc, ValueSerializer<?> serializer) {
        return serializer;
    }

    public ValueSerializer<?> modifyMapLikeSerializer(SerializationConfig config,
            MapLikeType valueType, BeanDescription beanDesc, ValueSerializer<?> serializer) {
        return serializer;
    }

    public ValueSerializer<?> modifyEnumSerializer(SerializationConfig config,
            JavaType valueType, BeanDescription beanDesc, ValueSerializer<?> serializer) {
        return serializer;
    }

    /**
     * Method called by {@link DeserializerFactory} after it has constructed the
     * default key serializer to use for serializing {@link java.util.Map} keys of
     * given type.
     * This makes it possible to either replace or augment default serializer with
     * additional functionality.
     *
     * @param config Configuration in use
     * @param valueType Type of keys the serializer is used for.
     * @param beanDesc Details of the type in question, to allow checking class annotations
     * @param serializer Default serializer that would be used.
     *
     * @return Serializer to use; either <code>serializer</code> that was passed
     *   in, or an instance method constructed.
     */
    public ValueSerializer<?> modifyKeySerializer(SerializationConfig config,
            JavaType valueType, BeanDescription beanDesc, ValueSerializer<?> serializer) {
        return serializer;
    }
}
