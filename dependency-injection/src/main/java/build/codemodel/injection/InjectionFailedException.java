package build.codemodel.injection;

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

import java.util.Objects;

/**
 * An {@link InjectionException} thrown when injection into an {@link InjectionPoint} fails.
 *
 * @author brian.oliver
 * @since May-2020
 */
public class InjectionFailedException
    extends InjectionException {

    /**
     * The {@link InjectionPoint}.
     */
    private final InjectionPoint injectionPoint;

    /**
     * Constructs a {@link InjectionFailedException}.
     *
     * @param injectionPoint the {@link InjectionPoint}
     */
    protected InjectionFailedException(final InjectionPoint injectionPoint) {
        this(injectionPoint, null, null);
    }

    /**
     * Constructs an {@link InjectionFailedException} with a specific message.
     *
     * @param injectionPoint the {@link InjectionPoint}
     * @param message        the message
     */
    public InjectionFailedException(final InjectionPoint injectionPoint, final String message) {
        this(injectionPoint, message, null);
    }

    /**
     * Constructs an {@link InjectionFailedException} caused by a specific {@link Throwable}.
     *
     * @param injectionPoint the {@link InjectionPoint}
     * @param cause          the {@link Throwable}
     */
    public InjectionFailedException(final InjectionPoint injectionPoint, final Throwable cause) {
        this(injectionPoint, cause == null ? null : getRootCause(cause).toString(), cause);
    }

    /**
     * Constructs an {@link InjectionFailedException} caused by a specific {@link Throwable} with a provided message.
     *
     * @param injectionPoint the {@link InjectionPoint}
     * @param message        the message
     * @param cause          the {@link Throwable}
     */
    public InjectionFailedException(final InjectionPoint injectionPoint,
                                    final String message,
                                    final Throwable cause) {
        super(message, cause);
        this.injectionPoint = Objects.requireNonNull(injectionPoint, "The InjectionPoint must not be null");
    }

    /**
     * Obtains the {@link InjectionPoint} that failed.
     *
     * @return the {@link InjectionPoint}
     */
    public InjectionPoint injectionPoint() {
        return this.injectionPoint;
    }

    /**
     * Gets the final cause of the {@link Throwable} by traversing down the cause chain.
     *
     * @param throwable the {@link Throwable}
     * @return the last cause in the {@link Throwable}'s chain, or the {@link Throwable} itself if there is no cause
     */
    private static Throwable getRootCause(final Throwable throwable) {
        if (throwable.getCause() == null) {
            return throwable;
        }
        return getRootCause(throwable.getCause());
    }

    @Override
    public String toString() {
        return "Injection failed for "
            + this.injectionPoint
            + (getMessage() == null ? "" : ": " + getMessage());
    }
}
