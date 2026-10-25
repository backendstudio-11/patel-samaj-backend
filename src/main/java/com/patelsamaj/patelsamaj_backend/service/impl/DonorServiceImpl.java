package com.patelsamaj.patelsamaj_backend.service.impl;

import com.patelsamaj.patelsamaj_backend.dto.DonorDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import com.patelsamaj.patelsamaj_backend.exception.ResourceNotFoundException;
import com.patelsamaj.patelsamaj_backend.mapper.DonorMapper;
import com.patelsamaj.patelsamaj_backend.model.Donor;
import com.patelsamaj.patelsamaj_backend.repository.DonorRepository;
import com.patelsamaj.patelsamaj_backend.service.DonorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DonorServiceImpl implements DonorService  {

    private final DonorRepository donorRepository;
    private final DonorMapper donorMapper;

    @Override
    @Transactional
    public DonorDTO upsertDonor(DonorDTO dto, int userId) {
        Donor donor;
        if (dto.getDonorId() != null && dto.getDonorId() != 0) {
            donor = donorRepository.findById(dto.getDonorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + dto.getDonorId()));
        } else {
            donor = new Donor();
            donor.setCreatedBy(userId);
            donor.setDonationDate(LocalDateTime.now());
        }
        donorMapper.updateEntityFromDto(dto, donor);
        Donor savedDonor = donorRepository.save(donor);
        return donorMapper.toDto(savedDonor);
    }

    @Override
    public Page<DonorDTO> getDonorList(PaginationDTO request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(request.getDirection(), request.getSortBy())
        );
        Page<Donor> donorPage;

        if (request.getSearch() == null || request.getSearch().isEmpty()) {
            donorPage = donorRepository.findAll(pageable);
        } else {
            donorPage = donorRepository.findByFullNameContainingIgnoreCase(request.getSearch(), pageable);
        }
        return donorPage.map(donorMapper::toDto);
    }

    @Override
    public DonorDTO getDonorById(int donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));
        return donorMapper.toDto(donor);
    }

    @Override
    @Transactional
    public void deleteDonorById(int donorId) {
        if (!donorRepository.existsById(donorId)) {
            throw new ResourceNotFoundException("Donor not found");
        }
        donorRepository.deleteById(donorId);
    }
}
