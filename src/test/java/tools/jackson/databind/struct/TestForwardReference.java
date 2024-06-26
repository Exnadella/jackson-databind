package tools.jackson.databind.struct;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.testutil.DatabindTestUtil;

/**
 * Test for testing forward reference handling
 */
public class TestForwardReference extends DatabindTestUtil {
	private final ObjectMapper MAPPER = jsonMapperBuilder()
	        .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
	        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
	        .enable(SerializationFeature.INDENT_OUTPUT)
	        .build();

	/** Tests that we can read a hierarchical structure with forward references*/
	@Test
	public void testForwardRef() throws IOException {
		MAPPER.readValue("{" +
				"  \"@type\" : \"TestForwardReference$ForwardReferenceContainerClass\"," +
				"  \"frc\" : \"willBeForwardReferenced\"," +
				"  \"yac\" : {" +
				"    \"@type\" : \"TestForwardReference$YetAnotherClass\"," +
				"    \"frc\" : {" +
				"      \"@type\" : \"One\"," +
				"      \"id\" : \"willBeForwardReferenced\"" +
				"    }," +
				"    \"id\" : \"anId\"" +
				"  }," +
				"  \"id\" : \"ForwardReferenceContainerClass1\"" +
				"}", ForwardReferenceContainerClass.class);


	}

	@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY)
	public static class ForwardReferenceContainerClass
	{
		public ForwardReferenceClass frc;
		public YetAnotherClass yac;
		public String id;
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonSubTypes({
			@JsonSubTypes.Type(value = ForwardReferenceClassOne.class, name = "One"),
			@JsonSubTypes.Type(value = ForwardReferenceClassTwo.class, name = "Two")})
	static abstract class ForwardReferenceClass
	{
		public String id;
		public void setId(String id) {
			this.id = id;
		}
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
	static class YetAnotherClass
	{
		public YetAnotherClass() {}
		public ForwardReferenceClass frc;
		public String id;
	}

	public static class ForwardReferenceClassOne extends ForwardReferenceClass { }

	public static class ForwardReferenceClassTwo extends ForwardReferenceClass { }
}
