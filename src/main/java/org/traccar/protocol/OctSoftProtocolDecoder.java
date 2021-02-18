/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import de.re.easymodbus.exceptions.ModbusException;
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
    private int reg2Flag = 0;
    public OctSoftProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = ((String) msg).replaceAll("[\\n\\r]+", "");
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, Context.getConfig().getString("wenglorCamId"));
        if (deviceSession == null) {
            return null;
        }
        
        Lot lot = Context.getDataManager().getCurrentLot();

        Label label = new Label();
        label.setLotid(lot.getId());
        label.setLabel(sentence.toUpperCase());
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
                // NOK
                sendDataToDevicePLC(attrs, 0);
            } else {
                //OK
                sendDataToDevicePLC(attrs, 1);
            }
        }
        return position;
    }
    private void sendDataToDevicePLC(Map<String,Object> attrs, int regValue) {     
        int plcReg1 = (Integer) attrs.get("plc_reg1");
        int plcReg2 = (Integer) attrs.get("plc_reg2");
        ModbusClient modbusClient = new ModbusClient(attrs.get("plc_ip").toString(), (Integer) attrs.get("plc_port"));
        try{            
            
            modbusClient.Connect();					
            reg2Flag = modbusClient.ReadHoldingRegisters(plcReg2, 1)[0];
            
            modbusClient.WriteSingleRegister(plcReg1, regValue); 
            //scrie in registrul 2 0 sau 1 in functie de valoarea precedenta            
            modbusClient.WriteSingleRegister(plcReg2, reg2Flag == 0 ? 1:0); 
           
            modbusClient.Disconnect();
            
        } catch (ModbusException | IOException ex) {
			 Logger.getLogger(OctSoftProtocolDecoder.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        finally {
            if(modbusClient != null) {
                try {
                    modbusClient.Disconnect();
                } catch (IOException ex) {
                    Logger.getLogger(OctSoftProtocolDecoder.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }

    }       
}
