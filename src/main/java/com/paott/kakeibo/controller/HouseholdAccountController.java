package com.paott.kakeibo.controller;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.paott.kakeibo.entity.ExpenseCategory;
import com.paott.kakeibo.entity.HouseholdAccount;
import com.paott.kakeibo.exception.ResourceNotFoundException;
import com.paott.kakeibo.service.HouseholdAccountService;


@RequestMapping("")
@Controller
public class HouseholdAccountController {

    @Autowired
    private HouseholdAccountService householdAccountService;

    @GetMapping("/")
    public String home() {
        return "index"; 
    }
    
    //////////////////////////////////////////////////////////////////////////　20250204追加
    @GetMapping("/edit-expense")
    public String editExpense(@RequestParam("expenseId") int expenseId, Model model) {
        HouseholdAccount householdAccount = householdAccountService.findHouseholdAccountById(expenseId);
        if (householdAccount == null) {
            throw new ResourceNotFoundException("Expense not found with id " + expenseId);
        }
        model.addAttribute("expense", householdAccount);
        model.addAttribute("categories", householdAccountService.findAllExpenseCategories());
        return "edit-expense";
    }

    @PostMapping("/update-expense")
    public String updateExpense(@RequestParam int id, @RequestParam String date, @RequestParam String person, 
                                @RequestParam int categoryId, @RequestParam String itemName, 
                                @RequestParam int amount, @RequestParam String store, @RequestParam String note) {
        HouseholdAccount householdAccount = householdAccountService.findHouseholdAccountById(id);
        if (householdAccount == null) {
            throw new ResourceNotFoundException("Expense not found with id " + id);
        }
        System.out.println("Found householdAccount: " + householdAccount);

        ExpenseCategory category = householdAccountService.findCategoryById(categoryId);
        if (category == null) {
            throw new ResourceNotFoundException("Category not found with id " + categoryId);
        }
        System.out.println("Found category: " + category);

        householdAccount.setDate(LocalDate.parse(date)); // 文字列をLocalDateに変換
        householdAccount.setPerson(person);
        householdAccount.setCategory(category);
        householdAccount.setItemName(itemName);
        householdAccount.setAmount(amount);
        householdAccount.setStore(store);
        householdAccount.setNote(note);
        householdAccountService.save(householdAccount);
        return "redirect:/expenses";
    }

    ////////////////////////////////////////////////////////////////////////////
    @GetMapping("/new-expense")
    public String newExpense(Model model, HttpSession session) {
        HouseholdAccount householdAccount = new HouseholdAccount();
        if (session.getAttribute("lastSelectedCategory") != null) {
            householdAccount.setCategory((ExpenseCategory) session.getAttribute("lastSelectedCategory"));
        }
        model.addAttribute("householdAccount", householdAccount);
        model.addAttribute("categories", householdAccountService.getAllCategories());
        return "new-expense";
    }

    @PostMapping("/add")
    public String addExpense(@ModelAttribute HouseholdAccount householdAccount, HttpSession session) {
        householdAccountService.save(householdAccount);
        // カテゴリーの状態をセッションに保存
        session.setAttribute("lastSelectedCategory", householdAccount.getCategory());
        return "redirect:/new-expense";
    }
    
    // 名前削除のためのエンドポイント
    @GetMapping("/delete-names")
    public String showDeleteNames(Model model) {
        List<String> persons = householdAccountService.getAllExpenses().stream()
            .map(HouseholdAccount::getPerson)
            .distinct()
            .collect(Collectors.toList());
        model.addAttribute("persons", persons);
        return "delete-names";
    }

    @PostMapping("/delete-names")
    public String deleteNames(@RequestParam("namesToDelete") List<String> namesToDelete) {
        householdAccountService.deleteExpensesByNames(namesToDelete);
        return "redirect:/expenses";
    }

    // 費目削除のためのエンドポイント
    @GetMapping("/delete-categories")
    public String showDeleteCategories(Model model) {
    	List<ExpenseCategory> categories = householdAccountService.getAllCategories(); // 修正
        model.addAttribute("categories", categories);
        return "delete-categories";
    }

    @PostMapping("/delete-categories")
    public String deleteCategories(@RequestParam("categoriesToDelete") List<String> categoriesToDelete) {
        householdAccountService.deleteExpensesByCategories(categoriesToDelete);
        return "redirect:/expenses";
    }

