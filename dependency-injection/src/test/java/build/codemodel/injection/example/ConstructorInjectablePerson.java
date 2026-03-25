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
 * A simple person class using constructor-based {@link Inject}ion.
 *
 * @author brian.oliver
 */
public class ConstructorInjectablePerson {

    /**
     * The first name of the person.
     */
    private final String firstName;

    /**
     * The last name of the person.
     */
    private final String lastName;

    /**
     * The age of the person.
     */
    private final int age;

    /**
     * Was post inject invoked?
     */
    private boolean postInjectInvoked;

    /**
     * Constructs a person.
     *
     * @param firstName the first name of the person
     * @param lastName  the last name of the person
     * @param age       the age of the person
     */
    @Inject
    public ConstructorInjectablePerson(@Named("FirstName") final String firstName,
                                       @Named("LastName") final String lastName,
                                       @Named("Age") final int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.postInjectInvoked = false;
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
     * Handle post-injection initialization.
     */
    @PostInject
    private void onPostInject() {
        this.postInjectInvoked = true;
    }

    /**
     * Determine if post-inject was invoked on this class.
     *
     * @return {@code true} if post-inject was invoked, {@code false} otherwise
     */
    public boolean isPostInjectInvoked() {
        return this.postInjectInvoked;
    }
}
