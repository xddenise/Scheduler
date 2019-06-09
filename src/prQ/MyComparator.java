package prQ;

import Time.ShiftType;
import main.Employee;
import Time.Shift;

import java.util.Comparator;
import java.util.Random;

public class MyComparator<T> implements Comparator {

    public MyComparator(){
    }

    public static long combinations(int n, int r) {
        long numerator = 1, denominator = 1;
        if (r > n - r) {
            r = n - r;
        }
        for (long i = 1L; i <= r; ++i) {
            denominator *= i;
        }
        for (long i = n - r + 1L; i <= n; ++i) {
            numerator *= i;
        }
        return numerator / denominator;
    }

    public int compare(Object o1, Object o2, Shift shift){
        double o1Val, o2Val;
        Employee e1 = (Employee) o1;
        Employee e2 = (Employee) o2;
        o1Val = e1.priorityOfEmployee(shift);
        o2Val = e2.priorityOfEmployee(shift);

        if(o1Val < o2Val){
            return -1;
        }else if(o1Val > o2Val){
            return 1;
        }else{
            //if they are equal in above comparison, the employee that has less numOfShift has higher priority
            o1Val = e1.numOfAssignedShifts();
            o2Val = e2.numOfAssignedShifts();
            //System.out.print("HERE"+ " "+ e1.getName()+" "+e2.getName()+"\n");
            if(o1Val < o2Val){
                return -1;
            } else if(o1Val > o2Val){
                return 1;
            }else{
                Random random = new Random();
                int hi = random.nextInt(2);
                if(hi == 0){
                    return -1;
                }else{
                    return 1;
                }
            }
        }
    }

    @Override
    public int compare(Object o1, Object o2) {
        /*
            int n1 = ((Shift) o1).employeePrQSize();
            int r1 = ((Shift) o1).numOfEmployeeStillNeeded();
            int n2 = ((Shift) o2).employeePrQSize();
            int r2 = ((Shift) o2).numOfEmployeeStillNeeded();

            long o1Val = combinations(n1,r1);
            long o2Val = combinations(n2,r2);

             */
        Shift s1 = (Shift) o1;
        Shift s2 = (Shift) o2;
        ShiftType s1ShiftType = s1.getShiftType();
        ShiftType s2ShiftType = s2.getShiftType();

        //if there is a morning shift that does not have any opening employee, it has higher priority
        if(s1ShiftType == ShiftType.MORNINGSHIFT && s2ShiftType == ShiftType.NIGHTSHIFT && !s1.hasOpeningEmployee()){
            return -1;
        }else if(s1ShiftType == ShiftType.NIGHTSHIFT&& s2ShiftType == ShiftType.MORNINGSHIFT && !s2.hasOpeningEmployee()){
            return 1;
        }

        int o1Val = (int)s1.priorityOfShift();
        int o2Val = (int)s2.priorityOfShift();

        if(o1Val < o2Val){
            return -1;
        }else if(o1Val > o2Val){
            return 1;
        }else{
            Random random = new Random();
            int hi = random.nextInt(2);
            if(hi == 0){
                return -1;
            }else{
                return 1;
            }
        }
    }
}
