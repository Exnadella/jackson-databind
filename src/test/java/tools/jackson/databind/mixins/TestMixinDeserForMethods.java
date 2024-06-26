package tools.jackson.databind.mixins;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class TestMixinDeserForMethods
    extends DatabindTestUtil
{
    /*
    /**********************************************************
    /* Helper bean classes
    /**********************************************************
     */

    static class BaseClass
    {
        protected HashMap<String,Object> values = new HashMap<String,Object>();

        public BaseClass() { }

        protected void addValue(String key, Object value) {
            values.put(key, value);
        }
    }

    interface MixIn
    {
        @JsonAnySetter void addValue(String key, Object value);
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    /**
     * Unit test that verifies that we can mix in @JsonAnySetter
     * annotation, as expected.
     */
    @Test
    public void testWithAnySetter() throws IOException
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .addMixIn(BaseClass.class, MixIn.class)
                .build();
        BaseClass result = mapper.readValue("{ \"a\" : 3, \"b\" : true }", BaseClass.class);
        assertNotNull(result);
        assertEquals(2, result.values.size());
        assertEquals(Integer.valueOf(3), result.values.get("a"));
        assertEquals(Boolean.TRUE, result.values.get("b"));
    }
}
