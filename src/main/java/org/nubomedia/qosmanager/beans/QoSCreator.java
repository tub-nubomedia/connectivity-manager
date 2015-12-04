package org.nubomedia.qosmanager.beans;

import org.nubomedia.qosmanager.values.Quality;
import org.openbaton.catalogue.mano.common.ConnectionPoint;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
        Map<String,Quality> qosVlr = this.getVlrs(vnfrs);




    }



    public void removeQos(Set<VirtualNetworkFunctionRecord> vnfrs){

    }

    private Map<String, ConnectionPoint> getServers(VirtualNetworkFunctionRecord vnfrs){

        return null;

    }

    private Map<String,Quality> getVlrs(Set<VirtualNetworkFunctionRecord> vnfrs) {
        Map<String,Quality> res = new LinkedHashMap<>();
        for (VirtualNetworkFunctionRecord vnfr : vnfrs){
            for (VirtualLinkRecord vlr : vnfr.getConnected_external_virtual_link()){
                for(String qosParam: vlr.getQos()) {
                    if (qosParam.contains("minimum_bandwith")){
                        Quality quality = this.mapValueQuality(qosParam);
                        res.put(vlr.getId(),quality);

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
