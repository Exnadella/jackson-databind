package tools.jackson.databind.jsontype;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.*;

public class TestTypedSerialization
    extends DatabindTestUtil
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

    /**
     * Polymorphic base class
     */
    @JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY)
    static abstract class Animal {
        public String name;

        protected Animal(String n)  { name = n; }
    }

    @JsonTypeName("doggie")
    static class Dog extends Animal
    {
        public int boneCount;

        private Dog() { super(null); }
        public Dog(String name, int b) {
            super(name);
            boneCount = b;
        }
    }

    @JsonTypeName("kitty")
    static class Cat extends Animal
    {
        public String furColor;

        private Cat() { super(null); }
        public Cat(String name, String c) {
            super(name);
            furColor = c;
        }
    }

    public class AnimalWrapper {
        public Animal animal;

        public AnimalWrapper(Animal a) { animal = a; }
    }

    @JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.WRAPPER_OBJECT)
    interface TypeWithWrapper { }

    @JsonTypeInfo(use=Id.CLASS, include=As.WRAPPER_ARRAY)
    interface TypeWithArray { }

    @JsonTypeInfo(use=Id.NAME)
    @JsonTypeName("empty")
    public class Empty { }

    @JsonTypeInfo(include=As.PROPERTY, use=Id.CLASS)
    public class Super {}
    public class A extends Super {}
    public class B extends Super {}

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * First things first, let's ensure we can serialize using
     * class name, written as main-level property name
     */
    @Test
    public void testSimpleClassAsProperty() throws Exception
    {
        Map<String,Object> result = writeAndMap(MAPPER, new Cat("Beelzebub", "tabby"));
        assertEquals(3, result.size());
        assertEquals("Beelzebub", result.get("name"));
        assertEquals("tabby", result.get("furColor"));
        // should we try customized class name?
        String classProp = Id.CLASS.getDefaultPropertyName();
        assertEquals(Cat.class.getName(), result.get(classProp));
    }

    /**
     * Test inclusion using wrapper style
     */
    @Test
    public void testTypeAsWrapper() throws Exception
    {
        ObjectMapper m = jsonMapperBuilder()
                .addMixIn(Animal.class, TypeWithWrapper.class)
                .build();
        Map<String,Object> result = writeAndMap(m, new Cat("Venla", "black"));
        // should get a wrapper; keyed by minimal class name ("Cat" here)
        assertEquals(1, result.size());
        // minimal class name is prefixed by dot, and for inner classes it's bit longer
        Map<?,?> cat = (Map<?,?>) result.get(".TestTypedSerialization$Cat");
        assertNotNull(cat);
        assertEquals(2, cat.size());
        assertEquals("Venla", cat.get("name"));
        assertEquals("black", cat.get("furColor"));
    }

    /**
     * Test inclusion using 2-element array
     */
    @Test
    public void testTypeAsArray() throws Exception
    {
        ObjectMapper m = jsonMapperBuilder()
                .addMixIn(Animal.class, TypeWithArray.class)
                .build();
        // hmmh. Not good idea to rely on exact output, order may change. But...
        Map<String,Object> result = writeAndMap(m, new AnimalWrapper(new Dog("Amadeus", 7)));
        // First level, wrapper
        assertEquals(1, result.size());
        List<?> l = (List<?>) result.get("animal");
        assertNotNull(l);
        assertEquals(2, l.size());
        assertEquals(Dog.class.getName(), l.get(0));
        Map<?,?> doggie = (Map<?,?>) l.get(1);
        assertNotNull(doggie);
        assertEquals(2, doggie.size());
        assertEquals("Amadeus", doggie.get("name"));
        assertEquals(Integer.valueOf(7), doggie.get("boneCount"));
    }

    /* !!! 30-Jan-2010, tatus: I am not completely sure below works as it should
     *    Problem is, context of "untyped" map should prevent type information
     *    being added to Animal entries, because Object.class has no type.
     *    If type information is included, it will not be useful for deserialization,
     *    since static type does not carry through (unlike in serialization).
     *
     *    But it is not quite clear how type information should be pushed through
     *    array types...
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testInArray() throws Exception
    {
        // ensure we'll use mapper with default configs
        ObjectMapper m = jsonMapperBuilder()
                // ... so this should NOT be needed...
                .deactivateDefaultTyping()
                .build();

        Animal[] animals = new Animal[] { new Cat("Miuku", "white"), new Dog("Murre", 9) };
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("a", animals);
        String json = m.writeValueAsString(map);
        Map<String,Object> result = m.readValue(json, Map.class);
        assertEquals(1, result.size());
        Object ob = result.get("a");
        if (!(ob instanceof List<?>)) {
            // 03-Feb-2010, tatu: Weird; seems to fail sometimes...
            fail("Did not map to entry with 'a' as List (but as "+ob.getClass().getName()+"): JSON == '"+json+"'");
        }
        List<?> l = (List<?>)ob;
        assertNotNull(l);
        assertEquals(2, l.size());
        Map<?,?> a1 = (Map<?,?>) l.get(0);
        assertEquals(3, a1.size());
        String classProp = Id.CLASS.getDefaultPropertyName();
        assertEquals(Cat.class.getName(), a1.get(classProp));
        Map<?,?> a2 = (Map<?,?>) l.get(1);
        assertEquals(3, a2.size());
        assertEquals(Dog.class.getName(), a2.get(classProp));
    }

    /**
     * Simple unit test to verify that serializing "empty" beans is ok
     */
    @Test
    public void testEmptyBean() throws Exception
    {
        ObjectMapper m = jsonMapperBuilder()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .build();
        assertEquals("{\"@type\":\"empty\"}", m.writeValueAsString(new Empty()));
    }

    @Test
    public void testTypedMaps() throws Exception
    {
        ObjectMapper mapper = newJsonMapper();

        Map<Long, Collection<Super>> map = new HashMap<Long, Collection<Super>>();
        List<Super> list = new ArrayList<Super>();
        list.add(new A());
        map.put(1L, list);
        String json = mapper.writerFor(new TypeReference<Map<Long, Collection<Super>>>() {}).writeValueAsString(map);
        assertTrue(json.contains("@class"), "JSON does not contain '@class': "+json);
    }
}

