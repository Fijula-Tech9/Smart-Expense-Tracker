package com.expense.tracker.dto.response;

import com.expense.tracker.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Category Response DTO
 * 
 * Used to return category information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    
    private Long id;
    private String name;
    private CategoryType type;
    private Boolean isSystemCategory;
}
