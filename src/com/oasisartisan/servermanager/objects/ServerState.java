package com.oasisartisan.servermanager.objects;

import java.io.Serializable;
/**
 *
 * @author OasisArtisan
 */
public enum ServerState implements Serializable {
    ONLINE, STOPPING, STARTING, TERMINATING, OFFLINE, NOTRESPONDING
}
