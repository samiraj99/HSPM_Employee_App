package com.sam.hspm_employee_app;

public class RegistrationData {
  public   String Name,Email,PhoneNo,Address;

 public    RegistrationData(String name, String email, String phoneNo, String address) {
        Name = name;
        Email = email;
        PhoneNo = phoneNo;
        Address = address;
    }

  public   RegistrationData(String name, String email) {
        Name = name;
        Email = email;
    }

    public RegistrationData(String st_name, String st_email, String st_phoneNo) {
        Name = st_name;
        Email = st_email;
        PhoneNo = st_phoneNo;
    }
}
