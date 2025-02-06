package com.paott.kakeibo.entity;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;  
    private String categoryName;

    @OneToMany(mappedBy = "category")
    private Set<HouseholdAccount> householdAccounts;

	public int getId() {
		return id;
	}

	public void setId(Integer id) {
        this.id = id;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Set<HouseholdAccount> getHouseholdAccounts() {
		return householdAccounts;
	}

	public void setHouseholdAccounts(Set<HouseholdAccount> householdAccounts) {
		this.householdAccounts = householdAccounts;
	}
	
	@Override
	public String toString() {
		return this.categoryName;
	}

    // Getters and setters
}
