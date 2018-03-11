package com.amazonaws.serverless.domain;

import java.io.Serializable;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "LastPosition")
public class LastPosition implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@DynamoDBHashKey
    private String id;
	
	@DynamoDBAttribute
    private Float x;

	@DynamoDBAttribute
    private Float y;

    @DynamoDBAttribute
    private Float z;

    public LastPosition(){}
    
    public LastPosition(String id, Float x, Float y, Float z){
    	this.id = id;
    	this.x = x;
    	this.y = y;
    	this.z = z;
    }

    public LastPosition(Position position){
    	this.id = position.getId();
    	this.x = position.getX();
    	this.y = position.getY();
    	this.z = position.getZ();
    }
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Float getX() {
		return x;
	}

	public void setX(Float x) {
		this.x = x;
	}

	public Float getY() {
		return y;
	}

	public void setY(Float y) {
		this.y = y;
	}

	public Float getZ() {
		return z;
	}

	public void setZ(Float z) {
		this.z = z;
	}
}
