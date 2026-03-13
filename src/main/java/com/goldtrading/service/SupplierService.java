package com.goldtrading.service;

import com.goldtrading.dao.SupplierDao;
import com.goldtrading.model.Supplier;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing supplier operations.
 */
public class SupplierService {

    private final SupplierDao supplierDao;

    public SupplierService() {
        this.supplierDao = new SupplierDao();
    }

    public int addSupplier(String name, String phone, String email, String address,
                           String companyName, String licenseNumber) {
        try {
            Supplier supplier = new Supplier(name, phone, email, address, companyName, licenseNumber);
            return supplierDao.insert(supplier);
        } catch (SQLException e) {
            System.err.println("[SUPPLIER] Error adding supplier: " + e.getMessage());
            return -1;
        }
    }

    public Optional<Supplier> getSupplier(int id) {
        try {
            return supplierDao.findById(id);
        } catch (SQLException e) {
            System.err.println("[SUPPLIER] Error retrieving supplier: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Supplier> getAllSuppliers() {
        try {
            return supplierDao.findAll();
        } catch (SQLException e) {
            System.err.println("[SUPPLIER] Error listing suppliers: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Supplier> searchSuppliers(String name) {
        try {
            return supplierDao.searchByName(name);
        } catch (SQLException e) {
            System.err.println("[SUPPLIER] Error searching suppliers: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean updateSupplier(Supplier supplier) {
        try {
            supplierDao.update(supplier);
            return true;
        } catch (SQLException e) {
            System.err.println("[SUPPLIER] Error updating supplier: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSupplier(int id) {
        try {
            supplierDao.delete(id);
            return true;
        } catch (SQLException e) {
            System.err.println("[SUPPLIER] Error deleting supplier: " + e.getMessage());
            return false;
        }
    }

    public int getSupplierCount() {
        try {
            return supplierDao.getCount();
        } catch (SQLException e) {
            System.err.println("[SUPPLIER] Error getting count: " + e.getMessage());
            return 0;
        }
    }
}
