/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.api.resource;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.traccar.Context;
import org.traccar.api.BaseObjectResource;
import org.traccar.model.Lot;

/**
 *
 * @author osantau
 */
@Path("lots")
public class LotResource extends BaseObjectResource<Lot> {
    
    public LotResource() {
        super(Lot.class);
}
    
    @GET
    @Path("all")   
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Lot>  getAll() throws SQLException{
      
        return Context.getDataManager().getLots();
    }
    
    @GET
    @Path("running")
    @Produces(MediaType.APPLICATION_JSON)
    public Lot getRunning() throws SQLException{
        return Context.getDataManager().getCurrentLot();
    }
    @POST
//    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addLot(Lot entity) throws SQLException {
        entity.setCreated(new Date());
        entity.setUpdated(new Date());   
        entity.setRunning("Y");         
        entity.setAttributes(getDefaultAttributes());
        Context.getDataManager().addObject(entity);
         return Response.ok(entity).build();
    } 
    
    @GET
    @Path("single/{lotId}")    
    @Produces(MediaType.APPLICATION_JSON)
    public Lot getSingle(@PathParam("lotId") Long lotId) throws SQLException {
        Lot lot = Context.getDataManager().getLotById(lotId);       
        return lot;
    }
    private Map<String, Object> getDefaultAttributes() {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>(); 
        attributes.put("total",Long.valueOf(0));
        attributes.put("errors",Long.valueOf(0));
        attributes.put("repeats",Long.valueOf(0));
        attributes.put("viables",Long.valueOf(0));
        return attributes;
    }   
}