/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oasisartisan.servermanager.storage;

import org.json.JSONObject;

/**
 *
 * @author OasisArtian
 */
public class JSONObjectWrapper extends JSONObject{
    public Long getLongNull(String key){
        return this.get(key) == NULL ? null : this.getLong(key);
    }
}
