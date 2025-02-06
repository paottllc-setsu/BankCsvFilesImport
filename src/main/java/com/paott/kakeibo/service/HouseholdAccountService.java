package com.paott.kakeibo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paott.kakeibo.entity.Expense;
import com.paott.kakeibo.entity.ExpenseCategory;
import com.paott.kakeibo.entity.HouseholdAccount;
import com.paott.kakeibo.repository.ExpenseCategoryRepository;
import com.paott.kakeibo.repository.ExpenseRepository;
import com.paott.kakeibo.repository.HouseholdAccountRepository;

@Service
public class HouseholdAccountService {

    @Autowired
    private HouseholdAccountRepository householdAccountRepository;
    
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;

    public void save(HouseholdAccount householdAccount) {
        householdAccountRepository.save(householdAccount);
    }

    public List<HouseholdAccount> getAllExpenses() {
        return (List<HouseholdAccount>) householdAccountRepository.findAll();
    }

    public List<HouseholdAccount> getExpensesByPerson(String person) {
        return householdAccountRepository.findByPerson(person);
    }

    public List<HouseholdAccount> getExpensesByCategoryId(Integer categoryId) {
        return householdAccountRepository.findByCategoryId(categoryId);
    }
    
    public List<HouseholdAccount> findAllCategories() {
        return householdAccountRepository.findAll();
    }

    // 文字列のリストとしてカテゴリ名を取得するメソッド
    public List<String> getAllCategoryNames() {
        return expenseCategoryRepository.findAll().stream()
            .map(ExpenseCategory::getCategoryName)
            .collect(Collectors.toList());
    }

    // エンティティのリストとしてカテゴリを取得するメソッド
    public List<ExpenseCategory> getAllCategories() {
        return expenseCategoryRepository.findAll();
    }

    public List<String> getAllPersons() {
        return householdAccountRepository.findAll().stream()
            .map(HouseholdAccount::getPerson)
            .distinct()
            .collect(Collectors.toList());
    }

    public List<LocalDate> getAllDistinctDates() {
        return householdAccountRepository.findDistinctDates();
    }

    public List<HouseholdAccount> getExpensesWithinPeriod(LocalDate startDate, LocalDate endDate) {
        return householdAccountRepository.findByDateBetween(startDate, endDate);
    }

    public void deleteExpensesByNames(List<String> namesToDelete) {
        householdAccountRepository.deleteByPersonIn(namesToDelete);
    }

    public void deleteExpensesByCategories(List<String> categoriesToDelete) {
        List<ExpenseCategory> expenseCategories = expenseCategoryRepository.findByCategoryNameIn(categoriesToDelete);
        householdAccountRepository.deleteByCategoryIn(expenseCategories);
    }
    
    //支出削除のためのメソッド
    public void deleteById(int id) {
        householdAccountRepository.deleteById(id);
    }
    
    public Expense findById(int id) {
        Optional<Expense> expense = expenseRepository.findById(id);
        return expense.orElse(null);
    }
    
	// household_accountからデータを取得
    public HouseholdAccount findHouseholdAccountById(int id) {
        Optional<HouseholdAccount> householdAccount = householdAccountRepository.findById(id);
        return householdAccount.orElse(null);
    }

    public void save(Expense expense) {
        expenseRepository.save(expense);
    }
    
    // expense_categoryからカテゴリを取得
    public List<ExpenseCategory> findAllExpenseCategories() {
        return expenseCategoryRepository.findAll();
    }

    public ExpenseCategory findCategoryById(int id) {
        Optional<ExpenseCategory> category = expenseCategoryRepository.findById(id);
        return category.orElse(null);
    }
    
    public List<HouseholdAccount> findAllOrderByDateAsc() {
        return householdAccountRepository.findAllOrderByDateAsc();
    }
}
