package org.nubomedia.qosmanager.beans.connectivitymanager;

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
    private Logger logger;
    private Map<String, List<Server>> internalData;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.internalData = new LinkedHashMap<>();
    }


    public boolean addQoS(List<QoSAllocation> queues, FlowAllocation flows, String nsrId){
        logger.debug("Start creating QOS for " + nsrId + " with queues " + queues.toString() + " and flows " + flows.toString());
        List<Server> servers = queueHandler.createQueues(queues);
        internalData.put(nsrId,servers);


        return true;
    }



}
