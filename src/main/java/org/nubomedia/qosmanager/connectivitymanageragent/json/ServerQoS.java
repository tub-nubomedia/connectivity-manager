package org.nubomedia.qosmanager.connectivitymanageragent.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by maa on 16.11.15.
 */
public class ServerQoS {

    @SerializedName("hypervisor_id") private String hypervisorId;
    @SerializedName("server_id") private String serverId;
    private List<InterfaceQoS> interfaces;

    public ServerQoS() {
    }

    public ServerQoS(String hypervisorId, String serverId, List<InterfaceQoS> interfaces) {
        this.hypervisorId = hypervisorId;
        this.serverId = serverId;
        this.interfaces = interfaces;
    }

    public String getHypervisorId() {
        return hypervisorId;
    }

    public void setHypervisorId(String hypervisorId) {
        this.hypervisorId = hypervisorId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public List<InterfaceQoS> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<InterfaceQoS> interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerQoS)) return false;

        ServerQoS serverQoS = (ServerQoS) o;

        if (!getServerId().equals(serverQoS.getServerId())) return false;
        return getInterfaces().equals(serverQoS.getInterfaces());

    }

    @Override
    public int hashCode() {
        int result = getServerId().hashCode();
        result = 31 * result + getInterfaces().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ServerQoS{" +
                "hypervisorId='" + hypervisorId + '\'' +
                ", serverId='" + serverId + '\'' +
                ", interfaces=" + interfaces +
                '}';
    }
}
