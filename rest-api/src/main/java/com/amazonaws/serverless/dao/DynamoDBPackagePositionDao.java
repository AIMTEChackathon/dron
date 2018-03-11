package com.amazonaws.serverless.dao;

import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.serverless.domain.PackagePosition;
import com.amazonaws.serverless.manager.DynamoDBManager;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

public class DynamoDBPackagePositionDao implements PackagePositionDao {

	private static final Logger log = Logger.getLogger(DynamoDBPackagePositionDao.class);

    private static final DynamoDBMapper mapper = DynamoDBManager.mapper();

    private static volatile DynamoDBPackagePositionDao instance;

    private DynamoDBPackagePositionDao() { }

    public static DynamoDBPackagePositionDao instance() {

        if (instance == null) {
            synchronized(DynamoDBPackagePositionDao.class) {
                if (instance == null)
                    instance = new DynamoDBPackagePositionDao();
            }
        }
        return instance;
    }

	@Override
	public PackagePosition getLastPackages() {
		List<PackagePosition> packages = mapper.scan(PackagePosition.class, new DynamoDBScanExpression());
		
		if(!packages.isEmpty()){
			Comparator<PackagePosition> comparator = Comparator.comparing(PackagePosition::getId);
			return packages.stream().max(comparator).get();			
		}
		
		return null;
	}

	@Override
	public void addPackage(PackagePosition packagePosition) {
		mapper.save(packagePosition);		
	}

}
