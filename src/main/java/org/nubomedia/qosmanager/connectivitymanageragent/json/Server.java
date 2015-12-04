package org.nubomedia.qosmanager.connectivitymanageragent.json;

import javax.persistence.*;
import java.util.List;

/**
 * Created by maa on 04.11.15.
 */
@Entity
public class Server {

    @Id
    private String id;
    private String name;
    @OneToMany(targetEntity = InterfaceQoS.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER,mappedBy = "server",orphanRemoval = true)
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
