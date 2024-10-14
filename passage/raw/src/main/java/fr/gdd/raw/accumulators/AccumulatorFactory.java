package fr.gdd.raw.accumulators;

import fr.gdd.passage.commons.interfaces.BackendAccumulator;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.expr.ExprList;

/**
 *  Provide the aggregator iterator with the count-distinct accumulator
 *  it needs based on the configuration in the execution context
 */
@FunctionalInterface
public interface AccumulatorFactory<ID,VALUE> {
    BackendAccumulator<ID,VALUE> create(ExprList varsAsExpr, ExecutionContext context, OpGroup group);
}
