package com.bootest.searcher;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

public class SearchSpecification<T> implements Specification<T> {

    private SearchCriteria criteria;

    public SearchSpecification(final SearchCriteria searchCriteria) {
        super();
        this.criteria = searchCriteria;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        if (SearchOperationType.NULL.equals(criteria.getOperation())) {
            return root.get(criteria.getDataField()).isNull();
        } else if (SearchOperationType.NOT_NULL.equals(criteria.getOperation())) {
            return root.get(criteria.getDataField()).isNotNull();
        } else {
            List<Object> arguments = criteria.getValues();
            Object arg = arguments.stream().findFirst().orElse(null);

            if (SearchOperationType.EQUAL.equals(criteria.getOperation())) {
                return builder.equal(root.get(criteria.getDataField()), arg);
            } else if (SearchOperationType.NOT_EQUAL.equals(criteria.getOperation())) {
                return builder.equal(root.get(criteria.getDataField()), arg).not();
            } else if (SearchOperationType.GREATER_THAN.equals(criteria.getOperation())) {
                return builder.greaterThan(root.get(criteria.getDataField()), (Comparable) arg);
            } else if (SearchOperationType.GREATER_THAN_OR_EQUAL.equals(criteria.getOperation())) {
                return builder.greaterThanOrEqualTo(root.get(criteria.getDataField()), (Comparable) arg);
            } else if (SearchOperationType.LESS_THAN.equals(criteria.getOperation())) {
                return builder.lessThan(root.get(criteria.getDataField()), (Comparable) arg);
            } else if (SearchOperationType.LESS_THAN_OR_EQUAL.equals(criteria.getOperation())) {
                return builder.lessThanOrEqualTo(root.get(criteria.getDataField()), (Comparable) arg);
            } else if (SearchOperationType.CONTAINS.equals(criteria.getOperation())) {
                return builder.like(root.get(criteria.getDataField()), "%" + arg.toString() + "%");
            } else if (SearchOperationType.CONTAINS_IGNORE_CASE.equals(criteria.getOperation())) {
                return builder.like(builder.upper(root.get(criteria.getDataField())),
                        "%" + arg.toString().toUpperCase() + "%");
            } else if (SearchOperationType.START_WITH.equals(criteria.getOperation())) {
                return builder.like(root.get(criteria.getDataField()), arg.toString() + "%");
            } else if (SearchOperationType.START_WITH_IGNORE_CASE.equals(criteria.getOperation())) {
                return builder.like(builder.upper(root.get(criteria.getDataField())),
                        arg.toString().toUpperCase() + "%");
            } else if (SearchOperationType.END_WITH.equals(criteria.getOperation())) {
                return builder.like(root.get(criteria.getDataField()), "%" + arg.toString());
            } else if (SearchOperationType.END_WITH_IGNORE_CASE.equals(criteria.getOperation())) {
                return builder.like(builder.upper(root.get(criteria.getDataField())),
                        "%" + arg.toString().toUpperCase());
            } else if (SearchOperationType.IN.equals(criteria.getOperation())) {
                return root.get(criteria.getDataField()).in(arguments);
            } else if (SearchOperationType.NOT_IN.equals(criteria.getOperation())) {
                return root.get(criteria.getDataField()).in(arguments).not();
            } else if (SearchOperationType.BETWEEN.equals(criteria.getOperation())) {
                return builder.between(root.get(criteria.getDataField()), (Comparable) arguments.get(0),
                        (Comparable) arguments.get(1));
            } else
                return null;
        }
    }
}
