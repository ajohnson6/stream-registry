/**
 * Copyright (C) 2018-2021 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.StreamBindingService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.StreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;

@Component
@RequiredArgsConstructor
public class StreamBindingMutationImpl implements StreamBindingMutation {
  private final StreamBindingService streamBindingService;

  @Override
  public StreamBinding insert(StreamBindingKeyInput key, SpecificationInput specification) {
    return streamBindingService.create(asStreamBinding(key, specification)).get();
  }

  @Override
  public StreamBinding update(StreamBindingKeyInput key, SpecificationInput specification) {
    return streamBindingService.update(asStreamBinding(key, specification)).get();
  }

  @Override
  public StreamBinding upsert(StreamBindingKeyInput key, SpecificationInput specification) {
    StreamBinding streamBinding = asStreamBinding(key, specification);
    if (!streamBindingService.unsecuredGet(streamBinding.getKey()).isPresent()) {
      return streamBindingService.create(streamBinding).get();
    } else {
      return streamBindingService.update(streamBinding).get();
    }
  }

  @Override
  public Boolean delete(StreamBindingKeyInput key) {
    StreamBinding streamBinding = new StreamBinding();
    streamBinding.setKey(key.asStreamBindingKey());
    try {
      streamBindingService.delete(streamBinding);
      return true;
    } catch (Exception e) {
      throw new ValidationException(e);
    }
  }

  @Override
  public StreamBinding updateStatus(StreamBindingKeyInput key, StatusInput status) {
    StreamBinding streamBinding = streamBindingService.unsecuredGet(key.asStreamBindingKey()).get();
    return streamBindingService.updateStatus(streamBinding, status.asStatus()).get();
  }

  private StreamBinding asStreamBinding(StreamBindingKeyInput key, SpecificationInput specification) {
    StreamBinding streamBinding = new StreamBinding();
    streamBinding.setKey(key.asStreamBindingKey());
    streamBinding.setSpecification(specification.asSpecification());
    maintainState(streamBinding, streamBindingService.unsecuredGet(streamBinding.getKey()));
    return streamBinding;
  }
}
