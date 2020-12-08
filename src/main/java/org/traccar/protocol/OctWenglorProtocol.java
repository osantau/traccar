/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.TrackerServer;

/**
 *
 * @author osantau
 */
public class OctWenglorProtocol extends BaseProtocol {

    public OctWenglorProtocol() {
                         
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new OctWenglorProtocolDecoder(OctWenglorProtocol.this));
            }
        });
    }

}
