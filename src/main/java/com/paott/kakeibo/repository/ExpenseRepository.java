package com.paott.kakeibo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paott.kakeibo.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
}
