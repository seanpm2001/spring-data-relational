package org.springframework.data.relational.core.sql;

import static java.util.stream.Collectors.*;

import java.util.List;

/**
 * A tuple as used in conditions like
 * 
 * <pre>
 *   WHERE (one, two) IN (select x, y from some_table)
 * </pre>
 *
 * @author Jens Schauder
 * @since 3.4
 */
public class TupleExpression extends AbstractSegment implements Expression {

	private final List<Expression> expressions;

	private static Segment[] children(List<Expression> expressions) {
		return expressions.toArray(new Segment[0]);
	}

	private TupleExpression(List<Expression> expressions) {

		super(children(expressions));

		this.expressions = expressions;
	}

	public static TupleExpression create(Expression... expressions) {
		return new TupleExpression(List.of(expressions));
	}

	@Override
	public String toString() {
		return "(" + expressions.stream().map(Expression::toString).collect(joining(", ")) + ")";
	}
}
