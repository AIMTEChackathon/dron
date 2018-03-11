package com.amazonaws.serverless.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.serverless.domain.LastPosition;
import com.amazonaws.serverless.manager.DynamoDBManager;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

public class DynamoDBLastPositionDao implements LastPositionDao {

	private static final Logger log = Logger.getLogger(DynamoDBLastPositionDao.class);

    private static final DynamoDBMapper mapper = DynamoDBManager.mapper();

    private static volatile DynamoDBLastPositionDao instance;

    private DynamoDBLastPositionDao() { }

    public static DynamoDBLastPositionDao instance() {

        if (instance == null) {
            synchronized(DynamoDBLastPositionDao.class) {
                if (instance == null)
                    instance = new DynamoDBLastPositionDao();
            }
        }
        return instance;
    }

	@Override
	public LastPosition getLastPosition() {
		List<LastPosition> positions = mapper.scan(LastPosition.class, new DynamoDBScanExpression());
		return positions != null && !positions.isEmpty() ? positions.get(0) : null;
	}

	@Override
	public void updatePosition(LastPosition lastPosition) {
		mapper.save(lastPosition);
	}
    

}
