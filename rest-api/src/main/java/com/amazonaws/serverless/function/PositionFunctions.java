package com.amazonaws.serverless.function;

import org.apache.log4j.Logger;

import com.amazonaws.serverless.dao.DynamoDBLastPositionDao;
import com.amazonaws.serverless.dao.DynamoDBPositionDao;
import com.amazonaws.serverless.domain.LastPosition;
import com.amazonaws.serverless.domain.Position;

public class PositionFunctions {
	private static final Logger log = Logger.getLogger(PositionFunctions.class);

    private static final DynamoDBPositionDao positionDao = DynamoDBPositionDao.instance();
    private static final DynamoDBLastPositionDao lastPositionDao = DynamoDBLastPositionDao.instance();
    
    public LastPosition getLastPosition() {
        log.info("GetLastPosition.");        
        LastPosition lastPosition = lastPositionDao.getLastPosition();
        return lastPosition != null ? lastPosition : new LastPosition("", 0f, 0f, 0f);
    }
    
    public void updatePosition(Position position) {    	
    	
    	if (null == position) {
            log.error("Add position received null input");
            throw new IllegalArgumentException("Cannot save null object");
        }
    		
        if (position.getId() == null || position.getId().isEmpty()) {
        	log.error("generate id");
        	position.setId(Long.toString(System.currentTimeMillis()));
        }
        
        log.info("Saving package = " + position.getId() + " x: " + position.getX() + 
        		" y: "+ position.getY() + " z: " + position.getZ());
        
        positionDao.addPosition(position);
        LastPosition lastPosition = lastPositionDao.getLastPosition();
        
        if (lastPosition == null)
        	lastPosition = new LastPosition(null, null, null, null);
        	
    	if (lastPosition.getId() == null || lastPosition.getId().isEmpty()) {
    		lastPosition.setId(Long.toString(System.currentTimeMillis()));
    	}
    	
        lastPosition.setX(position.getX());
        lastPosition.setY(position.getY());
        lastPosition.setZ(position.getZ());
        
        lastPositionDao.updatePosition(lastPosition);
        log.info("Successfully saved/updated position");
    }

}
