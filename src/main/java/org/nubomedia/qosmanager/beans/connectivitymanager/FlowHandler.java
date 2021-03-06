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
import org.nubomedia.qosmanager.connectivitymanageragent.json.*;
import org.nubomedia.qosmanager.openbaton.FlowAllocation;
import org.nubomedia.qosmanager.openbaton.FlowReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maa on 09.12.15.
 */
@Service
public class FlowHandler {

    @Autowired private ConnectivityManagerRequestor requestor;
    private Logger logger;
    private String protocol;
    private String priority;

    @PostConstruct
    private void init(){

        this.logger = LoggerFactory.getLogger(this.getClass());
        this.protocol = "tcp";
        this.priority = "2";

    }

    public void createFlows(Host host, List<Server> servers, FlowAllocation allocations){
        logger.debug("Received Flow allocation " + allocations.toString());
        List<FlowServer> flows = new ArrayList<>();
        for (String vlr : allocations.getAllVlr()){
            for (FlowReference fr : allocations.getIpsForVlr(vlr)){
                for(Server server : servers){
                    if(server.getName().equals(fr.getHostname())){
                        FlowServer fs = new FlowServer();
                        fs.setHypervisor_id(host.belongsTo(server.getName()));
                        fs.setServer_id(server.getName());
                        InterfaceQoS iface = server.getFromIp(fr.getIp());
                        List<Flow> internalFlows = new ArrayList<>();
                        for(String ip : allocations.getAllIpsForVlr(vlr)){
                            if (!ip.equals(fr.getIp())) {
                                Flow tmp = new Flow();
                                tmp.setDest_ipv4(ip);
                                Server dest = this.getServerRefFromIp(servers,ip);
                                tmp.setOvs_port_number(dest.getFromIp(ip).getOvs_port_number());
                                tmp.setPriority(priority);
                                tmp.setProtocol(protocol);
                                tmp.setSrc_ipv4(iface.getIp());
                                tmp.setQueue_number("" + dest.getFromIp(ip).getQos().getActualID());
                                internalFlows.add(tmp);
                            }
                        }
                        fs.setQos_flows(internalFlows);
                        flows.add(fs);
                    }
                }
            }
        }
        RequestFlows request = new RequestFlows(flows);
        logger.debug("REQUEST is " + request.toString());
        RequestFlows returningFlows = requestor.setFlow(request);
        logger.debug("Returning flows " + returningFlows.toString());
    }

    public void removeFlows(Host hostmap, List<String> serversIds, List<Server> servers){

        for(Server server : servers){
            if (serversIds.contains(server.getName())){
                String hypervisor = hostmap.belongsTo(server.getName());
                for  (InterfaceQoS iface : server.getInterfaces()){
                    requestor.deleteFlow(hypervisor,protocol,iface.getIp());
                }
            }
        }
    }

    private Server getServerRefFromIp (List<Server> servers, String ip){


        for (Server server : servers){
            if(server.getFromIp(ip) != null){
                return server;
            }
        }

        return null;
    }

}
