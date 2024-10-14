package fr.gdd.raw.iterators;

import fr.gdd.passage.commons.generics.BackendBindings;
import fr.gdd.passage.commons.generics.BackendSaver;
import fr.gdd.passage.commons.interfaces.Backend;
import fr.gdd.passage.commons.interfaces.BackendIterator;
import fr.gdd.passage.commons.interfaces.SPOC;
import fr.gdd.raw.executor.RawConstants;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;

import java.util.Iterator;
import java.util.Objects;

/**
 * A scan executes only once, in random settings.
 */
public class RandomScan<ID, VALUE> implements Iterator<BackendBindings<ID, VALUE>> {

    boolean consumed = false;
    Double currentProbability;

    final ExecutionContext context;
    final OpTriple triple;
    final BackendIterator<ID, VALUE, ?> iterator;
    final Backend<ID, VALUE, ?> backend;
    final Tuple3<Var> vars;

    public RandomScan(ExecutionContext context, OpTriple triple, Tuple<ID> spo) {
        this.backend = context.getContext().get(RawConstants.BACKEND);
        this.triple = triple;
        this.context = context;
        this.iterator = backend.search(spo.get(0), spo.get(1), spo.get(2));
        this.vars = TupleFactory.create3(
                triple.getTriple().getSubject().isVariable() && Objects.isNull(spo.get(0)) ? Var.alloc(triple.getTriple().getSubject()) : null,
                triple.getTriple().getPredicate().isVariable() && Objects.isNull(spo.get(1)) ? Var.alloc(triple.getTriple().getPredicate()) : null,
                triple.getTriple().getObject().isVariable() && Objects.isNull(spo.get(2)) ? Var.alloc(triple.getTriple().getObject()) : null);

        BackendSaver<ID,VALUE,?> saver = this.context.getContext().get(RawConstants.SAVER);
        if (Objects.nonNull(saver)) {
            saver.register(triple, this);
        }
    }

    @Override
    public boolean hasNext() {
        return !consumed && iterator.hasNext(); // at least 1 element, only called once anyway
    }

    @Override
    public BackendBindings<ID, VALUE> next() {
        consumed = true;
        this.currentProbability = iterator.random(); // position at random index
        iterator.next(); // read the value

        RawConstants.incrementScans(context);

        BackendBindings<ID, VALUE> newBinding = new BackendBindings<>();

        if (Objects.nonNull(vars.get(0))) { // ugly x3
            newBinding.put(vars.get(0), iterator.getId(SPOC.SUBJECT), backend).setCode(vars.get(0), SPOC.SUBJECT);
        }
        if (Objects.nonNull(vars.get(1))) {
            newBinding.put(vars.get(1), iterator.getId(SPOC.PREDICATE), backend).setCode(vars.get(1), SPOC.PREDICATE);
        }
        if (Objects.nonNull(vars.get(2))) {
            newBinding.put(vars.get(2), iterator.getId(SPOC.OBJECT), backend).setCode(vars.get(2), SPOC.OBJECT);
        }

        return newBinding;
    }

    public double getProbability() {
        return currentProbability;
    }

    public double cardinality() {return iterator.cardinality();}
}
