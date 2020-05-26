/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.api.resource;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.traccar.Context;
import org.traccar.model.Label;

/**
 *
 * @author osantau
 */
@Path("labels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LabelResource {
   
     @GET     
     public Collection<Label> getLabels() throws SQLException {
        return Context.getDataManager().getLabels();
    }
     
     
     @GET
     @Path("assoc")
       public Map<String, Collection<?>>getLabelsWithId() throws SQLException {
             Map<String, Collection<?>> data = new HashMap<>();
             data.put("data",Context.getDataManager().getLabels());
        return data ;
    }
    
    @GET 
    @Path("bylot")
    public Collection<Label> getLabelsByLotAndLabelName( @QueryParam("lotId") long lotId, @QueryParam("labelName") String labelName) throws SQLException {
        return Context.getDataManager().getLabels(lotId, labelName);
    }
         
}
