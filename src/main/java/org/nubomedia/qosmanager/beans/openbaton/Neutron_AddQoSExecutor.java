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

import com.google.common.base.Function;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.domain.Credentials;
import org.jclouds.openstack.keystone.v2_0.domain.Access;
import org.jclouds.openstack.v2_0.options.PaginationOptions;
import org.nubomedia.qosmanager.openbaton.QoSReference;
import org.nubomedia.qosmanager.values.Quality;
import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import com.google.inject.Module;
import com.google.common.collect.ImmutableSet;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.neutron.v2.domain.Port;
import org.jclouds.openstack.neutron.v2.features.PortApi;
import org.jclouds.openstack.neutron.v2.domain.Ports;
import org.nubomedia.qosmanager.configurations.OpenstackConfiguration;
import org.nubomedia.qosmanager.beans.neutron.QoSHandler;


import java.io.*;
import java.util.*;

/**
 * Created by lgr on 20/09/16.
 */
public class Neutron_AddQoSExecutor implements Runnable {

    private Logger logger;
    private Set<VirtualNetworkFunctionRecord> vnfrs;
    private OpenstackConfiguration configuration;
    private QoSHandler neutron_handler;


    public Neutron_AddQoSExecutor(Set<VirtualNetworkFunctionRecord> vnfrs,OpenstackConfiguration configuration,QoSHandler handler) {
        this.vnfrs = vnfrs;
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.configuration = configuration;
        this.neutron_handler = handler;
    }


    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }


    @Override
    public void run() {
        NeutronApi neutronApi;
        NovaApi novaApi;
        Set<String> regions;
        String response;
        Map<String, String> qos_map;
        logger.debug("Received VNFR with QoS to be configured");
        String nova_provider = "openstack-nova";
        String neutron_provider = "openstack-neutron";
        String tenant=configuration.getTenant();
        String username=configuration.getUser();
        String identity=tenant + ":" + username;
        String password=configuration.getPassword();
        String qos_endpoint=configuration.getUrl()+":"+configuration.getNeutron_port()+"/"+configuration.getApi_version();
        String endpoint=configuration.getUrl()+":"+configuration.getAuth_port()+"/"+configuration.getApi_version();
        logger.debug("tenant = "+tenant);
        logger.debug("user = "+username);
        logger.debug("qos_endpoint = "+qos_endpoint);
        logger.debug("endpoint = "+endpoint);

        // Getting the server names and their quality, all we need to talk to neutron
        List<QoSReference> qoses = this.getQosesRefs(vnfrs);
        logger.debug("adding qoses for " + qoses.toString());
        /*
            Which should look similiar to this
            [
                    QoSReference{
                        ip='192.168.134.133',
                            quality=SILVER
                    },
                    QoSReference{
                        ip='192.168.134.135',
                            quality=SILVER
                    }
                 ]
        */
        // Use nova to list our endpoints at a later stage
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());
        novaApi = ContextBuilder.newBuilder(nova_provider)
                .endpoint(endpoint)
                .credentials(identity, password)
                .modules(modules)
                .buildApi(NovaApi.class);
        // We will steal the x-auth token here, to be able to directly communicate with the neutron qos rest api
        ContextBuilder
                contextBuilder =
                ContextBuilder.newBuilder(nova_provider)
                        .credentials(identity, password)
                        .endpoint(endpoint);
        ComputeServiceContext context = contextBuilder.buildView(ComputeServiceContext.class);
        Function<Credentials, Access>
                auth =
                context.utils().injector().getInstance(Key.get(new TypeLiteral<Function<Credentials, Access>>() {
                }));
        Access
                access =
                auth.apply(new Credentials.Builder<Credentials>().identity(identity)
                        .credential(password)
                        .build());
        logger.debug("Received auth token");

        // Check which QoS policies are available already
        response = neutron_handler.neutron_http_connection(qos_endpoint + "/qos/policies", "GET", access, null);
        if (response == null){
            logger.error("Error trying to list existing QoS policies");
            return;
        }
        // Save those in a hash map for later usage
        qos_map = neutron_handler.parsePolicyMap(response);
        logger.debug(qos_map.toString());
        // With neutron we collect the necessary values to know which ports to modify
        neutronApi = ContextBuilder.newBuilder(neutron_provider)
                .endpoint(endpoint)
                .credentials(identity, password)
                .buildApi(NeutronApi.class);
        regions = novaApi.getConfiguredRegions();
        for (String region : regions) {
            PortApi portApi = neutronApi.getPortApi(region);
            Ports ports = portApi.list(PaginationOptions.Builder.limit(2).marker("abcdefg"));
            // For all neutron ports
            logger.debug("Iterarting over neutron ports");
            for (Port p : ports) {
                String ips = p.getFixedIps().toString();
                for (QoSReference ref : qoses) {
                    // Check for our ip addresses
                    if (ips.contains(ref.getIp())) {
                        logger.debug("port id : " + p.getId() + " will get qos " + ref.getQuality().name());
                        // if the quaility is missing, we should CREATE IT
                        // TODO : if the quality exists, check if max_bandwidth is set correctly + if its shared and accessable
                        if(qos_map.get(ref.getQuality().name())==null){
                            logger.debug("Did not found qos-policy with name "+ref.getQuality().name());
                            logger.debug("Will create qos-policy : "+ref.getQuality().name());
                            // Creating the not existing QoS policy
                            response = neutron_handler.neutron_http_connection(qos_endpoint + "/qos/policies", "POST", access, neutron_handler.createPolicyPayload(ref.getQuality().name()));
                            if (response == null){
                                logger.error("Error trying to create QoS policy");
                                return;
                            }
                            logger.debug("Created policy :"+ response);
                            String created_pol_id = neutron_handler.parsePolicyId(response);
                            // Since we now have the correct id of the policy , lets create the bandwidth rule
                            response = neutron_handler.neutron_http_connection(qos_endpoint + "/qos/policies/" + created_pol_id + "/bandwidth_limit_rules", "POST", access, neutron_handler.createBandwidthLimitRulePayload(ref.getQuality().getMax_rate()));
                            if (response == null){
                                logger.error("Error trying to create bandwidth rule for QoS policy");
                                return;
                            }
                            logger.debug("Created bandwidth rule for policy"+created_pol_id +": "+ response);
                        }
                        // Check which QoS policies are available already
                        response = neutron_handler.neutron_http_connection(qos_endpoint + "/qos/policies", "GET", access, null);
                        if (response == null){
                            logger.error("Error trying to list existing QoS policies");
                            return;
                        }
                        // Save those in a hash map for later usage
                        qos_map = neutron_handler.parsePolicyMap(response);
                        logger.debug(qos_map.toString());

                        // At this point we can be sure the policy exists
                        logger.debug("associated qos_policy is "+ qos_map.get(ref.getQuality().name()));
                        // Check if the port already got the correct qos-policy assigned
                        // TODO : if port already got the qos_policy we want to add, abort ( to avoid sending more traffic )
                        response = neutron_handler.neutron_http_connection(qos_endpoint + "/ports/" + p.getId() + ".json", "GET", access, null);
                        logger.debug("Port information before updating : "+ response);
                        // update port
                        response = neutron_handler.neutron_http_connection(qos_endpoint + "/ports/" + p.getId() + ".json", "PUT", access, neutron_handler.createPolicyUpdatePayload(qos_map.get(ref.getQuality().name())));
                    }
                }
                //logger.debug("Finished iterating over QoS references");
            }
            //logger.debug("Finished Iterating over neutron ports");
        }
        // Close the APIs
        try {
            novaApi.close();
            neutronApi.close();
        } catch (IOException e) {
            logger.error("Could not close novaAPI / neutronAPI");
            e.printStackTrace();
        }
    }

    // Modified method to collect the only information we need for neutron
    private List<QoSReference> getQosesRefs(Set<VirtualNetworkFunctionRecord> vnfrs) {
        Map<String, Quality> qualities = this.getVlrs(vnfrs);
        List<QoSReference> res = new ArrayList<>();
        for (VirtualNetworkFunctionRecord vnfr : vnfrs) {
            if (qualities.keySet().contains(vnfr.getName())){
                logger.debug("Found quality for "+vnfr.getName());
                for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
                    for (VNFCInstance vnfci : vdu.getVnfc_instance()) {
                        //if (this.hasQoS(qualities, vnfci.getConnection_point())) {
                        for (VNFDConnectionPoint cp : vnfci.getConnection_point()) {
                            //logger.debug("Creating new QoSReference");
                            QoSReference ref = new QoSReference();
                            // In the agent, the check goes over the network name, which makes problems
                            // if both services are using the same network, but different qualities...
                            // We modified the check here to go over the vnfr name
                            ref.setQuality(qualities.get(vnfr.getName()));
                            // TODO , what will happen here if we have multiple networks? ...
                            for (Ip ip : vnfci.getIps()) {
                                ref.setIp(ip.getIp());
                            }
                            logger.debug("GET QOSES REF: adding reference to list " + ref.toString());
                            res.add(ref);
                        }
                        //}
                    }
                }
            }else {
                logger.debug("There are no qualities defined for "+ vnfr.getName() +" in "+qualities.toString());
            }
        }
        /*
        for (VirtualNetworkFunctionRecord vnfr : vnfrs) {
            for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
                for (VNFCInstance vnfci : vdu.getVnfc_instance()) {
                    if (this.hasQoS(qualities, vnfci.getConnection_point())) {
                        //QoSAllocation tmp = new QoSAllocation();
                        //tmp.setServerName(vnfci.getHostname());
                        //List<QoSReference> ifaces = new ArrayList<>();
                        for (VNFDConnectionPoint cp : vnfci.getConnection_point()) {
                            if (qualities.keySet().contains(cp.getVirtual_link_reference())) {
                                QoSReference ref = new QoSReference();
                                ref.setQuality(qualities.get(cp.getVirtual_link_reference()));
                                for (Ip ip : vnfci.getIps()) {
                                    if (ip.getNetName().equals(cp.getVirtual_link_reference())) {
                                        ref.setIp(ip.getIp());
                                    }
                                }
                                logger.debug("GET QOSES REF: adding reference to list " + ref.toString());
                                res.add(ref);
                            }
                        }
                    }
                }
            }
        }*/
        return res;
    }

    private boolean hasQoS(Map<String, Quality> qualities, Set<VNFDConnectionPoint> ifaces) {
        for (VNFDConnectionPoint cp : ifaces) {
            if (qualities.keySet().contains(cp.getVirtual_link_reference()))
                return true;
        }
        return false;
    }

    private Map<String, Quality> getVlrs(Set<VirtualNetworkFunctionRecord> vnfrs) {
        Map<String, Quality> res = new LinkedHashMap<>();
        logger.debug("GETTING VLRS");
        for (VirtualNetworkFunctionRecord vnfr : vnfrs) {
            for (InternalVirtualLink vlr : vnfr.getVirtual_link()) {
                for (String qosParam : vlr.getQos()) {
                    if (qosParam.contains("minimum_bandwith")) {
                        Quality quality = this.mapValueQuality(qosParam);
                        //res.put(vlr.getName(), quality);
                        res.put(vnfr.getName(), quality);
                        //logger.debug("GET VIRTUAL LINK RECORD: insert in map vlr name " + vlr.getName() + " with quality " + quality);
                        logger.debug("GET VIRTUAL LINK RECORD: insert in map vlr name " + vnfr.getName() + " with quality " + quality);
                    }
                }
            }
        }
        return res;
    }

    private Quality mapValueQuality(String value) {
        logger.debug("MAPPING VALUE-QUALITY: received value " + value);
        String[] qos = value.split(":");
        logger.debug("MAPPING VALUE-QUALITY: quality is " + qos[1]);
        return Quality.valueOf(qos[1]);
    }



}
