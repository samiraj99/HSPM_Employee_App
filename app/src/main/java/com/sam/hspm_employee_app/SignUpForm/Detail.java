package com.sam.hspm_employee_app.SignUpForm;

public class Detail {
    public String FullName, BirthDate, State, Pincode, City, Gender, PhoneNo , Email;

    public Detail(String fullName, String birthDate, String gender, String phno , String email) {
        FullName = fullName;
        BirthDate = birthDate;
        Gender = gender;
        PhoneNo = phno;
        Email = email;
    }
}
