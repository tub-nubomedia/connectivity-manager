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

package org.nubomedia.qosmanager.values;

/**
 * Created by maa on 11.11.15.
 */
public enum Quality {

    GOLD ("1000000000","500000000"),
    SILVER ("500000000","250000000"),
    BRONZE ("250000000","125000000");

    private String max_rate;
    private String min_rate;

    Quality(String max_rate, String min_rate) {
        this.max_rate = max_rate;
        this.min_rate = min_rate;
    }

    public String getMax_rate() {
        return max_rate;
    }

    public void setMax_rate(String max_rate) {
        this.max_rate = max_rate;
    }

    public String getMin_rate() {
        return min_rate;
    }

    public void setMin_rate(String min_rate) {
        this.min_rate = min_rate;
    }
}
