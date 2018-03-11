package com.amazonaws.serverless.dao;

import java.util.List;
import com.amazonaws.serverless.domain.Package;

public interface PackageDao {
	List<Package> getAllPackages();

    void saveOrUpdatePackage(Package pack);
    
    void deleteAll();
}
