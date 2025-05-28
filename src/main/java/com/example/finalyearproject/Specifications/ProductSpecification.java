package com.example.finalyearproject.Specifications;

import com.example.finalyearproject.DataStore.CategoryType;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.Utility.ProductFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> getFilteredProducts(ProductFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by available products (stock > 0)
            predicates.add(criteriaBuilder.greaterThan(root.get("stock"), 0));

            // Search by searchTerm (name or description) - Using case-insensitive LIKE without lower()
            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
                String searchPattern = "%" + filter.getSearchTerm() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("name").as(String.class)), searchPattern.toUpperCase()),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("description").as(String.class)), searchPattern.toUpperCase())
                ));
            }

            // Filter by category
            if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
                try {
                    CategoryType category = CategoryType.valueOf(filter.getCategory().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("category"), category));
                } catch (IllegalArgumentException ignored) {
                    // Invalid category, ignore this filter
                }
            }

            // Filter by price range
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            // Filter by organic
            if (filter.getIsOrganic() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isOrganic"), filter.getIsOrganic()));
            }

            // Filter by farmer ID
            if (filter.getFarmerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("farmer").get("farmerId"), filter.getFarmerId()));
            }

            // Filter by minimum rating
            if (filter.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), filter.getMinRating()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}