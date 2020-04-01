/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import io.netty.channel.Channel;
import java.net.SocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.traccar.BaseProtocolDecoder;
import org.traccar.Context;
import org.traccar.DeviceSession;
import org.traccar.Protocol;
import org.traccar.model.Label;
import org.traccar.model.Lot;
import org.traccar.model.Position;

/**
 *
 * @author osantau
 */
public class OctSoftProtocolDecoder extends BaseProtocolDecoder {
    
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
        return position;
    }
}
