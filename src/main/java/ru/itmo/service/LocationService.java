package ru.itmo.service;

import jakarta.inject.Inject;
import ru.itmo.model.Location;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import lombok.Data;
import ru.itmo.repository.LocationRepository;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@Data
public class LocationService {
    @Inject
    private LocationRepository locationRepository;

    public List<Location> findAllLocationsPagedFiltered(int page, int pageSize, String sortField, String sortDirection, Map<String, String> filters) {
        return locationRepository.findAllPagedFiltered(page, pageSize, sortField, sortDirection, filters);
    }

    public Location findLocationById(Long id) {
        return locationRepository.findById(id);
    }

    public Location createLocation(Location location) {
        return locationRepository.create(location);
    }

    public Location updateLocation(Long id, Location location) {
        return locationRepository.update(id, location);
    }

    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id);
        if (location == null) throw new NoResultException("Location not found");
        locationRepository.delete(location);
    }

    public List<Location> findAllLocationsTruncated() {
        return locationRepository.findAllTruncated();
    }
}
