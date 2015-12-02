package org.nubomedia.qosmanager.beans;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.nubomedia.qosmanager.openbaton.OpenbatonEvent;
import org.nubomedia.qosmanager.utils.ConfigReader;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by maa on 11.11.15.
 */
@Service
public class OpenbatonEventSubscription {

    private NFVORequestor requestor;
    private Logger logger;
    private Properties properties;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private Gson mapper;

    @PostConstruct
    private void init() throws SDKException, IOException {

        this.properties = ConfigReader.readProperties();
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.requestor = new NFVORequestor(properties.getProperty("nfvo.username"),properties.getProperty("nfvo.password"),properties.getProperty("nfvo.baseURL"),properties.getProperty("nfvo.basePort"),"1");

        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setType(EndpointType.RABBIT);
        eventEndpoint.setEvent(Action.INSTANTIATE_FINISH);
        requestor.getEventAgent().create(eventEndpoint);

    }

    public void receiveNewNsr(String message){
        logger.info("received new NSR " + message);
        OpenbatonEvent evt;

        try {
            logger.debug("Trying to deserialize it");
            evt = mapper.fromJson(message, OpenbatonEvent.class);
        }
        catch (JsonParseException e) {
            logger.info("Error in payload, expected NSR " + e.getMessage());
            return;
        }

        NetworkServiceRecord nsr = evt.getPayload();

        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){

            for (VirtualLinkRecord vlr : vnfr.getConnected_external_virtual_link()){
                if(!vlr.getQos().isEmpty()){

                }
            }

        }






    }
}
