/*
 * Copyright (c) 2015 Technische Universität Berlin
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.nubomedia.qosmanager.openbaton;

import org.nubomedia.qosmanager.values.Quality;

/**
 * Created by maa on 09.12.15.
 */
public class QoSReference {

    private String ip;
    private Quality quality;

    public QoSReference(String ip, Quality quality) {
        this.ip = ip;
        this.quality = quality;
    }

    public QoSReference() {
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

    @Override
    public String toString() {
        return "QoSReference{" +
                "ip='" + ip + '\'' +
                ", quality=" + quality +
                '}';
    }
}