    @GetMapping("/expenses")
    public String showExpenses(Model model,
                               @RequestParam(required = false) String start,
                               @RequestParam(required = false) String end,
                               @RequestParam(required = false) String person,
                               @RequestParam(required = false) Integer categoryId) {
        List<LocalDate> dates = householdAccountService.getAllDistinctDates();
        List<ExpenseCategory> categories = householdAccountService.getAllCategories();

        // デバッグ用ログ
        System.out.println("categories: " + categories);
        System.out.println("selectedCategoryId: " + categoryId);

        model.addAttribute("categories", categories);
        model.addAttribute("dates", dates);
        model.addAttribute("persons", householdAccountService.getAllPersons());
        model.addAttribute("startDate", start != null ? start : "");
        model.addAttribute("endDate", end != null ? end : "");
        model.addAttribute("selectedPerson", person != null ? person : "");
        model.addAttribute("selectedCategoryId", categoryId != null ? categoryId : 0);

        List<HouseholdAccount> expenses;

        LocalDate startDate = (start != null && !start.isEmpty()) ? LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
        LocalDate endDate = (end != null && !end.isEmpty()) ? LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;

        if (startDate != null || endDate != null) {
            expenses = householdAccountService.getExpensesWithinPeriod(
                startDate != null ? startDate : LocalDate.MIN,
                endDate != null ? endDate : LocalDate.MAX
            ).stream().sorted(Comparator.comparing(HouseholdAccount::getDate)).collect(Collectors.toList());

            if (person != null && !person.isEmpty()) {
                expenses = expenses.stream()
                        .filter(expense -> expense.getPerson().equals(person))
                        .collect(Collectors.toList());
            }

            if (categoryId != null && categoryId != 0) {
                int catId = categoryId;
                expenses = expenses.stream()
                        .filter(expense -> expense.getCategory().getId() == catId)
                        .collect(Collectors.toList());
            }
        } else {
            expenses = householdAccountService.findAllOrderByDateAsc();
        }

        model.addAttribute("expenses", expenses);
        return "expenses";
    }

    
    @PostMapping("/delete-expenses")
    public String deleteExpenses(@RequestParam("expenseId") List<Integer> expenseIds) {
        for (int id : expenseIds) {
            householdAccountService.deleteById(id);
        }
        return "redirect:/expenses";
    }

    @GetMapping("/summary")
    public String showSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String person,
            Model model) {
        final Integer selectedYear = (year != null) ? year : LocalDate.now().getYear(); // final変数として定義
        
        List<HouseholdAccount> expenses = householdAccountService.getAllExpenses().stream()
            .filter(expense -> expense.getDate().getYear() == selectedYear)
            .filter(expense -> person == null || person.isEmpty() || expense.getPerson().equals(person))
            .collect(Collectors.toList());
        
        Map<Month, Integer> monthlyTotals = expenses.stream()
            .collect(Collectors.groupingBy(
                expense -> expense.getDate().getMonth(),
                Collectors.summingInt(HouseholdAccount::getAmount)
            ));
        
        // 利用可能な名前のリストを取得
        List<String> persons = householdAccountService.getAllExpenses().stream()
            .map(HouseholdAccount::getPerson)
            .distinct()
            .collect(Collectors.toList());

        Map<String, Map<Month, Integer>> categoryMonthlyTotals = new LinkedHashMap<>();
        // 特定の順序でカテゴリごとの月別合計を計算
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "食費");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "生活用品");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "衣料・美容");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "医療・健康");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "交通・通信");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "水・光熱費");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "住居");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "教育");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "娯楽");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "交際・行事");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "その他");
        initializeCategoryMonthlyTotals(categoryMonthlyTotals, "収入");

        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "食費");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "生活用品");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "衣料・美容");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "医療・健康");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "交通・通信");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "水・光熱費");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "住居");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "教育");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "娯楽");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "交際・行事");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "その他");
        addCategoryMonthlyTotals(categoryMonthlyTotals, expenses, "収入");

        Map<String, Integer> categoryTotals = new LinkedHashMap<>();
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "食費");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "生活用品");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "衣料・美容");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "医療・健康");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "交通・通信");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "水・光熱費");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "住居");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "教育");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "娯楽");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "交際・行事");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "その他");
        initializeCategoryTotal(categoryTotals, categoryMonthlyTotals, "収入");

        int yearlyTotal = monthlyTotals.values().stream().mapToInt(Integer::intValue).sum();

        model.addAttribute("monthlyTotals", monthlyTotals);
        model.addAttribute("categoryMonthlyTotals", categoryMonthlyTotals);
        model.addAttribute("categoryTotals", categoryTotals);
        model.addAttribute("yearlyTotal", yearlyTotal);
        model.addAttribute("year", LocalDate.now().format(DateTimeFormatter.ofPattern("yy"))); // 年の下2桁を追加
        model.addAttribute("year", selectedYear); // 選択された年を追加
        model.addAttribute("selectedPerson", person); // 選択された人を追加
        model.addAttribute("persons", persons); // 利用可能な名前のリストを追加

        return "summary";
    }

    private void initializeCategoryMonthlyTotals(Map<String, Map<Month, Integer>> categoryMonthlyTotals, String categoryName) {
        Map<Month, Integer> monthlyTotals = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            monthlyTotals.put(month, 0);
        }
        categoryMonthlyTotals.put(categoryName, monthlyTotals);
    }

    private void addCategoryMonthlyTotals(Map<String, Map<Month, Integer>> categoryMonthlyTotals, List<HouseholdAccount> expenses, String categoryName) {
        Map<Month, Integer> monthlyTotals = categoryMonthlyTotals.get(categoryName);
        for (HouseholdAccount expense : expenses) {
            if (categoryName.equals(expense.getCategory().getCategoryName())) {
                Month month = expense.getDate().getMonth();
                monthlyTotals.put(month, monthlyTotals.get(month) + expense.getAmount());
            }
        }
    }

    private void initializeCategoryTotal(Map<String, Integer> categoryTotals, Map<String, Map<Month, Integer>> categoryMonthlyTotals, String categoryName) {
        int total = categoryMonthlyTotals.get(categoryName).values().stream().mapToInt(Integer::intValue).sum();
        categoryTotals.put(categoryName, total);
    }
}
