package org.nubomedia.qosmanager.connectivitymanageragent.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by maa on 16.11.15.
 */
public class HypervisorQoS {

    @SerializedName("hypervisor_id") private String hypervisorId;
    private List<ServerQoS> servers;

    public HypervisorQoS() {
    }

    public HypervisorQoS(String hypervisorId, List<ServerQoS> servers) {
        this.hypervisorId = hypervisorId;
        this.servers = servers;
    }

    public String getHypervisorId() {
        return hypervisorId;
    }

    public void setHypervisorId(String hypervisorId) {
        this.hypervisorId = hypervisorId;
    }

    public List<ServerQoS> getServers() {
        return servers;
    }

    public void setServers(List<ServerQoS> servers) {
        this.servers = servers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HypervisorQoS)) return false;

        HypervisorQoS that = (HypervisorQoS) o;

        if (!getHypervisorId().equals(that.getHypervisorId())) return false;
        return getServers().equals(that.getServers());

    }

    @Override
    public int hashCode() {
        int result = getHypervisorId().hashCode();
        result = 31 * result + getServers().hashCode();
        return result;
    }
}
