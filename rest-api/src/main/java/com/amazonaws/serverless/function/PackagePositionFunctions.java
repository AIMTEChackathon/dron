package com.amazonaws.serverless.function;

import org.apache.log4j.Logger;

import com.amazonaws.serverless.dao.DynamoDBPackagePositionDao;
import com.amazonaws.serverless.domain.PackagePosition;

public class PackagePositionFunctions {
	 private static final Logger log = Logger.getLogger(PackagePositionFunctions.class);

    private static final DynamoDBPackagePositionDao packagePositionDao = DynamoDBPackagePositionDao.instance();

    public PackagePosition getLastPackagePosition() {
        log.info("GetLastPackagePosition.");        
        return packagePositionDao.getLastPackages();
    }
    
    public void addPackagePosition(PackagePosition packagePosition) {    	
    	
    	if (null == packagePosition) {
            log.error("Add position received null input");
            throw new IllegalArgumentException("Cannot save null object");
        }
    		
        if (packagePosition.getId() == null || packagePosition.getId().isEmpty()) {
        	log.error("generate id");
        	packagePosition.setId(Long.toString(System.currentTimeMillis()));
        }
        
        log.info("Saving or updating package = " + packagePosition.getId() + " , long = " + packagePosition.getLongtitude() + " lat = " + packagePosition.getLatitude());
        packagePositionDao.addPackage(packagePosition);
        log.info("Successfully saved/updated packagePosition");
    }

}
