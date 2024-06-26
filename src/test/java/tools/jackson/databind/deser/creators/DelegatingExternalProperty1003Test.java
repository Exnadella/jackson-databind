package tools.jackson.databind.deser.creators;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DelegatingExternalProperty1003Test
{
    static class HeroBattle {

        private final Hero hero;

        HeroBattle(Hero hero) {
            if (hero == null) throw new Error();
            this.hero = hero;
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "heroType")
        public Hero getHero() {
            return hero;
        }

        @JsonCreator
        static HeroBattle fromJson(Delegate json) {
            return new HeroBattle(json.hero);
        }
    }

    static class Delegate {
        @JsonProperty
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "heroType")
        public Hero hero;
    }

    public interface Hero { }

    static class Superman implements Hero {
        String name = "superman";

        public String getName() {
            return name;
        }
    }

    @Test
    public void testExtrnalPropertyDelegatingCreator() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        final String json = mapper.writeValueAsString(new HeroBattle(new Superman()));

        final HeroBattle battle = mapper.readValue(json, HeroBattle.class);

        assertTrue(battle.getHero() instanceof Superman);
    }
}
