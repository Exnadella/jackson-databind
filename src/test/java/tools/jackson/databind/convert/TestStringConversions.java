package tools.jackson.databind.convert;

import java.util.*;

import org.junit.jupiter.api.Test;

import tools.jackson.core.Base64Variants;
import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.util.StdConverter;

import static org.junit.jupiter.api.Assertions.*;

public class TestStringConversions
{
    static class LCConverter extends StdConverter<String,String>
    {
        @Override public String convert(String value) {
            return value.toLowerCase();
        }
    }

    static class StringWrapperWithConvert
    {
        @JsonSerialize(converter=LCConverter.class)
        @JsonDeserialize(converter=LCConverter.class)
        public String value;

        protected StringWrapperWithConvert() { }
        public StringWrapperWithConvert(String v) { value = v; }
    }

    private final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testSimple()
    {
        assertEquals(Boolean.TRUE, MAPPER.convertValue("true", Boolean.class));
        assertEquals(Integer.valueOf(-3), MAPPER.convertValue("-3", Integer.class));
        assertEquals(Long.valueOf(77), MAPPER.convertValue("77", Long.class));

        int[] ints = { 1, 2, 3 };
        List<Integer> Ints = new ArrayList<Integer>();
        Ints.add(1);
        Ints.add(2);
        Ints.add(3);

        assertArrayEquals(ints, MAPPER.convertValue(Ints, int[].class));
    }

    @Test
    public void testStringsToInts()
    {
        // let's verify our "neat trick" actually works...
        assertArrayEquals(new int[] { 1, 2, 3, 4, -1, 0 },
                          MAPPER.convertValue("1  2 3    4  -1 0".split("\\s+"), int[].class));
    }

    @Test
    public void testBytesToBase64AndBack() throws Exception
    {
        byte[] input = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        String encoded = MAPPER.convertValue(input, String.class);
        assertNotNull(encoded);

        assertEquals("AQIDBAUGBw==", encoded);

        // plus, ensure this is consistent:
        assertEquals(Base64Variants.MIME.encode(input), encoded);

        byte[] result = MAPPER.convertValue(encoded, byte[].class);
        assertArrayEquals(input, result);
    }

    @Test
    public void testBytestoCharArray() throws Exception
    {
        byte[] input = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        // first, do baseline encoding
        char[] expEncoded = MAPPER.convertValue(input, String.class).toCharArray();
        // then compare
        char[] actEncoded = MAPPER.convertValue(input, char[].class);
        assertArrayEquals(expEncoded, actEncoded);
    }

    @Test
    public void testLowerCasingSerializer() throws Exception
    {
        assertEquals("{\"value\":\"abc\"}", MAPPER.writeValueAsString(new StringWrapperWithConvert("ABC")));
    }

    @Test
    public void testLowerCasingDeserializer() throws Exception
    {
        StringWrapperWithConvert value = MAPPER.readValue("{\"value\":\"XyZ\"}", StringWrapperWithConvert.class);
        assertNotNull(value);
        assertEquals("xyz", value.value);
    }
}
