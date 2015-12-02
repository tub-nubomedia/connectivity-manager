package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 09/11/2015.
 */
public class QosRequest {

    private List<HypervisorQoS> values;

    public QosRequest(List<HypervisorQoS> values) {
        this.values = values;
    }

    public QosRequest() {
    }

    public List<HypervisorQoS> getQueues() {
        return values;
    }

    public void setQueues(List<HypervisorQoS> queues) {
        this.values = queues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QosRequest)) return false;

        QosRequest request = (QosRequest) o;

        return getQueues().equals(request.getQueues());

    }

    @Override
    public int hashCode() {
        return getQueues().hashCode();
    }
}
