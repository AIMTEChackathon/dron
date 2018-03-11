package com.amazonaws.serverless.domain;

import java.io.Serializable;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Package")
public class Package implements Serializable{

	//label number, position id, position name 
    
	private static final long serialVersionUID = 1L;

	@DynamoDBHashKey
    private Long packageId;
	
	@DynamoDBAttribute
    private String labelNumber;

	@DynamoDBAttribute
    private Long positionId;

    @DynamoDBAttribute
    private String positionName;

    public Package() { }

    public Package(Long packageId, String labelNumber, Long positionId, String positionName) {
        this.packageId = packageId;
        this.labelNumber = labelNumber;
        this.positionId = positionId;
        this.positionName = positionName;
    }

	public Long getPackageId() {
		return packageId;
	}

	public void setPackageId(Long packageId) {
		this.packageId = packageId;
	}

	public String getLabelNumber() {
		return labelNumber;
	}

	public void setLabelNumber(String labelNumber) {
		this.labelNumber = labelNumber;
	}

	public Long getPositionId() {
		return positionId;
	}

	public void setPositionId(Long positionId) {
		this.positionId = positionId;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}
}
