package com.goldtrading.service;

import com.goldtrading.dao.CustomerDao;
import com.goldtrading.model.Customer;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing customer operations.
 */
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService() {
        this.customerDao = new CustomerDao();
    }

    public int addCustomer(String name, String phone, String email, String address,
                           String idProof, String idNumber) {
        try {
            Customer customer = new Customer(name, phone, email, address, idProof, idNumber);
            return customerDao.insert(customer);
        } catch (SQLException e) {
            System.err.println("[CUSTOMER] Error adding customer: " + e.getMessage());
            return -1;
        }
    }

    public Optional<Customer> getCustomer(int id) {
        try {
            return customerDao.findById(id);
        } catch (SQLException e) {
            System.err.println("[CUSTOMER] Error retrieving customer: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Customer> getAllCustomers() {
        try {
            return customerDao.findAll();
        } catch (SQLException e) {
            System.err.println("[CUSTOMER] Error listing customers: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Customer> searchCustomers(String name) {
        try {
            return customerDao.searchByName(name);
        } catch (SQLException e) {
            System.err.println("[CUSTOMER] Error searching customers: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean updateCustomer(Customer customer) {
        try {
            customerDao.update(customer);
            return true;
        } catch (SQLException e) {
            System.err.println("[CUSTOMER] Error updating customer: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCustomer(int id) {
        try {
            customerDao.delete(id);
            return true;
        } catch (SQLException e) {
            System.err.println("[CUSTOMER] Error deleting customer: " + e.getMessage());
            return false;
        }
    }

    public int getCustomerCount() {
        try {
            return customerDao.getCount();
        } catch (SQLException e) {
            System.err.println("[CUSTOMER] Error getting count: " + e.getMessage());
            return 0;
        }
    }
}
