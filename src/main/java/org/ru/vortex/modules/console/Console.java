package org.ru.vortex.modules.console;

import arc.struct.Seq;
import arc.util.CommandHandler;
import mindustry.server.ServerControl;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.widget.TailTipWidgets;

import static arc.Core.app;
import static arc.util.Log.*;
import static java.lang.System.setOut;
import static org.jline.utils.AttributedString.fromAnsi;

public class Console
{

    public static final Seq<String> commandList = new Seq<>();
    public static CommandHandler commandHandler;
    public static BlockingPrintStream blockingPrintStream;
    private static LineReader lineReader;
    private static ServerControl serverControl;

    public static void init()
    {
        serverControl = (ServerControl) app.getListeners().find(listener -> listener instanceof ServerControl);
        commandHandler = serverControl.handler;
        commandHandler.getCommandList().forEach(command -> commandList.add(command.text));

        try
        {
            lineReader =
                    LineReaderBuilder.builder().highlighter(new CommandHighlighter()).completer(new StringsCompleter(commandList)).build();

            new TailTipWidgets(lineReader, AutoSuggestions.get(), 0, TailTipWidgets.TipType.TAIL_TIP).enable();
            blockingPrintStream = new BlockingPrintStream(string -> lineReader.printAbove(fromAnsi(string)));
            setOut(blockingPrintStream);
            infoTag("Console", "Console loaded");
        }
        catch (Exception e)
        {
            errTag("Console", format("Failed to load the plugin console:\n@", e));
        }

        serverControl.serverInput =
                () ->
                {
                    while (true)
                    {
                        try
                        {
                            String line = lineReader.readLine();
                            if (!line.isEmpty()) app.post(() -> serverControl.handleCommandString(line));
                        }
                        catch (UserInterruptException e)
                        {
                            app.exit();
                        }
                        catch (Exception e)
                        {
                            err(e);
                            app.exit();
                        }
                    }
                };
    }
}
