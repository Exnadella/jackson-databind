package tools.jackson.databind.ser.filter;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for checking that alternative settings for
 * inclusion annotation properties work as expected.
 */
public class JsonInclude1327Test
    extends DatabindTestUtil
{
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class Issue1327BeanEmpty {
        public List<String> myList = new ArrayList<String>();
    }

    static class Issue1327BeanAlways {
        @JsonInclude(JsonInclude.Include.ALWAYS)
        public List<String> myList = new ArrayList<String>();
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // for [databind#1327]
    @Test
    public void testClassDefaultsForEmpty() throws Exception {
        ObjectMapper om = jsonMapperBuilder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
        final String jsonString = om.writeValueAsString(new Issue1327BeanEmpty());

        if (jsonString.contains("myList")) {
            fail("Should not contain `myList`: "+jsonString);
        }
    }

    @Test
    public void testClassDefaultsForAlways() throws Exception {
        ObjectMapper om = jsonMapperBuilder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
                .build();
        final String jsonString = om.writeValueAsString(new Issue1327BeanAlways());
        if (!jsonString.contains("myList")) {
            fail("Should contain `myList` with Include.ALWAYS: "+jsonString);
        }
    }
}
