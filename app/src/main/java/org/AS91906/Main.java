package org.AS91906;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.Widget;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;

public class Main {
    static ArrayList<Account> accountsList = new ArrayList<>();
    static File accounts = new File("accounts/");
    static double total = 0;
    static double dailyNetDeposits = 0;
    static double dailyNetWithdraws = 0;

    enum Operation {
        OPEN,
        CREATE,
        CLEAR_ACCOUNTS,
        END_DAY
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String ui = " _____________________________ \n| View or edit an account (v) |\n| Create an account (c)       |\n| End day/quit (e/q)          |\n _____________________________ ";
        accounts.mkdir();
        updateAccounts();

        try (Terminal terminal = TerminalBuilder.builder()
                .name("KawaiiBank Teller")
                .jansi(true)
                .build()) {
            terminal.enterRawMode();
            BindingReader bindingReader = new BindingReader(terminal.reader());
            KeyMap<Operation> keyMap = new KeyMap<>();
            keyMap.bind(Operation.OPEN, "v");
            keyMap.bind(Operation.CLEAR_ACCOUNTS, "C");
            keyMap.bind(Operation.CREATE, "c");
            keyMap.bind(Operation.END_DAY, "e", "q");
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            terminal.puts(Capability.clear_screen);
            terminal.writer().println(ui);
            terminal.writer().flush();

            while (true) {
                terminal.puts(Capability.clear_screen);
                terminal.writer().println(ui);
                terminal.writer().flush();
                Operation op = bindingReader.readBinding(keyMap, null, false);
                if (op != null) {
                    switch (op) {
                        case OPEN -> {
                            if (!accounts.exists()) {
                                accounts.mkdir();
                            }
                            updateAccounts();
                            if (!accountsList.isEmpty()) {
                                terminal.puts(Capability.clear_screen);
                                StringBuilder builder = new StringBuilder();
                                for (int i = 0; i < accountsList.size(); i++) {
                                    Account acc = accountsList.get(i);
                                    builder.append(i).append(": ").append(acc.getFileRef().getName()).append("\n");
                                }
                                String formattedAccountsList = builder.toString();
                                terminal.writer().println("Accounts: \n"
                                        + formatFileName(formattedAccountsList));
                                terminal.writer().flush();
                                String in = readInput(terminal, lineReader,
                                        "What would you like to do? Delete account(d), Edit balance(e), View account(v), Press Enter to go back: ",
                                        false, "[devDEV]", true);
                                switch (in.toLowerCase()) {
                                    case "d" -> {
                                        int deletionSelect = selectIndex(terminal, lineReader,
                                                "Select the index of the account to delete: ");
                                        if (deletionSelect == -1) {
                                            terminal.writer().println("Aborting...");
                                            terminal.writer().flush();
                                            Thread.sleep(500);
                                        } else {
                                            deleteFile(terminal, lineReader, accountsList.get(deletionSelect));
                                        }

                                    }
                                    case "v" -> {
                                        int openSelect = selectIndex(terminal, lineReader,
                                                "Select the index of the account to open: ");
                                        if (openSelect == -1) {
                                            terminal.writer().println("Aborting...");
                                            terminal.writer().flush();
                                            Thread.sleep(500);
                                        } else {
                                            terminal.puts(Capability.clear_screen);
                                            openAccount(terminal, lineReader, accountsList.get(openSelect));
                                            readInput(terminal, lineReader, "Press enter  to continue", false, null,
                                                    false);
                                        }

                                    }
                                    case "e" -> {
                                        int editSelect = selectIndex(terminal, lineReader,
                                                "Select the index of the account to edit: ");
                                        if (editSelect == -1) {
                                            terminal.writer().println("Aborting...");
                                            terminal.writer().flush();
                                            Thread.sleep(500);
                                        } else {
                                            terminal.puts(Capability.clear_screen);
                                            editBalance(terminal, lineReader, accountsList.get(editSelect));
                                        }
                                    }

                                }
                            } else {
                                terminal.writer().println();
                                String in = yesNo(terminal, lineReader, "No accounts available, create one?");
                                if (in.equals("y")) {
                                    terminal.puts(Capability.clear_screen);
                                    createAccount(terminal, lineReader);
                                } else {
                                    terminal.writer().println("Aborting...");
                                    terminal.writer().flush();
                                    Thread.sleep(500);
                                }

                            }
                        }
                        case CLEAR_ACCOUNTS -> {
                            updateAccounts();
                            deleteDir(accounts);
                        }
                        case CREATE -> {

                            if (!accounts.exists()) {
                                accounts.mkdir();
                            }
                            String in = yesNo(terminal, lineReader, "Create an account?");
                            if (in.equals("y")) {
                                terminal.puts(Capability.clear_screen);
                                createAccount(terminal, lineReader);
                            } else {
                                terminal.writer().println("Aborting...");
                                terminal.writer().flush();
                                Thread.sleep(500);
                            }

                        }
                        case END_DAY -> {
                            total = 0;
                            for (Account account : accountsList) {
                                total += account.getBalance();
                            }
                            terminal.puts(Capability.clear_screen);
                            terminal.writer().println("Day ended.\nNet deposits for today: $" + dailyNetDeposits
                                    + "\nNet withdraws for today: $" + dailyNetWithdraws
                                    + "\nTotal money in accounts: $"
                                    + total);
                            if (!accountsList.isEmpty()) {
                                StringBuilder builder = new StringBuilder();
                                for (int i = 0; i < accountsList.size(); i++) {
                                    Account acc = accountsList.get(i);
                                    builder.append(i).append(": ").append(acc.getFileRef().getName()).append("\n");
                                }
                                String formattedAccountsList = builder.toString();
                                terminal.writer().println("Accounts in system: \n"
                                        + formatFileName(formattedAccountsList));
                                terminal.writer().flush();
                                terminal.writer().flush();
                            }
                            terminal.writer().flush();
                            System.exit(0);
                        }

                    }
                }

            }
        }
    }

