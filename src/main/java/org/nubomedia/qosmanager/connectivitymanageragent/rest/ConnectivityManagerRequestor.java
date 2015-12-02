package org.nubomedia.qosmanager.connectivitymanageragent.rest;

import com.google.gson.Gson;
import org.nubomedia.qosmanager.connectivitymanageragent.AddQueue;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Host;
import org.nubomedia.qosmanager.connectivitymanageragent.json.QosRequest;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Server;
import org.nubomedia.qosmanager.utils.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Carlo on 10/11/2015.
 */
@Service
public class ConnectivityManagerRequestor {

    @Autowired private Gson mapper;
    private Properties cmProp;
    private Logger logger;
    private RestTemplate template;

    @PostConstruct
    private void init() throws IOException {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.template = new RestTemplate();
        this.cmProp = ConfigReader.readProperties();
    }

    public Host getHost(){
        String url = cmProp.getProperty("cmUrl") + "/hosts";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> getEntity = new HttpEntity<>(headers);
        ResponseEntity<String> hosts = template.exchange(url, HttpMethod.GET,getEntity,String.class);

        logger.debug("hosts " + hosts.getBody());

        if(!hosts.getStatusCode().is2xxSuccessful()){
            return null;
        }
        else{
            return mapper.fromJson(hosts.getBody(),Host.class);
        }
    }

    public QosRequest setQoS(QosRequest qosRequest){

        String url = cmProp.getProperty("cmUrl") + "/qoses";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> setEntity = new HttpEntity<>(mapper.toJson(qosRequest,QosRequest.class),headers);
        ResponseEntity<String> insert = template.exchange(url,HttpMethod.POST,setEntity,String.class);

        logger.debug("Setting of QoS has produced http status:" + insert.getStatusCode() + " with body: " + insert.getBody());

        if(!insert.getStatusCode().is2xxSuccessful()){
            return null;
        }
        else {
            return mapper.fromJson(insert.getBody(),QosRequest.class);
        }
    }

    public Server getServerData(String hypervisorName, String serverName){

        String url = cmProp.getProperty("cmUrl") + "/server/" + hypervisorName + "/" + serverName;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> getEntity = new HttpEntity<>(headers);
        ResponseEntity<String> server = template.exchange(url,HttpMethod.GET,getEntity,String.class);

        logger.debug("Setting of QoS has produced http status:" + server.getStatusCode() + " with body: " + server.getBody());

        if(!server.getStatusCode().is2xxSuccessful()){
            return null;
        }
        else{
            return mapper.fromJson(server.getBody(),Server.class);
        }
    }

    public HttpStatus delQos(String hypervisorName,String qosId){

        String url = cmProp.getProperty("cmUrl") + "/qoses/" + hypervisorName + "/" + qosId;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> delentity = new HttpEntity<>(headers);
        ResponseEntity<String> delete = template.exchange(url,HttpMethod.DELETE,delentity,String.class);

        logger.debug("deleting qos " + qosId + " has returned " + delete.getStatusCode());

        return delete.getStatusCode();
    }

    public HttpStatus delQueue(String hypervisorName, String qosId, String queueId, String queueNumber){

        String url = cmProp.getProperty("cmUrl") + "/queue/" + hypervisorName + "/" + queueId + "/" + queueNumber + "/" + qosId;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> delQueueEntity = new HttpEntity<>(headers);
        ResponseEntity<String> delete = template.exchange(url,HttpMethod.DELETE,delQueueEntity,String.class);

        logger.debug("deleting queue " + queueId + " with qosID " + qosId + " has returned " + delete.getStatusCode());

        return delete.getStatusCode();
    }

    public AddQueue addQueue (AddQueue add){

        String url = cmProp.getProperty("cmUrl") + "/queue";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> postEntity = new HttpEntity<>(mapper.toJson(add,AddQueue.class),headers);
        ResponseEntity<String> addResp = template.exchange(url,HttpMethod.POST,postEntity,String.class);

        logger.debug("posted " + add.toString() + " and returned " + addResp.getBody());

        if(!addResp.getStatusCode().is2xxSuccessful()){
            return null;
        }
        else{
            return mapper.fromJson(addResp.getBody(),AddQueue.class);
        }

    }

}
