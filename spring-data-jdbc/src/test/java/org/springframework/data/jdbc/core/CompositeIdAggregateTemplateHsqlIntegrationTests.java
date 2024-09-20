/*
 * Copyright 2017-2024 the original author or authors.
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
package org.springframework.data.jdbc.core;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.testing.DatabaseType;
import org.springframework.data.jdbc.testing.EnabledOnDatabase;
import org.springframework.data.jdbc.testing.IntegrationTest;
import org.springframework.data.jdbc.testing.TestConfiguration;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

/**
 * Integration tests for {@link JdbcAggregateTemplate} and it's handling of entities with embedded entities as keys.
 *
 * @author Jens Schauder
 */
@IntegrationTest
@EnabledOnDatabase(DatabaseType.HSQL)
public class CompositeIdAggregateTemplateHsqlIntegrationTests {

	@Autowired JdbcAggregateOperations template;

	@Test
	// GH-574
	void saveAndLoadSimpleEntity() {

		SimpleEntity entity = template.insert(new SimpleEntity(new WrappedPk(23L), "alpha"));

		assertThat(entity.wrappedPk).isNotNull() //
				.extracting(WrappedPk::id).isNotNull();

		SimpleEntity reloaded = template.findById(entity.wrappedPk, SimpleEntity.class);

		assertThat(reloaded).isEqualTo(entity);
	}

	@Test
	// GH-574
	void saveAndLoadEntityWithList() {

		WithList entity = template
				.insert(new WithList(new WrappedPk(23L), "alpha", List.of(new Child("Romulus"), new Child("Remus"))));

		assertThat(entity.wrappedPk).isNotNull() //
				.extracting(WrappedPk::id).isNotNull();

		WithList reloaded = template.findById(entity.wrappedPk, WithList.class);

		assertThat(reloaded).isEqualTo(entity);
	}

	@Test
	// GH-574
	void saveAndLoadSimpleEntityWithEmbeddedPk() {

		SimpleEntityWithEmbeddedPk entity = template
				.insert(new SimpleEntityWithEmbeddedPk(new EmbeddedPk(23L, "x"), "alpha"));

		SimpleEntityWithEmbeddedPk reloaded = template.findById(entity.embeddedPk, SimpleEntityWithEmbeddedPk.class);

		assertThat(reloaded).isEqualTo(entity);
	}

	@Test
	// GH-574
	void saveAndLoadSimpleEntitiesWithEmbeddedPk() {

		List<SimpleEntityWithEmbeddedPk> entities = (List<SimpleEntityWithEmbeddedPk>) template
				.insertAll(List.of(new SimpleEntityWithEmbeddedPk(new EmbeddedPk(23L, "x"), "alpha")));

		Iterable<SimpleEntityWithEmbeddedPk> reloaded = template.findAllById(entities.stream().map(SimpleEntityWithEmbeddedPk::embeddedPk).toList(), SimpleEntityWithEmbeddedPk.class);

		assertThat(reloaded).containsExactlyInAnyOrder(entities.toArray(new SimpleEntityWithEmbeddedPk[0]));
	}

	private record WrappedPk(Long id) {
	}

	private record SimpleEntity( //
			@Id @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) WrappedPk wrappedPk, //
			String name //
	) {
	}

	private record Child(String name) {
	}

	private record WithList( //
			@Id @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) WrappedPk wrappedPk, //
			String name, List<Child> children) {
	}

	private record EmbeddedPk(Long one, String two) {
	}

	private record SimpleEntityWithEmbeddedPk( //
			@Id @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) EmbeddedPk embeddedPk, //
			String name //
	) {
	}

	@Configuration
	@Import(TestConfiguration.class)
	static class Config {

		@Bean
		Class<?> testClass() {
			return CompositeIdAggregateTemplateHsqlIntegrationTests.class;
		}

		@Bean
		JdbcAggregateOperations operations(ApplicationEventPublisher publisher, RelationalMappingContext context,
				DataAccessStrategy dataAccessStrategy, JdbcConverter converter) {
			return new JdbcAggregateTemplate(publisher, context, converter, dataAccessStrategy);
		}
	}
}
