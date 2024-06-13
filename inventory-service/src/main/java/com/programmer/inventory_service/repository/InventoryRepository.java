package com.programmer.inventory_service.repository;

import com.programmer.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {

    public Optional<Inventory> findBySkuCode(String skuCode);
}