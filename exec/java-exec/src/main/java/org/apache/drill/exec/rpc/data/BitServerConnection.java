/**
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.rpc.data;

import io.netty.buffer.AccountingByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import org.apache.drill.exec.memory.BufferAllocator;
import org.apache.drill.exec.memory.OutOfMemoryException;
import org.apache.drill.exec.proto.ExecProtos.FragmentHandle;
import org.apache.drill.exec.rpc.RemoteConnection;
import org.apache.drill.exec.work.fragment.FragmentManager;

public class BitServerConnection extends RemoteConnection{
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BitServerConnection.class);

  private AllocatorProxy proxy = new AllocatorProxy();
  private volatile FragmentManager manager;
  
  public BitServerConnection(Channel channel, BufferAllocator initialAllocator) {
    super(channel);
    proxy.setAllocator(initialAllocator);
  }
  
  void setManager(FragmentManager manager){
    this.manager = manager;
    if (manager != null) { // Do this check for TestBitRpc test
      this.proxy.setAllocator(manager.getFragmentContext().getAllocator());
      manager.addConnection(this);
    }
  }

  @Override
  public BufferAllocator getAllocator() {
    if(manager != null){
      return manager.getFragmentContext().getAllocator();
    }
    return proxy;
  }
  
  public FragmentManager getFragmentManager(){
   return manager;
  }

  final static String ERROR_MESSAGE = "Attempted to access AllocatorProxy";

  private static class AllocatorProxy implements BufferAllocator {
    private BufferAllocator allocator;

    public void setAllocator(BufferAllocator allocator) {
      this.allocator = allocator;
    }

    @Override
    public AccountingByteBuf buffer(int size) {
      if (allocator == null) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
      }
      return allocator.buffer(size);
    }

    @Override
    public AccountingByteBuf buffer(int minSize, int maxSize) {
      if (allocator == null) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
      }
      return allocator.buffer(minSize, maxSize);
    }

    @Override
    public ByteBufAllocator getUnderlyingAllocator() {
      if (allocator == null) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
      }
      return allocator.getUnderlyingAllocator();
    }

    @Override
    public BufferAllocator getChildAllocator(FragmentHandle handle, long initialReservation, long maximumReservation) throws OutOfMemoryException {
      if (allocator == null) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
      }
      return allocator.getChildAllocator(handle, initialReservation, maximumReservation);
    }

    @Override
    public PreAllocator getNewPreAllocator() {
      if (allocator == null) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
      }
      return allocator.getNewPreAllocator();
    }

    @Override
    public void close() {
      if (allocator != null) {
        allocator.close();
      }
    }

    @Override
    public long getAllocatedMemory() {
      if (allocator == null) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
      }
      return allocator.getAllocatedMemory();
    }
  }
  
}
