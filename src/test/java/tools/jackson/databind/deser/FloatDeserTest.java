package tools.jackson.databind.deser;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static tools.jackson.databind.testutil.DatabindTestUtil.newJsonMapper;

public class FloatDeserTest
{

    /*
    /**********************************************************
    /* Tests
    /**********************************************************
     */

    private final ObjectMapper MAPPER = newJsonMapper();

    @Test
    public void testFloatPrimitive() throws Exception
    {
        assertEquals(7.038531e-26f, MAPPER.readValue("\"7.038531e-26\"", float.class));
        assertEquals(1.1999999f, MAPPER.readValue("\"1.199999988079071\"", float.class));
        assertEquals(3.4028235e38f, MAPPER.readValue("\"3.4028235677973366e38\"", float.class));
        //this assertion fails unless toString is used
        assertEquals("1.4E-45", MAPPER.readValue("\"7.006492321624086e-46\"", float.class).toString());
    }

    @Test
    public void testFloatClass() throws Exception
    {
        assertEquals(Float.valueOf(7.038531e-26f), MAPPER.readValue("\"7.038531e-26\"", Float.class));
        assertEquals(Float.valueOf(1.1999999f), MAPPER.readValue("\"1.199999988079071\"", Float.class));
        assertEquals(Float.valueOf(3.4028235e38f), MAPPER.readValue("\"3.4028235677973366e38\"", Float.class));
        //this assertion fails unless toString is used
        assertEquals("1.4E-45", MAPPER.readValue("\"7.006492321624086e-46\"", Float.class).toString());
    }

    @Test
    public void testArrayOfFloatPrimitives() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[')
                .append("\"7.038531e-26\",")
                .append("\"1.199999988079071\",")
                .append("\"3.4028235677973366e38\",")
                .append("\"7.006492321624086e-46\"")
                .append(']');
        float[] floats = MAPPER.readValue(sb.toString(), float[].class);
        assertEquals(4, floats.length);
        assertEquals(7.038531e-26f, floats[0]);
        assertEquals(1.1999999f, floats[1]);
        assertEquals(3.4028235e38f, floats[2]);
        assertEquals("1.4E-45", Float.toString(floats[3])); //this assertion fails unless toString is used
    }

    // for [jackson-core#757]
    @Test
    public void testBigArrayOfFloatPrimitives() throws Exception {
        try (InputStream stream = FloatDeserTest.class.getResourceAsStream("/data/float-array-755.txt")) {
            float[] floats = MAPPER.readValue(stream, float[].class);
            assertEquals(1004, floats.length);
            assertEquals(7.038531e-26f, floats[0]);
            assertEquals(1.1999999f, floats[1]);
            assertEquals(3.4028235e38f, floats[2]);
            assertEquals(7.006492321624086e-46f, floats[3]); //this assertion fails unless toString is used
        }
    }

    @Test
    public void testArrayOfFloats() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[')
                .append("\"7.038531e-26\",")
                .append("\"1.199999988079071\",")
                .append("\"3.4028235677973366e38\",")
                .append("\"7.006492321624086e-46\"")
                .append(']');
        Float[] floats = MAPPER.readValue(sb.toString(), Float[].class);
        assertEquals(4, floats.length);
        assertEquals(Float.valueOf(7.038531e-26f), floats[0]);
        assertEquals(Float.valueOf(1.1999999f), floats[1]);
        assertEquals(Float.valueOf(3.4028235e38f), floats[2]);
        assertEquals(Float.valueOf("1.4E-45"), floats[3]);
    }

}
