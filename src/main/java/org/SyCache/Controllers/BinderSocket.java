package org.SyCache.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.CacheTag;
import org.SyCache.Services.MiddleService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/*
    Bind Cache Service And Input Connection Command Together
    This Service Read Input Execute Command And Write Output
 */
public class BinderSocket extends Binder implements Runnable{
    boolean isShutdown=false;
    Socket inputSocket;

    public BinderSocket(MiddleService middleService, Socket inputSocket, Cache cache, HashMap<String, CacheCommand> commands){
        super(middleService, cache,commands);
        this.inputSocket=inputSocket;
    }

    @Override
    public void run() {
        try {

            DataInputStream dataInputStream=new DataInputStream(inputSocket.getInputStream());
            DataOutputStream dataOutputStream=new DataOutputStream(inputSocket.getOutputStream());

            String inputCommand;
            ObjectMapper objectMapper=new ObjectMapper();

            while (!isShutdown){
                inputCommand=dataInputStream.readLine();

                if(!inputCommand.isEmpty()) {
                    InterfaceBaseCommandModel command = (objectMapper.readValue(inputCommand, InterfaceBaseCommandModel.class));

                    CacheResult result = commandCenter(command);

                    dataOutputStream.write(objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8));
                    dataOutputStream.write("\n".getBytes(StandardCharsets.UTF_8));
                }
            }

            inputSocket.shutdownInput();
            inputSocket.shutdownOutput();
            inputSocket.close();

        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        finally {
            middleService.shutdownWorker(this);
        }
    }
}
