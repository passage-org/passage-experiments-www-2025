package fr.gdd.raw;

import fr.gdd.passage.blazegraph.BlazegraphBackend;
import fr.gdd.raw.accumulators.CountDistinctChaoLee;
import fr.gdd.raw.executor.RawOpExecutor;
import fr.gdd.raw.iterators.RandomAggregator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Set of tests dedicated to COUNT DISTINCT queries.
 * The dataset is WatDiv with 10M triples. It does not take too much time to
 * ingest, does not have a lot of weird characters, etc. Although it has long
 * strings as object literal sometimes.
 */
@Disabled
public class RawWatdivCountDistinctTest {

    private final static Logger log = LoggerFactory.getLogger(RawWatdivCountDistinctTest.class);
    static BlazegraphBackend watdivBlazegraph;

    static {
        try {
            watdivBlazegraph = new BlazegraphBackend("/Users/nedelec-b-2/Desktop/Projects/temp/watdiv10m-blaze/watdiv10M.jnl");
        } catch (SailException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Disabled
    @Test
    public void count_distinct_s_on_spo () {
        String queryAsString = "SELECT (COUNT( DISTINCT ?s ) AS ?count) WHERE { ?s ?p ?o }";
        RandomAggregator.SUBQUERY_LIMIT = 1;
        RawOpExecutorTest.execute(queryAsString, watdivBlazegraph, 100_000L); // 521,585 (+blaze default ones)
    }

    @Disabled
    @Test
    public void count_distinct_s_on_spo_with_chao_lee () {
        // chao lee is not expected to be sample efficient…
        String queryAsString = "SELECT (COUNT( DISTINCT ?s ) AS ?count) WHERE { ?s ?p ?o }";
        RandomAggregator.SUBQUERY_LIMIT = 1;
        RawOpExecutor executor = new RawOpExecutor();
        executor.setBackend(watdivBlazegraph).setLimit(1_000_000L).setCountDistinct(CountDistinctChaoLee::new).setMaxThreads(10);
        RawOpExecutorTest.execute(queryAsString, executor); // 521,585 (+blaze default ones)
    }

    @Disabled
    @Test
    public void count_distinct_p_on_spo () {
        String queryAsString = "SELECT (COUNT( DISTINCT ?p ) AS ?count) WHERE { ?s ?p ?o }";
        RandomAggregator.SUBQUERY_LIMIT = 1;
        RawOpExecutorTest.execute(queryAsString, watdivBlazegraph, 100_000L); // 86 (+blaze default ones)
    }

    @Disabled
    @Test
    public void count_distinct_o_on_spo () {
        String queryAsString = "SELECT (COUNT( DISTINCT ?o ) AS ?count) WHERE { ?s ?p ?o }";
        RandomAggregator.SUBQUERY_LIMIT = 1;
        RawOpExecutorTest.execute(queryAsString, watdivBlazegraph, 100_000L); // 1,005,832 (+blaze default ones)
    }

    @Disabled
    @Test
    public void count_distinct_on_query_10069 () throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        String twoTPsQuery = """
                SELECT (COUNT( DISTINCT ?v4 ) AS ?count) WHERE {
                    ?v0 <http://db.uwaterloo.ca/~galuc/wsdbm/gender> <http://db.uwaterloo.ca/~galuc/wsdbm/Gender1> .
                    ?v0 <http://xmlns.com/foaf/givenName> ?v1 .
                    ?v0 <http://schema.org/nationality> ?v3 .
                    ?v2 <http://www.geonames.org/ontology#parentCountry> ?v3 .
                    ?v4 <http://schema.org/eligibleRegion> ?v3 .
                }""";
        //var results = watdivBlazegraph.executeQuery(twoTPsQuery);
        //log.debug("{}", results.toString());
        RandomAggregator.SUBQUERY_LIMIT = 5*20;
        // C(Q) = 4.17E9 results
        // CD(v4) = 44935 offers
        // CD(v1) = 1720 names
        RawOpExecutorTest.execute(twoTPsQuery, watdivBlazegraph, 1_000_000L);
    }

    @Disabled
    @Test
    public void count_distinct_on_8_tps_of_query_10020 () throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        String twoTPsQuery = """
                SELECT (COUNT(DISTINCT(?v4)) AS ?count) WHERE {
                        ?v1 <http://schema.org/priceValidUntil> ?v8.
                        ?v1 <http://purl.org/goodrelations/validFrom> ?v2.
                        ?v1 <http://purl.org/goodrelations/validThrough> ?v3.
                        ?v1 <http://schema.org/eligibleQuantity> ?v6.
                        ?v0 <http://purl.org/goodrelations/offers> ?v1.
                        ?v1 <http://schema.org/eligibleRegion> ?v7.
                        ?v4 <http://schema.org/nationality> ?v7.
                        ?v4 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://db.uwaterloo.ca/~galuc/wsdbm/Role0>.
                }""";
        // var results = watdivBlazegraph.executeQuery(twoTPsQuery);
        // log.debug("{}", results.toString());
        RandomAggregator.SUBQUERY_LIMIT = 8*100;
        // C(Q) = 7_554_617 elements
        // CD(v4) = 11735
        RawOpExecutorTest.execute(twoTPsQuery, watdivBlazegraph, 1_000_000L);
    }



    /* ********************************************************************* */

    @Disabled
    @Test
    public void issue_with_blobs_not_being_blobs () throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        // In the dataset, the long string (of the query below) is considered as a blob,
        // don't know why since its length
        // is smaller than 256 which is the inner to blob threshold.
        // When the parser read the query, it reads the long query as inner.
        // Therefore, it finds no results… Should investigate on this…
        String queryAsString = """
            SELECT * WHERE { ?s ?p "hooey porringers curved Coriolis floating rapid Hispaniola's rectifying averages militarization islander's nonaligned instigators obviated confrontational deathblow flank provoke lutes peroxide deerskin's shirrs unknown" }
            """;
        long nbResults = watdivBlazegraph.countQuery(queryAsString);
        assertTrue(nbResults > 0);
    }

}
