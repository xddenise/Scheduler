package Time;

import java.util.Objects;

public class Availability extends TimeSlot {

    private boolean fixed;

    public Availability(String shiftType, int date, String location, boolean fixed) {
        super(shiftType, date, location);
        this.fixed = fixed;
    }

    public Availability(ShiftType shiftType, int date, String location, boolean fixed) {
        super(shiftType, date, location);
        this.fixed = fixed;
    }

    public boolean getFixed(){
        return fixed;
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
