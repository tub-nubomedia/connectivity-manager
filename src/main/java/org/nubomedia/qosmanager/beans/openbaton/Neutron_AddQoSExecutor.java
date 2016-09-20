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
import org.json.JSONArray;
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
import org.json.JSONObject;
import org.jclouds.openstack.neutron.v2.domain.Port;
import org.jclouds.openstack.neutron.v2.features.PortApi;
import org.jclouds.openstack.neutron.v2.domain.Ports;
import org.nubomedia.qosmanager.configurations.OpenstackConfiguration;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by lgr on 20/09/16.
 */
public class Neutron_AddQoSExecutor implements Runnable {

    private Logger logger;
    private Set<VirtualNetworkFunctionRecord> vnfrs;

    private NeutronApi neutronApi;
    private NovaApi novaApi;
    private String nova_provider;
    private String neutron_provider;
    private Set<String> regions;

    private String qos_endpoint;
    private String endpoint;
    private String tenant;
    private String username;
    private String identity;
    private String password;
    private Map<String, String> qos_map = new HashMap<String, String>();
    private OpenstackConfiguration configuration;

    public Neutron_AddQoSExecutor(Set<VirtualNetworkFunctionRecord> vnfrs,OpenstackConfiguration configuration) {
        this.vnfrs = vnfrs;
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.configuration = configuration;
    }


    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }


    @Override
    public void run() {
        logger.debug("Received VNFR with QoS to be configured");

        this.nova_provider = "openstack-nova";
        this.neutron_provider = "openstack-neutron";
        this.tenant=configuration.getTenant();
        this.username=configuration.getUser();
        this.identity=tenant + ":" + username;
        this.password=configuration.getPassword();
        this.qos_endpoint=configuration.getUrl()+":"+configuration.getNeutron_port()+"/"+configuration.getApi_version();
        this.endpoint=configuration.getUrl()+":"+configuration.getAuth_port()+"/"+configuration.getApi_version();

        logger.debug("tenant = "+this.tenant);
        logger.debug("user = "+this.username);
        logger.debug("qos_endpoint = "+this.qos_endpoint);
        logger.debug("endpoint = "+this.endpoint);

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
        // We will steal the x-auth token here, to be able to directly communicate with the neutron qos rest api
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());
        novaApi = ContextBuilder.newBuilder(nova_provider)
                .endpoint(endpoint)
                .credentials(identity, password)
                .modules(modules)
                .buildApi(NovaApi.class);
        ContextBuilder
                contextBuilder =
                ContextBuilder.newBuilder("openstack-nova")
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
        HttpURLConnection connection = null;
        URL url = null;
        // so for neutron we need the qos-policy list
        try {
            url = new URL(qos_endpoint + "/qos/policies");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "python-neutronclient");
            connection.setRequestProperty("X-Auth-Token", access.getToken().getId());
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            connection.disconnect();
            //Parse json to object
            //logger.debug("Response of final request is: " + response.toString());
            JSONObject ans = new JSONObject(response.toString());
            //logger.debug("Created JSON-Object : "+ans.toString());
            JSONArray qos_p = ans.getJSONArray("policies");
            //logger.debug("Created JSON-Array : "+qos_p.toString());
            logger.debug("There are "+qos_p.length() + " qos-policies available");
            for (int i = 0; i < qos_p.length(); i++){
                JSONObject o = qos_p.getJSONObject(i);
                //logger.debug(o.toString());
                qos_map.put(o.getString("name"),o.getString("id"));
            }
        } catch (IOException e) {
            logger.error("Problem contacting openstack-neutron");
            e.printStackTrace();
            // If we have found a problem, we should probably end this thread
            return;
        }
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
                        // TODO : if the quality exists, check if max_bandwidth is set correctly
                        if(qos_map.get(ref.getQuality().name())==null){
                            logger.debug("Did not found qos-policy with name "+ref.getQuality().name());
                            logger.debug("Will create qos-policy : "+ref.getQuality().name());
                            JSONObject pol_payload = new JSONObject("{\"policy\":{\"name\":\""+ref.getQuality().name()+"\",\"description\":\"generated by connectivity manager\",\"shared\":\"false\"}}");
                            logger.debug("JSON to be send to neutron : "+pol_payload.toString());
                            connection = null;
                            url = null;
                            try {
                                url = new URL(qos_endpoint + "/qos/policies");
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setDoOutput(true);
                                //connection.setRequestProperty("Accept", "application/json");
                                //connection.setRequestProperty("Content-Type", "application/json");
                                connection.setRequestProperty("User-Agent", "python-neutronclient");
                                connection.setRequestProperty("X-Auth-Token", access.getToken().getId());
                                OutputStreamWriter pol_output = new OutputStreamWriter(connection.getOutputStream());
                                pol_output.write(pol_payload.toString());
                                pol_output.flush();
                                pol_output.close();
                                InputStream pol_is = connection.getInputStream();
                                BufferedReader pol_rd = new BufferedReader(new InputStreamReader(pol_is));
                                StringBuilder pl_response = new StringBuilder();
                                String pl_line;
                                while ((pl_line = pol_rd.readLine()) != null) {
                                    pl_response.append(pl_line);
                                    pl_response.append('\r');
                                }
                                pol_rd.close();
                                connection.disconnect();
                                // At this point we created the policy, now we also need to create the bandwidth limiting rule
                                logger.debug("Created policy :"+ pl_response.toString());
                                JSONObject created_pol = new JSONObject(pl_response.toString());
                                String created_pol_id = created_pol.getJSONObject("policy").getString("id");
                                connection = null;
                                url = null;
                                // TODO : fix creating bandwidth rule
                                url = new URL(qos_endpoint + "/qos/policies/"+created_pol_id+"/bandwidth_limit_rules/");
                                JSONObject bw_payload = new JSONObject("{\"bandwidth_limit_rule\":{\"max_kbps\":\""+ref.getQuality().getMax_rate()+"\"}}");
                                logger.debug("JSON to be send to neutron : "+bw_payload.toString());
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setDoOutput(true);
                                //connection.setRequestProperty("Accept", "application/json");
                                //connection.setRequestProperty("Content-Type", "application/json");
                                connection.setRequestProperty("User-Agent", "python-neutronclient");
                                connection.setRequestProperty("X-Auth-Token", access.getToken().getId());
                                OutputStreamWriter bw_output = new OutputStreamWriter(connection.getOutputStream());
                                bw_output.write(bw_payload.toString());
                                bw_output.flush();
                                bw_output.close();
                                InputStream bw_is = connection.getInputStream();
                                BufferedReader bw_rd = new BufferedReader(new InputStreamReader(bw_is));
                                StringBuilder bw_response = new StringBuilder();
                                String bw_line;
                                while ((bw_line = bw_rd.readLine()) != null) {
                                    bw_response.append(bw_line);
                                    bw_response.append('\r');
                                }
                                bw_rd.close();
                                connection.disconnect();
                                logger.debug("Created bandwidth rule for policy"+created_pol_id +": "+ bw_response.toString());
                            } catch (IOException e) {
                                logger.error("Problem contacting openstack-neutron");
                                e.printStackTrace();
                                // If we have found a problem, we should probably end this thread
                                return;
                            }
                        }
                        logger.debug("associated qos_policy is "+ qos_map.get(ref.getQuality().name()));

                        // TODO : if port already got the qos_policy we want to add, abort ( to avoid sending more traffic )
                        JSONObject payload = new JSONObject("{\"port\":{\"qos_policy_id\":\""+qos_map.get(ref.getQuality().name())+"\"}}");
                        connection = null;
                        url = null;
                        // Since we now know what qos_policy_id to use, lets update the port !
                        try {
                            url = new URL(qos_endpoint + "/ports/"+p.getId()+".json");
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setDoOutput(true);
                            connection.setRequestProperty("Accept", "application/json");
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setRequestProperty("User-Agent", "python-neutronclient");
                            connection.setRequestProperty("X-Auth-Token", access.getToken().getId());
                            InputStream is = connection.getInputStream();
                            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = rd.readLine()) != null) {
                                response.append(line);
                                response.append('\r');
                            }
                            rd.close();
                            connection.disconnect();
                            logger.debug("Current port information for "+ref.getIp() +" :" + response.toString());
                        } catch (IOException e) {
                            logger.error("Problem contacting openstack-neutron");
                            e.printStackTrace();
                            // If we have found a problem, we should probably end this thread
                            return;
                        }
                        connection = null;
                        url = null;
                        // Since we now know what qos_policy_id to use, lets update the port !
                        try {
                            url = new URL(qos_endpoint + "/ports/"+p.getId()+".json");
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("PUT");
                            connection.setDoOutput(true);
                            connection.setRequestProperty("Accept", "application/json");
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setRequestProperty("User-Agent", "python-neutronclient");
                            connection.setRequestProperty("X-Auth-Token", access.getToken().getId());
                            OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
                            output.write(payload.toString());
                            output.flush();
                            output.close();
                            InputStream is = connection.getInputStream();
                            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = rd.readLine()) != null) {
                                response.append(line);
                                response.append('\r');
                            }
                            rd.close();
                            connection.disconnect();
                            //logger.debug("Response of final request is: " + response.toString());
                            logger.debug("Finished applying QoS for "+ref.getIp());
                        } catch (IOException e) {
                            logger.error("Problem contacting openstack-neutron");
                            e.printStackTrace();
                            // If we have found a problem, we should probably end this thread
                            return;
                        }
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
        }
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
                        res.put(vlr.getName(), quality);
                        logger.debug("GET VIRTUAL LINK RECORD: insert in map vlr name " + vlr.getName() + " with quality " + quality);
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
