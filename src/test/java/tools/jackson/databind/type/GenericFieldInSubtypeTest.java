package tools.jackson.databind.type;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GenericFieldInSubtypeTest
{
    // [JACKSON-677]
    @Test
    public void test677() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // and bit more checking as per later comments
        JavaType t677 = mapper.constructType(Result677.Success677.class);
        assertNotNull(t677);
        Result677.Success677<Integer> s = new Result677.Success677<Integer>(Integer.valueOf(4));
        String json = mapper.writeValueAsString(s);
        assertEquals("{\"value\":4}", json);
    }

 // [JACKSON-887]
    @Test
    public void testInnerType() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        BaseType.SubType<?> r = mapper.readValue("{}", BaseType.SubType.class);
        assertNotNull(r);
    }

}

class Result677<T> {
    public static class Success677<K> extends Result677<K> {
     public K value;

     public Success677() { }
     public Success677(K k) { value = k; }
    }
}

abstract class BaseType<T> {
    public T value;

    public final static class SubType<T extends Number> extends BaseType<T>
    {
    }
}
