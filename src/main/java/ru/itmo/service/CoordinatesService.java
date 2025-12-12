package ru.itmo.service;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import ru.itmo.model.Coordinates;
import jakarta.enterprise.context.ApplicationScoped;
import ru.itmo.repository.CoordinatesRepository;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CoordinatesService {
    @Inject
    private CoordinatesRepository coordinatesRepository;

    public List<Coordinates> findAllCoordinatesPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        return coordinatesRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
    }

    public Coordinates createCoordinates(Coordinates coordinates) {
        return coordinatesRepository.create(coordinates);
    }

    public Coordinates updateCoordinates(Long id, Coordinates coordinates) {
        return coordinatesRepository.update(id, coordinates);
    }

    public void deleteCoordinates(Long id) {
        Coordinates coordinates = coordinatesRepository.findById(id);
        if (coordinates == null) throw new NoResultException("Coordinates not found");
        coordinatesRepository.delete(coordinates);
    }

    public Coordinates findCoordinatesById(Long id) {
        return coordinatesRepository.findById(id);
    }

    public List<Coordinates> findAllCoordinatesTruncated() {
        return coordinatesRepository.findAllTruncated();
    }
}
