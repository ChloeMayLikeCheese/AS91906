package org.AS91906;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    static ArrayList<File> accountsList = new ArrayList<>();
    static File accounts = new File("accounts/");

    enum Operation {
        OPEN,
        CREATE,
        BACK,
        CLEAR_ACCOUNTS
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String topBar = "Open or edit an account (o) | Create an account (c)\n";
        accounts.mkdir();
        updateAccounts();

        try (Terminal terminal = TerminalBuilder.builder()
                .name("KawaiiBank Teller")
                .jansi(true)
                .build()) {
            terminal.enterRawMode();
            BindingReader bindingReader = new BindingReader(terminal.reader());
            KeyMap<Operation> keyMap = new KeyMap<>();
            keyMap.bind(Operation.OPEN, "o");
            keyMap.bind(Operation.CLEAR_ACCOUNTS, "C");
            keyMap.bind(Operation.CREATE, "c");
            keyMap.bind(Operation.BACK, "\\033[D");
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            terminal.puts(Capability.clear_screen);
            terminal.writer().println(topBar);
            terminal.writer().flush();

            while (true) {
                terminal.puts(Capability.clear_screen);
                terminal.writer().println(topBar);
                terminal.writer().flush();
                Operation op = bindingReader.readBinding(keyMap, null, false);
                if (op != null) {
                    switch (op) {
                        case OPEN -> {
                            if (!accounts.mkdir()) {
                                updateAccounts();
                                if (!accountsList.isEmpty()) {
                                    StringBuilder builder = new StringBuilder();
                                    for (int i = 0; i < accountsList.size(); i++) {
                                        File value = accountsList.get(i);
                                        builder.append(i).append(": ").append(value).append("\n");
                                    }
                                    String formattedAccountsList = builder.toString();
                                    terminal.writer().println("Accounts: \n"
                                            + formattedAccountsList.replace("accounts/", "").replace("_", " "));
                                    terminal.writer().flush();
                                    String in = readInput(terminal, lineReader,
                                            "What would you like to do? Delete account(d), Add balance(a), Withdraw balance(w), Press Enter to go back: ",
                                            false, "[daw]", true);
                                    switch (in) {
                                        case "d" -> {
                                            int deletionSelect = Integer.parseInt(readInput(terminal, lineReader,
                                                    "Select the index of the account to delete: ", true, "[0-9]",
                                                    false));
                                            if (accountsList.get(deletionSelect).delete()) {
                                                StringBuilder deletedFile = new StringBuilder();
                                                deletedFile.append(accountsList.get(deletionSelect));
                                                String formattedDeletedFile = deletedFile.toString();
                                                terminal.writer().println("Account deleted: " + formattedDeletedFile
                                                        .replace("accounts/", "").replace("_", " "));
                                                terminal.writer().flush();
                                                String deleteContinue = readInput(terminal, lineReader,
                                                        "Press enter  to continue", false, null, false);
                                            } else {

                                            }
                                        }

                                    }
                                } else {
                                    terminal.writer().println();
                                    String in = readInput(terminal, lineReader,
                                            "No accounts available, would you like to create one?(y/N) ", false, null,
                                            false);
                                    if (in.equals("y")) {
                                        createAccount(terminal, lineReader);
                                    } else {
                                        terminal.writer().println("Aborting...");
                                        terminal.writer().flush();
                                        Thread.sleep(500);
                                    }

                                }
                            } else {
                                if (!accountsList.isEmpty()) {
                                    StringBuilder builder = new StringBuilder();
                                    for (int i = 0; i < accountsList.size(); i++) {
                                        File value = accountsList.get(i);
                                        builder.append(i).append(": ").append(value).append("\n");
                                    }
                                    String formattedAccountsList = builder.toString();
                                    terminal.writer().println("Accounts: \n"
                                            + formattedAccountsList.replace("accounts/", "").replace("_", " "));
                                    terminal.writer().flush();
                                    String in = readInput(terminal, lineReader,
                                            "What would you like to do? Delete account(d), Add balance(a), Withdraw balance(w), Press Enter to go back: ",
                                            false, "[daw]", true);
                                    switch (in) {
                                        case "d" -> {
                                            int deletionSelect = Integer.parseInt(readInput(terminal, lineReader,
                                                    "Select the index of the account to delete: ", true, "[0-9]",
                                                    false));
                                            if (accountsList.get(deletionSelect).delete()) {
                                                StringBuilder deletedFile = new StringBuilder();
                                                deletedFile.append(accountsList.get(deletionSelect));
                                                String formattedDeletedFile = deletedFile.toString();
                                                terminal.writer().println("Account deleted: " + formattedDeletedFile
                                                        .replace("accounts/", "").replace("_", " "));
                                                terminal.writer().flush();
                                                String deleteContinue = readInput(terminal, lineReader,
                                                        "Press enter  to continue", false, null, false);
                                            } else {

                                            }
                                        }

                                    }
                                } else {
                                    terminal.writer().println();
                                    String in = readInput(terminal, lineReader,
                                            "No accounts available, would you like to create one?(y/N) ", false, null,
                                            false);
                                    if (in.equals("y")) {
                                        createAccount(terminal, lineReader);
                                    } else {
                                        terminal.writer().println("Aborting...");
                                        terminal.writer().flush();
                                        Thread.sleep(500);
                                    }

                                }
                            }
                        }
                        case CLEAR_ACCOUNTS -> {
                            updateAccounts();
                            deleteDir(accounts);
                        }
                        case CREATE -> {
                            if (accounts.mkdir()) {
                                String in = readInput(terminal, lineReader, "Create an account?(y/N) ", false, null,
                                        false);
                                if (in.equals("y")) {
                                    createAccount(terminal, lineReader);
                                } else {
                                    terminal.writer().println("Aborting...");
                                    terminal.writer().flush();
                                    Thread.sleep(500);
                                }
                            } else {
                                String in = readInput(terminal, lineReader, "Create an account?(y/N) ", false, null,
                                        false);
                                if (in.equals("y")) {
                                    createAccount(terminal, lineReader);
                                } else {
                                    terminal.writer().println("Aborting...");
                                    terminal.writer().flush();
                                    Thread.sleep(500);
                                }
                            }

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
                if (ch != null && ch.matches(regex)) {
                    return originalSelfInsert.apply();
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

    public static void createAccount(Terminal terminal, LineReader lineReader) throws InterruptedException {
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
            accountWriter.write(name + ";" + address + ";" + accountNumber + ";" + type);
        } catch (Exception e) {
        }
        updateAccounts();
        StringBuilder builder = new StringBuilder();
        builder.append(newAccount);
        String newAccountName = builder.toString();
        terminal.writer().println("Account created: "
                + newAccountName.replace("accounts/", "").replace("_", " "));
        terminal.writer().flush();
        String in = readInput(terminal, lineReader, "Press enter to continue", false, null, false);
    }

    public static void deleteDir(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                c.delete();
            }
            f.delete();
        }
    }

    public static void updateAccounts() {
        accountsList.clear();
        File[] accountsArray = accounts.listFiles(
                (dir, name) -> name.matches("^.*\\d{2}-\\d{4}-\\d{7}-\\d{2}$") && new File(dir, name).isFile());
        Arrays.sort(accountsArray);
        accountsList.addAll(Arrays.asList(accountsArray));
    }

}