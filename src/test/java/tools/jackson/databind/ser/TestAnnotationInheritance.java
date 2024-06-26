package tools.jackson.databind.ser;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This unit test suite tests use of Annotations for
 * bean serialization.
 */
public class TestAnnotationInheritance
    extends DatabindTestUtil
{
    /*
    /**********************************************************
    /* Annotated helper classes
    /**********************************************************
     */

    /// Base class for testing {@link JsonProperty} annotations
    static class BasePojo
    {
        @JsonProperty public int width() { return 3; }
        @JsonProperty public int length() { return 7; }
    }

    /**
     * It should also be possible to specify annotations on interfaces,
     * to be implemented by classes. This should not only work when interface
     * is used (which may be the case for de-serialization) but also
     * when implementing class is used and overrides methods. In latter
     * case overriding methods should still "inherit" annotations -- this
     * is not something JVM runtime provides, but Jackson class
     * instrospector does.
     */
    interface PojoInterface
    {
        @JsonProperty int width();
        @JsonProperty int length();
    }

    /**
     * Sub-class for testing that inheritance is handled properly
     * wrt annotations.
     */
    static class PojoSubclass extends BasePojo
    {
        /**
         * Should still be recognized as a Getter here.
         */
        @Override
        public int width() { return 9; }
    }

    static class PojoImpl implements PojoInterface
    {
        // Both should be recognized as getters here

        @Override
        public int width() { return 1; }
        @Override
        public int length() { return 2; }

        public int getFoobar() { return 5; }
    }

    /*
    /**********************************************************
    /* Main tests
    /**********************************************************
     */

    @Test
    public void testSimpleGetterInheritance() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new PojoSubclass());
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(7), result.get("length"));
        assertEquals(Integer.valueOf(9), result.get("width"));
    }

    @Test
    public void testSimpleGetterInterfaceImpl() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new PojoImpl());
        // should get 2 from interface, and one more from impl itself
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(5), result.get("foobar"));
        assertEquals(Integer.valueOf(1), result.get("width"));
        assertEquals(Integer.valueOf(2), result.get("length"));
    }
}
