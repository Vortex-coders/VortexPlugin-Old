package org.ru.vortex.modules.console;

import static org.ru.vortex.modules.console.Console.commandList;

import java.util.regex.Pattern;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.*;

public class CommandHighlighter implements Highlighter {

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        AttributedStringBuilder builder = new AttributedStringBuilder();
        builder.style(new AttributedStyle().foreground(AttributedStyle.RED));

        String command;

        try {
            command = buffer.split(" ")[0];
        } catch (Exception e) {
            command = buffer;
        }

        if (commandList.contains(command)) {
            builder.style(new AttributedStyle().foreground(AttributedStyle.GREEN));
            builder.append(buffer);
        } else {
            builder.style(new AttributedStyle().foreground(AttributedStyle.RED));
            builder.append(buffer);
        }

        return builder.toAttributedString();
    }

    @Override
    public void setErrorPattern(Pattern errorPattern) {}

    @Override
    public void setErrorIndex(int errorIndex) {}
}
