/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rockpartymc.servermanager.menus;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author OmarAlama
 */
public class ConsoleInterface extends Thread{
    public List<InterfaceComponent> trace;

    public ConsoleInterface() {
        this.trace = new ArrayList();
    }
    
    public void start()
    {
        
    }
}
