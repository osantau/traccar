/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.TrackerServer;

/**
 *
 * @author osantau
 */
public class OctWenglorProtocol extends BaseProtocol {

    public OctWenglorProtocol() {
                         
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                  pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());       
                pipeline.addLast(new OctWenglorProtocolDecoder(OctWenglorProtocol.this));
            }
        });
    }

}
