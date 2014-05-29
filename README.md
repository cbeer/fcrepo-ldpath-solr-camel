# LDPath/Solr indexer for Fedora 4 using Apache Camel

This war can be dropped into the servlet container hosting a Fedora 4 instance, and it will listen to the JMS
message queue for Fedora change events. The events will trigger a Solr update to Solr indexes configured within
the repository itself.

## Workflow

An update request is made to Fedora:

```console
$ curl -X POST http://localhost:8080/rest/
```

Fedora sends a JMS message:

 ```
 http://fedora.info/definitions/v4/repository#identifier = /a/b/c/d
 http://fedora.info/definitions/v4/repository#timestamp = 2014-01-10T12:23:34Z
 http://fedora.info/definitions/v4/repository#eventType = .../NODE_CHANGED
 ```

This listener receives the message, aggregates messages by identifier (so multiple updates to the same object
in 500ms are only handled once)

We interrogate the repository (using the LDPath program given by the property `ldpathProgramForIndexers`) to
retrieve a list of solr indexes and LDPath programs to use to construct the Solr document. The out-of-the-box
demo uses a program like:

```
@prefix dc: <http://purl.org/dc/terms/> ;
@prefix dor-indexer: <http://library.stanford.edu/dlss/dor/indexer#> ;
@prefix relsext: <http://fedora.info/definitions/v4/rels-ext#>;

indexers = (relsext:isMemberOfCollection | .) / dc:isPartOf[dor-indexer:isIndexedTo] / fn:concat(dor-indexer:isIndexedTo, "|", dor-indexer:hasService) :: xsd:string ;
```

This program retrieves a core/program tuple for every indexing directive (given by the dor-indexer:isIndexedTo) asserted by this item, or by collections to which this item belongs.

We evaluate the given programs (and LDPath will helpfully cache data retrieval, minimizing round-trips to the repository for multiple indexing operations) and
use Solr's JSON update syntax to index into the given Solr core.

# NOTE

This integration depends on an outstanding pull request on Apache Marmotta:

https://github.com/apache/marmotta/pull/4
