package com.bootest.searcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specification 쿼리 생성기
 * 참조1:
 * https://www.baeldung.com/rest-api-search-language-spring-data-specifications
 * 참조2:
 */
public class SearchBuilder<T> {
	private final List<SearchCriteria> params;

	public SearchBuilder() {
		this.params = new ArrayList<>();
	}

	public SearchBuilder<T> with(String dataField, SearchOperationType operation, List<Object> values) {
		this.params.add(new SearchCriteria(dataField, operation, values, false));
		return this;
	}

	public SearchBuilder<T> isNull(String dataField) {
		isNull(dataField, false);
		return this;
	}

	public SearchBuilder<T> isNull(String dataField, boolean isOrOperation) {
		this.params.add(new SearchCriteria(dataField, SearchOperationType.NULL, null, isOrOperation));
		return this;
	}

	public SearchBuilder<T> isNotNull(String dataField) {
		isNotNull(dataField, false);
		return this;
	}

	public SearchBuilder<T> isNotNull(String dataField, boolean isOrOperation) {
		this.params.add(new SearchCriteria(dataField, SearchOperationType.NOT_NULL, null, isOrOperation));
		return this;
	}

	public SearchBuilder<T> with(String dataField, SearchOperationType operation, List<Object> values,
			boolean isOrOperation) {
		this.params.add(new SearchCriteria(dataField, operation, values, isOrOperation));
		return this;
	}

	public SearchBuilder<T> with(String dataField, SearchOperationType operation, Object value) {
		this.params.add(new SearchCriteria(dataField, operation, List.of(value), false));
		return this;
	}

	public SearchBuilder<T> with(String dataField, SearchOperationType operation, Object value,
			boolean isOrOperation) {
		this.params.add(new SearchCriteria(dataField, operation, List.of(value), isOrOperation));
		return this;
	}

	public static <T> SearchBuilder<T> builder() {
		return new SearchBuilder<T>();
	}

	public Specification<T> build() {
		if (params.size() == 0) {
			return null;
		}

		List<Specification<T>> specs = params.stream().map(SearchSpecification<T>::new).collect(Collectors.toList());

		Specification<T> result = specs.get(0);

		for (int i = 1; i < params.size(); i++) {
			result = params.get(i).isOrOperation() ? Specification.where(result).or(specs.get(i))
					: Specification.where(result).and(specs.get(i));
		}
		return result;
	}
}