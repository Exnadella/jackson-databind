package tools.jackson.databind.ext.jdk8;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.*;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import tools.jackson.databind.ser.BeanSerializerFactory;
import tools.jackson.databind.ser.SerializationContextExt;
import tools.jackson.databind.ser.SerializerCache;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class OptionalUnwrappedTest
    extends DatabindTestUtil
{
    static class Child {
        public String name = "Bob";
    }

    static class Parent {
        private Child child = new Child();

        @JsonUnwrapped
        public Child getChild() {
            return child;
        }
    }

    static class OptionalParent {
        @JsonUnwrapped(prefix = "XX.")
        public Optional<Child> child = Optional.of(new Child());
    }

    static class Bean {
        public String id;
        @JsonUnwrapped(prefix="child")
        public Optional<Bean2> bean2;

        public Bean(String id, Optional<Bean2> bean2) {
            this.id = id;
            this.bean2 = bean2;
        }
    }

    static class Bean2 {
        public String name;
    }

    public void testUntypedWithOptionalsNotNulls() throws Exception
    {
		final ObjectMapper mapper = newJsonMapper();
		String jsonExp = a2q("{'XX.name':'Bob'}");
		String jsonAct = mapper.writeValueAsString(new OptionalParent());
		assertEquals(jsonExp, jsonAct);
	}

	// for [datatype-jdk8#20]
	public void testShouldSerializeUnwrappedOptional() throws Exception {
         final ObjectMapper mapper = newJsonMapper();

	    assertEquals("{\"id\":\"foo\"}",
	            mapper.writeValueAsString(new Bean("foo", Optional.<Bean2>empty())));
	}

	// for [datatype-jdk8#26]
	public void testPropogatePrefixToSchema() throws Exception {
        final ObjectMapper mapper = newJsonMapper();

        final AtomicReference<String> propertyName = new AtomicReference<>();
        mapper.acceptJsonFormatVisitor(OptionalParent.class, new JsonFormatVisitorWrapper.Base(
                new SerializationContextExt.Impl(new JsonFactory(),
                        mapper.serializationConfig(), null,
                        BeanSerializerFactory.instance, new SerializerCache())) {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType type) {
                return new JsonObjectFormatVisitor.Base(getProvider()) {
                    @Override
                    public void optionalProperty(BeanProperty prop) {
                        propertyName.set(prop.getName());
                    }
                };
            }
        });
        assertEquals("XX.name", propertyName.get());
    }
}
