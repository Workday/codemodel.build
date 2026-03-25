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

public class SetterInjectablePerson {

    /**
     * The first name of the person.
     */
    private String firstName;

    /**
     * The last name of the person.
     */
    private String lastName;

    /**
     * The age of the person.
     */
    private int age;

    /**
     * Obtains the first name of the person.
     *
     * @return the first name
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Sets the first name of the person
     *
     * @param firstName the first name
     */
    @Inject
    public void setFirstName(@Named("FirstName") final String firstName) {
        this.firstName = firstName;
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
     * Sets the last name of the person.
     *
     * @param lastName the last name
     */
    @Inject
    private void setLastName(@Named("LastName") final String lastName) {
        this.lastName = lastName;
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
     * Sets the age of the person.
     *
     * @param age the age
     */
    @Inject
    void setAge(@Named("Age") final int age) {
        this.age = age;
    }
}
