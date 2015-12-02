package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 02.12.15.
 */
public class FlowHypervisor {

    private String hypervisor_id;
    private List<FlowServer> hypervisor_flows;

    public FlowHypervisor(String hypervisor_id, List<FlowServer> hypervisor_flows) {
        this.hypervisor_id = hypervisor_id;
        this.hypervisor_flows = hypervisor_flows;
    }

    public FlowHypervisor() {
    }

    public String getHypervisor_id() {
        return hypervisor_id;
    }

    public void setHypervisor_id(String hypervisor_id) {
        this.hypervisor_id = hypervisor_id;
    }

    public List<FlowServer> getHypervisor_flows() {
        return hypervisor_flows;
    }

    public void setHypervisor_flows(List<FlowServer> hypervisor_flows) {
        this.hypervisor_flows = hypervisor_flows;
    }
}
