package org.ru.vortex.modules.console;

import static org.ru.vortex.modules.console.Console.commandHandler;

import arc.util.CommandHandler.CommandParam;
import java.util.*;
import org.jline.console.ArgDesc;
import org.jline.console.CmdDesc;

public class AutoSuggestions {

    public static Map<String, CmdDesc> get() {
        Map<String, CmdDesc> tailTips = new HashMap<>();

        commandHandler
            .getCommandList()
            .forEach(command -> {
                List<String> params = new ArrayList<>();
                for (int i = 0; i < command.params.length; i++) {
                    CommandParam param = command.params[i];
                    params.add(param.optional ? "[" + param.name + "]" : "<" + param.name + ">");
                }
                tailTips.put(command.text, new CmdDesc(ArgDesc.doArgNames(params)));
            });

        return tailTips;
    }
}
