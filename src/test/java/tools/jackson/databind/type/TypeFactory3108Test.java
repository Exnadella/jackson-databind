package tools.jackson.databind.type;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JavaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// [databind#3108]: canonical type description for non-generic subtypes
@SuppressWarnings("serial")
public class TypeFactory3108Test
{
    static class StringList3108 extends ArrayList<String> {}

    static class StringStringMap3108 extends HashMap<String, String> {}

    static class ParamType3108<T> {}

    static class ConcreteType3108 extends ParamType3108<Integer> {}

    // [databind#3108] with custom Collection
    @Test
    public void testCanonicalWithCustomCollection()
    {
        final TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringListType = tf.constructType(StringList3108.class);
        String canonical = stringListType.toCanonical();
        JavaType type = tf.constructFromCanonical(canonical);
        assertEquals(StringList3108.class, type.getRawClass());
        assertTrue(type.isCollectionLikeType());
    }

    // [databind#3108] with custom Map
    @Test
    public void testCanonicalWithCustomMap()
    {
        final TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringListType = tf.constructType(StringStringMap3108.class);
        String canonical = stringListType.toCanonical();
        JavaType type = tf.constructFromCanonical(canonical);
        assertEquals(StringStringMap3108.class, type.getRawClass());
        assertTrue(type.isMapLikeType());
    }

    // [databind#3108] with custom generic type
    @Test
    public void testCanonicalWithCustomGenericType()
    {
        final TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringListType = tf.constructType(ConcreteType3108.class);
        String canonical = stringListType.toCanonical();
        JavaType type = tf.constructFromCanonical(canonical);
        assertEquals(ConcreteType3108.class, type.getRawClass());
    }
}
