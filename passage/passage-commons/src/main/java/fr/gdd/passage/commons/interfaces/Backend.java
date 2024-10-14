package fr.gdd.passage.commons.interfaces;

import java.io.Serializable;

/**
 * Basic interface that a backend must implement to execute the code
 * generated by `sage-query-generator`. The generated code allows
 * pausing/resuming query execution, i.e., "preemptive" querying.
 */
public interface Backend<ID, VALUE, SKIP extends Serializable> {

    /**
     * Returns a preemptable iterator that enables scanning the triple pattern.
     * @param s The identifier of the subject.
     * @param p The identifier of the predicate.
     * @param o The identifier of the object.
     * @return A preemptable iterator.
     */
    BackendIterator<ID, VALUE, SKIP> search(final ID s, final ID p, final ID o);

    /**
     * Same as above, but with a context or graph parameter.
     * @param c (Optional) The identifier of the context, also known as graph.
     * @return A preemptable iterator.
     */
    BackendIterator<ID, VALUE, SKIP> search(final ID s, final ID p, final ID o, final ID c);

    /**
     * Calls the underlying dictionary to retrieve the identifier
     * corresponding to the value. This enables saving once and for
     * all the identifier for future usage.
     * @param value The value to retrieve the identifier from
     * @param type (Optional) The type of the value to look
     * for. Depending on the backend, it may improve lookup time.
     * @return The identifier of the value.
     */
    default ID getId(final String value, final int... type) {throw new UnsupportedOperationException();}
    default ID getId(final VALUE value, final int... type) {throw new UnsupportedOperationException();}

    /**
     * Calls the underlying dictionary to retrieve the value
     * corresponding to the identifier. This allows a
     * `BackendIterator` to call and cache the value as long as it can
     * stay relevant in the execution context.
     * @param value The identifier of the value to retrieve in the dictionary.
     * @return The value as a string corresponding to the identifier.
     */
    default VALUE getValue(final String value, final int... type) {throw new UnsupportedOperationException();}
    default VALUE getValue(final ID id, final int... type) {throw new UnsupportedOperationException();}

    /**
     * Calls the underlying dictionary to retrieve the value
     * corresponding to the identifier. This allows a
     * `BackendIterator` to call and cache the value as long as it can
     * stay relevant in the execution context.
     * @param id The identifier of the value to retrieve in the dictionary.
     * @return The value as a string corresponding to the identifier.
     */
    default String getString(final ID id, final int... type) {throw new UnsupportedOperationException();}

    /**
     * @return The identifier of the wildcard `any` or `*` for the
     * backend.
     */
    ID any();
}