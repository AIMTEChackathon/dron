package com.amazonaws.serverless.dao;

import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.serverless.domain.Command;
import com.amazonaws.serverless.domain.Event;
import com.amazonaws.serverless.domain.Package;
import com.amazonaws.serverless.domain.PackagePosition;
import com.amazonaws.serverless.manager.DynamoDBManager;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

public class DynamoDBCommandDao implements CommandDao {

	private static final Logger log = Logger.getLogger(DynamoDBCommandDao.class);

    private static final DynamoDBMapper mapper = DynamoDBManager.mapper();

    private static volatile DynamoDBCommandDao instance;

    private DynamoDBCommandDao() { }

    public static DynamoDBCommandDao instance() {

        if (instance == null) {
            synchronized(DynamoDBCommandDao.class) {
                if (instance == null)
                    instance = new DynamoDBCommandDao();
            }
        }
        return instance;
    }

	@Override
	public Command getCommand() {
		List<Command> commands = mapper.scan(Command.class, new DynamoDBScanExpression());
		
		if(commands != null && !commands.isEmpty()){
			
			Comparator<Command> comparator = Comparator.comparing(Command::getId);
			Command command = commands.stream().max(comparator).get();			
			
			for(Command cmd : commands){
				mapper.delete(cmd);
			}
			
			return command;
		}
		else {
			return new Command("", "");
		}
	}

	@Override
	public void addCommand(Command command) {
		mapper.save(command);
	}
	
}
