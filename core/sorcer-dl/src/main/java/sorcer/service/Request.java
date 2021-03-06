/*
 * Copyright 2016 the original author or authors.
 * Copyright 2016 SorcerSoft.org.
 *
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
 */

package sorcer.service;

import sorcer.service.modeling.Data;

import java.rmi.RemoteException;

/**
 * An top-level common interface for all request service in SORCER.
 * Request services are frontend service created by users. Standalone
 * request services are called subroutine services and request federated
 * services are called mograms.
 *
 * @author Mike Sobolewski
 */
public interface Request extends Service, Identifiable {

    public Fi getMultiFi();

    public Morpher getMorpher();

    public void setName(String name);

}
