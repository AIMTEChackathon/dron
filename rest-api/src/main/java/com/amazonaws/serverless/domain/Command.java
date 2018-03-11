package com.amazonaws.serverless.domain;

import java.io.Serializable;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Command")
public class Command implements Serializable{

	private static final long serialVersionUID = 1L;

	@DynamoDBHashKey
    private String id;
	
	@DynamoDBAttribute
    private String command;
    public Command() { }

    public Command(String id, String command) {
        this.id = id;
        this.command = command;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
