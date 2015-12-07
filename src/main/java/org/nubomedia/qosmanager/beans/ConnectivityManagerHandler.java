package org.nubomedia.qosmanager.beans;

import com.google.gson.Gson;
import org.nubomedia.qosmanager.connectivitymanageragent.beans.ConnectivityManagerRequestor;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Host;
import org.nubomedia.qosmanager.connectivitymanageragent.json.HypervisorQoS;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Server;
import org.nubomedia.qosmanager.connectivitymanageragent.json.ServerQoS;
import org.nubomedia.qosmanager.openbaton.QoSMapper;
import org.nubomedia.qosmanager.persistence.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by maa on 03.12.15.
 */
@Service
public class ConnectivityManagerHandler {

    @Autowired private ConnectivityManagerRequestor requestor;
    @Autowired private ServerRepository repository;
    private String protocolPort;
    private Logger logger;
    private Host hostMap;
    private String defaultPriority;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.hostMap = new Host();
        this.protocolPort = "8888";
        this.defaultPriority = "1";
    }

    @Scheduled(initialDelay = 0, fixedDelay = 30000)
    public void updateHost(){
        this.hostMap = this.requestor.getHost();
    }

    public void addQoS(Map<String, List<QoSMapper>> qoses){

        List<Server> servers = new ArrayList<>();
        List<HypervisorQoS> hyperv= new ArrayList<>();
        List<ServerQoS> serverQoSes = new ArrayList<>();

        for(String serverName : qoses.keySet()){

            String hypervisor = hostMap.belongsTo(serverName);
            Server server = requestor.getServerData(hypervisor,serverName);


        }


        repository.save(servers);
    }

}
