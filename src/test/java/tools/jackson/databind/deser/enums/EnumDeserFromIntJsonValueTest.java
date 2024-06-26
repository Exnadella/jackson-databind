package tools.jackson.databind.deser.enums;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonValue;

import tools.jackson.databind.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static tools.jackson.databind.testutil.DatabindTestUtil.newJsonMapper;

public class EnumDeserFromIntJsonValueTest
{
    // [databind#1850]

    enum Bean1850IntMethod {
        A(101);
        final int x;
        Bean1850IntMethod(int x) { this.x = x; }
        @JsonValue
        public int code() { return x; }
    }

    enum Bean1850IntField {
        A(202);
        @JsonValue
        public final int x;
        Bean1850IntField(int x) { this.x = x; }
    }

    enum Bean1850LongMethod {
        A(-13L);
        final long x;
        Bean1850LongMethod(long x) { this.x = x; }
        @JsonValue
        public long code() { return x; }
    }

    enum Bean1850LongField {
        A(29L);
        @JsonValue
        public final long x;
        Bean1850LongField(long x) { this.x = x; }
    }

    private final ObjectMapper MAPPER = newJsonMapper();

    // [databind#1850] pass tests

    @Test
    public void testEnumFromInt1850Method() throws Exception
    {
        String json = MAPPER.writeValueAsString(Bean1850IntMethod.A);
        Bean1850IntMethod e1 = MAPPER.readValue(json, Bean1850IntMethod.class);
        assertEquals(Bean1850IntMethod.A, e1);
    }

    @Test
    public void testEnumFromInt1850Field() throws Exception
    {
        String json = MAPPER.writeValueAsString(Bean1850IntField.A);
        Bean1850IntField e2 = MAPPER.readValue(json, Bean1850IntField.class);
        assertEquals(Bean1850IntField.A, e2);
    }

    @Test
    public void testEnumFromLong1850Method() throws Exception
    {
        String json = MAPPER.writeValueAsString(Bean1850LongMethod.A);
        Bean1850LongMethod e1 = MAPPER.readValue(json, Bean1850LongMethod.class);
        assertEquals(Bean1850LongMethod.A, e1);
    }

    @Test
    public void testEnumFromLong1850Field() throws Exception
    {
        String json = MAPPER.writeValueAsString(Bean1850LongField.A);
        Bean1850LongField e2 = MAPPER.readValue(json, Bean1850LongField.class);
        assertEquals(Bean1850LongField.A, e2);
    }
}
