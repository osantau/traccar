/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import de.re.easymodbus.modbusclient.ModbusClient;
import io.netty.channel.Channel;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.traccar.BaseProtocolDecoder;
import org.traccar.Context;
import org.traccar.DeviceSession;
import org.traccar.Protocol;
import org.traccar.database.DeviceManager;
import org.traccar.model.Label;
import org.traccar.model.Lot;
import org.traccar.model.Position;
import org.traccar.model.Device;

/**
 *
 * @author osantau
 */
public class OctSoftProtocolDecoder extends BaseProtocolDecoder {
    private static int reg2Flag = 0;
    public OctSoftProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = ((String) msg).replaceAll("[\\n\\r]+", "");
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, Context.getConfig().getString("deviceId"));
        if (deviceSession == null) {
            return null;
        }
        
        Lot lot = Context.getDataManager().getCurrentLot();

        Label label = new Label();
        label.setLotid(lot.getId());
        label.setLabel(sentence);
        label.setProtocol(getProtocolName());
        Label insertedLabel = Context.getDataManager().addLabel(label);
        //get with updated info
        lot = Context.getDataManager().getCurrentLot();
        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.setDeviceTime(new Date());
        position.setFixTime(position.getDeviceTime());
        position.setValid(true);
        position.setLatitude(0);
        position.setLongitude(0);
        position.setAltitude(0);
        position.setSpeed(0);
        position.setCourse(0);
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("lot", lot);
        attributes.put("label", insertedLabel);
        position.setAttributes(attributes);
        
        DeviceManager deviceManager = Context.getDeviceManager();
        Device device = deviceManager.getById(deviceSession.getDeviceId());
        Map<String,Object> attrs = device.getAttributes();  

        if (attrs.get("plc_active").toString().equals("Y")) {
            if (insertedLabel.getError().equals("Y") || insertedLabel.getRepeated().equals("Y")) {
                sendDataToDevicePLC(attrs.get("plc_ip").toString(), (Integer) attrs.get("plc_port"), (Integer) attrs.get("plc_register"), 1);
            } else {
                sendDataToDevicePLC(attrs.get("plc_ip").toString(), (Integer) attrs.get("plc_port"), (Integer) attrs.get("plc_register"), 0);
            }
        }
        return position;
    }
    private void sendDataToDevicePLC(String ip, int port, int register, int regValue) {     
        ModbusClient modbusClient = new ModbusClient(ip, port);
        try{            
            
            modbusClient.Connect();					
            modbusClient.WriteSingleRegister(register, regValue); 
            //scrie in registrul 2 0 sau 1 in functie de ce se transmite
            modbusClient.WriteSingleRegister(1, reg2Flag); 
            reg2Flag = reg2Flag==0?1:0;                       
            modbusClient.Disconnect();
            
        } catch(Exception ex) {
			 Logger.getLogger(OctSoftProtocolDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            if(modbusClient != null) {
                try {
                    modbusClient.Disconnect();
                } catch (IOException ex) {
                    Logger.getLogger(OctSoftProtocolDecoder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }       
}
