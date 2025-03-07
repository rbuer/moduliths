/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.olivergierke.moduliths.model;

import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.tngtech.archunit.base.DescribedIterable;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.HasName;

/**
 * @author Oliver Gierke
 */
@ToString
class Classes implements DescribedIterable<JavaClass> {

	private final List<JavaClass> classes;

	/**
	 * Creates a new {@link Classes} for the given {@link JavaClass}es.
	 *
	 * @param classes must not be {@literal null}.
	 */
	private Classes(List<JavaClass> classes) {

		Assert.notNull(classes, "JavaClasses must not be null!");

		this.classes = classes.stream() //
				.sorted(Comparator.comparing(JavaClass::getName)) //
				.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
	}

	/**
	 * Creates a new {@link Classes} for the given {@link JavaClass}es.
	 *
	 * @param classes must not be {@literal null}.
	 * @return
	 */
	static Classes of(JavaClasses classes) {

		return new Classes(StreamSupport.stream(classes.spliterator(), false) //
				.collect(Collectors.toList()));
	}

	/**
	 * Creates a new {@link Classes} for the given {@link JavaClass}es.
	 *
	 * @param classes must not be {@literal null}.
	 * @return
	 */
	static Classes of(List<JavaClass> classes) {
		return new Classes(classes);
	}

	/**
	 * Returns {@link Classes} that match the given {@link DescribedPredicate}.
	 *
	 * @param predicate must not be {@literal null}.
	 * @return
	 */
	Classes that(DescribedPredicate<? super JavaClass> predicate) {

		Assert.notNull(predicate, "Predicate must not be null!");

		return classes.stream() //
				.filter((Predicate<JavaClass>) it -> predicate.apply(it)) //
				.collect(Collectors.collectingAndThen(Collectors.toList(), Classes::new));
	}

	/**
	 * Returns a Classes with the current elements and the given other ones combined.
	 *
	 * @param others must not be {@literal null}.
	 * @return
	 */
	Classes and(Collection<JavaClass> others) {

		Assert.notNull(others, "JavaClasses must not be null!");

		if (others.isEmpty()) {
			return this;
		}

		List<JavaClass> result = new ArrayList<>(classes);

		others.forEach(it -> {
			if (!result.contains(it)) {
				result.add(it);
			}
		});

		return new Classes(result);
	}

	Stream<JavaClass> stream() {
		return classes.stream();
	}

	boolean isEmpty() {
		return !classes.iterator().hasNext();
	}

	Optional<JavaClass> toOptional() {
		return isEmpty() ? Optional.empty() : Optional.of(classes.iterator().next());
	}

	boolean contains(JavaClass type) {
		return that(new SameClass(type)).iterator().hasNext();
	}

	boolean contains(String className) {
		return that(HasName.Predicates.name(className)).iterator().hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see com.tngtech.archunit.base.HasDescription#getDescription()
	 */
	@Override
	public String getDescription() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<JavaClass> iterator() {
		return classes.iterator();
	}

	static String format(JavaClass type) {
		return format(type, "");
	}

	static String format(JavaClass type, String basePackage) {

		Assert.notNull(type, "Type must not be null!");
		Assert.notNull(basePackage, "Base package must not be null!");

		String prefix = type.getModifiers().contains(JavaModifier.PUBLIC) ? "+" : "o";
		String name = StringUtils.hasText(basePackage) //
				? type.getName().replace(basePackage, "…") //
				: type.getName();

		return String.format("%s %s", prefix, name);
	}

	private static class SameClass extends DescribedPredicate<JavaClass> {

		private final JavaClass reference;

		public SameClass(JavaClass reference) {
			super(" is the same class as ");
			this.reference = reference;
		}

		/*
		 * (non-Javadoc)
		 * @see com.tngtech.archunit.base.DescribedPredicate#apply(java.lang.Object)
		 */
		@Override
		public boolean apply(JavaClass input) {
			return reference.getName().equals(input.getName());
		}
	}
}
