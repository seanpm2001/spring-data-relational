/*
 * Copyright 2019-2024 the original author or authors.
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
package org.springframework.data.jdbc.core.convert;

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Builder for {@link Identifier}. Mainly for internal use within the framework
 *
 * @author Jens Schauder
 * @since 1.1
 */
public class JdbcIdentifierBuilder {

	private Identifier identifier;

	private JdbcIdentifierBuilder(Identifier identifier) {
		this.identifier = identifier;
	}

	public static JdbcIdentifierBuilder empty() {
		return new JdbcIdentifierBuilder(Identifier.empty());
	}

	/**
	 * Creates ParentKeys with backreference for the given path and value of the parents id.
	 */
	public static JdbcIdentifierBuilder forBackReferences(JdbcConverter converter, AggregatePath path,
			@Nullable Object value) {

		// TODO: we need to actually convert this

		RelationalPersistentProperty idProperty = path.getIdDefiningParentPath().getRequiredIdProperty();

		if (value != null && idProperty.isEntity() && idProperty.isEmbedded()) {
			// TODO: Fix for more than one property
			RelationalPersistentEntity<?> propertyType = converter.getMappingContext().getRequiredPersistentEntity(idProperty.getType());
			PersistentPropertyAccessor<Object> propertyAccessor = propertyType.getPropertyAccessor(value);
			Object[] bucket = new Object[1];
			propertyType.doWithProperties((SimplePropertyHandler) p -> {
				if (bucket[0] != null) {
					throw new IllegalStateException("Can't handle embededs with more than one property");
				}
				bucket[0] = propertyAccessor.getProperty(p);
			});
			value = bucket[0];
		}

		Identifier identifier = Identifier.of( //
				path.getTableInfo().reverseColumnInfo().name(), //
				value, //
				converter.getColumnType(idProperty) //
		);

		return new JdbcIdentifierBuilder(identifier);
	}

	/**
	 * Adds a qualifier to the identifier to build. A qualifier is a map key or a list index.
	 *
	 * @param path path to the map that gets qualified by {@code value}. Must not be {@literal null}.
	 * @param value map key or list index qualifying the map identified by {@code path}. Must not be {@literal null}.
	 * @return this builder. Guaranteed to be not {@literal null}.
	 */
	public JdbcIdentifierBuilder withQualifier(AggregatePath path, Object value) {

		Assert.notNull(path, "Path must not be null");
		Assert.notNull(value, "Value must not be null");

		identifier = identifier.withPart(path.getTableInfo().qualifierColumnInfo().name(), value,
				path.getTableInfo().qualifierColumnType());

		return this;
	}

	public Identifier build() {
		return identifier;
	}
}
