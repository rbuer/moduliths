/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.olivergierke.moduliths.model;

import de.olivergierke.moduliths.Modulith;
import de.olivergierke.moduliths.Modulithic;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;

interface ModulithMetadata {

	static final String ANNOTATION_MISSING = "Modules can only be retrieved from a root type, but %s is not annotated with either @%s, @%s or @%s!";

	/**
	 * Creates a new {@link ModulithMetadata} for the given annotated type. Expecteds the type either be annotated with
	 * {@link Modulith}, {@link Modulithic} or {@link SpringBootApplication}.
	 *
	 * @param annotated must not be {@literal null}.
	 * @return
	 * @throws IllegalArgumentException in case none of the above mentioned annotations is present on the given type.
	 */
	public static ModulithMetadata of(Class<?> annotated) {

		Assert.notNull(annotated, "Annotated type must not be null!");

		Supplier<IllegalArgumentException> exception = () -> new IllegalArgumentException(
				String.format(ANNOTATION_MISSING, annotated.getSimpleName(), Modulith.class.getSimpleName(),
						Modulithic.class.getSimpleName(), SpringBootApplication.class.getSimpleName()));

		Supplier<ModulithMetadata> withDefaults = () -> DefaultModulithMetadata.of(annotated).orElseThrow(exception);

		return AnnotationModulithMetadata.of(annotated).orElseGet(withDefaults);
	}

	Class<?> getModulithType();

	/**
	 * Returns the names of the packages that are supposed to be considered modulith base packages, i.e. for which to
	 * consider all direct sub-packages modules by default.
	 *
	 * @return will never be {@literal null}.
	 */
	List<String> getAdditionalPackages();

	/**
	 * Whether to use fully-qualified module names, i.e. rather use the fully-qualified package name instead of the local
	 * one.
	 *
	 * @return
	 */
	boolean useFullyQualifiedModuleNames();

	/**
	 * Returns the name of shared modules, i.e. modules that are supposed to always be included in bootstraps.
	 *
	 * @return will never be {@literal null}.
	 */
	Stream<String> getSharedModuleNames();

	/**
	 * Returns the name of the system.
	 *
	 * @return will never be {@literal null}.
	 */
	Optional<String> getSystemName();
}
