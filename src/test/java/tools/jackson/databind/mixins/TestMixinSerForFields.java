package tools.jackson.databind.mixins;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class TestMixinSerForFields
    extends DatabindTestUtil
{
    /*
    /**********************************************************
    /* Helper bean classes
    /**********************************************************
     */

    static class BaseClass
    {
        public String a;
        protected String b;

        public BaseClass(String a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    static class SubClass
        extends BaseClass
    {
        public SubClass(String a, String b) {
            super(a, b);
        }
    }

    abstract class MixIn {
        // Let's add 'b' as "banana"
        @JsonProperty("banana")
        public String b;
    }

    abstract class MixIn2 {
        // Let's remove 'a'
        @JsonIgnore
        public String a;

        // also: add a dummy field that is NOT to match anything
        @JsonProperty public String xyz;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    @Test
    public void testFieldMixInsTopLevel() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> result;
        BaseClass bean = new BaseClass("1", "2");

        // first: with no mix-ins:
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("1", result.get("a"));

        // and then with simple mix-in
        mapper = jsonMapperBuilder()
                .addMixIn(BaseClass.class, MixIn.class)
                .build();
        result = writeAndMap(mapper, bean);
        assertEquals(2, result.size());
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("banana"));
    }

    @Test
    public void testMultipleFieldMixIns() throws IOException
    {
        // ordering here shouldn't matter really...
        HashMap<Class<?>,Class<?>> mixins = new HashMap<Class<?>,Class<?>>();
        mixins.put(SubClass.class, MixIn.class);
        mixins.put(BaseClass.class, MixIn2.class);

        ObjectMapper mapper = jsonMapperBuilder()
                .addMixIns(mixins)
                .build();

        Map<String,Object> result = writeAndMap(mapper, new SubClass("1", "2"));
        assertEquals(1, result.size());
        // 'a' should be suppressed; 'b' mapped to 'banana'
        assertEquals("2", result.get("banana"));
    }
}
