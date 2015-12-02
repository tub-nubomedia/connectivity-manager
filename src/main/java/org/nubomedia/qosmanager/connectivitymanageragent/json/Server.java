package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 04.11.15.
 */
public class Server {

    private String id;
    private String name;
    private List<InterfaceQoS> interfaces;

    public Server() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<InterfaceQoS> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<InterfaceQoS> interfaces) {
        this.interfaces = interfaces;
    }
}
