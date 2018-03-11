package com.amazonaws.serverless.dao;

import com.amazonaws.serverless.domain.PackagePosition;

public interface PackagePositionDao {
	
	PackagePosition getLastPackages();
    void addPackage(PackagePosition packagePosition);
}
