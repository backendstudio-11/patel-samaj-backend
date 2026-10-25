package com.patelsamaj.patelsamaj_backend.dto;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class PaginationDTO {
    private String search;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy;
    private Sort.Direction direction = Sort.Direction.DESC;
}
