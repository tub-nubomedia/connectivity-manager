package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 02.12.15.
 */
public class RequestFlows {

    private List<FlowHypervisor> flows;

    public RequestFlows(List<FlowHypervisor> flows) {
        this.flows = flows;
    }

    public RequestFlows() {
    }

    public List<FlowHypervisor> getFlows() {
        return flows;
    }

    public void setFlows(List<FlowHypervisor> flows) {
        this.flows = flows;
    }
}
