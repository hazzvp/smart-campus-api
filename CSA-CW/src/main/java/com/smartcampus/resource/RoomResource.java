/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author Hasandi Vihanga
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class RoomResource {
    //GET /api/v1/rooms - return all rooms
    @GET
    public Response getAllRooms(){
        Collection<Room> allRooms = DataStore.rooms.values();
        return Response.ok(new ArrayList<>(allRooms)).build();
    }
    
    // POST /api/v1/rooms - create a new room
    @POST 
    public Response createRoom(Room room){
        if(room.getId() == null || room.getId().isEmpty()){
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("error", "Bad ID is requried");
            error.put("message", "Room ID is required");
            return Response.status(400).entity(error).build();
        }
        if(DataStore.rooms.containsKey(room.getId())){
            Map<String, Object>error = new  HashMap<>();
            error.put("status", 409);
            error.put("error", "Conflict");
            error.put("message", "Room with ID " + room.getId() + " already exists");
            return Response.status(409).entity(error).build();
        }
        DataStore.rooms.put(room.getId(), room);
        return Response
                .status(Response.Status.CREATED)
                .entity(room)
                .header("Location", "/api/v1/rooms/" + room.getId())
                .build();
    }
    
    // get a specific room

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId")String roomId){
        Room room = DataStore.rooms.get(roomId);
        if(room == null){
            Map<String, Object> error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not found");
            error.put("message", "Room not found: " + roomId);
            return Response.status(404).entity(error).build();
        }
        return Response.ok(room).build();
    }
    
    //delete room only if no sensors
    
    
    @DELETE
    @Path("/{roomId}")
    public  Response deleteRoom(@PathParam("roomId") String roomId){
        Room room  = DataStore.rooms.get(roomId);
        if(room == null){
            Map<String, Object>error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not found");
            error.put("message", "Room not found: " + roomId);
            return Response.status(404).entity(error).build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
            "cannot delete room " + roomId + " It still has " + room.getSensorIds().size() + " active sensor/sensors assigned to it."
            );
        }
        DataStore.rooms.remove(roomId);
        Map<String, Object> response  = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Room " + roomId + "deleted successfully");
        return Response.ok(response).build();
    }
}
