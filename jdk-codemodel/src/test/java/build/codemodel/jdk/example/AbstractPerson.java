package build.codemodel.jdk.example;

/*-
 * #%L
 * JDK Code Model
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

/**
 * An abstract person for testing {@link build.codemodel.jdk.JDKCodeModel}.
 *
 * @author brian.oliver
 * @since Jul-2018
 */
public abstract class AbstractPerson {

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
     * Is the person tall?
     */
    private boolean tall;

    /**
     * Constructs a person.
     *
     * @param firstName the first name of the person
     * @param lastName  the last name of the person
     */
    protected AbstractPerson(final String firstName, final String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = 0;
        this.tall = false;
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
     * Sets if a person is tall.
     *
     * @param tall if a person is tall
     */
    public void setTall(final boolean tall) {
        this.tall = tall;
    }

    /**
     * Obtains if a person is tall.
     *
     * @return is a person tall
     */
    public boolean isTall() {
        return this.tall;
    }
}
