package org.nubomedia.qosmanager.connectivitymanageragent.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by maa on 16.11.15.
 */
public class ServerQoS {

    @SerializedName("server_id") private String serverId;
    private List<InterfaceQoS> interfaces;

    public ServerQoS() {
    }

    public ServerQoS(String serverId, List<InterfaceQoS> interfaces) {
        this.serverId = serverId;
        this.interfaces = interfaces;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public List<InterfaceQoS> getValues() {
        return interfaces;
    }

    public void setValues(List<InterfaceQoS> interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerQoS)) return false;

        ServerQoS serverQoS = (ServerQoS) o;

        if (!getServerId().equals(serverQoS.getServerId())) return false;
        return getValues().equals(serverQoS.getValues());

    }

    @Override
    public int hashCode() {
        int result = getServerId().hashCode();
        result = 31 * result + getValues().hashCode();
        return result;
    }
}
