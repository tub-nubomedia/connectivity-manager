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

package org.nubomedia.qosmanager.beans.connectivitymanager;


import org.nubomedia.qosmanager.connectivitymanageragent.beans.ConnectivityManagerRequestor;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Host;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Server;
import org.nubomedia.qosmanager.openbaton.FlowAllocation;
import org.nubomedia.qosmanager.openbaton.QoSAllocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maa on 03.12.15.
 */
@Service
public class ConnectivityManagerHandler {

    @Autowired private QoSHandler queueHandler;
    @Autowired private FlowHandler flowsHandler;
    @Autowired private ConnectivityManagerRequestor requestor;
    private Logger logger;
    private Map<String, List<Server>> internalData;
    private Host hostMap;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.internalData = new LinkedHashMap<>();
        this.hostMap = new Host();
    }


    public boolean addQoS(List<QoSAllocation> queues, FlowAllocation flows, String nsrId){
        logger.debug("Start creating QOS for " + nsrId + " with queues " + queues.toString() + " and flows " + flows.toString());
        this.updateHost();
        List<Server> servers = queueHandler.createQueues(hostMap, queues);
        internalData.put(nsrId,servers);
        logger.debug("MAP VALUE IS " + nsrId + " -> " + servers.toString());

        flowsHandler.createFlows(hostMap,servers,flows);

        return true;
    }

    private void updateHost() {
        this.hostMap = requestor.getHost();
    }

    public boolean removeQoS(List<String> servers,String nsrID){

        List<Server> serversList;

        try {
            serversList = internalData.get(nsrID);
            logger.info("SERVER LIST FOR DELETING IS " + serversList.toString());
        }
        catch (NullPointerException e){
            logger.debug("Servers for " + nsrID + " not found");
            return false;
        }

        queueHandler.removeQos(hostMap,serversList,servers);
        flowsHandler.removeFlows(hostMap,servers,internalData.get(nsrID));
        internalData.remove(nsrID);
        return true;
    }


}
