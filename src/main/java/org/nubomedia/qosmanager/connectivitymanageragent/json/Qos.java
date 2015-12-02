package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 04.11.15.
 */
public class Qos {

    private List<QosQueue> queues;
    private String qos_uuid;

    public Qos() {
    }

    public Qos(List<QosQueue> queues, String qos_uuid) {
        this.queues = queues;
        this.qos_uuid = qos_uuid;
    }

    public List<QosQueue> getQueues() {
        return queues;
    }

    public void setQueues(List<QosQueue> queues) {
        this.queues = queues;
    }

    public String getQos_uuid() {
        return qos_uuid;
    }

    public void setQos_uuid(String qos_uuid) {
        this.qos_uuid = qos_uuid;
    }
}
