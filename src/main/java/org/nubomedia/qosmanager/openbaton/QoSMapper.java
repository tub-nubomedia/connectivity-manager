package org.nubomedia.qosmanager.openbaton;

import org.nubomedia.qosmanager.values.Quality;

import java.util.Map;

/**
 * Created by maa on 07.12.15.
 */
public class QoSMapper {

    private String ip;
    private String netName;
    private Quality quality;

    public QoSMapper(String ip, String netName, Quality quality) {
        this.ip = ip;
        this.netName = netName;
        this.quality = quality;
    }

    public QoSMapper() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }
}
