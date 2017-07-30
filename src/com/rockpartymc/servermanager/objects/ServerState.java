package com.rockpartymc.servermanager.objects;

import java.io.Serializable;

public enum ServerState implements Serializable{
    ONLINE, STOPPING, STARTING, TERMINATING, OFFLINE, NOTRESPONDING
}
