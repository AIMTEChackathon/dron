package com.amazonaws.serverless.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.serverless.domain.Package;
import com.amazonaws.serverless.manager.DynamoDBManager;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

public class DynamoDBPackageDao implements PackageDao{

	private static final Logger log = Logger.getLogger(DynamoDBPackageDao.class);

    private static final DynamoDBMapper mapper = DynamoDBManager.mapper();

    private static volatile DynamoDBPackageDao instance;

    private DynamoDBPackageDao() { }

    public static DynamoDBPackageDao instance() {

        if (instance == null) {
            synchronized(DynamoDBPackageDao.class) {
                if (instance == null)
                    instance = new DynamoDBPackageDao();
            }
        }
        return instance;
    }
	
	@Override
	public List<Package> getAllPackages() {
		return mapper.scan(Package.class, new DynamoDBScanExpression());
	}

	@Override
	public void saveOrUpdatePackage(Package pack) {
		mapper.save(pack);
	}

	@Override
	public void deleteAll() {
		List<Package> packages = mapper.scan(Package.class, new DynamoDBScanExpression());
		for(Package pack : packages){
			mapper.delete(pack);
		}
	}
}
