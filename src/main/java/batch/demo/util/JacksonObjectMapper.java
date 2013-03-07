package batch.demo.util;

import javax.annotation.PostConstruct;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

public class JacksonObjectMapper extends ObjectMapper {

	@PostConstruct
	public void afterPropertiesSet() {
		disable(Feature.FAIL_ON_EMPTY_BEANS);
	}

}
