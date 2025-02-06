package com.paott.kakeibo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.paott.kakeibo.entity.ExpenseCategory;
import com.paott.kakeibo.entity.HouseholdAccount;

public interface HouseholdAccountRepository extends JpaRepository<HouseholdAccount, Integer> {
    List<HouseholdAccount> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<HouseholdAccount> findByPerson(String person);

    List<HouseholdAccount> findByCategoryId(Integer categoryId);

    @Query("SELECT DISTINCT date FROM HouseholdAccount ORDER BY date ASC")
    List<LocalDate> findDistinctDates();

    @Query("SELECT DISTINCT person FROM HouseholdAccount ORDER BY person ASC")
    List<String> findAllPersons();
    
    List<HouseholdAccount> findAll();

    @Query("SELECT DISTINCT e.category FROM HouseholdAccount e")
    List<ExpenseCategory> findAllCategories();
    
    @Query(value = "SELECT * FROM household_account ORDER BY date ASC", nativeQuery = true)
    List<HouseholdAccount> findAllOrderByDateAsc();

    void deleteByPersonIn(List<String> namesToDelete);

    void deleteByCategoryIn(List<ExpenseCategory> categoriesToDelete);
    
    //　支出削除のためのリポジトリ
    void deleteByIdIn(List<Integer> ids);
}
