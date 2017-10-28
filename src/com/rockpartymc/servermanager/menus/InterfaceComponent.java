/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rockpartymc.servermanager.menus;

/**
 *
 * @author OmarAlama
 */
public abstract class InterfaceComponent {
    public final String name;
    public InterfaceComponent(String name)
    {
        this.name = name;
    }
    public abstract void display();
}
