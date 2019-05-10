package com.sam.hspm_employee_app.SignUpForm;

public class Detail {
    public String FullName, BirthDate, State, Pincode, City, Gender, PhoneNo;

    public Detail(String fullName, String birthDate, String gender, String phno) {
        FullName = fullName;
        BirthDate = birthDate;
        Gender = gender;
        PhoneNo = phno;
    }
}
