package Time;

import Exceptions.FailedScheduleException;
import Exceptions.UnexpectedException;
import javafx.util.Pair;
import main.Employee;
import prQ.MyComparator;
import prQ.MyPriorityQueue;

import java.util.ArrayList;
import java.util.Objects;

public class Shift extends TimeSlot {

    private int numOfEmployeesNeeded;
    private ArrayList<Employee> possibleEmployees;
    private int curNumOfEmployees;
    private ArrayList<Employee> assignedEmployees;
    private boolean completed;
    private MyComparator myComparator;
    private MyPriorityQueue<Employee> employeePrQ;

    public Shift(String shiftType, int date, int numOfEmployee, String location){
        super(shiftType,date,location);
        this.numOfEmployeesNeeded = numOfEmployee;
        possibleEmployees = new ArrayList<>();
        curNumOfEmployees = 0;
        assignedEmployees = new ArrayList<>();
        completed = false;
        myComparator = new MyComparator();
        employeePrQ = new MyPriorityQueue<>(20,myComparator);
    }

    public Shift(ShiftType shiftType, int date, int numOfEmployee, String location){
        super(shiftType,date, location);
        this.numOfEmployeesNeeded = numOfEmployee;
        possibleEmployees = new ArrayList<>();
        curNumOfEmployees = 0;
        assignedEmployees = new ArrayList<>();
        completed = false;
        myComparator = new MyComparator();
        employeePrQ = new MyPriorityQueue<>(20,myComparator);
    }

    public int getNumOfEmployeesNeeded(){
        return numOfEmployeesNeeded;
    }

    public int getCurNumOfEmployees(){
        return curNumOfEmployees;
    }

    //return the number of employees in employeePrQ
    public int employeePrQSize(){
        return employeePrQ.size();
    }

    //return the number of employees that this shift still needs in order to be completed
    public int numOfEmployeeStillNeeded(){
        return numOfEmployeesNeeded - curNumOfEmployees;
    }

    //for comparing two shifts, determine which shift has higher priority
    //the smaller the number, the higher the priority
    //return the priority of this shift
    public double priorityOfShift(){
        return (employeePrQ.size()/(numOfEmployeesNeeded - curNumOfEmployees));
    }

    public boolean getCompleted(){
        return completed;
    }

    public ArrayList<Employee> getAssignedEmployees(){
        return assignedEmployees;
    }

    //add employee to list of possibleEmployee
    public void addToPossibleEmployee(Employee employee){
        if((employee != null) && (!possibleEmployees.contains(employee))){
            possibleEmployees.add(employee);

        }
    }

    public boolean hasOpeningEmployee(){
        for(Employee employee: assignedEmployees){
            try {
                if(employee.getOpeningAt(location)){
                    return true;
                }
            } catch (UnexpectedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //checks whether this shift has same time as timeSlot
    public boolean atSameTime(TimeSlot timeSlot){
        return (shiftType == timeSlot.getShiftType()) && (date == timeSlot.getDate());
    }

    //checks whether employee is in employeePrQ
    //return true only when employee is in employeePrQ
    public boolean isEmployeeInPrQ(Employee employee){
        if(employee == null){
            return false;
        }else{
            return employeePrQ.contains(employee);
        }
    }

    //remove employee from employeePrQ
    //assume employee is in employeePrQ
    public void removeEmployeeFromPrQ(Employee employee){
        employeePrQ.remove(employee,this);
    }

    public void addToEmployeePrQ(Employee employee){
        if(!employeePrQ.contains(employee)){
            employeePrQ.add(employee,this);
        }
    }

    //create the employee priority queue
    public void createEmployeePrQ(){
        for(int i=0; i<possibleEmployees.size(); i++){
            if(possibleEmployees.get(i) != null && !employeePrQ.contains(possibleEmployees.get(i))){
                employeePrQ.add(possibleEmployees.get(i), this);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(shiftType,date,location);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        Shift shift = (Shift) obj;
        return (shift.getShiftType() == shiftType) && (shift.getDate() == date) && (shift.getLocation() == location);
    }

    //assigns a employee with highest priority in employeePrQ to this shift
    //called by fillSchedule
    public Employee fillShift() throws FailedScheduleException, UnexpectedException {
        Employee chosenEmployee;
        if(employeePrQ.isEmpty() && (!completed)){
            throw new FailedScheduleException("Not enough employee to choose from for"+date+" "+shiftType);
        }else if(completed){
            throw new UnexpectedException("Shift is completed but fillShift on this shift is called!");
        }else if(employeePrQ.isEmpty()){
            throw new UnexpectedException("EmployeePrQ is empty but fillShift on this shift is called!");
        }else{
            chosenEmployee = employeePrQ.peek();
            employeePrQ.poll(this);

            /*
            boolean goodMatch = false;
            //to initialize chosenEmployee
            chosenEmployee = employeePrQ.peek();

            while(!goodMatch){
                //1. remove the first employee
                chosenEmployee = employeePrQ.peek();
                employeePrQ.poll(this);

                //2. check if there is consecutive shift for chosenEmployee if this shift is assigned
                //if causes consecutive shift, set employee.hasConsecShiftFlag to true then reinsert to EmployeePrQ
                //else goodMatch = true and continue
                if(chosenEmployee.isThereConsecShift(this)){
                    chosenEmployee.setHasConsecShiftFlag(true);
                    employeePrQ.add(chosenEmployee);
                }else{
                    goodMatch = true;
                }
            }

             */

            if(chosenEmployee.canEmployeeBeInShift(this)){
                //2. add employee as assignedEmployees, add this shift as assignedShift for employee
                assignedEmployees.add(chosenEmployee);
                chosenEmployee.addToAssignedShifts(this);

                //3. remove all shifts with this.shift's time from chosenEmployee's available time
                chosenEmployee.removeAvailableTimeIfExist(this);

                //4. change value of chosenEmployee's balancedShifts if it is true
                if(chosenEmployee.needBalancedShifts()){
                    int valPrev = chosenEmployee.getBalancedShiftsVal();
                    int valNow;
                    if(shiftType == ShiftType.MORNINGSHIFT){
                        valNow = valPrev + 1;
                    }else{
                        valNow = valPrev - 1;
                    }
                    Pair<Boolean, Integer> newBalanceShifts = new Pair<>(true,valNow);
                    chosenEmployee.setBalancedShifts(newBalanceShifts);
                }

                //5. curNumOfEmployees + 1
                curNumOfEmployees++;
            }else{
                throw new UnexpectedException("Employee in PrQ but cannot be assigned to shift!");
            }

            //6. mark this shift as completed if enough employee is assigned
            if(numOfEmployeesNeeded == curNumOfEmployees){
                completed = true;
            }

            return chosenEmployee;
        }
    }

}