    public static String readInput(Terminal terminal, LineReader lineReader, String prompt, boolean restrict,
            String regex, boolean singleDigit)
            throws InterruptedException {

        String in = null;
        Widget originalSelfInsert = lineReader.getWidgets().get(LineReader.SELF_INSERT);
        if (singleDigit) {
            lineReader.getWidgets().put(LineReader.SELF_INSERT, () -> {
                if (lineReader.getBuffer().length() == 0) {
                    String ch = lineReader.getLastBinding();
                    if (ch != null && ch.matches(regex)) {
                        return originalSelfInsert.apply();
                    }
                }
                return true;
            });
        } else if (restrict) {
            lineReader.getWidgets().put(LineReader.SELF_INSERT, () -> {
                String ch = lineReader.getLastBinding();
                if (ch != null) {
                    String current = lineReader.getBuffer().toString();
                    String updated = current + ch;
                    if (updated.matches(regex)) {
                        return originalSelfInsert.apply();
                    }
                }

                return true;
            });
        } else {
            lineReader.getWidgets().put(LineReader.SELF_INSERT, originalSelfInsert);
        }
        try {
            in = lineReader.readLine(prompt).strip();
        } catch (UserInterruptException e) {
            terminal.writer().flush();
        } finally {
            lineReader.getWidgets().put(LineReader.SELF_INSERT, originalSelfInsert);
        }

        if (in != null) {
            return in;
        } else {
            return "";
        }

    }

    public static void createAccount(Terminal terminal, LineReader lineReader)
            throws InterruptedException, FileNotFoundException {
        double balance = 0.0;
        String name = readInput(terminal, lineReader, "Enter your first and last name: ", false, null, false);
        if (name.equals("")) {
            terminal.writer().println("Aborting...");
            terminal.writer().flush();
            Thread.sleep(500);
            return;
        }
        String address = readInput(terminal, lineReader, "Enter your address: ", false, null, false);
        if (address.equals("")) {
            terminal.writer().println("Aborting...");
            terminal.writer().flush();
            Thread.sleep(500);
            return;
        }
        String type = readInput(terminal, lineReader, "Enter account type: Everyday(e), Savings(s), current(c) : ",
                false,
                "[escESC]", true);
        if (type.equals("")) {
            terminal.writer().println("Aborting...");
            terminal.writer().flush();
            Thread.sleep(500);
            return;
        }

        String[] nameArray = name.split(" ");
        name = "";
        for (int i = 0; i < nameArray.length; i++) {
            char firstLetter = nameArray[i].charAt(0);
            firstLetter = Character.toUpperCase(firstLetter);
            String firstLetterString = "" + nameArray[i].charAt(0);
            name += nameArray[i].replaceFirst(firstLetterString, "" + firstLetter) + " ";
        }

        int rand;
        int[] accountNumberArray = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        for (int i = 0; i < 15; i++) {
            rand = (int) (Math.random() * 10) + 0;
            accountNumberArray[i] = rand;
        }
        String accountNumber = "";
        for (int i = 0; i < 2; i++) {
            accountNumber += accountNumberArray[i];
        }
        accountNumber += "-";
        for (int i = 0; i < 4; i++) {
            accountNumber += accountNumberArray[i + 2];
        }
        accountNumber += "-";
        for (int i = 0; i < 7; i++) {
            accountNumber += accountNumberArray[i + 4];
        }
        accountNumber += "-";
        for (int i = 0; i < 2; i++) {
            accountNumber += accountNumberArray[i + 7];
        }
        switch (type.toLowerCase()) {
            case "e" -> {
                type = "Everyday";
            }
            case "s" -> {
                type = "Savings";
            }
            case "c" -> {
                type = "Current";
            }
        }

        File newAccount = new File(
                "accounts/" + name.strip().replaceAll(" ", "_") + "_" + type + "_" + accountNumber);
        updateAccounts();
        try (FileWriter accountWriter = new FileWriter(newAccount)) {
            accountWriter.write(name + ";" + address + ";" + accountNumber + ";" + type + ";" + balance);
        } catch (Exception e) {
        }
        updateAccounts();
        StringBuilder builder = new StringBuilder();
        builder.append(newAccount);
        String newAccountName = builder.toString();
        terminal.writer().println("Account created: "
                + formatFileName(newAccountName));
        terminal.writer().flush();
        readInput(terminal, lineReader, "Press enter to continue", false, null, false);
    }

