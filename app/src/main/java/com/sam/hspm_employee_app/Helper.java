package com.sam.hspm_employee_app;

public class Helper {
    public String issuedProblem;
    public int amount;

    public Helper(String issuedProblem, int amount) {
        this.issuedProblem = issuedProblem;
        this.amount = amount;
    }

    public String getIssuedProblem() {
        return issuedProblem;
    }

    public void setIssuedProblem(String issuedProblem) {
        this.issuedProblem = issuedProblem;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

