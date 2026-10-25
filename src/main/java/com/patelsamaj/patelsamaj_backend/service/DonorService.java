package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.dto.DonorDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import org.springframework.data.domain.Page;

public interface DonorService {
    DonorDTO upsertDonor(DonorDTO dto, int userId);
    Page<DonorDTO> getDonorList(PaginationDTO dto);
    DonorDTO getDonorById(int donorId);
    void deleteDonorById(int donorId);
}