    public static void deleteDir(File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                c.delete();
            }
            file.delete();
        }
    }

    public static void updateAccounts() throws FileNotFoundException {
        accountsList.clear();
        File[] accountsArray = accounts.listFiles(
                (dir, name) -> name.matches("^.*\\d{2}-\\d{4}-\\d{7}-\\d{2}$") && new File(dir, name).isFile());
        Arrays.sort(accountsArray);
        if (accountsArray != null) {
            Arrays.sort(accountsArray);
            for (File file : accountsArray) {
                try (Scanner accountReader = new Scanner(file)) {
                    if (accountReader.hasNextLine()) {
                        accountsList.add(new Account(accountReader.nextLine(), file));
                    }
                }
            }
        }
    }

    public static String formatFileName(String file) {
        return file.replace("accounts/", "").replace("_", " ");
    }

    public static void deleteFile(Terminal terminal, LineReader lineReader, Account account)
            throws InterruptedException, FileNotFoundException {
        File file = account.getFileRef();
        if (file.delete()) {
            String formattedDeletedFile = formatFileName(file.toString());
            terminal.writer().println(
                    "Account deleted: " + formatFileName(formattedDeletedFile));
            terminal.writer().flush();
            readInput(terminal, lineReader, "Press enter  to continue", false, null, false);
        } else {
            terminal.writer().println("Failed to delete account");
            terminal.writer().flush();
            readInput(terminal, lineReader, "Press enter  to continue", false, null, false);
        }
        updateAccounts();
    }

    public static void openAccount(Terminal terminal, LineReader lineReader, Account account) {
        terminal.writer().println(account.displayFormat());
        terminal.writer().flush();
    }

    public static void editBalance(Terminal terminal, LineReader lineReader, Account account)
            throws InterruptedException, IOException {
        openAccount(terminal, lineReader, account);

        String depositOrWithdraw = readInput(terminal, lineReader, "Deposit(d) or Withdraw(w) balance? ", true,
                "[dwDW]", true);

        switch (depositOrWithdraw.toLowerCase()) {
            case "d" -> {
                double depositAmount = Double.parseDouble(readInput(terminal, lineReader,
                        "Enter how much money to deposit: ", true, "\\d*\\.?\\d{0,2}", false));
                account.deposit(depositAmount);
                dailyNetDeposits += depositAmount;
            }
            case "w" -> {
                double withdrawAmount = Double.parseDouble(readInput(terminal, lineReader,
                        "Enter how much money to withdraw(Up to $5000, Overdraft only available for 'current' accounts, limit of $1000): ",
                        true, "^(?:[0-4]?\\d{1,3}(?:\\.\\d{0,2})?|5000(?:\\.0{0,2})?)$", false));
                double newBalance = account.getBalance() - withdrawAmount;
                if (!account.getType().equals("Current") && newBalance < 0) {
                    terminal.writer().println("Failed to withdraw: Overdraft is only available for 'current' accounts");
                    terminal.writer().flush();
                    Thread.sleep(500);
                    return;
                } else if (newBalance < -1000) {
                    terminal.writer().println("Failed to withdraw: Overdraft limit reached");
                    terminal.writer().flush();
                    Thread.sleep(500);
                    return;
                } else {
                    account.withdraw(withdrawAmount);
                    dailyNetWithdraws += withdrawAmount;
                }
            }

        }
        try (FileWriter changeBalance = new FileWriter(account.getFileRef())) {
            changeBalance.write(account.toFileString());
        }

        updateAccounts();

    }

    public static int selectIndex(Terminal terminal, LineReader lineReader, String prompt)
            throws NumberFormatException, InterruptedException {
        while (true) {
            String input = readInput(terminal, lineReader, prompt, true, "\\d*", false);

            if (input.equals("")) {
                return -1;
            }
            int index = Integer.parseInt(input);
            if (index >= 0 && index < accountsList.size()) {
                return index;
            } else {
                terminal.writer().println("Please enter a valid index");
            }

        }
    }

    public static String yesNo(Terminal terminal, LineReader lineReader, String prompt) throws InterruptedException {
        String in = readInput(terminal, lineReader, prompt + "(y/n): ", true, "[ynYN]", true);
        return in;
    }

}