package com.amazonaws.serverless.domain;

import java.io.Serializable;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "PackagePosition")
public class PackagePosition implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@DynamoDBHashKey
    private String id;
	
	@DynamoDBAttribute
    private String data;

	@DynamoDBAttribute
    private String time;

    @DynamoDBAttribute
    private String longtitude;
    
    @DynamoDBAttribute
    private String latitude;

    public PackagePosition(){}
    
    public PackagePosition(String id, String data, String time, String longtitude, String latitude){
    	this.id = id;
    	this.data = data;
    	this.time = time;
    	this.longtitude = longtitude;
    	this.latitude = latitude;
    }
    
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(String longtitude) {
		this.longtitude = longtitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
}
