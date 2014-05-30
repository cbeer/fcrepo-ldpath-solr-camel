package info.cbeer.fcrepo.ldpath.solr.camel;

import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class TranslationServiceTest extends CamelSpringTestSupport {


    @Test
    public void testDoSomething() throws Exception {
        getMockEndpoint("mock:results").expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("LDpathContextUri", "http://127.0.0.1:8080/rest/qb/438/pg/7646/qb438pg7646");
        template.sendBodyAndHeaders("direct:start", "", headers);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("spring-test/test-config.xml");
    }
}