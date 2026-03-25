package build.codemodel.jdk.descriptor;

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

import build.base.foundation.Capture;
import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.objectoriented.descriptor.ClassTypeDescriptor;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

/**
 * A {@link ClassTypeDescriptor} for JDK {@link Class}es.
 *
 * @author brian.oliver
 * @since Feb-2025
 */
public class JDKClassTypeDescriptor
    extends ClassTypeDescriptor
    implements JDKTypeDescriptor {

    /**
     * Constructs a {@link JDKClassTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     */
    private JDKClassTypeDescriptor(final CodeModel codeModel,
                                   final TypeName typeName) {

        super(codeModel, typeName);
    }

    /**
     * {@link Unmarshal} a {@link JDKClassTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public JDKClassTypeDescriptor(@Bound final CodeModel codeModel,
                                  final Marshaller marshaller,
                                  final TypeName typeName,
                                  final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, typeName, traits);
    }

    /**
     * {@link Marshal} a {@link JDKClassTypeDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param typeName   the {@link Capture}d {@link TypeName}
     * @param traits     the {@link Capture}d {@link Marshalled} {@link Trait}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<TypeName> typeName,
                           final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, typeName, traits);
    }

    /**
     * Creates a {@link JDKClassTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     * @return a new {@link ClassTypeDescriptor}
     */
    public static JDKClassTypeDescriptor of(final CodeModel codeModel,
                                            final TypeName typeName) {

        return new JDKClassTypeDescriptor(codeModel, typeName);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(JDKClassTypeDescriptor.class, MethodHandles.lookup());
    }

}
