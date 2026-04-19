/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author Hasandi Vihanga
 */
import com.smartcampus.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class SensorReadingResource {
    private final String sensorId;
    
    public SensorReadingResource(String sensorId){
        this.sensorId = sensorId;
    }
    
    @GET
    public Response getReadings(){
        Sensor sensor = DataStore.sensors.get(sensorId);
        if(sensor == null){
            Map<String, Object> error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not found");
            error.put("message", "Sensor not found: " + sensorId);
            return Response.status(404).entity(error).build();
        }
        
        List<SensorReading> sensorReadings = DataStore.readings.get(sensorId);
        if(sensorReadings == null){
            sensorReadings = new ArrayList<>();
        }
        return Response.ok(sensorReadings).build();
    }
    
    @POST 
    public Response addReading(SensorReading reading){
        Sensor sensor = DataStore.sensors.get(sensorId);
        if(sensor == null){
            Map<String, Object> error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not found");
            error.put("message", "Sensor not found: " + sensorId);
            return Response.status(404).entity(error).build();
        }
        if("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())){
            throw new SensorUnavailableException(
            "Sensor " + sensorId + " is currently under MAINTENANCE and cannot accept new readings."
            );
        }
        
        SensorReading newReading = new SensorReading(reading.getValue());
        
        List<SensorReading> readingsList = DataStore.readings.get(sensorId);
        if(readingsList == null){
            readingsList = new ArrayList<>();
            DataStore.readings.put(sensorId,  readingsList);
           
        }
        readingsList.add(newReading);
        sensor.setCurrentValue(reading.getTimeStamp());
        
        return Response
                .status(Response.Status.CREATED)
                .entity(newReading)
                .build();
    }
}
