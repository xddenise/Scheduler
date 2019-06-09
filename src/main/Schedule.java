package main;

import Time.Shift;
import Time.ShiftType;
import javafx.util.Pair;

import java.util.ArrayList;

public class Schedule {
    protected ArrayList<ArrayList<Shift>> shiftTable;
    private String location;


    public Schedule(String location){
        shiftTable = new ArrayList<>(7);
        this.location = location;
    }

    public void formShiftTable(){
        for(int i=0; i<7; i++){
            shiftTable.add(new ArrayList<Shift>(2));
            int date = i+1;
            shiftTable.get(i).add(new Shift(ShiftType.MORNINGSHIFT,date,2,location));
            if(i>=4 && i<=6){
                shiftTable.get(i).add(new Shift(ShiftType.NIGHTSHIFT,date,3,location));
            }else{
                shiftTable.get(i).add(new Shift(ShiftType.NIGHTSHIFT,date,2,location));
            }
        }
    }

    //add employee to EmployeePrQ in this shift (indicated by shiftType and date)
    public void addToPossibleEmployee(Employee employee, String shiftType, int date){
        Pair<Integer,Integer> coord = shiftTableCoord(shiftType,date);
        shiftTable.get(coord.getKey()).get(coord.getValue()).addToPossibleEmployee(employee);
    }

    public Pair<Integer, Integer> shiftTableCoord(String shiftType, int date){
        int x,y;
        x = date-1;
        switch(shiftType){
            case "MORNINGSHIFT":
                y = 0;
                break;
            case "NIGHTSHIFT":
                y = 1;
                break;
            default:
                throw new IllegalArgumentException();
        }
        Pair<Integer,Integer> pair = new Pair<>(x,y);
        return pair;
    }

    public void createAllEmployeePrQ(){
        for(int i=0; i<shiftTable.size(); i++){
            for(int j=0; j<shiftTable.get(0).size(); j++){
                shiftTable.get(i).get(j).createEmployeePrQ();
            }
        }
    }

    public void replaceShift(Shift shift){
        Pair<Integer,Integer> coord = shiftTableCoord(shift.getShiftType().toString(), shift.getDate());
        shiftTable.get(coord.getKey()).set(coord.getValue(),shift);
    }

    public String getLocation(){
        return location;
    }
}
