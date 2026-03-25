package build.codemodel.jdk.statement;

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

import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.expression.Expression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.imperative.AbstractStatement;
import build.codemodel.imperative.Block;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@code synchronized} statement: {@code synchronized(lock) body}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class Synchronized
    extends AbstractStatement {

    /**
     * The lock expression evaluated to obtain the monitor.
     */
    private final Expression lock;

    /**
     * The synchronized block body.
     */
    private final Block body;

    private Synchronized(final Expression lock, final Block body) {
        super(Objects.requireNonNull(lock, "lock must not be null").codeModel());
        this.lock = lock;
        this.body = Objects.requireNonNull(body, "body must not be null");
    }

    @Unmarshal
    public Synchronized(@Bound final CodeModel codeModel,
                        final Marshaller marshaller,
                        final Stream<Marshalled<Trait>> traits,
                        final Marshalled<Expression> lock,
                        final Marshalled<Block> body) {
        super(codeModel, marshaller, traits);
        this.lock = marshaller.unmarshal(lock);
        this.body = marshaller.unmarshal(body);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> lock,
                           final Out<Marshalled<Block>> body) {
        super.destructor(marshaller, traits);
        lock.set(marshaller.marshal(this.lock));
        body.set(marshaller.marshal(this.body));
    }

    /**
     * Obtains the lock expression.
     *
     * @return the lock {@link Expression}
     */
    public Expression lock() {
        return this.lock;
    }

    /**
     * Obtains the synchronized block body.
     *
     * @return the body {@link Block}
     */
    public Block body() {
        return this.body;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Synchronized other
            && Objects.equals(this.lock, other.lock)
            && Objects.equals(this.body, other.body)
            && super.equals(other);
    }

    /**
     * Creates a {@link Synchronized} statement.
     *
     * @param lock the lock {@link Expression}
     * @param body the body {@link Block}
     * @return a new {@link Synchronized}
     */
    public static Synchronized of(final Expression lock, final Block body) {
        return new Synchronized(lock, body);
    }

    static {
        Marshalling.register(Synchronized.class, MethodHandles.lookup());
    }
}
