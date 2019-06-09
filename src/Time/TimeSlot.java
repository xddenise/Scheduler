package Time;

import java.util.Objects;

public class TimeSlot {

    protected ShiftType shiftType;
    protected int date;
    protected String location;

    public TimeSlot(String shiftType, int date, String location){
        this.shiftType = ShiftType.valueOf(shiftType);
        this.date = date;
        this.location = location;
    }

    public TimeSlot(ShiftType shiftType, int date, String location){
        this.shiftType = shiftType;
        this.date = date;
        this.location = location;
    }

    public TimeSlot(ShiftType shiftType, int date){
        this.shiftType = shiftType;
        this.date = date;
    }

    public ShiftType getShiftType(){
        return shiftType;
    }

    public int getDate(){
        return date;
    }

    public String getLocation(){
        return location;
    }

    public void setShiftType(ShiftType shiftType){
        this.shiftType = shiftType;
    }

    public void setDate(int date){
        this.date = date;
    }

    public void setLocation(String location){
        this.location = location;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shiftType,date);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        TimeSlot timeSlot = (TimeSlot) obj;
        return (timeSlot.getShiftType() == shiftType) && (timeSlot.getDate() == date);
    }
}
