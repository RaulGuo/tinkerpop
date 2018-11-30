/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.driver.ser.binary.types.sample;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.apache.tinkerpop.gremlin.driver.ser.SerializationException;
import org.apache.tinkerpop.gremlin.driver.ser.binary.DataType;
import org.apache.tinkerpop.gremlin.driver.ser.binary.GraphBinaryReader;
import org.apache.tinkerpop.gremlin.driver.ser.binary.GraphBinaryWriter;
import org.apache.tinkerpop.gremlin.driver.ser.binary.types.CustomTypeSerializer;

import java.util.Date;

/**
 * A sample custom type serializer.
 */
class SamplePersonSerializer implements CustomTypeSerializer<SamplePerson> {
    private final byte[] typeInfoBuffer = new byte[] { 0, 0, 0, 0 };

    @Override
    public String getTypeName() {
        return "sampleProvider.SamplePerson";
    }

    @Override
    public DataType getDataType() {
        return DataType.CUSTOM;
    }

    @Override
    public SamplePerson read(ByteBuf buffer, GraphBinaryReader context) throws SerializationException {
        // {custom type info}, {value_flag} and {value}
        // No custom_type_info
        assert buffer.readInt() == 0;

        final byte valueFlag = buffer.readByte();
        if ((valueFlag & 1) == 1) {
            return null;
        }

        // Read the buffer int, no necessary in this case
        buffer.readInt();

        return new SamplePerson(
                context.readValue(buffer, String.class, false), context.readValue(buffer, Date.class,false));
    }

    @Override
    public SamplePerson readValue(ByteBuf buffer, GraphBinaryReader context, boolean nullable) throws SerializationException {
        throw new SerializationException("SamplePersonSerializer can not read a value without type information");
    }

    @Override
    public ByteBuf write(SamplePerson value, ByteBufAllocator allocator, GraphBinaryWriter context) throws SerializationException {
        if (value == null) {
            return allocator.compositeBuffer(2).addComponents(true,
                    // No custom_type_info
                    Unpooled.wrappedBuffer(typeInfoBuffer),
                    // Value_flag null set
                    context.getValueFlagNull()
            );
        }

        ByteBuf valueBuffer = allocator.compositeBuffer(2).addComponents(true,
                context.writeValue(value.getName(), allocator, false),
                context.writeValue(value.getBirthDate(), allocator, false));

        return allocator.compositeBuffer(4).addComponents(true,
                // No custom_type_info
                Unpooled.wrappedBuffer(typeInfoBuffer),
                // value_flag empty
                context.getValueFlagNone(),
                allocator.buffer(4).writeInt(valueBuffer.readableBytes()),
                valueBuffer);
    }

    @Override
    public ByteBuf writeValue(SamplePerson value, ByteBufAllocator allocator, GraphBinaryWriter context, boolean nullable) throws SerializationException {
        throw new SerializationException("SamplePersonSerializer can not write a value without type information");
    }
}
