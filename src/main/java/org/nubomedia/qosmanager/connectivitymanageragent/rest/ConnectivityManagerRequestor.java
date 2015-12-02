package org.nubomedia.qosmanager.connectivitymanageragent.rest;

import com.google.gson.Gson;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Host;
import org.nubomedia.qosmanager.connectivitymanageragent.json.QosRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * Created by Carlo on 10/11/2015.
 */
@Service
public class ConnectivityManagerRequestor {

    @Autowired private Gson mapper;
    @Autowired
    @Qualifier("readProperties")
    private Properties cmProp;
    private Logger logger;
    private RestTemplate template;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.template = new RestTemplate();
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

    public HttpStatus deleteAllQoS(){

        String url = cmProp.getProperty("cmUrl") + "/delallqos";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> delEntity = new HttpEntity<>(headers);
        ResponseEntity<String> delete = template.exchange(url,HttpMethod.DELETE,delEntity,String.class);

        logger.debug("delete method has resulted in " + delete.getStatusCode());

        return delete.getStatusCode();

    }

}
