package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 04.11.15.
 */
public class Datacenter {

    private String name;
    private int cpu_total;
    private int cpu_used;
    private int id;
    private int instances;
    private String ip;
    private int ram_total;
    private int ram_used;
    private List<Server> servers;

    public Datacenter(String name, int cpu_total, int cpu_used, int id, int instances, String ip, int ram_total, int ram_used, List<Server> servers) {
        this.name = name;
        this.cpu_total = cpu_total;
        this.cpu_used = cpu_used;
        this.id = id;
        this.instances = instances;
        this.ip = ip;
        this.ram_total = ram_total;
        this.ram_used = ram_used;
        this.servers = servers;
    }

    public Datacenter() {
    }

    public int getCpu_total() {
        return cpu_total;
    }

    public void setCpu_total(int cpu_total) {
        this.cpu_total = cpu_total;
    }

    public int getCpu_used() {
        return cpu_used;
    }

    public void setCpu_used(int cpu_used) {
        this.cpu_used = cpu_used;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInstances() {
        return instances;
    }

    public void setInstances(int instances) {
        this.instances = instances;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getRam_total() {
        return ram_total;
    }

    public void setRam_total(int ram_total) {
        this.ram_total = ram_total;
    }

    public int getRam_used() {
        return ram_used;
    }

    public void setRam_used(int ram_used) {
        this.ram_used = ram_used;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
