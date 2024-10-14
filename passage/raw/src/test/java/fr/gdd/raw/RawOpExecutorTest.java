package fr.gdd.raw;

import com.bigdata.rdf.sail.BigdataSail;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import fr.gdd.passage.blazegraph.BlazegraphBackend;
import fr.gdd.passage.commons.generics.BackendBindings;
import fr.gdd.passage.commons.interfaces.Backend;
import fr.gdd.passage.databases.inmemory.IM4Blazegraph;
import fr.gdd.raw.executor.RawOpExecutor;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class RawOpExecutorTest {

    private static final Logger log = LoggerFactory.getLogger(RawOpExecutorTest.class);
    // private static final Dataset dataset = IM4Jena.triple9();
    private static final BigdataSail blazegraph = IM4Blazegraph.triples9();

    @Test
    public void select_all_from_simple_spo () throws RepositoryException { // as per usual
        String queryAsString = "SELECT * WHERE {?s ?p ?o}";
        // Multiset<String> results = execute(queryAsString, new JenaBackend(dataset), 100L);
        // assertEquals(9, results.elementSet().size());

        Multiset<String> resultsBlaze = execute(queryAsString, new BlazegraphBackend(blazegraph), 1000L);
        // spo contains other default triples… That why we need more than 100L to
        // retrieve expected spo with high probability.
        // results.elementSet().forEach(e -> assertTrue(resultsBlaze.contains(e)));
    }

    @Test
    public void simple_project_on_spo () throws RepositoryException { // as per usual
        String queryAsString = "SELECT ?s WHERE {?s ?p ?o}";
        // Multiset<String> results = execute(queryAsString, new JenaBackend(dataset), 100L);
        // assertEquals(6, results.elementSet().size()); // Alice repeated 4 times

        Multiset<String> resultsBlaze = execute(queryAsString, new BlazegraphBackend(blazegraph), 1000L);
        // spo contains other default triples… That why we need more than 100L to
        // retrieve expected spo with high probability.
        // results.elementSet().forEach(e -> assertTrue(resultsBlaze.contains(e)));
    }

    @Test
    public void simple_triple_pattern () throws RepositoryException {
        String queryAsString = "SELECT * WHERE {?s <http://address> ?o}";
        // Multiset<String> results = execute(queryAsString, new JenaBackend(dataset), 100L);
        //assertEquals(3, results.elementSet().size());

        Multiset<String> resultsBlaze = execute(queryAsString, new BlazegraphBackend(blazegraph), 1000L);
        // spo contains other default triples… That why we need more than 100L to
        // retrieve expected spo with high probability.
        // results.elementSet().forEach(e -> assertTrue(resultsBlaze.contains(e)));
        assertEquals(3, resultsBlaze.elementSet().size());
    }

    @Test
    public void simple_bgp() throws RepositoryException {
        String queryAsString = "SELECT * WHERE {?s <http://address> ?c . ?s <http://own> ?a}";
        // Multiset<String> results = execute(queryAsString, new JenaBackend(dataset), 100L);
        // assertEquals(3, results.elementSet().size());

        Multiset<String> resultsBlaze = execute(queryAsString, new BlazegraphBackend(blazegraph), 1000L);
        // spo contains other default triples… That why we need more than 100L to
        // retrieve expected spo with high probability.
        // results.elementSet().forEach(e -> assertTrue(resultsBlaze.contains(e)));
        assertEquals(3, resultsBlaze.elementSet().size());
    }

    @Test
    public void simple_bgp_of_3_tps() throws RepositoryException {
        String queryAsString = "SELECT * WHERE {?s <http://address> ?c . ?s <http://own> ?a . ?a <http://species> ?r}";
        // Multiset<String> results = execute(queryAsString, new JenaBackend(dataset), 100L);
        // assertEquals(3, results.elementSet().size());

        Multiset<String> resultsBlaze = execute(queryAsString, new BlazegraphBackend(blazegraph), 1000L);
        // spo contains other default triples… That why we need more than 100L to
        // retrieve expected spo with high probability.
        // results.elementSet().forEach(e -> assertTrue(resultsBlaze.contains(e))); // TODO output contains <> while the expected do not
        assertEquals(3, resultsBlaze.elementSet().size());
    }

    @Disabled
    @Test
    public void simple_bind_on_a_triple_pattern () throws RepositoryException {
        String queryAsString = "SELECT * WHERE {BIND (<http://Alice> AS ?s) ?s ?p ?o}";
        execute(queryAsString, new BlazegraphBackend(blazegraph), 100L);
    }

    @Disabled
    @Test
    public void count_of_simple_triple_pattern () throws RepositoryException {
        String queryAsString = "SELECT (COUNT(*) AS ?c) WHERE {<http://Alice> ?p ?o}";
        execute(queryAsString, new BlazegraphBackend(blazegraph), 10L);
        // should be 4
    }

    @Disabled
    @Test
    public void count_of_carthesian_product_bgp () throws RepositoryException {
        String queryAsString = "SELECT (COUNT(*) AS ?c) WHERE {<http://Alice> ?p ?o . <http://Alice> <http://own> ?a}";
        execute(queryAsString, new BlazegraphBackend(blazegraph), 1L); // 12 since cartesian product
    }

    @Disabled
    @Test
    public void count_of_bgp () throws RepositoryException {
        String queryAsString = "SELECT (COUNT(*) AS ?c) WHERE {?person <http://address> ?location . ?person <http://own> ?animal}";
        // ~3 since only Alice has animals. Choosing a person that has no animal makes the RW fails, hence the approximate
        // value.
        execute(queryAsString, new BlazegraphBackend(blazegraph), 1L);
    }

    @Disabled
    @Test
    public void count_with_group_on_simple_tp () throws RepositoryException {
        // TODO TODO not good yet
        String queryAsString = "SELECT (COUNT(*) AS ?c) ?p WHERE {?s ?p ?o} GROUP BY ?p";
        execute(queryAsString, new BlazegraphBackend(blazegraph), 1L);
        assert false; // The group by should not be handled as in Sage…
    }

    @Disabled
    @Test
    public void count_distinct_of_simple_triple_pattern () throws RepositoryException {
        String queryAsString = "SELECT (COUNT(DISTINCT ?s) AS ?c) WHERE {?s ?p ?o}";
        execute(queryAsString, new BlazegraphBackend(blazegraph), 1L);
    }


    /* ************************************************************* */

    /**
     * @param queryAsString The query to execute.
     * @param backend The backend to use.
     * @param limit The number of scans to perform.
     * @return The random solutions mappings.
     */
    public static <ID,VALUE> Multiset<String> execute(String queryAsString, Backend<ID,VALUE,?> backend, Long limit) {
        Op query = Algebra.compile(QueryFactory.create(queryAsString));

        RawOpExecutor<ID,VALUE> executor = new RawOpExecutor<ID,VALUE>()
                .setBackend(backend)
                .setLimit(limit);

        Iterator<BackendBindings<ID,VALUE>> iterator = executor.execute(query);

        Multiset<String> results = HashMultiset.create();
        while (iterator.hasNext()) {
            String binding = iterator.next().toString();
            results.add(binding);
            log.debug("{}", binding);
        }
        return results;
    }

    /**
     * @param queryAsString The query to execute.
     * @param executor The fully configured executor that will run the query.
     * @return The sample-based solutions mappings.
     */
    public static <ID,VALUE> Multiset<String> execute(String queryAsString, RawOpExecutor<ID,VALUE> executor) {
        Op query = Algebra.compile(QueryFactory.create(queryAsString));

        Iterator<BackendBindings<ID,VALUE>> iterator = executor.execute(query);

        Multiset<String> results = HashMultiset.create();
        while (iterator.hasNext()) {
            String binding = iterator.next().toString();
            results.add(binding);
            log.debug("{}", binding);
        }
        return results;
    }

}