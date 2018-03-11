package com.amazonaws.serverless.function;

import org.apache.log4j.Logger;

import com.amazonaws.serverless.dao.DynamoDBCommandDao;
import com.amazonaws.serverless.domain.Command;

public class CommandFunctions {
	 private static final Logger log = Logger.getLogger(CommandFunctions.class);

    private static final DynamoDBCommandDao commandDao = DynamoDBCommandDao.instance();

    public Command getCommand() {
        log.info("GetAllPackages invoked to scan table for ALL events");        
        return commandDao.getCommand();
    }
    
    public void addCommand(Command command) {    	
    	if (null == command) {
            log.error("SaveEvent received null input");
            throw new IllegalArgumentException("Cannot save null object");
        }
    		
        if (command.getId() == null || command.getId().isEmpty()) {
        	command.setId(Long.toString(System.currentTimeMillis()));
        }
        
        log.info("Saving command = " + command.getCommand());
        commandDao.addCommand(command);
        log.info("Successfully saved command");
    }

}
