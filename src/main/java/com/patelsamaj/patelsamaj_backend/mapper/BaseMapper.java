package com.patelsamaj.patelsamaj_backend.mapper;

import org.mapstruct.MappingTarget;
import java.util.List;

public interface BaseMapper<D, E> {

    D toDto(E entity);

    E toEntity(D dto);

    List<D> toDtoList(List<E> entityList);

    List<E> toEntityList(List<D> dtoList);

    void updateEntityFromDto(D dto, @MappingTarget E entity);
}
