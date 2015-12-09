package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 02.12.15.
 */
//the method requires an array
public class AddQueue {

    private String hypervisor_id;
    private List<Qos> values;

    public AddQueue() {
    }

    public AddQueue(String hypervisor_id, List<Qos> values) {
        this.hypervisor_id = hypervisor_id;
        this.values = values;
    }

    public String getHypervisor_id() {
        return hypervisor_id;
    }

    public void setHypervisor_id(String hypervisor_id) {
        this.hypervisor_id = hypervisor_id;
    }

    public List<Qos> getValues() {
        return values;
    }

    public void setValues(List<Qos> values) {
        this.values = values;
    }
}
