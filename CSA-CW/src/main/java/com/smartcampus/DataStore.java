/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Hasandi Vihanga
 */
public class DataStore {
    public static final Map<String, Room> rooms = new HashMap<>();
    public static final Map<String, Sensor> sensor = new HashMap<>();
    public static final Map<String, List<SensorReading>> readings = new HashMap<>();
    
    static{
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "Active", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "Active", 400.0, "LAB-101");
        sensor.put(s1.getId(), s1);
        sensor.put(s2.getId(), s2);
        
        r1.getSensorIds().add(s1.getId());
        r2.getSensorIds().add(s2.getId());
        
        List<SensorReading> r1Readings = new ArrayList<>();
        r1Readings.add(new SensorReading(21.0));
        r1Readings.add(new SensorReading(22.5));
        readings.put(s1.getId(), r1Readings);
        
        List<SensorReading> r2Readings = new ArrayList<>();
        r2Readings.add(new SensorReading(398.0));
        readings.put(s2.getId(), r2Readings);
    }
}
