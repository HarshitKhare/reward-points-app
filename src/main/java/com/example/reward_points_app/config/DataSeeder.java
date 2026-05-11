package com.example.reward_points_app.config;

import com.example.reward_points_app.model.Customer;
import com.example.reward_points_app.model.Transaction;
import com.example.reward_points_app.repository.CustomerRepository;
import com.example.reward_points_app.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void run(String... args) {

        //Create customers
        Customer alice = Customer.builder()
                .name("Alice Johnson").email("alice@example.com").build();
        Customer bob = Customer.builder()
                .name("Bob Smith").email("bob@example.com").build();
        Customer carol = Customer.builder()
                .name("Carol Williams").email("carol@example.com").build();
        Customer david = Customer.builder()
                .name("David Brown").email("david@example.com").build();
        Customer emma = Customer.builder()
                .name("Emma Davis").email("emma@example.com").build();

        customerRepository.saveAll(List.of(alice, bob, carol, david, emma));

        //Seed transactions
        List<Transaction> transactions = List.of(

            // Alice Johnson
            // January
            txn(alice, "120.00", LocalDate.of(2025, 1, 5),  "Electronics purchase"),   // 90 pts
            txn(alice, "200.00", LocalDate.of(2025, 1, 14), "Furniture"),               // 250 pts
            txn(alice,  "45.00", LocalDate.of(2025, 1, 22), "Grocery top-up"),         // 0 pts
            // February
            txn(alice, "150.00", LocalDate.of(2025, 2, 3),  "Clothing haul"),          // 150 pts
            txn(alice,  "75.00", LocalDate.of(2025, 2, 18), "Kitchen gadgets"),        // 25 pts
            txn(alice, "210.00", LocalDate.of(2025, 2, 26), "TV purchase"),            // 270 pts
            // March
            txn(alice, "130.00", LocalDate.of(2025, 3, 7),  "Sporting goods"),         // 110 pts
            txn(alice,  "55.00", LocalDate.of(2025, 3, 19), "Books"),                  // 5 pts
            txn(alice, "300.00", LocalDate.of(2025, 3, 28), "Laptop"),                 // 550 pts

            // Bob Smith
            // January
            txn(bob,    "80.00", LocalDate.of(2025, 1, 8),  "Restaurant supply"),      // 30 pts
            txn(bob,   "110.00", LocalDate.of(2025, 1, 20), "Home decor"),             // 70 pts
            txn(bob,    "30.00", LocalDate.of(2025, 1, 29), "Stationery"),             // 0 pts
            // February
            txn(bob,   "100.00", LocalDate.of(2025, 2, 11), "Shoes"),                  // 50 pts
            txn(bob,    "65.00", LocalDate.of(2025, 2, 21), "Pet supplies"),           // 15 pts
            // March
            txn(bob,   "175.00", LocalDate.of(2025, 3, 4),  "Power tools"),            // 200 pts
            txn(bob,    "50.00", LocalDate.of(2025, 3, 15), "Candles"),                // 0 pts
            txn(bob,    "90.00", LocalDate.of(2025, 3, 27), "Gardening set"),          // 40 pts

            // Carol Williams
            // January
            txn(carol, "500.00", LocalDate.of(2025, 1, 10), "Camera equipment"),      // 850 pts
            txn(carol,  "40.00", LocalDate.of(2025, 1, 25), "Memory cards"),          // 0 pts
            // February
            txn(carol,  "95.00", LocalDate.of(2025, 2, 8),  "Printer"),               // 45 pts
            txn(carol, "250.00", LocalDate.of(2025, 2, 17), "Office chair"),          // 350 pts
            // March
            txn(carol,  "60.00", LocalDate.of(2025, 3, 3),  "Desk lamp"),             // 10 pts
            txn(carol, "180.00", LocalDate.of(2025, 3, 22), "Standing desk"),         // 210 pts

            // David Brown
            // January
            txn(david,  "55.00", LocalDate.of(2025, 1, 6),  "Snack box subscription"), // 5 pts
            txn(david,  "48.00", LocalDate.of(2025, 1, 13), "Magazine bundle"),        // 0 pts
            txn(david, "120.00", LocalDate.of(2025, 1, 21), "Fitness tracker"),        // 90 pts
            // February
            txn(david,  "70.00", LocalDate.of(2025, 2, 5),  "Cooking class"),          // 20 pts
            txn(david,  "35.00", LocalDate.of(2025, 2, 14), "Valentine's chocolates"), // 0 pts
            txn(david, "105.00", LocalDate.of(2025, 2, 23), "Headphones"),             // 60 pts
            // March
            txn(david,  "52.00", LocalDate.of(2025, 3, 9),  "Yoga mat"),               // 2 pts
            txn(david,  "88.00", LocalDate.of(2025, 3, 18), "Backpack"),               // 38 pts
            txn(david, "115.00", LocalDate.of(2025, 3, 30), "Smart watch"),            // 80 pts

            // Emma Davis
            // January
            txn(emma,  "350.00", LocalDate.of(2025, 1, 3),  "Handbag"),               // 650 pts
            txn(emma,   "99.00", LocalDate.of(2025, 1, 17), "Perfume"),               // 49 pts
            txn(emma,  "150.00", LocalDate.of(2025, 1, 30), "Sunglasses"),            // 150 pts
            // February
            txn(emma,  "400.00", LocalDate.of(2025, 2, 2),  "Jewellery"),             // 750 pts
            txn(emma,   "85.00", LocalDate.of(2025, 2, 15), "Skincare set"),          // 35 pts
            // March
            txn(emma,  "220.00", LocalDate.of(2025, 3, 1),  "Designer shoes"),        // 290 pts
            txn(emma,   "60.00", LocalDate.of(2025, 3, 12), "Scarf"),                 // 10 pts
            txn(emma,  "175.00", LocalDate.of(2025, 3, 25), "Watch band & straps"),   // 200 pts
            txn(emma,   "45.00", LocalDate.of(2025, 3, 31), "Earrings"),              // 0 pts
            txn(emma,  "130.00", LocalDate.of(2025, 3, 31), "Gift wrapping hamper"))  // 110 pts
        ;

        transactionRepository.saveAll(transactions);
        log.info("Seeded {} customers and {} transactions into the DB.", 5, transactions.size());
    }

    private Transaction txn(Customer customer, String amount, LocalDate date, String desc) {
        return Transaction.builder()
                .customer(customer)
                .amount(new BigDecimal(amount))
                .transactionDate(date)
                .description(desc)
                .build();
    }
}
