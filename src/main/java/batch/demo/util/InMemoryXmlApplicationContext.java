package batch.demo.util;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

public class InMemoryXmlApplicationContext extends AbstractXmlApplicationContext {

    Resource inMemoryXml;

    public InMemoryXmlApplicationContext(String xml) {
        inMemoryXml = new InMemoryResource(xml);
    }

    protected Resource[] getConfigResources() {
        return new Resource[] {inMemoryXml};
    }
}
