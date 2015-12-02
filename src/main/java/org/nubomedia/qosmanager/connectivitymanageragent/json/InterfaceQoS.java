package org.nubomedia.qosmanager.connectivitymanageragent.json;

/**
 * Created by maa on 24.11.15.
 */
public class InterfaceQoS {

    private String ip;
    private String ovs_port_number;
    private Qos qos;

    public InterfaceQoS(String ip, String ovs_port_number, Qos qos) {
        this.ip = ip;
        this.ovs_port_number = ovs_port_number;
        this.qos = qos;
    }

    public InterfaceQoS() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOvs_port_number() {
        return ovs_port_number;
    }

    public void setOvs_port_number(String ovs_port_number) {
        this.ovs_port_number = ovs_port_number;
    }

    public Qos getQos() {
        return qos;
    }

    public void setQos(Qos qos) {
        this.qos = qos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InterfaceQoS)) return false;

        InterfaceQoS that = (InterfaceQoS) o;

        if(this.getIp().equals(that.getIp())) return true;
        return false;

    }

    @Override
    public int hashCode() {
        int result = getIp().hashCode();
        result = 31 * result + getOvs_port_number().hashCode();
        result = 31 * result + getQos().hashCode();
        return result;
    }
}
