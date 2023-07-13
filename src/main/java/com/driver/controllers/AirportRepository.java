package com.driver.controllers;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class AirportRepository {
    //for storing Passengers details --> HM<PassengerId, PassengerObject>
    HashMap<Integer, Passenger> passengerDB = new HashMap<>();

    //for storing Airport details --> HM<AirportName, AirportObject>
    HashMap<String, Airport> airportDB = new HashMap<>();

    //for storing Flight details --> HM<FlightId, FlightObject>
    HashMap<Integer, Flight> flightDB = new HashMap<>();

    //for storing Flight-Passengers details --> HM<FlightId, List<PassengerId>>
    HashMap<Integer, List<Integer>> flightPassengerDB = new HashMap<>();

    //for storing Flight-Airport details --> HM<AirportName, List<FlightId>>, CityName - the city in which the airport belongs
    HashMap<String, List<Integer>> flightAirportDB = new HashMap<>();

    //for storing Airport belong to which City --> HM<CityName, AirportName>
    HashMap<City, String> airportCtyDB = new HashMap<>();

    public void addAirport(Airport airport) {
        airportDB.put(airport.getAirportName(), airport);
        airportCtyDB.put(airport.getCity(), airport.getAirportName());
    }

    public String getLargestAirportName() {
        int maxTerminal = 0;
        String airportName = "";
        for(String currAirport : airportDB.keySet()){
            if(airportDB.get(currAirport).getNoOfTerminals() > maxTerminal){
                maxTerminal = airportDB.get(currAirport).getNoOfTerminals();
                airportName = currAirport;
            }else if(airportDB.get(currAirport).getNoOfTerminals() == maxTerminal) {
                if(airportName.length() == 0)
                    airportName = currAirport;
                else{
                    if(currAirport.compareTo(airportName) < 0)
                        airportName = currAirport;
                }
            }
        }
        return airportName;
    }

    public void addFlight(Flight flight) {
        flightDB.put(flight.getFlightId(), flight);

        //Add the flight to the Airport from which it departs
        String airportName = airportCtyDB.get(flight.getFromCity());
        if(flightAirportDB.containsKey(airportName)){
            flightAirportDB.get(airportName).add(flight.getFlightId());
        }else{
            List<Integer> flights = new ArrayList<>();
            flights.add(flight.getFlightId());
            flightAirportDB.put(airportName, flights);
        }

        //Add the flight to the Airport in which it lands
        airportName = airportCtyDB.get(flight.getToCity());
        if(flightAirportDB.containsKey(airportName)){
            flightAirportDB.get(airportName).add(flight.getFlightId());
        }else{
            List<Integer> flights = new ArrayList<>();
            flights.add(flight.getFlightId());
            flightAirportDB.put(airportName, flights);
        }
    }

    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity) {
        double minDuration = Double.MAX_VALUE;
        boolean found = false;
        for(int currFlight : flightDB.keySet()){
            City currFlightFromCity = flightDB.get(currFlight).getFromCity();
            City currFlightToCity = flightDB.get(currFlight).getToCity();
            if(currFlightFromCity == fromCity && currFlightToCity == toCity){
                minDuration = Math.min(minDuration, flightDB.get(currFlight).getDuration());
                found = true;
            }
        }
        if(!found)
            return Double.parseDouble("-1");
        else
            return minDuration;
    }

    public int getNumberOfPeopleOn(Date date, String airportName) {
        int people = 0;
        if(flightAirportDB.containsKey(airportName)){
            List<Integer> flights = flightAirportDB.get(airportName);
            for(int currFlight : flights){
                if(flightDB.get(currFlight).getFlightDate().equals(date))
                    people += flightPassengerDB.containsKey(currFlight) ? flightPassengerDB.get(currFlight).size() : 0;
            }
        }
        return people;
    }

    public void addPassenger(Passenger passenger) {
        passengerDB.put(passenger.getPassengerId(),passenger);
    }

    public int calculateFlightFare(Integer flightId) {
        int numberOfPerson = 0;
        if(flightDB.containsKey(flightId) && flightPassengerDB.containsKey(flightId))
            numberOfPerson =  flightPassengerDB.get(flightId).size();
        return numberOfPerson;
    }

    public String bookATicket(Integer flightId, Integer passengerId) {
        if(!passengerDB.containsKey(passengerId))
            return "FAILURE";
        if(!flightDB.containsKey(flightId))
            return "FAILURE";
        for(int currFlight : flightPassengerDB.keySet()){
            List<Integer> currPassengerList = flightPassengerDB.get(currFlight);
            if(currPassengerList.contains(passengerId) && flightId == currFlight)
                return "FAILURE";
        }
        if(flightPassengerDB.containsKey(flightId) && flightPassengerDB.get(flightId).size() >= flightDB.get(flightId).getMaxCapacity())
            return "FAILURE";

        //Now book the ticket
        List<Integer> passengerList = new ArrayList<>();
        if(flightPassengerDB.containsKey(flightId))
            passengerList = flightPassengerDB.get(flightId);
        passengerList.add(passengerId);
        flightPassengerDB.put(flightId, passengerList);
        return "SUCCESS";
    }

    public String cancelATicket(Integer flightId, Integer passengerId) {
        if(!passengerDB.containsKey(passengerId))
            return "FAILURE";
        if(!flightDB.containsKey(flightId))
            return "FAILURE";
        if(flightPassengerDB.containsKey(flightId) && !flightPassengerDB.get(flightId).contains(passengerId))
            return "FAILURE";

        //Now cancel the ticket
        List<Integer> passengerList = flightPassengerDB.get(flightId);
        passengerList.remove(passengerId);
        flightPassengerDB.put(flightId, passengerList);
        return "SUCCESS";
    }

    public int calculateRevenueOfAFlight(Integer flightId) {
        int numberOfPerson = 0;
        if(flightDB.containsKey(flightId) && flightPassengerDB.containsKey(flightId))
            numberOfPerson =  flightPassengerDB.get(flightId).size();
        return numberOfPerson;
    }

    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId) {
        if(!passengerDB.containsKey(passengerId))
            return  0;
        int count = 0;
        for(int currFlight : flightPassengerDB.keySet()){
            List<Integer> currPassengerList = flightPassengerDB.get(currFlight);
            if(currPassengerList.contains(passengerId))
                count++;
        }
        return count;
    }

    public String getAirportNameFromFlightId(Integer flightId) {
        if(!flightDB.containsKey(flightId))
            return null;
        City fromCity = flightDB.get(flightId).getFromCity();
        if(!airportCtyDB.containsKey(fromCity))
            return null;
        return airportCtyDB.get(fromCity);
    }
}
