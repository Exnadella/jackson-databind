package tools.jackson.databind.node;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.*;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

public class JsonNodeFactoryTest extends NodeTestBase
{
    private final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .build();

    static class SortingNodeFactory extends JsonNodeFactory {
        private static final long serialVersionUID = 1L;

        @Override
        public ObjectNode objectNode() {
            return new ObjectNode(this, new TreeMap<String, JsonNode>());
        }
    }

    @Test
    public void testSimpleCreation()
    {
        JsonNodeFactory f = MAPPER.getNodeFactory();

        JsonNode n;

        n = f.numberNode((byte) 4);
        assertTrue(n.isInt());
        assertEquals(4, n.intValue());

        assertTrue(f.numberNode((Byte) null).isNull());

        assertTrue(f.numberNode((Short) null).isNull());

        assertTrue(f.numberNode((Integer) null).isNull());

        assertTrue(f.numberNode((Long) null).isNull());

        assertTrue(f.numberNode((Float) null).isNull());

        assertTrue(f.numberNode((Double) null).isNull());

        assertTrue(f.numberNode((BigDecimal) null).isNull());

        assertTrue(f.numberNode((BigInteger) null).isNull());

        assertTrue(f.missingNode().isMissingNode());
    }

    // 09-Sep-2020, tatu: Let's try something more useful: auto-sorting
    //    Tree Model!

    @Test
    public void testSortingObjectNode() throws Exception
    {
       final String SIMPLE_INPUT = "{\"b\":2,\"a\":1}";

       // First, by default, ordering retained:
       assertEquals(SIMPLE_INPUT,
               MAPPER.writeValueAsString(MAPPER.readTree(SIMPLE_INPUT)));

       // But can change easily
       ObjectMapper mapper = JsonMapper.builder()
               .nodeFactory(new SortingNodeFactory())
               .build();
       JsonNode sort = mapper.readTree(SIMPLE_INPUT);
       assertEquals("{\"a\":1,\"b\":2}", MAPPER.writeValueAsString(sort));

       final String BIGGER_INPUT = a2q("['x',{'b':1,'c':true,'a':3},false]");
       final String BIGGER_OUTPUT = a2q("['x',{'a':3,'b':1,'c':true},false]");

       assertEquals(BIGGER_OUTPUT,
               MAPPER.writeValueAsString(mapper.readTree(BIGGER_INPUT)));
   }

   // 06-Nov-2022, tatu: Wasn't being tested, oddly enough
    @Test
   public void testBigDecimalNormalization() throws Exception
   {
       final BigDecimal NON_NORMALIZED = new BigDecimal("12.5000");
       final BigDecimal NORMALIZED = NON_NORMALIZED.stripTrailingZeros();

       // By default, 2.x WILL normalize but 3.x WON'T
       JsonNode n1 = MAPPER.readTree(String.valueOf(NON_NORMALIZED));
       assertEquals(NON_NORMALIZED, n1.decimalValue());

       // But can change
       ObjectMapper normMapper = JsonMapper.builder()
               .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
               .enable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
               .build();
       JsonNode n3 = normMapper.readTree(String.valueOf(NON_NORMALIZED));
       assertEquals(NORMALIZED, n3.decimalValue());
   }
}
