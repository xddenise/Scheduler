package main;

import Exceptions.UnexpectedException;
import Time.Availability;
import Time.Shift;
import Time.ShiftType;
import Time.TimeSlot;
import javafx.util.Pair;

import java.util.ArrayList;

import static prQ.MyComparator.combinations;

public class Employee {
    private String name;
    private String gender;
    //desiredNumOfShifts = 0: does not matter how many shift is assigned
    //                   = infinity: does not matter but lowest priority
    private double desiredNumOfShifts;
    private ArrayList<Availability> availableTime;
    //already assigned to num of shifts
    private ArrayList<Shift> assignedShifts;
    //indicates employee's ability to open a shift (order based on the locations arraylist in Scheduler)
    private ArrayList<Boolean> openings;
    private ArrayList<String> locations;
    //whether this.employee need balanced shifts (almost equal amount of morningShifts and nightShifts)
    //out of 100, balance: val = 50
    //            morningShift# more than nightShift#: val > 50
    //            nightShift# more than morningShift#: val < 50
    private Pair<Boolean,Integer> balancedShifts;

    public Employee(String name, String gender, double desiredNumOfShifts, ArrayList<Boolean> openings, ArrayList<String> locations, Pair<Boolean,Integer> balancedShifts){
        this.name = name;
        this.gender = gender;
        this.desiredNumOfShifts = desiredNumOfShifts;
        assignedShifts = new ArrayList<>();
        availableTime = new ArrayList<>();
        this.openings = openings;
        this.locations = locations;
        this.balancedShifts = balancedShifts;
    }

    public String getName(){
        return name;
    }

    public String getGender(){
        return gender;
    }

    public double getDesiredNumOfShifts(){
        return desiredNumOfShifts;
    }

    public ArrayList<Availability> getAvailableTime(){
        return availableTime;
    }

    public boolean needBalancedShifts(){
        return balancedShifts.getKey();
    }

    public Integer getBalancedShiftsVal(){
        return balancedShifts.getValue();
    }

    public void setBalancedShifts(Pair<Boolean,Integer> p){
        balancedShifts = p;
    }

    public void setAvailableTime(ArrayList<Availability> availableTime){
        if(availableTime != null){
            this.availableTime = availableTime;
        }
    }

    public void removeAvailableTimeIfExist(Shift shift){
        TimeSlot timeSlot = new TimeSlot(shift.getShiftType(),shift.getDate());
        while(availableTime.contains(timeSlot)){
            availableTime.remove(timeSlot);
        }
    }

    public int numOfAssignedShifts(){
        return assignedShifts.size();
    }

    public void addToAssignedShifts(Shift shift){
        if(shift != null){
            assignedShifts.add(shift);
        }
    }

    //returns double that represents this employee's priority in shift
    public double priorityOfEmployee(Shift shift){
        if(isAvailabilityFixed(shift)){
            return 0;
        }else if(desiredNumOfShifts == Double.POSITIVE_INFINITY){
            return 1000000000;
        }else if(isThereConsecShift(shift)){
            return Double.POSITIVE_INFINITY;
        }else if(shift.getShiftType() == ShiftType.MORNINGSHIFT && !shift.hasOpeningEmployee()) {
            int ind = locations.indexOf(shift.getLocation());
            if (openings.get(ind)) {
                return 0.5;
            }
        }
        int n1 = numOfAvailTime();
        int r1 = (int)desiredNumOfShifts- numOfAssignedShifts();
        double priority = combinations(n1-r1+1,r1);
        if((balancedShifts.getKey()) &&
                ((shift.getShiftType() == ShiftType.MORNINGSHIFT && balancedShifts.getValue()<50)
                        || (shift.getShiftType() == ShiftType.NIGHTSHIFT && balancedShifts.getValue()>50))){
            priority = priority*0.3;
        }
        return priority;
    }

    public boolean isAvailabilityFixed(Shift shift){
        TimeSlot timeSlot = new TimeSlot(shift.getShiftType(), shift.getDate());
        int ind = availableTime.indexOf(timeSlot);
        return availableTime.get(ind).getFixed();
    }

    //checks if there is assigned shifts that is consecutive with parameter shift
    public boolean isThereConsecShift(Shift shift){
        TimeSlot timeBefore, timeAfter;
        if(shift.getShiftType() == ShiftType.MORNINGSHIFT){
            if(shift.getDate() == 1){
                timeBefore = new TimeSlot(ShiftType.NIGHTSHIFT, 7);
            }else{
                timeBefore = new TimeSlot(ShiftType.NIGHTSHIFT, shift.getDate()-1);
            }
            timeAfter = new TimeSlot(ShiftType.NIGHTSHIFT,shift.getDate());
        }else{
            if(shift.getDate() == 7){
                timeAfter = new TimeSlot(ShiftType.MORNINGSHIFT,1);
            }else{
                timeAfter = new TimeSlot(ShiftType.MORNINGSHIFT,shift.getDate()+1);
            }
            timeBefore = new TimeSlot(ShiftType.MORNINGSHIFT, shift.getDate());
        }

        for(Shift s: assignedShifts){
            if(s.atSameTime(timeBefore) || s.atSameTime(timeAfter)|| s.atSameTime(shift)){
                return true;
            }
        }
        return false;
    }

    public boolean getOpeningAt(String location) throws UnexpectedException {
        if(!locations.contains(location)){
            throw new UnexpectedException(location+" is not involved!");
        }
        int ind = locations.indexOf(location);
        return openings.get(ind);
    }

    //checks whether an employee is available at a timeSlot.
    //returns true if timeSlot is in availableTime
    //else returns false (return false for timeSlot == null)
    public boolean isAvailable(TimeSlot timeSlot){
        if(timeSlot == null){
            return false;
        }else{
            return availableTime.contains(timeSlot);
        }
    }

    public int numOfAvailTime(){
        return availableTime.size();
    }

    public boolean isDesiredNumShiftReached(){
        if(desiredNumOfShifts == Double.POSITIVE_INFINITY){
            return false;
        }else if(desiredNumOfShifts == 0){
            if(assignedShifts.size() < availableTime.size()){
                return false;
            }else{
                return true;
            }
        }else{
            return desiredNumOfShifts == assignedShifts.size();
        }
    }

    public boolean canEmployeeBeInShift(Shift shift){
        return !isDesiredNumShiftReached() && !assignedShifts.contains(shift);
    }
}
