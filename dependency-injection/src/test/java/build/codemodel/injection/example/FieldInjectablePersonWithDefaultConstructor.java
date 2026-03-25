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

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * A person class with a default constructor and injectable fields.
 *
 * @author brian.oliver
 * @since Aug-2019
 */
public class FieldInjectablePersonWithDefaultConstructor {

    /**
     * The first name of the person.
     */
    @Inject
    @Named("FirstName")
    public String firstName;

    /**
     * The last name of the person.
     */
    @Inject
    @Named("LastName")
    private String lastName;

    /**
     * The age of the person.
     */
    @Inject
    @Named("Age")
    int age;

    /**
     * Has the person graduated?
     */
    @Inject
    private boolean graduated;

    /**
     * Constructs a {@link FieldInjectablePersonWithDefaultConstructor}.
     */
    public FieldInjectablePersonWithDefaultConstructor() {
        this.firstName = null;
        this.lastName = null;
        this.age = 0;
        this.graduated = false;
    }

    /**
     * Obtains the first name of the person.
     *
     * @return the first name
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Obtains the last name of the person.
     *
     * @return the last name
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Obtains the age of the person.
     *
     * @return the age
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Obtains the graduation status of the person.
     *
     * @return the graduation status
     */
    public boolean isGraduated() {
        return this.graduated;
    }
}
