package com.amazonaws.serverless.dao;

import org.apache.log4j.Logger;

import com.amazonaws.serverless.domain.Position;
import com.amazonaws.serverless.manager.DynamoDBManager;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DynamoDBPositionDao implements PositionDao {

	private static final Logger log = Logger.getLogger(DynamoDBPositionDao.class);

    private static final DynamoDBMapper mapper = DynamoDBManager.mapper();

    private static volatile DynamoDBPositionDao instance;

    private DynamoDBPositionDao() { }

    public static DynamoDBPositionDao instance() {

        if (instance == null) {
            synchronized(DynamoDBPositionDao.class) {
                if (instance == null)
                    instance = new DynamoDBPositionDao();
            }
        }
        return instance;
    }
    
	@Override
	public void addPosition(Position position) {
		mapper.save(position);
	}

}
