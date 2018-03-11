package com.amazonaws.serverless.dao;

import com.amazonaws.serverless.domain.LastPosition;

public interface LastPositionDao {
	
    LastPosition getLastPosition();
    
    void updatePosition(LastPosition lastPosition);
}
