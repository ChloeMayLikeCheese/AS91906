package org.AS91906;

import java.io.IOException;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Main {
    enum Operation {
        TEST
    }

    public static void main(String[] args) throws IOException {
        
        while (true) {
            try (Terminal terminal = TerminalBuilder.builder()
                    .name("KawaiiBank Teller")
                    .jansi(true) 
                    .build()) {
                terminal.enterRawMode(); 

                BindingReader bindingReader = new BindingReader(terminal.reader());
                KeyMap<Operation> keyMap = new KeyMap<>();
                keyMap.bind(Operation.TEST, "a");

                Operation op = bindingReader.readBinding(keyMap, null, false);
                if (op != null) {
                    switch (op) {
                        case TEST -> {
                            System.out.println("Hello World");
                        }
                    }
                }
            }
        }
    }
}