package tools.jackson.databind.deser.jdk;

import java.util.*;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonLocation;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.ValueInstantiator;
import tools.jackson.databind.deser.jackson.JsonLocationInstantiator;

/**
 * Container for a set of {@link ValueInstantiator}s used for certain critical
 * JDK value types, either as performance optimization for initialization time observed
 * by profiling, or due to difficulty in otherwise finding constructors.
 */
public abstract class JDKValueInstantiators
{
    public static ValueInstantiator findStdValueInstantiator(DeserializationConfig config,
            Class<?> raw)
    {
        if (raw == JsonLocation.class) {
            return new JsonLocationInstantiator();
        }
        // [databind#1868]: empty List/Set/Map
        // [databind#2416]: optimize commonly needed default creators
        if (Collection.class.isAssignableFrom(raw)) {
            if (raw == ArrayList.class) {
                return ArrayListInstantiator.INSTANCE;
            }
            if (Collections.EMPTY_SET.getClass() == raw) {
                return new ConstantValueInstantiator(Collections.EMPTY_SET);
            }
            if (Collections.EMPTY_LIST.getClass() == raw) {
                return new ConstantValueInstantiator(Collections.EMPTY_LIST);
            }
        } else if (Map.class.isAssignableFrom(raw)) {
            if (raw == LinkedHashMap.class) {
                return LinkedHashMapInstantiator.INSTANCE;
            }
            if (raw == HashMap.class) {
                return HashMapInstantiator.INSTANCE;
            }
            if (Collections.EMPTY_MAP.getClass() == raw) {
                return new ConstantValueInstantiator(Collections.EMPTY_MAP);
            }
        }
        return null;
    }

    private abstract static class JDKValueInstantiator
        extends ValueInstantiator.Base
    {
        public JDKValueInstantiator(Class<?> type) {
            super(type);
        }

        @Override
        public final boolean canInstantiate() { return true; }

        @Override
        public final boolean canCreateUsingDefault() {  return true; }

        // Make abstract to force (re)implementation
        @Override
        public abstract Object createUsingDefault(DeserializationContext ctxt)
            throws JacksonException;
    }

    private static class ArrayListInstantiator
        extends JDKValueInstantiator
    {
        public final static ArrayListInstantiator INSTANCE = new ArrayListInstantiator();
        public ArrayListInstantiator() {
            super(ArrayList.class);
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) throws JacksonException {
            return new ArrayList<>();
        }
    }

    private static class HashMapInstantiator
        extends JDKValueInstantiator
    {
        public final static HashMapInstantiator INSTANCE = new HashMapInstantiator();

        public HashMapInstantiator() {
            super(HashMap.class);
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) throws JacksonException {
            return new HashMap<>();
        }
    }

    private static class LinkedHashMapInstantiator
        extends JDKValueInstantiator
    {
        public final static LinkedHashMapInstantiator INSTANCE = new LinkedHashMapInstantiator();

        public LinkedHashMapInstantiator() {
            super(LinkedHashMap.class);
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) throws JacksonException {
            return new LinkedHashMap<>();
        }
    }

    private static class ConstantValueInstantiator
        extends JDKValueInstantiator
    {
        protected final Object _value;

        public ConstantValueInstantiator(Object value) {
            super(value.getClass());
            _value = value;
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) throws JacksonException {
            return _value;
        }
    }
}
