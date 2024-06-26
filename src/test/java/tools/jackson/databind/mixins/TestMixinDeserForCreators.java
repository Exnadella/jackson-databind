package tools.jackson.databind.mixins;

import java.io.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class TestMixinDeserForCreators
    extends DatabindTestUtil
{
    /*
    /**********************************************************
    /* Helper bean classes
    /**********************************************************
     */

    static class BaseClass
    {
        protected String _a;

        public BaseClass(String a) {
            _a = a+"...";
        }

        private BaseClass(String value, boolean dummy) {
            _a = value;
        }

        public static BaseClass myFactory(String a) {
            return new BaseClass(a+"X", true);
        }
    }

    static class BaseClassWithPrivateCtor
    {
        protected String _a;
        private BaseClassWithPrivateCtor(String a) {
            _a = a+"...";
        }

    }

    /**
     * Mix-in class that will effectively suppresses String constructor,
     * and marks a non-auto-detectable static method as factory method
     * as a creator.
     *<p>
     * Note that method implementations are not used for anything; but
     * we have to a class: interface won't do, as they can't have
     * constructors or static methods.
     */
    static class MixIn
    {
        @JsonIgnore protected MixIn(String s) { }

        @JsonCreator
        static BaseClass myFactory(String a) { return null; }
    }

    static class MixInForPrivate
    {
        @JsonCreator MixInForPrivate(String s) { }
    }

    static class StringWrapper {
        String _value;
        private StringWrapper(String s, boolean foo) { _value = s; }

        @SuppressWarnings("unused")
        private static StringWrapper create(String str) {
            return new StringWrapper(str, false);
        }
    }

    abstract static class StringWrapperMixIn {
        @JsonCreator static StringWrapper create(String str) { return null; }
    }

    // [databind#2020]
    static class Pair2020 {
        final int x, y;

        private Pair2020(int x0, int y0) {
            x = x0;
            y = y0;
        }

        static Pair2020 with(Object x0, Object y0) {
            return new Pair2020(((Number) x0).intValue(),
                    ((Number) y0).intValue());
        }
    }

    @JsonIgnoreProperties("size")
    static class MyPairMixIn8 { // with and without <Long, String>
         @JsonCreator
         static TestMixinDeserForCreators.Pair2020 with(@JsonProperty("value0") Object value0,
                   @JsonProperty("value1") Object value1)
         {
             // body does not matter, only signature
             return null;
         }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    @Test
    public void testForConstructor() throws IOException
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .addMixIn(BaseClassWithPrivateCtor.class, MixInForPrivate.class)
                .build();
        BaseClassWithPrivateCtor result = mapper.readValue("\"?\"", BaseClassWithPrivateCtor.class);
        assertEquals("?...", result._a);
    }

    @Test
    public void testForFactoryAndCtor() throws IOException
    {
        BaseClass result;

        // First: test default behavior: should use constructor
        result = new ObjectMapper().readValue("\"string\"", BaseClass.class);
        assertEquals("string...", result._a);

        // Then with simple mix-in: should change to use the factory method
        ObjectMapper mapper = jsonMapperBuilder()
                .addMixIn(BaseClass.class, MixIn.class)
                .build();
        result = mapper.readValue("\"string\"", BaseClass.class);
        assertEquals("stringX", result._a);
    }

    @Test
    public void testFactoryDelegateMixIn() throws IOException
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .addMixIn(StringWrapper.class, StringWrapperMixIn.class)
                .build();
        StringWrapper result = mapper.readValue("\"a\"", StringWrapper.class);
        assertEquals("a", result._value);
    }

    // [databind#2020]
    @Test
    public void testFactoryPropertyMixin() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
            .addMixIn(Pair2020.class, MyPairMixIn8.class)
            .build();

        String doc = a2q( "{'value0' : 456, 'value1' : 789}");
        Pair2020 pair2 = mapper.readValue(doc, Pair2020.class);
        assertEquals(456, pair2.x);
        assertEquals(789, pair2.y);
    }
}
