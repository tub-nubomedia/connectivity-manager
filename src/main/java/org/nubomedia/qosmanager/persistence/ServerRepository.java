package org.nubomedia.qosmanager.persistence;

import org.nubomedia.qosmanager.connectivitymanageragent.json.Server;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by maa on 04.12.15.
 */
public interface ServerRepository extends CrudRepository<Server, String> {
}
