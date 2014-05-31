package info.cbeer.fcrepo.ldpath.solr.camel;

import org.apache.camel.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.backend.infinispan.LDCachingInfinispanBackend;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.services.LDCache;
import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.backend.linkeddata.LDCacheBackend;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

public class TranslationService {

    private final LDCache ldcache;
    private String defaultTemplateUri;
    private final LDCacheBackend backend;
    private final LDPath<Value> ldpath;
    private final CloseableHttpClient templateClient;

    public TranslationService() {

        LDCachingBackend cacheBackend = new LDCachingInfinispanBackend();
        cacheBackend.initialize();

        ldcache = new LDCache(new CacheConfiguration(), cacheBackend);

        backend = new LDCacheBackend(ldcache);
        ldpath = new LDPath<Value>(backend);
        CacheConfig cacheConfig = CacheConfig.custom()
                                      .setMaxCacheEntries(100)
                                      .setMaxObjectSize(81920)
                                      .build();
        RequestConfig requestConfig = RequestConfig.custom()
                                          .setConnectTimeout(30000)
                                          .setSocketTimeout(30000)
                                          .build();
        templateClient = CachingHttpClients.custom()
                                                .setCacheConfig(cacheConfig)
                                                .setDefaultRequestConfig(requestConfig)
                                                .build();
    }

    public Map<String, Collection<?>> executeLDPathProgram(String body,
                                                  @Header("CamelLDpathProgramUri") String templateUri,
                                                  @Header("LDpathContextUri") String context) throws LDPathParseException, IOException {

        final URI uri = backend.createURI(context);
        ldcache.expire(uri);
        return ldpath.programQuery(uri, getTemplateReader(templateUri));
    }

    private BufferedReader getTemplateReader(String templateUri) throws IOException {
        final CloseableHttpResponse response = templateClient.execute(new HttpGet(templateUri));

        return new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    }

    public String getDefaultTemplateUri() {
        return defaultTemplateUri;
    }

    public void setDefaultTemplateUri(String defaultTemplateUri) {
        this.defaultTemplateUri = defaultTemplateUri;
    }
}
