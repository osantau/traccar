/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramChannel;
import java.net.SocketAddress;
import org.traccar.BaseProtocolDecoder;
import org.traccar.Context;
import org.traccar.DeviceSession;
import org.traccar.Protocol;
import org.traccar.model.Device;
import org.traccar.model.WenglorCam;

/**
 *
 * @author osantau
 */
public class OctWenglorProtocolDecoder extends BaseProtocolDecoder {        
    public OctWenglorProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
       
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String sentence = new String(bytes);
        boolean isEmpty = sentence.replaceAll(",","").trim().isEmpty();
        
        if(channel instanceof DatagramChannel)
        {
            System.out.println(sentence);
            String[] params = isEmpty ? new String[]{"","","","","","","",""} : sentence.split(",");                
            System.out.println(Context.getConfig().getString("wenglorCamId"));
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, Context.getConfig().getString("wenglorCamId"));                                              
            WenglorCam wenglorCam = new WenglorCam();
            wenglorCam.setDeviceId(deviceSession.getDeviceId());
            wenglorCam.setProtocol(getProtocolName());
            wenglorCam.setMessage(sentence);
            wenglorCam.setDescription(isEmpty ? "Empty message !" : null);
            wenglorCam.setP1(params[0]);            
            wenglorCam.setP2(params[1]);
            wenglorCam.setP3(params[2]);
            wenglorCam.setP4(params[3]);
            wenglorCam.setP5(params[4]);
            wenglorCam.setP6(params[5]);
            wenglorCam.setP7(params[6]);
            wenglorCam.setP8(params[7]);             
            Context.getDataManager().addWenglorCamData(wenglorCam);
        }
        return null;
    }     
}
