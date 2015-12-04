package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 04.11.15.
 */
public class Datacenter {

    private String name;
    private List<String> servers;

    public Datacenter(String name, List<String> servers) {
        this.name = name;
        this.servers = servers;
    }

    public Datacenter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }
}
