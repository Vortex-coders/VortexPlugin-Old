package org.ru.vortex.utils.console;

import static org.ru.vortex.PluginVars.outlineCommands;
import static org.ru.vortex.PluginVars.outlinePassword;

import arc.util.CommandHandler;
import arc.util.Log;
import java.io.*;
import java.net.Socket;

public class ConsoleServerConnection extends Thread {

  public final BufferedWriter out;
  private final Socket socket;
  private final BufferedReader in;

  public ConsoleServerConnection(Socket socket) throws IOException {
    this.socket = socket;
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    start();
  }

  @Override
  public void run() {
    String input = "";
    try {
      input = in.readLine();
      if (!input.equals(outlinePassword)) {
        //todo send disconnect
        Log.err(
          "[REMOTE_CONSOLE]" +
          socket.getInetAddress().getHostAddress() +
          " authorization FAILED"
        );
        this.downService();
      } else {
        Log.info(
          "[REMOTE_CONSOLE]" +
          socket.getInetAddress().getHostAddress() +
          " authorization DONE"
        );
      }
      while (true) {
        input = in.readLine();
        Log.debug("[REMOTE_CONSOLE]" + "{IN_}" + " |>| " + input);
        CommandHandler.CommandResponse r = outlineCommands.handleMessage(
          input,
          this
        );

        //switch не пройдет
        if (r.type == CommandHandler.ResponseType.noCommand) {
          Log.info("[REMOTE_CONSOLE] |<|>| " + input);
          // TODO: 30.12.2022 response to remote
        } else if (r.type == CommandHandler.ResponseType.fewArguments) {
          // TODO: 30.12.2022 response to remote
        } else if (r.type == CommandHandler.ResponseType.manyArguments) {
          // TODO: 30.12.2022 response to remote
        } else if (r.type == CommandHandler.ResponseType.valid) {
          // TODO: 30.12.2022 response to remote
        }
        //todo handleInput
      }
    } catch (Exception e) {
      Log.debug(e);
      this.downService();
    }
  }

  public void send(String msg) {
    try {
      Log.debug("[REMOTE_CONSOLE]" + "{OUT}" + " |>| " + msg);
      out.write(msg + "\n");
      out.flush();
    } catch (IOException ignored) {}
  }

  public void downService() {
    try {
      if (!socket.isClosed()) {
        socket.close();
        in.close();
        out.close();
        this.interrupt();
      }
    } catch (IOException ignored) {}
  }
}
