package org.AS91906;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;

public class Main {
    static ArrayList<File> accountsList = new ArrayList<>();
    static File accounts = new File("accounts/");

    enum Operation {
        OPEN,
        CREATE,
        CLEAR_ACCOUNTS
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String topBar = "Open an account (o)\n";
        accounts.mkdir();

        // System.out.println(Arrays.asList(accounts.listFiles()));
        // System.exit(0);
        // accountsList.addAll(Arrays.asList(accounts.listFiles()));

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
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            // accountsList.addAll(Arrays.asList(accounts.listFiles()));
            // System.out.println(accountsList.getLast());
            // System.exit(0);
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
                            if (!accountsList.isEmpty()) {
                            } else {
                                terminal.writer().println();
                                String in = readInput(terminal, lineReader,
                                        "No accounts available, would you like to create one?(y/N) ");
                                if (in.equals("y")) {
                                    createAccount(terminal, lineReader);
                                } else {
                                    terminal.writer().println("Aborting...");
                                    terminal.writer().flush();
                                    Thread.sleep(500);
                                }

                            }

                        }
                        case CLEAR_ACCOUNTS -> {
                            deleteDir(accounts);
                        }
                        // case CREATE -> {
                        // String in = readInput(terminal, lineReader, "Create an account?(y/N) ");
                        // if (in.equals("y")) {
                        // createAccount(terminal, lineReader);
                        // } else {
                        // terminal.writer().println("Aborting...");
                        // terminal.writer().flush();
                        // Thread.sleep(500);
                        // }
                        // }

                    }
                }

            }
        }
    }

    public static String readInput(Terminal terminal, LineReader lineReader, String prompt)
            throws InterruptedException {
        String in = null;
        try {
            in = lineReader.readLine(prompt).strip();
        } catch (UserInterruptException e) {
            terminal.writer().flush();
        }
        if (in != null) {
            return in;
        } else {
            return "";
        }
    }

    public static void createAccount(Terminal terminal, LineReader lineReader) throws InterruptedException {
        String name = readInput(terminal, lineReader, "Enter your name: ");
        String address = readInput(terminal, lineReader, "Enter your address: ");
        String type = readInput(terminal, lineReader, "Enter account type: (e)veryday, (s)avings, (c)urrent : ");
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
        terminal.writer().println("Account created: " + accountNumber);
       // accountsList.add(new File( accountNumber + ".txt"));
        switch (type.toLowerCase().strip()) {
            case "everyday", "e" -> {
                type = "everyday";
            }
            case "savings", "s" -> {
                type = "savings";
            }
            case "current", "c" -> {
                type = "current";
            }
            default -> {
                break;
            }
        }
        File newAccount = new File("accounts/" + accountNumber + ".txt");
        accountsList.add(newAccount);
        try (FileWriter accountWriter = new FileWriter(newAccount)) {
            accountWriter.write(name + ";" + address + ";" + accountNumber + ";" + type);
        } catch (Exception e) {
        }
        terminal.writer().println(accountsList);
        terminal.writer().flush();
        String in = readInput(terminal, lineReader, "Press enter to continue");
    }

    public static void deleteDir(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                c.delete();
            }
            f.delete();
        }
    }

}