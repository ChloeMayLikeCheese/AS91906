package org.AS91906;

import java.io.File;

public class Account {
    public String name;
    public String address;
    public String accountNumber;
    public String type;
    public double balance;
    public File fileRef;

    public Account(String fileData, File fileRef) {
        String[] accountData = fileData.split(";");
        this.name = accountData[0];
        this.address = accountData[1];
        this.accountNumber = accountData[2];
        this.type = accountData[3];
        this.balance = Double.parseDouble(accountData[4]);
        this.fileRef = fileRef;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getBalance() {
        return balance;
    }

    public File getFileRef() {
        return fileRef;
    }

    public void deposit(double amount) {
        this.balance += amount;
    }

    public void withdraw(double amount) {
        this.balance -= amount;
    }

    public String toFileString() {
        return name + ";" + address + ";" + accountNumber + ";" + type + ";" + balance;
    }

    public String displayFormat() {
        return "Name: " + name + "\n" +
                "Address: " + address + "\n" +
                "Account Number: " + accountNumber + "\n" +
                "Account Type: " + type + "\n" +
                "Account Balance: $" + balance + "\n";
    }
}
