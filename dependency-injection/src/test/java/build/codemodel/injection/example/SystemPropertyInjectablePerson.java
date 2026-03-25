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

import build.codemodel.injection.SystemProperty;
import jakarta.inject.Inject;

/**
 * An example of a class that uses {@link SystemProperty} annotations to inject system properties
 * into its fields. This class represents a person with a first name, last name, and age.
 *
 * @author brian.oliver
 * @since Jul-2018
 */
public class SystemPropertyInjectablePerson {

    /**
     * The first name of the person.
     */
    @Inject
    @SystemProperty("FirstName")
    private String firstName;

    /**
     * The last name of the person.
     */
    @Inject
    @SystemProperty("LastName")
    private String lastName;

    /**
     * The age of the person.
     */
    @Inject
    @SystemProperty("Age")
    @SystemProperty.Default("42")
    private int age;

    /**
     * Obtains the first name of the person.
     *
     * @return the first name
     */
    public String firstName() {
        return this.firstName;
    }

    /**
     * Obtains the last name of the person.
     *
     * @return the last name
     */
    public String lastName() {
        return this.lastName;
    }

    /**
     * Obtains the age of the person.
     *
     * @return the age
     */
    public int age() {
        return this.age;
    }
}
