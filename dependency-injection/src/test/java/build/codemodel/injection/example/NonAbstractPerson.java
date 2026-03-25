package build.codemodel.injection.example;

/*-
 * #%L
 * Dependency Injection
 * %%
 * Copyright (C) 2026 Workday, Inc.
 * %%
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
 * #L%
 */

import build.codemodel.injection.PostInject;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * A non-abstract person to test injection into abstract {@link Class}es.
 *
 * @author brian.oliver
 * @since Jul-2018
 */
public class NonAbstractPerson
    extends AbstractPerson {

    /**
     * Was the {@link #onNonAbstractPostInject()} invoked?
     */
    private boolean onNonAbstractPostInjectInvoked;

    /**
     * Constructs a person.
     *
     * @param firstName the first name of the person
     * @param lastName  the last name of the person
     */
    @Inject
    public NonAbstractPerson(@Named("FirstName") final String firstName, @Named("LastName") final String lastName) {
        super(firstName, lastName);

        this.onNonAbstractPostInjectInvoked = false;
    }

    /**
     * Invoked after injection of the {@link NonAbstractPerson}.
     */
    @PostInject
    void onNonAbstractPostInject() {
        this.onNonAbstractPostInjectInvoked = true;
    }

    /**
     * Determines if {@link #onNonAbstractPostInject()} was invoked.
     *
     * @return {@code true} when invoked, {@code false} otherwise
     */
    public boolean isNonAbstractPostInjectInvoked() {
        return this.onNonAbstractPostInjectInvoked;
    }
}
