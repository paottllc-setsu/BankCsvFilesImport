package com.paott.kakeibo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paott.kakeibo.entity.ExpenseCategory;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Integer> {
    List<ExpenseCategory> findAll();
    List<ExpenseCategory> findByCategoryNameIn(List<String> categoryNames);
}
