package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 09/11/2015.
 */
public class QosAdd {

    private List<ServerQoS> values;

    public QosAdd(List<ServerQoS> values) {
        this.values = values;
    }

    public QosAdd() {
    }

    public List<ServerQoS> getValues() {
        return values;
    }

    public void setValues(List<ServerQoS> values) {
        this.values = values;
    }

    public ServerQoS getQosByServerID(String serverId){

        for(ServerQoS qos : values){
            if(qos.getServerId().equals(serverId))
                return qos;
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QosAdd)) return false;

        QosAdd request = (QosAdd) o;

        return getValues().equals(request.getValues());

    }

    @Override
    public int hashCode() {
        return getValues().hashCode();
    }
}
