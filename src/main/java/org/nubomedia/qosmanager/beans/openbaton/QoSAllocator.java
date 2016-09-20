/*
 * Copyright (c) 2015 Technische Universität Berlin
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.nubomedia.qosmanager.beans.openbaton;

import org.nubomedia.qosmanager.beans.connectivitymanager.ConnectivityManagerHandler;
import org.nubomedia.qosmanager.configurations.OpenstackConfiguration;
import org.nubomedia.qosmanager.configurations.ConnectivityManagerConfiguration;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by maa on 02.12.15.
 */
@Service
public class QoSAllocator {

    @Autowired private ConnectivityManagerHandler handler;
    private final ScheduledExecutorService qtScheduler = Executors.newScheduledThreadPool(1);
    private Logger logger;

    @Autowired private OpenstackConfiguration op_configuration;
    @Autowired private ConnectivityManagerConfiguration cm_configuration;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void addQos(Set<VirtualNetworkFunctionRecord> vnfrs,String nsrId){
        logger.debug("Creating ADD Thread");

        // Check which driver to use
        if(cm_configuration.getDriver().equals("neutron")){
            Neutron_AddQoSExecutor aqe = new Neutron_AddQoSExecutor(vnfrs,this.op_configuration);
            qtScheduler.schedule(aqe,100, TimeUnit.MILLISECONDS);
        }
        // Else , always assume we are using the cm_agent
        else{
            AddQoSExecutor aqe = new AddQoSExecutor(handler,vnfrs,nsrId);
            qtScheduler.schedule(aqe,100, TimeUnit.MILLISECONDS);
        }
        logger.debug("ADD Thread created and scheduled");
    }

    public void removeQos(Set<VirtualNetworkFunctionRecord> vnfrs,String nsrId){
        logger.debug("Creating REMOVE Thread");

        // Check which driver to use
        if(cm_configuration.getDriver().equals("neutron")){
            //Neutron_RemoveQoSExecutor rqe = new Neutron_RemoveQoSExecutor(vnfrs,this.op_configuration);
            //qtScheduler.schedule(rqe,10,TimeUnit.SECONDS);
            logger.debug("Neutron does delete the ports and the applied QoS on machien deletion, will not create REMOVE Thread");
        }
        // Else , always assume we are using the cm_agent
        else{
            RemoveQoSExecutor rqe = new RemoveQoSExecutor(handler,vnfrs,nsrId);
            qtScheduler.schedule(rqe,10,TimeUnit.SECONDS);
            logger.debug("REMOVE Thread created and scheduled");
        }

    }

}
