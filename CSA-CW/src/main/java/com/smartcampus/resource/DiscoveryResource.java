/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author Hasandi Vihanga
 */
@Path("/discovery")
public class DiscoveryResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover(){
        Map<String, Object> response = new HashMap<>();
        response.put("api", "Smart Campus API");
        response.put("version", "1.0");
        response.put("description", "RESTful API for Smart Campus Sensor and Room Management");
        response.put("contact", "admin@smartcampus.ac.uk");
        response.put("module", "5COSC022W - Client Server Architectures");
        
        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "api/v1/sensors");
        response.put("resources", links);
        
        return Response.ok(response).build();
    }
}
