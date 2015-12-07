package org.nubomedia.qosmanager.beans;

import org.nubomedia.qosmanager.openbaton.QoSMapper;
import org.nubomedia.qosmanager.values.Quality;
import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by maa on 02.12.15.
 */
@Service
public class QoSCreator {

    @Autowired private ConnectivityManagerHandler handler;
    private Logger logger;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void addQos(Set<VirtualNetworkFunctionRecord> vnfrs){
        logger.debug("Received vnfrs with qos");

        Map<String, List<QoSMapper>> qoses = this.getServers(vnfrs);

        logger.debug("adding QoS for " + qoses.toString()); //print map@id


    }



    public void removeQos(Set<VirtualNetworkFunctionRecord> vnfrs){

    }

    private Map<String, List<QoSMapper>> getServers(Set<VirtualNetworkFunctionRecord> vnfrs){

        Map<String, Quality> qualities = this.getVlrs(vnfrs);
        Map<String, List<QoSMapper>> res = new LinkedHashMap<>();

        for (VirtualNetworkFunctionRecord vnfr : vnfrs){
            for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                for(VNFCInstance vnfc : vdu.getVnfc_instance()){
                    for(VNFDConnectionPoint cp : vnfc.getConnection_point()){
                        if(qualities.keySet().contains(cp.getVirtual_link_reference())) {

                            List<QoSMapper> qoses = res.containsKey(vnfc.getHostname()) ? res.get(vnfc.getHostname()) : new ArrayList<QoSMapper>();
                            QoSMapper tobeAdded = new QoSMapper();
                            tobeAdded.setQuality(qualities.get(cp.getVirtual_link_reference()));

                            for(Ip ip: vnfc.getIps()){
                                if(ip.getNetName().equals(cp.getId())){
                                    tobeAdded.setIp(ip.getIp());
                                }
                            }

                            tobeAdded.setNetName(cp.getVirtual_link_reference());

                            qoses.add(tobeAdded);
                            res.put(vnfc.getHostname(),qoses);
                        }
                    }
                }
            }
        }

        return res;

    }

    private Map<String,Quality> getVlrs(Set<VirtualNetworkFunctionRecord> vnfrs) {
        Map<String,Quality> res = new LinkedHashMap<>();
        for (VirtualNetworkFunctionRecord vnfr : vnfrs){
            for (VirtualLinkRecord vlr : vnfr.getConnected_external_virtual_link()){
                for(String qosParam: vlr.getQos()) {
                    if (qosParam.contains("minimum_bandwith")){
                        Quality quality = this.mapValueQuality(qosParam);
                        res.put(vlr.getName(),quality);

                    }
                }
            }
        }
        return res;
    }

    private Quality mapValueQuality(String value){

        String[] qos = value.split(":");
        return Quality.valueOf(qos[1]);
    }



}
