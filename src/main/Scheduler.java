package main;

import Exceptions.FailedScheduleException;
import Exceptions.UnexpectedException;
import Time.Availability;
import Time.Shift;
import Time.TimeSlot;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import prQ.MyComparator;

import java.io.*;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Scheduler {
    //private ArrayList<ArrayList<Shift>> shiftTable;
    private PriorityQueue<Shift> shiftPrQ;
    private MyComparator myComparator;
    private ArrayList<Employee> employees;
    private ArrayList<Schedule> schedules;
    //the list locations is to quickly indicate the index of corresponding schedule
    private ArrayList<String> locations;

    public Scheduler(){
        //shiftTable = new ArrayList<>(7);
        myComparator = new MyComparator();
        shiftPrQ = new PriorityQueue<Shift>(14,myComparator);
        employees = new ArrayList<>();
        schedules = new ArrayList<>();
        locations = new ArrayList<>();
    }

    public void schedule(){
        retrieveInfo();

        for(int i=0; i<schedules.size(); i++){
            schedules.get(i).createAllEmployeePrQ();
        }

        createShiftPrQ();
        try {
            fillSchedule();
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
        printSchedule();
    }

    //initiate the shifts inside shiftTable
    /*
    public void formShiftTable(){
        for(int i=0; i<7; i++){
            shiftTable.add(new ArrayList<Shift>(2));
            int date = i+1;
            shiftTable.get(i).add(new Shift(ShiftType.MORNINGSHIFT,date,2));
            if(i>=4 && i<=6){
                shiftTable.get(i).add(new Shift(ShiftType.NIGHTSHIFT,date,3));
            }else{
                shiftTable.get(i).add(new Shift(ShiftType.NIGHTSHIFT,date,2));
            }
        }
    }

     */

    //get the info in JSON file
    public void retrieveInfo(){
        try {
            FileReader fileReader = new FileReader("C:\\Users\\denis\\IdeaProjects\\Scheduler\\src\\files\\OldDataFile");
            JSONTokener JsonTokener = new JSONTokener(fileReader);
            JSONObject jsonObject = new JSONObject(JsonTokener);

            JSONArray locationListJSON = jsonObject.getJSONArray("location");
            for(int i=0; i<locationListJSON.length(); i++){
                String location = locationListJSON.getString(i);
                Schedule tempSchedule = new Schedule(location);
                tempSchedule.formShiftTable();
                locations.add(location);
                schedules.add(tempSchedule);
            }

            JSONArray employeeListJSON = jsonObject.getJSONArray("employees");
            for(int i=0; i<employeeListJSON.length(); i++){
                JSONObject tempEmployee = employeeListJSON.getJSONObject(i);
                String name = tempEmployee.getString("name");
                String gender = tempEmployee.getString("gender");
                double num = tempEmployee.getInt("want");
                Pair<Boolean,Integer> balancedShifts = new Pair<>(tempEmployee.getBoolean("balancedShifts"),50);
                JSONArray openingJSON = tempEmployee.getJSONArray("opening");
                ArrayList<Boolean> openings = new ArrayList<>();
                for(int j=0; j<openingJSON.length(); j++){
                    openings.add(openingJSON.getBoolean(j));
                }
                if(num == -1){
                    num = Double.POSITIVE_INFINITY;
                }
                ArrayList<Availability> availableTime = new ArrayList<>();

                JSONArray availableTimeListJSON = tempEmployee.getJSONArray("availability");
                Employee employee = new Employee(name,gender,num,openings,locations,balancedShifts);
                for(int j=0; j<availableTimeListJSON.length(); j++){
                    JSONObject tempAvailability = availableTimeListJSON.getJSONObject(j);
                    String shiftType = tempAvailability.getString("shiftType");
                    int date = tempAvailability.getInt("date");
                    String location = tempAvailability.getString("location");
                    boolean fixed = tempAvailability.getBoolean("fixed");
                    Availability availability = new Availability(shiftType,date,location,fixed);
                    availableTime.add(availability);
                    int indOfLocation = locations.indexOf(location);
                    schedules.get(indOfLocation).addToPossibleEmployee(employee,shiftType,date);
                }
                employee.setAvailableTime(availableTime);
                employees.add(employee);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //add employee to EmployeePrQ in this shift (indicated by shiftType and date)
    /*
    public void addToPossibleEmployee(Employee employee, String shiftType, int date){
        Pair<Integer,Integer> coord = shiftTableCoord(shiftType,date);
        shiftTable.get(coord.getKey()).get(coord.getValue()).addToPossibleEmployee(employee);
    }

     */

    public void createShiftPrQ(){
        for(int i=0; i<schedules.size(); i++){
            Schedule tempSchedule = schedules.get(i);
            for(int j=0; j<tempSchedule.shiftTable.size(); j++){
                shiftPrQ.addAll(tempSchedule.shiftTable.get(j));
            }
        }
        /*
        for(int i=0; i<shiftTable.size(); i++){
            shiftPrQ.addAll(shiftTable.get(i));
        }

         */
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

    public void printSchedule(){
        try {
            File file = new File("C:\\Users\\denis\\IdeaProjects\\Scheduler\\src\\files\\output.txt");
            //delete file if file already exist (to clear the content)
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            //the spacing for each column (15 spaces)
            String spacing = "               ";
            //length of a line so far (1 = 1 char)
            int lineLen;

            for(int i=0; i<schedules.size(); i++){
                //write location name
                writer.write(schedules.get(i).getLocation());
                writer.newLine();

                //schedule to print
                Schedule schedule = schedules.get(i);

                //- three rows (first row: title, second: morningShift, third: nightShift)
                //- Note: cellRow1Line1 meaning: the string for row 1, line 1
                //- Each row can have more than 1 line so row 2, line 3 means the third line in row 2
                //- Ex. row1 contains all lines in row 1
                ArrayList<String> row1, row2, row3;
                row1 = new ArrayList<>();
                row2 = new ArrayList<>();
                row3 = new ArrayList<>();

                //1. first column
                //Note: cellRow1Line1 is empty cell ending with "|"
                String cellRow1Line1, cellRow2Line1, cellRow3Line1;
                cellRow1Line1 = spacing + "|";
                String title1 = schedule.shiftTable.get(0).get(0).getShiftType().toString();
                String title2 = schedule.shiftTable.get(0).get(1).getShiftType().toString();
                cellRow2Line1 = spacing.substring(0,spacing.length()-title1.length()) + title1 + "|";
                cellRow3Line1 = spacing.substring(0,spacing.length()-title2.length()) + title2 + "|";
                row1.add(cellRow1Line1);
                row2.add(cellRow2Line1);
                row3.add(cellRow3Line1);
                lineLen = cellRow1Line1.length();

                //2. other columns
                for(int j=0; j<schedule.shiftTable.size(); j++){
                    //row 1
                    int dateNum = schedule.shiftTable.get(j).get(0).getDate();
                    String date;
                    switch (dateNum){
                        case 1:
                            date = "MON";
                            break;
                        case 2:
                            date = "TUE";
                            break;
                        case 3:
                            date = "WED";
                            break;
                        case 4:
                            date = "THU";
                            break;
                        case 5:
                            date = "FRI";
                            break;
                        case 6:
                            date = "SAT";
                            break;
                        case 7:
                            date = "SUN";
                            break;
                        default:
                            throw new UnexpectedException("Date is not between 1 and 7!");
                    }

                    row1.set(0, row1.get(0) + spacing.substring(0,spacing.length()-date.length()) + date + "|");
                    //cellRow1Line1 += spacing.substring(1,spacing.length()-date.length()) + date + "|";

                    //row 2
                    ArrayList<Employee> morningEmployees = schedule.shiftTable.get(j).get(0).getAssignedEmployees();
                    modifyRows(spacing, lineLen, row2, morningEmployees);

                    //row 3
                    ArrayList<Employee> nightEmployees = schedule.shiftTable.get(j).get(1).getAssignedEmployees();
                    modifyRows(spacing, lineLen, row3, nightEmployees);

                    lineLen = row1.get(0).length();
                }

                //3. write row 1
                writer.write(row1.get(0));
                writer.newLine();
                writeVerticalSpacing(writer, spacing, lineLen);

                //4. write row 2
                for(int j = 0; j<row2.size(); j++){
                    writer.write(row2.get(j));
                    writer.newLine();
                }
                writeVerticalSpacing(writer, spacing, lineLen);

                //5. write row 3
                for(int j = 0; j<row3.size(); j++){
                    writer.write(row3.get(j));
                    writer.newLine();
                }

                //6. spaces for next schedule
                if((i+1)<schedules.size()){
                    writer.newLine();
                    writer.newLine();
                }
            }

            /*
            writer.write(spacing);
            lineLen += spacing.length();

            //1. writing the first line (title)
            //Note: cellRow1Line1 meaning: the string in cell of row 1, line 1
            //Each cell can have more than 1 line so row 2, line 3 means the third line in row 2 cell
            for(int i=0; i<shiftTable.size(); i++){
                String date = shiftTable.get(i).get(0).getDate();
                String cellRow1Line1 = "|" + spacing.substring(1,spacing.length()-date.length()) + date;
                writer.write(cellRow1Line1);
                lineLen += cellRow1Line1.length();
            }
            writeVerticalSpacing(writer, spacing, lineLen);

            //2. creating the lines for morningShifts and nightShifts
            String cellRow2Line1, cellRow3Line1;
            String verticalTitle1 = shiftTable.get(0).get(0).getShiftType().toString();
            String verticalTitle2 = shiftTable.get(0).get(1).getShiftType().toString();
            cellRow2Line1 = spacing.substring(1,spacing.length()-verticalTitle1.length()) + verticalTitle1 + "|";
            cellRow3Line1 = spacing.substring(1,spacing.length()-verticalTitle2.length()) + verticalTitle1 + "|";

            for(int i=0; i<shiftTable.size(); i++){
                
            }

             */

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
    }

    //write strings with the correct employee names and spacing
    public void modifyRows(String spacing, int lineLen, ArrayList<String> row, ArrayList<Employee> employees) {
        String emptyColumn = spacing + "|";
        for(int j=0; j<employees.size(); j++){
            if(row.size()-1 < j){
                //create empty string with previous columns
                //num = number of columns in front of the current column
                int num = lineLen/emptyColumn.length();
                String newStr = "";
                while(num != 0){
                    newStr += emptyColumn;
                    num--;
                }
                row.add(newStr);
            }

            String name = employees.get(j).getName();
            row.set(j, row.get(j) + spacing.substring(0, spacing.length() - name.length()) + name + "|");
        }
    }

    //write a separator between two rows
    //Note: this method ends at another new line
    public void writeVerticalSpacing(BufferedWriter writer, String spacing, int lineLen) throws IOException {
        for(int i=1; i<=lineLen; i++){
            if(i%(spacing.length()+1) == 0){
                writer.write("|");
            }else{
                writer.write("-");
            }
        }
        writer.newLine();
    }

    public void fillSchedule() throws UnexpectedException {
        while(!shiftPrQ.isEmpty()){
            //1. peek the first shift
            Shift shift = shiftPrQ.peek();

            //2. remove the shift
            Employee employee;
            shiftPrQ.poll();

            //3. if it is not completed, then call fillShift method in this shift, which returns the employee being filled
            if(shift.getCompleted()){
                throw new UnexpectedException("Shift is completed but still in shiftPrQ!");
            }else{
                try {
                    employee = shift.fillShift();
                    //System.out.print("fill shift:"+employee.getName()+" "+shift.getDate()+" "+shift.getShiftType()+" "+shift.getLocation()+"\n");

                    //4. check whether employee has reached desiredNumOfShift, if reached, remove employee from all other shifts
                    //   otherwise, adjust employee's priority in shifts
                    //   remove employee from shifts with same time as shift
                    boolean numReached = employee.isDesiredNumShiftReached();
                    for(Shift s: shiftPrQ) {
                        if (s.isEmployeeInPrQ(employee)) {
                            s.removeEmployeeFromPrQ(employee);
                            if(!numReached && !s.atSameTime(shift)){
                                s.addToEmployeePrQ(employee);
                            }
                        }
                    }

                } catch (FailedScheduleException e) {
                    e.printStackTrace();
                } catch (UnexpectedException e){
                    e.printStackTrace();
                }
            }
            //5. if shift completed, update shift in shiftTable
            //6. if shift not completed, add the shift back into shiftPrQ
            if(shift.getCompleted()){
                int ind = locations.indexOf(shift.getLocation());
                schedules.get(ind).replaceShift(shift);
                /*
                Pair<Integer,Integer> coord = shiftTableCoord(shift.getShiftType().toString(), shift.getDate());
                shiftTable.get(coord.getKey()).set(coord.getValue(),shift);

                 */
            }else{
                shiftPrQ.add(shift);
            }
        }
    }
}
