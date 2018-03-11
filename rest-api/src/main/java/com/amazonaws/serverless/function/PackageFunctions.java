package com.amazonaws.serverless.function;

import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.serverless.dao.DynamoDBPackageDao;
import com.amazonaws.serverless.domain.Package;

public class PackageFunctions {
	 private static final Logger log = Logger.getLogger(PackageFunctions.class);

    private static final DynamoDBPackageDao packageDao = DynamoDBPackageDao.instance();

    public List<Package> getAllPackagesHandler() {
        log.info("GetAllPackages invoked to scan table for ALL events");        
        List<Package> packages = packageDao.getAllPackages();
	    return packages;
    }
    
    public void saveOrUpdatePackage(Package pack) {    	
    	log.error("In");
    	
    	if (null == pack) {
            log.error("SaveEvent received null input");
            throw new IllegalArgumentException("Cannot save null object");
        }
    		
        if (pack.getPackageId() == null) {
        	log.error("generate id");
        	pack.setPackageId(System.currentTimeMillis());
        }
        
        log.info("Saving or updating event for team = " + pack.getLabelNumber() + " , position = " + pack.getPositionName());
        packageDao.saveOrUpdatePackage(pack);
        log.info("Successfully saved/updated package");
    }
    
    public void deleteAllPackages(){
    	packageDao.deleteAll();
    	log.info("Successfully deleted");
    }

}
